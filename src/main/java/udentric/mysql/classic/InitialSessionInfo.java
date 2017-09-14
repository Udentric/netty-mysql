/*
 * Copyright (c) 2017 Alex Dubov <oakad@yahoo.com>
 *
 * This file is made available under the GNU General Public License
 * version 2 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
/*
 * May contain portions of MySQL Connector/J implementation
 *
 * Copyright (c) 2002, 2017, Oracle and/or its affiliates. All rights reserved.
 *
 * The MySQL Connector/J is licensed under the terms of the GPLv2
 * <http://www.gnu.org/licenses/old-licenses/gpl-2.0.html>, like most MySQL
 * Connectors. There are special exceptions to the terms and conditions of
 * the GPLv2 as it is applied to this software, see the FOSS License Exception
 * <http://www.mysql.com/about/legal/licensing/foss-exception.html>.
 */

package udentric.mysql.classic;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.DecoderException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Arrays;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import udentric.mysql.Config;
import udentric.mysql.MysqlErrorNumbers;
import udentric.mysql.ServerVersion;
import udentric.mysql.classic.dicta.Dictum;
import udentric.mysql.classic.dicta.MysqlNativePasswordAuth;


public class InitialSessionInfo {
	InitialSessionInfo(Client cl_) {
		cl = cl_;
		charsetInfo = CharsetInfo.forId(223);
	}

	public void onAuth(
		Packet.ServerAck ack, ChannelHandlerContext ctx,
		ChannelPromise chp
	) {
		LOGGER.debug("authenticated: {}", ack.info);
		Channel ch = ctx.channel();
		Client.discardActiveDictum(ch);

		ch.attr(Client.SESSION_INFO).set(new SessionInfo(this));
		ch.attr(Client.INITIAL_SESSION_INFO).set(null);
		chp.setSuccess();
	}

	public void processInitialHandshake(
		ByteBuf src, ChannelHandlerContext ctx, ChannelPromise chp
	) {
		Channel ch = ctx.channel();
		try {
			Client.discardActiveDictum(ch);
			ch.writeAndFlush(
				decodeInitialHandshake(src, ctx, chp)
			).addListener(chf -> {
				if (!chf.isSuccess())
					chp.setFailure(chf.cause());
			});
		} catch (Exception e) {
			chp.setFailure(e);
		}
	}

	private Dictum decodeInitialHandshake(
		ByteBuf src, ChannelHandlerContext ctx, ChannelPromise chp
	) throws SQLException {
		int seqNum = Packet.getSeqNum(src);
		src.skipBytes(Packet.HEADER_SIZE);

		int protoVers = Packet.readInt1(src);
		if (Client.MYSQL_PROTOCOL_VERSION != protoVers) {
			throw new DecoderException(
				"unsupported protocol version "
				+ Integer.toString(protoVers)
			);
		}

		String authPluginName
		= MysqlNativePasswordAuth.AUTH_PLUGIN_NAME;

		version = new ServerVersion(Packet.readStringNT(
			src, StandardCharsets.UTF_8
		));
		srvConnId = src.readIntLE();
		LOGGER.debug(
			"server identity set: {} ({})", srvConnId, version
		);

		secret = new byte[8];
		src.readBytes(secret);
		src.skipBytes(1);

		if (!src.isReadable()) {
			return selectAuthCommand(
				authPluginName, seqNum, ctx, chp
			);
		}

		serverCaps = Packet.readLong2(src);

		if (!src.isReadable()) {
			updateClientCapabilities();
			return selectAuthCommand(
				authPluginName, seqNum, ctx, chp
			);
		}

		charsetInfo = CharsetInfo.forId(Packet.readInt1(src));

		src.skipBytes(2);//short statusFlags = msg.readShortLE();

		serverCaps |= Packet.readLong2(src) << 16;
		updateClientCapabilities();
		int s2Len = Packet.readInt1(src);
		src.skipBytes(10);

		if (ClientCapability.PLUGIN_AUTH.get(serverCaps)) {
			int oldLen = secret.length;
			secret = Arrays.copyOf(secret, s2Len - 1);
			src.readBytes(
				secret, oldLen, s2Len - oldLen - 1
			);
			src.skipBytes(1);
			authPluginName = Packet.readStringNT(
				src, charsetInfo.javaCharset
			);
		} else {
			s2Len = Math.max(12, src.readableBytes());
			int oldLen = secret.length;
			secret = Arrays.copyOf(secret, oldLen + s2Len);
			src.readBytes(secret, oldLen, s2Len);
		}

		return selectAuthCommand(
			authPluginName, seqNum, ctx, chp
		);
	}

	private Dictum selectAuthCommand(
		String authPluginName, int seqNum,
		ChannelHandlerContext ctx, ChannelPromise chp
	) throws SQLException {
		ByteBuf attrBuf = null;

		switch (authPluginName) {
		case "mysql_native_password":
			if (ClientCapability.CONNECT_ATTRS.get(clientCaps)) {
				attrBuf = encodeAttrs(ctx);
			}

			if (cl.getConfig().containsKey(Config.Key.password)) {
				clientCaps |= ClientCapability.SECURE_CONNECTION.mask();
				if (ClientCapability.PLUGIN_AUTH_LENENC_CLIENT_DATA.get(
					serverCaps
				))
					clientCaps
					|= ClientCapability.PLUGIN_AUTH_LENENC_CLIENT_DATA.mask();
			}

			++seqNum;
			return new MysqlNativePasswordAuth(
				this, seqNum, chp, attrBuf
			);
		default:
			throw Packet.makeError(
				MysqlErrorNumbers.ER_NOT_SUPPORTED_AUTH_MODE,
				authPluginName
			);
		}
	}

	private ByteBuf encodeAttrs(ChannelHandlerContext ctx) {
		ByteBuf buf = ctx.alloc().buffer();
		cl.connAttributes.forEach((k, v) -> {
			byte[] b = k.getBytes(charsetInfo.javaCharset);
			Packet.writeIntLenenc(buf, b.length);
			buf.writeBytes(b);
			b = v.getBytes(charsetInfo.javaCharset);
			Packet.writeIntLenenc(buf, b.length);
			buf.writeBytes(b);
		});
		return buf;
	}

	private void updateClientCapabilities() {
		clientCaps = ClientCapability.LONG_PASSWORD.mask()
			| ClientCapability.LONG_FLAG.mask()
			| ClientCapability.PROTOCOL_41.mask()
			| ClientCapability.TRANSACTIONS.mask()
			| ClientCapability.MULTI_STATEMENTS.mask()
			| ClientCapability.MULTI_RESULTS.mask()
			| ClientCapability.PS_MULTI_RESULTS.mask()
			| ClientCapability.PLUGIN_AUTH.mask();

		if (ClientCapability.CONNECT_ATTRS.get(serverCaps))
			clientCaps |= ClientCapability.CONNECT_ATTRS.mask();

		if (ClientCapability.DEPRECATE_EOF.get(serverCaps))
			clientCaps |= ClientCapability.DEPRECATE_EOF.mask();
		
	}

	public static final Logger LOGGER = LogManager.getLogger(
		InitialSessionInfo.class
	);

	public final Client cl;
	public ServerVersion version;
	public CharsetInfo.Entry charsetInfo;
	public long serverCaps;
	public long clientCaps;
	public int srvConnId;
	public byte[] secret;
}
