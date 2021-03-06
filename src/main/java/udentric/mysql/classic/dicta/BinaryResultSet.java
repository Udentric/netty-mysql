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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import udentric.mysql.PreparedStatement;
import udentric.mysql.classic.BinaryDataRow;
import udentric.mysql.classic.Channels;
import udentric.mysql.classic.ColumnValueMapper;
import udentric.mysql.classic.ResultSetConsumer;
import udentric.mysql.classic.SessionInfo;
import udentric.mysql.classic.type.AdapterState;

public class BinaryResultSet extends ResultSet {
	public BinaryResultSet(
		int columnCount_, int lastSeqNum_, ResultSetConsumer rsc_
	) {
		super(columnCount_, lastSeqNum_, rsc_);
	}

	public BinaryResultSet(int lastSeqNum_, ResultSetConsumer rsc_) {
		super(lastSeqNum_, rsc_);
	}

	public BinaryResultSet withStatement(PreparedStatement pstmt_) {
		pstmt = pstmt_;
		return this;
	}

	@Override
	protected void initRow(
		ColumnValueMapper mapper_, ByteBufAllocator alloc
	) {
		colState = new AdapterState(alloc);
		mapper = mapper_ != null ? mapper_ : ColumnValueMapper.DEFAULT;
		row = BinaryDataRow.init(row, columns, mapper);
		colDataPos = 0;
		rowConsumed = true;
	}

	@Override
	protected void acceptRowData(ByteBuf src) {
		if (rowConsumed) {
			row.initValues(mapper);
			rowConsumed = false;
			src.skipBytes(1);
			row.readNullBitmap(src);
		}

		for (; colDataPos < columns.size(); colDataPos++) {
			if (row.checkAndSetNull(colDataPos))
				continue;

			row.decodeValue(colDataPos, src, colState, columns);
			if (colState.done())
				colState.reset();
			else
				break;
		}
	}

	@Override
	protected void consumeRow() {
		if (colDataPos < columns.size())
			LOGGER.error(
				"Incomplete row read: want {} columns, have {} columns",
				columns.size(), colDataPos
			);

		rsc.acceptRow(row);
		colDataPos = 0;
		rowConsumed = true;
	}

	@Override
	protected void fetchFromCursor(
		ChannelHandlerContext ctx, SessionInfo si
	) {
		Channels.discardActiveDictum(ctx.channel());
		ctx.channel().writeAndFlush(new FetchStatementResult(
			pstmt, columns, rsc
		)).addListener(Channels::defaultSendListener);
	}

	private static final Logger LOGGER = LogManager.getLogger(
		BinaryResultSet.class
	);

	protected BinaryDataRow row;
	protected PreparedStatement pstmt;
}
