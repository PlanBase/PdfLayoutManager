# PdfLayoutMgr2

A wrapper for PDFBox to add line-breaking, page-breaking, and tables.
Uses a box-model (like HTML) for styles.
Requires PDFBox which in turn requires Log4J or apache commons Logging.

![Sample Output](sampleScreenShot.png)

# NOTICE: No longer Apache!
You can use this as free software under the [Affero GPL](https://www.gnu.org/licenses/agpl-3.0.en.html) or contact PlanBase about a commercial license.
See the [FAQ](#faq) below

## Usage

Example: [TestManualllyPdfLayoutMgr.java](src/test/java/TestManualllyPdfLayoutMgr.java)

[API Docs](https://glenkpeterson.github.io/PdfLayoutManager/apidocs/)

# Maven Dependency
```xml
    <!-- Now Affero GPL - no longer Apache -->
    <dependency>
        <groupId>com.planbase.pdf</groupId>
        <artifactId>PdfLayoutMgr2</artifactId>
        <version>1.0.0-STAGING</version>
    </dependency>
```

# FAQ

#### Q: Can I use this in closed-source software?
**A:** You can purchase a commercial license from [PlanBase Inc.](https://planbase.com)
Otherwise, you must comply with all the terms of the [Affero GPL](https://www.gnu.org/licenses/agpl-3.0.en.html).
Affero GPL software cannot be combined with closed-source software in the same JVM even if the AGPL software is used over a network or the Internet.

#### Q: Can I use this in non-AfferoGPL Open-Sourced software?
**A:** No.
Any software that uses AfferoGPL code (in the same JVM) must be released under the AfferoGPL. 

#### Q: Why isn't this Apache-licensed any more?
**A:** The recent version required a near-total rewrite in order to accommodate inline images and style changes.
PlanBase paid for that.  It was a significant investment and they deserve the chance to profit from it.
You can still use the old PdfLayoutManager2 versions 0.x under the Apache license, but it lacks inline styles and images.

#### Q: What languages/character sets does PdfLayoutManager2 support?
**A:** If you embed fonts, you can use whatever characters are in that font.  PDFBox throws an exception if you request a character that's not covered by your font list.

The PDF spec guarantees support for [WinAnsiEncoding AKA Windows Code Page 1252](http://en.wikipedia.org/wiki/Windows-1252) and maybe four PDType1Fonts fonts without any font embedding.  WinAnsi covers the following languages:

Afrikaans (af), Albanian (sq), Basque (eu), Catalan (ca), Danish (da), Dutch (nl), English (en), Faroese (fo), Finnish (fi), French (fr), Galician (gl), German (de), Icelandic (is), Irish (ga), Italian (it), Norwegian (no), Portuguese (pt), Scottish (gd), Spanish (es), and Swedish (sv)


#### Q: I don't want text wrapping.  I just want to set the size of a cell and let it chop off whatever I put in there.
**A:** PdfLayoutManager2 was intended to provide html-table-like flowing of text and resizing of cells to fit whatever you put in them, even across multiple pages.  If you don't need that, use PDFBox directly.  If you need other features of PdfLayoutManager2, there is a minHeight() setting on table rows.  Combined with padding and alignment, that may get you what you need to layout things that will always fit in the box.

#### Q: Will PdfLayoutManager2 ever support cropping the contents of a fixed-size box?
**A:** If individual letters or images have a dimension which is bigger than the same dimension of their bounding box, we either have to suppress their display, or crop them.  The PDF spec mentions something about a "clipping path" that might be usable for cropping overflow if you turn it on, render your object, then turn it off again.

If the contents are all little things, we could just show as many little letters or images as completely fit, then no more (truncate the list of contents).  Showing none could make truncation work for big objects too, but this is conceptually very different from the very reason for the existence of PdfLayoutManager.

Maybe some day we will provide some sample code so you can do truncation yourself.  [TextStyle](src/main/java/com/planbase/pdf/layoutmanager/TextStyle.java) has lineHeight() and stringWidthInDocUnits() that you may find useful for writing your own compatible cropping algorithm.  If you do that (and it works well), consider contributing it back to PdfLayoutManager2 (at least to this doc) so that others can benefit!

#### Q: Why doesn't PdfLayoutManager line-wrap my insanely long single-word test string properly?
**A:** For text wrapping to work, the text needs occasional whitespace.  In HTML, strings without whitespace do not wrap at all!  In PdfLayoutManager, a long enough string will wrap at some point wider than the cell.

The text wrapping algorithm picks a slightly long starting guess for where to wrap the text, then steps backward looking for whitespace. If it doesn't find any whitspace, it splits the first line at it's original guess length and continues trying to wrap the rest of the text on the next line.

# License
Copyright 2017 PlanBase Inc.

PdfLayoutMgr2 is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

PdfLayoutMgr2 is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with PdfLayoutMgr2.  If not, see [https://www.gnu.org/licenses/agpl-3.0.en.html](https://www.gnu.org/licenses/agpl-3.0.en.html)

If you wish to use this code with proprietary software,
contact [PlanBase Inc.](https://planbase.com) to purchase a commercial license.


# Building from Source

Requires Maven 3, Java 8, and Kotlin 1.1.51 or greater.  Jar file ends up in the `target/` sub-folder.

API documentation can be built with `mvn javadoc:javadoc` and is then found at `target/site/apidocs/index.html`

A sample PDF named `test.pdf` shows up in the root folder of this project when you run `mvn test`.

A jar file can be built with `mvn clean package` and ends up in the `target/` sub-folder.  Or type `mvn clean install` to build and install into your local maven repository.
