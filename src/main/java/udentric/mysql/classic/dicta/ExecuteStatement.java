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

package udentric.mysql.classic.dicta;

import java.util.Arrays;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import udentric.mysql.PreparedStatement;
import udentric.mysql.classic.Channels;
import udentric.mysql.classic.ColumnDefinition;
import udentric.mysql.classic.Packet;
import udentric.mysql.classic.PreparedStatementCursorType;
import udentric.mysql.classic.ResultSetConsumer;

public class ExecuteStatement implements Dictum {
	public ExecuteStatement(
		PreparedStatement pstmt_, ResultSetConsumer rsc_,
		Object... args_
	) {
		pstmt = pstmt_;
		params = pstmt.parameters();
		rsc = rsc_;

		args = args_.length < params.fieldCount()
			? Arrays.copyOf(args_, params.fieldCount())
			: args_;
	}

	@Override
	public void emitClientMessage(
		ByteBuf dst, ChannelHandlerContext ctx
	) {
		dst.writeByte(OPCODE);
		dst.writeIntLE(pstmt.getServerId());
		dst.writeByte(PreparedStatementCursorType.NONE.mask());
		dst.writeIntLE(1);
		ColumnDefinition params = pstmt.parameters();
		if (params.fieldCount() == 0)
			return;

		writeNullBitmap(dst);
	}

	private void writeNullBitmap(ByteBuf dst) {
		byte[] nb = new byte[(params.fieldCount() + 7) / 8];

		for (int pos = 0; pos < params.fieldCount(); pos++) {
			if (args[pos] == null) {
				nb[pos >>> 3] |= (byte)(1 << (pos & 7));
			}
		}

		dst.writeBytes(nb);
	}

	@Override
	public void acceptServerMessage(
		ByteBuf src, ChannelHandlerContext ctx
	) {
	}

	@Override
	public void handleFailure(Throwable cause) {
		rsc.acceptFailure(cause);
	}

	public static final int OPCODE = 23;

	private final PreparedStatement pstmt;
	private final ColumnDefinition params;
	private final ResultSetConsumer rsc;
	private final Object[] args;
}
