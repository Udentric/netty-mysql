/*
 * Copyright (c) 2017 - 2018 Alex Dubov <oakad@yahoo.com>
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

package udentric.mysql.classic.dicta;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import udentric.mysql.PreparedStatement;
import udentric.mysql.classic.Channels;
import udentric.mysql.classic.FieldImpl;
import udentric.mysql.classic.FieldSetImpl;
import udentric.mysql.classic.Packet;
import udentric.mysql.classic.ResultSetConsumer;
import udentric.mysql.classic.ServerAck;
import udentric.mysql.classic.ServerStatus;
import udentric.mysql.classic.SessionInfo;
import udentric.mysql.classic.prepared.CursorType;
import udentric.mysql.classic.prepared.Statement;
import udentric.mysql.classic.type.AdapterState;
import udentric.mysql.classic.type.ValueAdapter;

public class ExecuteStatement implements Dictum {
	public ExecuteStatement(
		PreparedStatement pstmt_, ResultSetConsumer rsc_,
		Object... params_
	) {
		pstmt = (Statement)pstmt_;
		fields = (FieldSetImpl)pstmt.parameters();
		omitTypeDeclaration = pstmt.typesDeclared();
		rsc = rsc_;

		params = params_;
		state = this::emitCommand;
	}

	@Override
	public int getSeqNum() {
		return seqNum;
	}

	@Override
	public boolean emitClientMessage(
		ByteBuf dst, ChannelHandlerContext ctx
	) {
		SessionInfo si = Channels.sessionInfo(ctx.channel());
		bufferStartPos = dst.writerIndex();
		return state.apply(dst, ctx, si);
	}

	private boolean emitCommand(
		ByteBuf dst, ChannelHandlerContext ctx, SessionInfo si
	) {
		pstmt.check(ctx.channel());

		dst.writeByte(OPCODE);
		dst.writeIntLE(pstmt.getServerId());
		dst.writeByte(CursorType.NONE.mask());
		dst.writeIntLE(1);
		if (fields.size() == 0)
			return false;

		writeNullBitmap(dst);
		if (!omitTypeDeclaration) {
			dst.writeByte(1);
			fields.forEach(fld -> {
				dst.writeByte(((FieldImpl)fld).type.id);
				dst.writeByte(((FieldImpl)fld).paramFlags());
			});
		}

		paramEncoderState = new AdapterState(ctx.alloc());
		state = this::emitParamData;
		return emitParamDataImpl(dst, ctx, si);
	}

	private void writeNullBitmap(ByteBuf dst) {
		int bitCount = (fields.size() + 7) & (~7);

		for (int wPos = 0; wPos < bitCount; wPos += 8) {
			int acc = 0;
			for (int bPos = 0; bPos < 8; bPos++) {
				int argPos = bPos + wPos;
				if (argPos >= fields.size())
					break;

				if (((
					params.length <= argPos
				) || (
					params[argPos] == null
				)) && !pstmt.parameterPreloaded(argPos)) {
					acc |= 1 << bPos;
				}
			}
			dst.writeByte(acc);
		}
	}

	private boolean emitParamData(
		ByteBuf dst, ChannelHandlerContext ctx, SessionInfo si
	) {
		seqNum++;
		return emitParamDataImpl(dst, ctx, si);
	}

	@SuppressWarnings("unchecked")
	private boolean emitParamDataImpl(
		ByteBuf dst, ChannelHandlerContext ctx, SessionInfo si
	) {
		while (true) {
			if (appendLeftOver(dst, si))
				return true;

			if (paramEncoderState.done()) {
				paramPos++;
				paramEncoderState.reset();
				adapter = null;
				if (paramPos >= fields.size())
					return false;
			}

			Object param = paramPos < params.length
				? params[paramPos]
				: null;

			if (
				pstmt.parameterPreloaded(paramPos)
				|| (param == null)
			) {
				paramEncoderState.markAsDone();
				continue;
			}

			int lastPos = dst.writerIndex() - bufferStartPos;
			int limit = si.packetSize - lastPos;
			if (limit == 0)
				return true;

			FieldImpl fld = (FieldImpl)fields.get(paramPos);
			if (adapter == null) {
				adapter = (ValueAdapter<Object>)fld.type.binaryAdapterSelector.find(
					param.getClass()
				);
			}

			if (adapter == null) {
				throw new IllegalStateException(String.format(
					"no binary data adapter of type %s for object class %s",
					fld.type, param.getClass()
				));
			}

			adapter.encodeValue(
				dst, param, paramEncoderState, limit, fld
			);

			if (preserveLeftOver(dst, si))
				return true;
		}
	}

	private boolean appendLeftOver(ByteBuf dst, SessionInfo si) {
		if (leftOver == null)
			return false;

		int limit = si.packetSize - dst.writerIndex() + bufferStartPos;
		if (limit > leftOver.readableBytes()) {
			dst.writeBytes(leftOver);
			leftOver.release();
			leftOver = null;
			return false;
		} else {
			dst.writeBytes(leftOver, limit);
			return true;
		}
	}

	private boolean preserveLeftOver(ByteBuf dst, SessionInfo si) {
		int count = dst.writerIndex() - bufferStartPos;
		if (count < si.packetSize)
			return false;

		int nextPos = bufferStartPos + si.packetSize;
		leftOver = dst.retainedSlice(
			nextPos, count - si.packetSize
		);
		dst.writerIndex(nextPos);
		return true;
	}

	@Override
	public void acceptServerMessage(
		ByteBuf src, ChannelHandlerContext ctx
	) {
		if (Packet.invalidSeqNum(
			ctx.channel(), src, Packet.nextSeqNum(seqNum)
		))
			return;

		pstmt.resetPreloaded();
		seqNum = Packet.getSeqNum(src);
		Channel ch = ctx.channel();
		SessionInfo si = Channels.sessionInfo(ch);

		int type = src.getByte(
			src.readerIndex() + Packet.HEADER_SIZE
		) & 0xff;

		switch (type) {
		case Packet.OK:
			src.skipBytes(Packet.HEADER_SIZE + 1);
			try {
				ServerAck ack = ServerAck.fromOk(src, si);
				if (ServerStatus.MORE_RESULTS_EXISTS.get(
					ack.srvStatus
				)) {
					ch.attr(Channels.ACTIVE_DICTUM).set(
						new BinaryResultSet(
							seqNum, rsc
						).withStatement(pstmt)
					);
					rsc.acceptAck(ack, false);
				} else {
					Channels.discardActiveDictum(ch);
					rsc.acceptAck(ack, true);
				}
			} catch (Exception e) {
				Channels.discardActiveDictum(ch, e);
			}
			break;
		case Packet.ERR:
			src.skipBytes(Packet.HEADER_SIZE + 1);
			Channels.discardActiveDictum(
				ch, Packet.parseError(src, si.charset())
			);
			return;
		default:
			src.skipBytes(Packet.HEADER_SIZE);
			ch.attr(Channels.ACTIVE_DICTUM).set(
				new BinaryResultSet(
					Packet.readIntLenenc(src), seqNum, rsc
				).withStatement(pstmt)
			);
		}
	}

	@Override
	public void handleFailure(Throwable cause) {
		rsc.acceptFailure(cause);
	}

	public static final int OPCODE = 23;

	private final Statement pstmt;
	private final FieldSetImpl fields;
	private final ResultSetConsumer rsc;
	private final boolean omitTypeDeclaration;
	private final Object[] params;
	private AdapterState paramEncoderState;
	private ClientMessageEmitter state;
	private int seqNum;
	private int bufferStartPos;
	private int paramPos;
	private ByteBuf leftOver;
	private ValueAdapter<Object> adapter;
}
