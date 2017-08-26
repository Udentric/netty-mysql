/*
 * Copyright (c) 2017 Alex Dubov <oakad@yahoo.com>
 *
 * This file is made available under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package udentric.mysql.classic;

public class CharsetInfo {
	public static class Entry {
		private Entry(int id_, String charset_, String collation_) {
			id = id_;
			charset = charset_;
			collation = collation_;
		}

		public final int id;
		public final String charset;
		public final String collation;
	}

	private static final Entry[] entries = new Entry[]{
		null,
		new Entry(1, "big5", "big5_chinese_ci"),
		new Entry(2, "latin2", "latin2_czech_cs"),
		new Entry(3, "dec8", "dec8_swedish_ci"),
		new Entry(4, "cp850", "cp850_general_ci"),
		new Entry(5, "latin1", "latin1_german1_ci"),
		new Entry(6, "hp8", "hp8_english_ci"),
		new Entry(7, "koi8r", "koi8r_general_ci"),
		new Entry(8, "latin1", "latin1_swedish_ci"),
		new Entry(9, "latin2", "latin2_general_ci"),
		new Entry(10, "swe7", "swe7_swedish_ci"),
		new Entry(11, "ascii", "ascii_general_ci"),
		new Entry(12, "ujis", "ujis_japanese_ci"),
		new Entry(13, "sjis", "sjis_japanese_ci"),
		new Entry(14, "cp1251", "cp1251_bulgarian_ci"),
		new Entry(15, "latin1", "latin1_danish_ci"),
		new Entry(16, "hebrew", "hebrew_general_ci"),
		null,
		new Entry(18, "tis620", "tis620_thai_ci"),
		new Entry(19, "euckr", "euckr_korean_ci"),
		new Entry(20, "latin7", "latin7_estonian_cs"),
		new Entry(21, "latin2", "latin2_hungarian_ci"),
		new Entry(22, "koi8u", "koi8u_general_ci"),
		new Entry(23, "cp1251", "cp1251_ukrainian_ci"),
		new Entry(24, "gb2312", "gb2312_chinese_ci"),
		new Entry(25, "greek", "greek_general_ci"),
		new Entry(26, "cp1250", "cp1250_general_ci"),
		new Entry(27, "latin2", "latin2_croatian_ci"),
		new Entry(28, "gbk", "gbk_chinese_ci"),
		new Entry(29, "cp1257", "cp1257_lithuanian_ci"),
		new Entry(30, "latin5", "latin5_turkish_ci"),
		new Entry(31, "latin1", "latin1_german2_ci"),
		new Entry(32, "armscii8", "armscii8_general_ci"),
		new Entry(33, "utf8", "utf8_general_ci"),
		new Entry(34, "cp1250", "cp1250_czech_cs"),
		new Entry(35, "ucs2", "ucs2_general_ci"),
		new Entry(36, "cp866", "cp866_general_ci"),
		new Entry(37, "keybcs2", "keybcs2_general_ci"),
		new Entry(38, "macce", "macce_general_ci"),
		new Entry(39, "macroman", "macroman_general_ci"),
		new Entry(40, "cp852", "cp852_general_ci"),
		new Entry(41, "latin7", "latin7_general_ci"),
		new Entry(42, "latin7", "latin7_general_cs"),
		new Entry(43, "macce", "macce_bin"),
		new Entry(44, "cp1250", "cp1250_croatian_ci"),
		new Entry(45, "utf8mb4", "utf8mb4_general_ci"),
		new Entry(46, "utf8mb4", "utf8mb4_bin"),
		new Entry(47, "latin1", "latin1_bin"),
		new Entry(48, "latin1", "latin1_general_ci"),
		new Entry(49, "latin1", "latin1_general_cs"),
		new Entry(50, "cp1251", "cp1251_bin"),
		new Entry(51, "cp1251", "cp1251_general_ci"),
		new Entry(52, "cp1251", "cp1251_general_cs"),
		new Entry(53, "macroman", "macroman_bin"),
		new Entry(54, "utf16", "utf16_general_ci"),
		new Entry(55, "utf16", "utf16_bin"),
		new Entry(56, "utf16le", "utf16le_general_ci"),
		new Entry(57, "cp1256", "cp1256_general_ci"),
		new Entry(58, "cp1257", "cp1257_bin"),
		new Entry(59, "cp1257", "cp1257_general_ci"),
		new Entry(60, "utf32", "utf32_general_ci"),
		new Entry(61, "utf32", "utf32_bin"),
		new Entry(62, "utf16le", "utf16le_bin"),
		new Entry(63, "binary", "binary"),
		new Entry(64, "armscii8", "armscii8_bin"),
		new Entry(65, "ascii", "ascii_bin"),
		new Entry(66, "cp1250", "cp1250_bin"),
		new Entry(67, "cp1256", "cp1256_bin"),
		new Entry(68, "cp866", "cp866_bin"),
		new Entry(69, "dec8", "dec8_bin"),
		new Entry(70, "greek", "greek_bin"),
		new Entry(71, "hebrew", "hebrew_bin"),
		new Entry(72, "hp8", "hp8_bin"),
		new Entry(73, "keybcs2", "keybcs2_bin"),
		new Entry(74, "koi8r", "koi8r_bin"),
		new Entry(75, "koi8u", "koi8u_bin"),
		null,
		new Entry(77, "latin2", "latin2_bin"),
		new Entry(78, "latin5", "latin5_bin"),
		new Entry(79, "latin7", "latin7_bin"),
		new Entry(80, "cp850", "cp850_bin"),
		new Entry(81, "cp852", "cp852_bin"),
		new Entry(82, "swe7", "swe7_bin"),
		new Entry(83, "utf8", "utf8_bin"),
		new Entry(84, "big5", "big5_bin"),
		new Entry(85, "euckr", "euckr_bin"),
		new Entry(86, "gb2312", "gb2312_bin"),
		new Entry(87, "gbk", "gbk_bin"),
		new Entry(88, "sjis", "sjis_bin"),
		new Entry(89, "tis620", "tis620_bin"),
		new Entry(90, "ucs2", "ucs2_bin"),
		new Entry(91, "ujis", "ujis_bin"),
		new Entry(92, "geostd8", "geostd8_general_ci"),
		new Entry(93, "geostd8", "geostd8_bin"),
		new Entry(94, "latin1", "latin1_spanish_ci"),
		new Entry(95, "cp932", "cp932_japanese_ci"),
		new Entry(96, "cp932", "cp932_bin"),
		new Entry(97, "eucjpms", "eucjpms_japanese_ci"),
		new Entry(98, "eucjpms", "eucjpms_bin"),
		new Entry(99, "cp1250", "cp1250_polish_ci"),
		null,
		new Entry(101, "utf16", "utf16_unicode_ci"),
		new Entry(102, "utf16", "utf16_icelandic_ci"),
		new Entry(103, "utf16", "utf16_latvian_ci"),
		new Entry(104, "utf16", "utf16_romanian_ci"),
		new Entry(105, "utf16", "utf16_slovenian_ci"),
		new Entry(106, "utf16", "utf16_polish_ci"),
		new Entry(107, "utf16", "utf16_estonian_ci"),
		new Entry(108, "utf16", "utf16_spanish_ci"),
		new Entry(109, "utf16", "utf16_swedish_ci"),
		new Entry(110, "utf16", "utf16_turkish_ci"),
		new Entry(111, "utf16", "utf16_czech_ci"),
		new Entry(112, "utf16", "utf16_danish_ci"),
		new Entry(113, "utf16", "utf16_lithuanian_ci"),
		new Entry(114, "utf16", "utf16_slovak_ci"),
		new Entry(115, "utf16", "utf16_spanish2_ci"),
		new Entry(116, "utf16", "utf16_roman_ci"),
		new Entry(117, "utf16", "utf16_persian_ci"),
		new Entry(118, "utf16", "utf16_esperanto_ci"),
		new Entry(119, "utf16", "utf16_hungarian_ci"),
		new Entry(120, "utf16", "utf16_sinhala_ci"),
		new Entry(121, "utf16", "utf16_german2_ci"),
		new Entry(122, "utf16", "utf16_croatian_ci"),
		new Entry(123, "utf16", "utf16_unicode_520_ci"),
		new Entry(124, "utf16", "utf16_vietnamese_ci"),
		null,
		null,
		null,
		new Entry(128, "ucs2", "ucs2_unicode_ci"),
		new Entry(129, "ucs2", "ucs2_icelandic_ci"),
		new Entry(130, "ucs2", "ucs2_latvian_ci"),
		new Entry(131, "ucs2", "ucs2_romanian_ci"),
		new Entry(132, "ucs2", "ucs2_slovenian_ci"),
		new Entry(133, "ucs2", "ucs2_polish_ci"),
		new Entry(134, "ucs2", "ucs2_estonian_ci"),
		new Entry(135, "ucs2", "ucs2_spanish_ci"),
		new Entry(136, "ucs2", "ucs2_swedish_ci"),
		new Entry(137, "ucs2", "ucs2_turkish_ci"),
		new Entry(138, "ucs2", "ucs2_czech_ci"),
		new Entry(139, "ucs2", "ucs2_danish_ci"),
		new Entry(140, "ucs2", "ucs2_lithuanian_ci"),
		new Entry(141, "ucs2", "ucs2_slovak_ci"),
		new Entry(142, "ucs2", "ucs2_spanish2_ci"),
		new Entry(143, "ucs2", "ucs2_roman_ci"),
		new Entry(144, "ucs2", "ucs2_persian_ci"),
		new Entry(145, "ucs2", "ucs2_esperanto_ci"),
		new Entry(146, "ucs2", "ucs2_hungarian_ci"),
		new Entry(147, "ucs2", "ucs2_sinhala_ci"),
		new Entry(148, "ucs2", "ucs2_german2_ci"),
		new Entry(149, "ucs2", "ucs2_croatian_ci"),
		new Entry(150, "ucs2", "ucs2_unicode_520_ci"),
		new Entry(151, "ucs2", "ucs2_vietnamese_ci"),
		null,
		null,
		null,
		null,
		null,
		null,
		null,
		new Entry(159, "ucs2", "ucs2_general_mysql500_ci"),
		new Entry(160, "utf32", "utf32_unicode_ci"),
		new Entry(161, "utf32", "utf32_icelandic_ci"),
		new Entry(162, "utf32", "utf32_latvian_ci"),
		new Entry(163, "utf32", "utf32_romanian_ci"),
		new Entry(164, "utf32", "utf32_slovenian_ci"),
		new Entry(165, "utf32", "utf32_polish_ci"),
		new Entry(166, "utf32", "utf32_estonian_ci"),
		new Entry(167, "utf32", "utf32_spanish_ci"),
		new Entry(168, "utf32", "utf32_swedish_ci"),
		new Entry(169, "utf32", "utf32_turkish_ci"),
		new Entry(170, "utf32", "utf32_czech_ci"),
		new Entry(171, "utf32", "utf32_danish_ci"),
		new Entry(172, "utf32", "utf32_lithuanian_ci"),
		new Entry(173, "utf32", "utf32_slovak_ci"),
		new Entry(174, "utf32", "utf32_spanish2_ci"),
		new Entry(175, "utf32", "utf32_roman_ci"),
		new Entry(176, "utf32", "utf32_persian_ci"),
		new Entry(177, "utf32", "utf32_esperanto_ci"),
		new Entry(178, "utf32", "utf32_hungarian_ci"),
		new Entry(179, "utf32", "utf32_sinhala_ci"),
		new Entry(180, "utf32", "utf32_german2_ci"),
		new Entry(181, "utf32", "utf32_croatian_ci"),
		new Entry(182, "utf32", "utf32_unicode_520_ci"),
		new Entry(183, "utf32", "utf32_vietnamese_ci"),
		null,
		null,
		null,
		null,
		null,
		null,
		null,
		null,
		new Entry(192, "utf8", "utf8_unicode_ci"),
		new Entry(193, "utf8", "utf8_icelandic_ci"),
		new Entry(194, "utf8", "utf8_latvian_ci"),
		new Entry(195, "utf8", "utf8_romanian_ci"),
		new Entry(196, "utf8", "utf8_slovenian_ci"),
		new Entry(197, "utf8", "utf8_polish_ci"),
		new Entry(198, "utf8", "utf8_estonian_ci"),
		new Entry(199, "utf8", "utf8_spanish_ci"),
		new Entry(200, "utf8", "utf8_swedish_ci"),
		new Entry(201, "utf8", "utf8_turkish_ci"),
		new Entry(202, "utf8", "utf8_czech_ci"),
		new Entry(203, "utf8", "utf8_danish_ci"),
		new Entry(204, "utf8", "utf8_lithuanian_ci"),
		new Entry(205, "utf8", "utf8_slovak_ci"),
		new Entry(206, "utf8", "utf8_spanish2_ci"),
		new Entry(207, "utf8", "utf8_roman_ci"),
		new Entry(208, "utf8", "utf8_persian_ci"),
		new Entry(209, "utf8", "utf8_esperanto_ci"),
		new Entry(210, "utf8", "utf8_hungarian_ci"),
		new Entry(211, "utf8", "utf8_sinhala_ci"),
		new Entry(212, "utf8", "utf8_german2_ci"),
		new Entry(213, "utf8", "utf8_croatian_ci"),
		new Entry(214, "utf8", "utf8_unicode_520_ci"),
		new Entry(215, "utf8", "utf8_vietnamese_ci"),
		null,
		null,
		null,
		null,
		null,
		null,
		null,
		new Entry(223, "utf8", "utf8_general_mysql500_ci"),
		new Entry(224, "utf8mb4", "utf8mb4_unicode_ci"),
		new Entry(225, "utf8mb4", "utf8mb4_icelandic_ci"),
		new Entry(226, "utf8mb4", "utf8mb4_latvian_ci"),
		new Entry(227, "utf8mb4", "utf8mb4_romanian_ci"),
		new Entry(228, "utf8mb4", "utf8mb4_slovenian_ci"),
		new Entry(229, "utf8mb4", "utf8mb4_polish_ci"),
		new Entry(230, "utf8mb4", "utf8mb4_estonian_ci"),
		new Entry(231, "utf8mb4", "utf8mb4_spanish_ci"),
		new Entry(232, "utf8mb4", "utf8mb4_swedish_ci"),
		new Entry(233, "utf8mb4", "utf8mb4_turkish_ci"),
		new Entry(234, "utf8mb4", "utf8mb4_czech_ci"),
		new Entry(235, "utf8mb4", "utf8mb4_danish_ci"),
		new Entry(236, "utf8mb4", "utf8mb4_lithuanian_ci"),
		new Entry(237, "utf8mb4", "utf8mb4_slovak_ci"),
		new Entry(238, "utf8mb4", "utf8mb4_spanish2_ci"),
		new Entry(239, "utf8mb4", "utf8mb4_roman_ci"),
		new Entry(240, "utf8mb4", "utf8mb4_persian_ci"),
		new Entry(241, "utf8mb4", "utf8mb4_esperanto_ci"),
		new Entry(242, "utf8mb4", "utf8mb4_hungarian_ci"),
		new Entry(243, "utf8mb4", "utf8mb4_sinhala_ci"),
		new Entry(244, "utf8mb4", "utf8mb4_german2_ci"),
		new Entry(245, "utf8mb4", "utf8mb4_croatian_ci"),
		new Entry(246, "utf8mb4", "utf8mb4_unicode_520_ci"),
		new Entry(247, "utf8mb4", "utf8mb4_vietnamese_ci"),
		new Entry(248, "gb18030", "gb18030_chinese_ci"),
		new Entry(249, "gb18030", "gb18030_bin"),
		new Entry(250, "gb18030", "gb18030_unicode_520_ci")
	};
}