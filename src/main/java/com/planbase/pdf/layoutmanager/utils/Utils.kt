// Copyright 2017 PlanBase Inc.
//
// This file is part of PdfLayoutMgr2
//
// PdfLayoutMgr is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// PdfLayoutMgr is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with PdfLayoutMgr.  If not, see <https://www.gnu.org/licenses/agpl-3.0.en.html>.
//
// If you wish to use this code with proprietary software,
// contact PlanBase Inc. <https://planbase.com> to purchase a commercial license.

package com.planbase.pdf.layoutmanager.utils

import org.apache.pdfbox.pdmodel.graphics.color.PDColor
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceCMYK
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.util.Arrays
import java.util.Collections
import java.util.HashMap
import java.util.regex.Pattern

/**
 * Holds utility functions.
 */
class Utils private constructor() {
    init {
        throw UnsupportedOperationException("No instances!")
    }

    companion object {

        val CMYK_BLACK = PDColor(floatArrayOf(0f, 0f, 0f, 1f), PDDeviceCMYK.INSTANCE)
        val CMYK_WHITE = PDColor(floatArrayOf(0f, 0f, 0f, 0f), PDDeviceCMYK.INSTANCE)

        val RGB_BLACK = PDColor(floatArrayOf(0f, 0f, 0f), PDDeviceRGB.INSTANCE)
        val RGB_WHITE = PDColor(floatArrayOf(1f, 1f, 1f), PDDeviceRGB.INSTANCE)

        /** For implementing briefer toString() methods */
        fun colorToString(color:PDColor?) =
                when {
                    color == null -> "null"
                    pdColorEquator(color, CMYK_BLACK) -> "CMYK_BLACK"
                    pdColorEquator(color, CMYK_WHITE) -> "CMYK_WHITE"
                    pdColorEquator(color, RGB_BLACK) -> "RGB_BLACK"
                    pdColorEquator(color, RGB_WHITE) -> "RGB_WHITE"
                    else -> {
                        if (color.patternName != null) {
                            color.toString()
                        } else {
                            var ret = color.colorSpace.toString()
                            if (color.components != null) {
                                ret += Arrays.toString(color.components)
                            }
                            ret
                        }
                    }
                }

        fun pdColorEquator(a:PDColor, b:PDColor):Boolean =
                if (a === b) { true }
                else (a.colorSpace == b.colorSpace) &&
                     (a.patternName == b.patternName) &&
                     Arrays.equals(a.components, b.components)

        //    public static String toString(PDColor c) {
        //        if (c == null) { return "null"; }
        //        return new StringBuilder("#").append(twoDigitHex(c.getRed()))
        //                .append(twoDigitHex(c.getGreen()))
        //                .append(twoDigitHex(c.getBlue())).toString();
        //    }
        //    public static String twoDigitHex(int i) {
        //        String h = Integer.toHexString(i);
        //        return (h.length() < 2) ? "0" + h : h;
        //    }
        //    public static void println(CharSequence cs) { System.out.println(cs); }

        internal fun equals(o1: Any?, o2: Any): Boolean {
            return o1 === o2 || o1 != null && o1 == o2
        }

        internal fun floatHashCode(value: Float): Int {
            return java.lang.Float.floatToIntBits(value)
        }

        val ISO_8859_1:Charset = charset("ISO_8859_1")
        val BULLET_CHAR = "\u2022"

        // PDFBox uses an encoding that the PDF spec calls WinAnsiEncoding.  The spec says this is
        // Windows Code Page 1252.
        // http://en.wikipedia.org/wiki/Windows-1252
        // It has a lot in common with ISO-8859-1, but it defines some additional characters such as
        // the Euro symbol.
        private val utf16ToWinAnsi: Map<String, String>

        init {
            val tempMap = HashMap<String, String>()

            try {
                // 129, 141, 143, 144, and 157 are undefined in WinAnsi.
                // I had mapped A0-FF to 160-255 without noticing that that maps each character to
                // itself, meaning that Unicode and WinAnsii are the same in that range.

                // Unicode characters with exact WinAnsi equivalents
                tempMap.put("\u0152", String(byteArrayOf(0, 140.toByte()), ISO_8859_1)) // OE
                tempMap.put("\u0153", String(byteArrayOf(0, 156.toByte()), ISO_8859_1)) // oe
                tempMap.put("\u0160", String(byteArrayOf(0, 138.toByte()), ISO_8859_1)) // S Acron
                tempMap.put("\u0161", String(byteArrayOf(0, 154.toByte()), ISO_8859_1)) // s acron
                tempMap.put("\u0178", String(byteArrayOf(0, 159.toByte()), ISO_8859_1)) // Y Diaeresis
                tempMap.put("\u017D", String(byteArrayOf(0, 142.toByte()), ISO_8859_1)) // Capital Z-caron
                tempMap.put("\u017E", String(byteArrayOf(0, 158.toByte()), ISO_8859_1)) // Lower-case Z-caron
                tempMap.put("\u0192", String(byteArrayOf(0, 131.toByte()), ISO_8859_1)) // F with a hook (like jf put together)
                tempMap.put("\u02C6", String(byteArrayOf(0, 136.toByte()), ISO_8859_1)) // circumflex (up-caret)
                tempMap.put("\u02DC", String(byteArrayOf(0, 152.toByte()), ISO_8859_1)) // Tilde

                // Cyrillic letters map to their closest Romanizations according to ISO 9:1995
                // http://en.wikipedia.org/wiki/ISO_9
                // http://en.wikipedia.org/wiki/A_(Cyrillic)

                // Cyrillic extensions
                // 0400 Ѐ Cyrillic capital letter IE WITH GRAVE
                // ≡ 0415 Е  0300 (left-accent)
                tempMap.put("\u0400", String(byteArrayOf(0, 200.toByte()), ISO_8859_1))
                // 0401 Ё Cyrillic capital letter IO
                // ≡ 0415 Е  0308 (diuresis)
                tempMap.put("\u0401", String(byteArrayOf(0, 203.toByte()), ISO_8859_1))
                // 0402 Ђ Cyrillic capital letter DJE
                tempMap.put("\u0402", String(byteArrayOf(0, 208.toByte()), ISO_8859_1))
                // 0403 Ѓ Cyrillic capital letter GJE
                // ≡ 0413 Г  0301 (accent)
                // Ghe only maps to G-acute, which is not in our charset.
                // 0404 Є Cyrillic capital letter UKRAINIAN IE
                tempMap.put("\u0404", String(byteArrayOf(0, 202.toByte()), ISO_8859_1))
                // 0405 Ѕ Cyrillic capital letter DZE
                tempMap.put("\u0405", "S") //
                // 0406 І Cyrillic capital letter BYELORUSSIAN-
                // UKRAINIAN I
                // → 0049 I  latin capital letter i
                // → 0456 і  cyrillic small letter byelorussian-
                // ukrainian i
                // → 04C0 Ӏ  cyrillic letter palochka
                tempMap.put("\u0406", String(byteArrayOf(0, 204.toByte()), ISO_8859_1))
                // 0407 Ї Cyrillic capital letter YI
                // ≡ 0406 І  0308 (diuresis)
                tempMap.put("\u0407", String(byteArrayOf(0, 207.toByte()), ISO_8859_1))
                // 0408 Ј Cyrillic capital letter JE
                // 0409 Љ Cyrillic capital letter LJE
                // 040A Њ Cyrillic capital letter NJE
                // 040B Ћ Cyrillic capital letter TSHE
                // 040C Ќ Cyrillic capital letter KJE
                // ≡ 041A К  0301 (accent)
                // 040D Ѝ Cyrillic capital letter I WITH GRAVE
                // ≡ 0418 И  0300 (accent)
                // 040E Ў Cyrillic capital letter SHORT U
                // ≡ 0423 У  0306 (accent)
                // 040F Џ Cyrillic capital letter DZHE

                // Basic Russian alphabet
                // See: http://www.unicode.org/charts/PDF/U0400.pdf
                // 0410 А Cyrillic capital letter A => Latin A
                tempMap.put("\u0410", "A")
                // 0411 Б Cyrillic capital letter BE => Latin B
                // → 0183 ƃ  latin small letter b with topbar
                tempMap.put("\u0411", "B")
                // 0412 В Cyrillic capital letter VE => Latin V
                tempMap.put("\u0412", "V")
                // 0413 Г Cyrillic capital letter GHE => Latin G
                tempMap.put("\u0413", "G")
                // 0414 Д Cyrillic capital letter DE => Latin D
                tempMap.put("\u0414", "D")
                // 0415 Е Cyrillic capital letter IE => Latin E
                tempMap.put("\u0415", "E")
                // 0416 Ж Cyrillic capital letter ZHE => Z-caron
                tempMap.put("\u0416", String(byteArrayOf(0, 142.toByte()), ISO_8859_1))
                // 0417 З Cyrillic capital letter ZE => Latin Z
                tempMap.put("\u0417", "Z")
                // 0418 И Cyrillic capital letter I => Latin I
                tempMap.put("\u0418", "I")
                // 0419 Й Cyrillic capital letter SHORT I => Latin J
                // ≡ 0418 И  0306 (a little mark)
                // The two-character form (reversed N plus the mark) is not supported.
                tempMap.put("\u0419", "J")
                // 041A К Cyrillic capital letter KA => Latin K
                tempMap.put("\u041A", "K")
                // 041B Л Cyrillic capital letter EL => Latin L
                tempMap.put("\u041B", "L")
                // 041C М Cyrillic capital letter EM => Latin M
                tempMap.put("\u041C", "M")
                // 041D Н Cyrillic capital letter EN => Latin N
                tempMap.put("\u041D", "N")
                // 041E О Cyrillic capital letter O => Latin O
                tempMap.put("\u041E", "O")
                // 041F П Cyrillic capital letter PE => Latin P
                tempMap.put("\u041F", "P")
                // 0420 Р Cyrillic capital letter ER => Latin R
                tempMap.put("\u0420", "R")
                // 0421 С Cyrillic capital letter ES => Latin S
                tempMap.put("\u0421", "S")
                // 0422 Т Cyrillic capital letter TE => Latin T
                tempMap.put("\u0422", "T")
                // 0423 У Cyrillic capital letter U => Latin U
                // → 0478 Ѹ  cyrillic capital letter uk
                // → 04AF ү  cyrillic small letter straight u
                // → A64A Ꙋ  cyrillic capital letter monograph uk
                tempMap.put("\u0423", "U")
                tempMap.put("\u0478", "U") // Is this right?
                tempMap.put("\u04AF", "U") // Is this right?
                tempMap.put("\uA64A", "U") // Is this right?
                // 0424 Ф Cyrillic capital letter EF => Latin F
                tempMap.put("\u0424", "F")
                // 0425 Х Cyrillic capital letter HA => Latin H
                tempMap.put("\u0425", "H")
                // 0426 Ц Cyrillic capital letter TSE => Latin C
                tempMap.put("\u0426", "C")
                // 0427 Ч Cyrillic capital letter CHE => Mapping to "Ch" because there is no
                // C-caron - hope this is the best choice!  A also had this as "CH" but some make it
                // Tch as in Tchaikovsky, really didn't know what to do here.
                tempMap.put("\u0427", "Ch")
                // 0428 Ш Cyrillic capital letter SHA => S-caron
                tempMap.put("\u0428", String(byteArrayOf(0, 138.toByte()), ISO_8859_1))
                // 0429 Щ Cyrillic capital letter SHCHA => Latin "Shch" because there is no
                // S-circumflex to map it to.  Should it go to S-caron like SHA?
                tempMap.put("\u0429", "Shch")
                // 042A Ъ Cyrillic capital letter HARD SIGN => Latin double prime, or in this case,
                // right double-quote.
                tempMap.put("\u042A", String(byteArrayOf(0, 148.toByte()), ISO_8859_1))
                // 042B Ы Cyrillic capital letter YERU => Latin Y
                tempMap.put("\u042B", "Y")
                // 042C Ь Cyrillic capital letter SOFT SIGN => Latin prime, or in this case,
                // the right-single-quote.
                tempMap.put("\u042C", String(byteArrayOf(0, 146.toByte()), ISO_8859_1))
                // 042D Э Cyrillic capital letter E => Latin E-grave
                tempMap.put("\u042D", String(byteArrayOf(0, 200.toByte()), ISO_8859_1))
                // 042E Ю Cyrillic capital letter YU => Latin U-circumflex
                tempMap.put("\u042E", String(byteArrayOf(0, 219.toByte()), ISO_8859_1))
                // 042F Я Cyrillic capital letter YA => A-circumflex
                tempMap.put("\u042F", String(byteArrayOf(0, 194.toByte()), ISO_8859_1))
                // 0430 а Cyrillic small letter A
                tempMap.put("\u0430", "a")
                // 0431 б Cyrillic small letter BE
                tempMap.put("\u0431", "b")
                // 0432 в Cyrillic small letter VE
                tempMap.put("\u0432", "v")
                // 0433 г Cyrillic small letter GHE
                tempMap.put("\u0433", "g")
                // 0434 д Cyrillic small letter DE
                tempMap.put("\u0434", "d")
                // 0435 е Cyrillic small letter IE
                tempMap.put("\u0435", "e")
                // 0436 ж Cyrillic small letter ZHE
                tempMap.put("\u0436", String(byteArrayOf(0, 158.toByte()), ISO_8859_1))
                // 0437 з Cyrillic small letter ZE
                tempMap.put("\u0437", "z")
                // 0438 и Cyrillic small letter I
                tempMap.put("\u0438", "i")
                // 0439 й Cyrillic small letter SHORT I
                // ≡ 0438 и  0306 (accent)
                tempMap.put("\u0439", "j")
                // 043A к Cyrillic small letter KA
                tempMap.put("\u043A", "k")
                // 043B л Cyrillic small letter EL
                tempMap.put("\u043B", "l")
                // 043C м Cyrillic small letter EM
                tempMap.put("\u043C", "m")
                // 043D н Cyrillic small letter EN
                tempMap.put("\u043D", "n")
                // 043E о Cyrillic small letter O
                tempMap.put("\u043E", "o")
                // 043F п Cyrillic small letter PE
                tempMap.put("\u043F", "p")
                // 0440 р Cyrillic small letter ER
                tempMap.put("\u0440", "r")
                // 0441 с Cyrillic small letter ES
                tempMap.put("\u0441", "s")
                // 0442 т Cyrillic small letter TE
                tempMap.put("\u0442", "t")
                // 0443 у Cyrillic small letter U
                tempMap.put("\u0443", "u")
                // 0444 ф Cyrillic small letter EF
                tempMap.put("\u0444", "f")
                // 0445 х Cyrillic small letter HA
                tempMap.put("\u0445", "h")
                // 0446 ц Cyrillic small letter TSE
                tempMap.put("\u0446", "c")
                // 0447 ч Cyrillic small letter CHE - see notes on capital letter.
                tempMap.put("\u0447", "ch")
                // 0448 ш Cyrillic small letter SHA
                tempMap.put("\u0448", String(byteArrayOf(0, 154.toByte()), ISO_8859_1))
                // 0449 щ Cyrillic small letter SHCHA
                tempMap.put("\u0449", "shch")
                // 044A ъ Cyrillic small letter HARD SIGN
                tempMap.put("\u044A", String(byteArrayOf(0, 148.toByte()), ISO_8859_1))
                // 044B ы Cyrillic small letter YERU
                // → A651 ꙑ  cyrillic small letter yeru with back yer
                tempMap.put("\u044B", "y")
                // 044C ь Cyrillic small letter SOFT SIGN
                // → 0185 ƅ  latin small letter tone six
                // → A64F ꙏ  cyrillic small letter neutral yer
                tempMap.put("\u044C", String(byteArrayOf(0, 146.toByte()), ISO_8859_1))
                // 044D э Cyrillic small letter E
                tempMap.put("\u044D", String(byteArrayOf(0, 232.toByte()), ISO_8859_1))
                // 044E ю Cyrillic small letter YU
                // → A655 ꙕ  cyrillic small letter reversed yu
                tempMap.put("\u044E", String(byteArrayOf(0, 251.toByte()), ISO_8859_1))
                tempMap.put("\uA655", String(byteArrayOf(0, 251.toByte()), ISO_8859_1)) // is this right?
                // 044F я Cyrillic small letter YA => a-circumflex
                tempMap.put("\u044F", String(byteArrayOf(0, 226.toByte()), ISO_8859_1))

                // Cyrillic extensions
                // 0450 ѐ CYRILLIC SMALL LETTER IE WITH GRAVE
                // • Macedonian
                // ≡ 0435 е  0300 $̀
                tempMap.put("\u0450", String(byteArrayOf(0, 232.toByte()), ISO_8859_1)) // e-grave => e-grave
                // 0451 ё CYRILLIC SMALL LETTER IO
                // • Russian, ...
                // ≡ 0435 е  0308 $̈
                tempMap.put("\u0451", String(byteArrayOf(0, 235.toByte()), ISO_8859_1))
                // 0452 ђ CYRILLIC SMALL LETTER DJE
                // • Serbian
                // → 0111 đ  latin small letter d with stroke
                tempMap.put("\u0452", String(byteArrayOf(0, 240.toByte()), ISO_8859_1))
                // 0453 ѓ CYRILLIC SMALL LETTER GJE - only maps to g-acute, which is not in our charset.
                // • Macedonian
                // ≡ 0433 г  0301 $́
                // 0454 є CYRILLIC SMALL LETTER UKRAINIAN IE
                // = Old Cyrillic yest
                tempMap.put("\u0454", String(byteArrayOf(0, 234.toByte()), ISO_8859_1))
                // 0455 ѕ CYRILLIC SMALL LETTER DZE
                // • Macedonian
                // → A643 ꙃ  cyrillic small letter dzelo
                tempMap.put("\u0455", "s")
                // 0456 CYRILLIC SMALL LETTER BYELORUSSIAN-
                // UKRAINIAN I
                // = Old Cyrillic i
                tempMap.put("\u0456", String(byteArrayOf(0, 236.toByte()), ISO_8859_1))
                // 0457 ї CYRILLIC SMALL LETTER YI
                // • Ukrainian
                // ≡ 0456 і  0308 $̈
                tempMap.put("\u0457", String(byteArrayOf(0, 239.toByte()), ISO_8859_1))
                // 0458 ј CYRILLIC SMALL LETTER JE
                // • Serbian, Azerbaijani, Altay
                // 0459 љ CYRILLIC SMALL LETTER LJE
                // • Serbian, Macedonian
                // → 01C9 lj  latin small letter lj
                // 045A њ CYRILLIC SMALL LETTER NJE
                // • Serbian, Macedonian
                // → 01CC nj  latin small letter nj
                // 045B ћ CYRILLIC SMALL LETTER TSHE
                // • Serbian
                // → 0107 ć  latin small letter c with acute
                // → 0127 ħ  latin small letter h with stroke
                // → 040B Ћ  cyrillic capital letter tshe
                // → 210F ħ  planck constant over two pi
                // → A649 ꙉ  cyrillic small letter djerv
                // 045C ќ CYRILLIC SMALL LETTER KJE
                // • Macedonian
                // ≡ 043A к  0301 $́
                // 045D ѝ CYRILLIC SMALL LETTER I WITH GRAVE
                // • Macedonian, Bulgarian
                // ≡ 0438 и  0300 $̀
                // 045E ў CYRILLIC SMALL LETTER SHORT U
                // • Byelorussian, Uzbek
                // ≡ 0443 у  0306 $̆
                // 045F џ CYRILLIC SMALL LETTER DZHE
                // • Serbian, Macedonian, Abkhasian
                // → 01C6 dž  latin small letter dz with caron

                // Extended Cyrillic
                // ...
                // 0490 Ґ CYRILLIC CAPITAL LETTER GHE WITH UPTURN => G ?
                tempMap.put("\u0490", "G") // Ghe with upturn
                // 0491 ґ CYRILLIC SMALL LETTER GHE WITH UPTURN
                // • Ukrainian
                tempMap.put("\u0491", "g")

                // Other commonly-used unicode characters with exact WinAnsi equivalents
                tempMap.put("\u2013", String(byteArrayOf(0, 150.toByte()), ISO_8859_1)) // En-dash
                tempMap.put("\u2014", String(byteArrayOf(0, 151.toByte()), ISO_8859_1)) // Em-dash
                tempMap.put("\u2018", String(byteArrayOf(0, 145.toByte()), ISO_8859_1)) // Curved single open quote
                tempMap.put("\u2019", String(byteArrayOf(0, 146.toByte()), ISO_8859_1)) // Curved single close-quote
                tempMap.put("\u201A", String(byteArrayOf(0, 130.toByte()), ISO_8859_1)) // Low single curved-quote
                tempMap.put("\u201C", String(byteArrayOf(0, 147.toByte()), ISO_8859_1)) // Curved double open quote
                tempMap.put("\u201D", String(byteArrayOf(0, 148.toByte()), ISO_8859_1)) // Curved double close-quote
                tempMap.put("\u201E", String(byteArrayOf(0, 132.toByte()), ISO_8859_1)) // Low right double quote.
                tempMap.put("\u2020", String(byteArrayOf(0, 134.toByte()), ISO_8859_1)) // Dagger
                tempMap.put("\u2021", String(byteArrayOf(0, 135.toByte()), ISO_8859_1)) // Double dagger
                tempMap.put(BULLET_CHAR, String(byteArrayOf(0, 149.toByte()), ISO_8859_1)) // Bullet - use this as replacement character.
                tempMap.put("\u2026", String(byteArrayOf(0, 133.toByte()), ISO_8859_1)) // Ellipsis
                tempMap.put("\u2030", String(byteArrayOf(0, 137.toByte()), ISO_8859_1)) // Permille
                tempMap.put("\u2039", String(byteArrayOf(0, 139.toByte()), ISO_8859_1)) // Left angle-quote
                tempMap.put("\u203A", String(byteArrayOf(0, 155.toByte()), ISO_8859_1)) // Right angle-quote
                tempMap.put("\u20ac", String(byteArrayOf(0, 128.toByte()), ISO_8859_1)) // Euro symbol
                tempMap.put("\u2122", String(byteArrayOf(0, 153.toByte()), ISO_8859_1)) // Trademark symbol

            } catch (uee: UnsupportedEncodingException) {
                throw IllegalStateException("Problem creating translation table due to Unsupported Encoding (coding error)", uee)
            }

            utf16ToWinAnsi = Collections.unmodifiableMap(tempMap)
        }


        // private static final Pattern whitespacePattern = Pattern.compile("\\p{Z}+");
        // What about \u00ba??
        // \u00a0-\u00a9 \u00ab-\u00b9 \u00bb-\u00bf \u00d7 \u00f7
        private val nonAsciiPattern = Pattern.compile("[^\u0000-\u00ff]")

        /**
         *
         * PDF files are limited to the 217 characters of Windows-1252 which the PDF spec calls WinAnsi
         * and Java calls ISO-8859-1.  This method transliterates the standard Java UTF-16 character
         * representations to their Windows-1252 equivalents where such translation is possible.  Any
         * character (e.g. Kanji) which does not have an appropriate substitute in Windows-1252 will be
         * mapped to the bullet character (a round dot).
         *
         *
         * This transliteration covers the modern alphabets of the following languages:<br></br>
         *
         * Afrikaans (af),
         * Albanian (sq), Basque (eu), Catalan (ca), Danish (da), Dutch (nl), English (en), Faroese (fo),
         * Finnish (fi), French (fr), Galician (gl), German (de), Icelandic (is), Irish (ga),
         * Italian (it), Norwegian (no), Portuguese (pt), Scottish (gd), Spanish (es), Swedish (sv).
         *
         *
         * Romanized substitutions are used for the Cyrillic characters of the modern Russian (ru)
         * alphabet according to ISO 9:1995 with the following phonetic substitutions: 'Ch' for Ч and
         * 'Shch' for Щ.
         *
         *
         * The PdfLayoutMgr calls this method internally whenever it renders text (transliteration has
         * to happen before line breaking), but is available externally in case you wish to use it
         * directly with PDFBox.
         *
         * @param orig a string in the standard Java UTF-16 encoding
         * @return a string in Windows-1252 (informally called ISO-8859-1 or WinAnsi)
         */
        fun convertJavaStringToWinAnsi(orig: String): String {
            //        ByteBuffer bb = StandardCharsets.UTF_16.encode(CharBuffer.wrap(in));
            //        // then decode those bytes as US-ASCII
            //        return StandardCharsets.ISO_8859_1.decode(bb).toString();
            // return java.nio.charset.StandardCharsets.ISO_8859_1.encode(in);

            val m = nonAsciiPattern.matcher(orig)

            val sB = StringBuilder()
            var idx = 0
            while (m.find()) {

                val start = m.start() // first character of match.
                if (idx < start) {
                    // Append everything from the last match up to this one.
                    sB.append(orig.subSequence(idx, start))
                }

                var s: String? = utf16ToWinAnsi[m.group()]

                // "In WinAnsiEncoding, all unused codes greater than 40 map to the bullet character."
                // source: PDF spec, Annex D.3 PDFDocEncoding Character Set p. 656 footnote about
                // WinAnsiEncoding.
                //
                // I think the bullet is the closest thing to a "replacement character" in the
                // WinAnsi character set, so that's what I'll use it for.  It looks tons better than
                // nullnullnull...
                if (s == null) {
                    s = utf16ToWinAnsi[BULLET_CHAR]
                }
                sB.append(s)

                idx = m.end() // m.end() is exclusive
            }
            if (idx < orig.length) {
                sB.append(orig.subSequence(idx, orig.length))
            }
            return sB.toString()
        }
    }

}
