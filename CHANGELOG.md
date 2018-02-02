# Changelog

#### 2.0.7 - 2018-02-01 "Page Numbers"
Together these changes should allow the creation of a table of contents and an index.
Instead of adding each paragraph to a single cell for a whole chapter, you can now add each top-level LineWrapped element directly to the page.
Each add returns the page number range that the final rendered element so that you can store it and later insert page numbers.
 - Changed RenderTarget methods to return a HeightAndPage instead of Float and  IntRange instead of RenderTarget or void.
 - Changed LineWrapped.render to return DimAndPages instead of just Dim.
 - Changed PageGrouping constructor to take the top-left of the Body (was the lower-left).
 - Added PageGrouping.add() which returns an adjusted height and page range.

## 2.0.6 - 2018-01-22
 - Added justified text style: Align.TOP_LEFT_JUSTIFY.
 This may be expanded some day by allowing the client application to supply a hyphenation dictionary
 (key on the whole-word, value is the preferred hyphenation)
 - Renamed BoxStyle.NONE to NO_PAD_NO_BORDER and CellStyle.Default to TOP_LEFT_BORDERLESS.
 - Added const and JvmField annotations for Java compatibility.
 - Upgraded to Kotlin 1.2.21
 - Enhanced toString() representations to produce briefer, valid Kotlin code.
 This facilitates cutting production issues and pasting them into unit tests which saves time reproducing issues.
 - Allowed text to line-wrap on a slash ("/") or several types of hyphens.
 - Fixed bug where a bigger text style later on a line could sometimes end up on the next page - now it takes the whole
 line with it.

###### 2.0.5 - 2018-01-15
 - Changed PageGrouping.appropriatePage() to ignore requiredSpaceBelow when the given height won't fit on a single page.
 It used to throw an exception.

###### 2.0.4 - 2018-01-12
 - Added requiredSpaceBelow param to Cell as a way to push things like headings onto the next page if there isn't
 going to be enough room under them.

###### 2.0.3 - 2018-01-09
 - Fixed: Line-wrapping sometimes drops the space before the last word on a line.
   Only happened when the last word should have been too long to line wrap by a width of less than one space.
 - Fixed: When nesting tables, the inner table gets extra space beneath it after a page break.
   Went back to earlier idea of adding reallyRender boolean parameter to LineWrapped.render() so
   that we could share all the logic between measuring and actually rendering.

###### 2.0.2 - 2018-01-01
 - Upgraded Apache PDFBox to 2.0.0 and Kotlin to 1.2.10
 - Added this changelog
 - Added Javadocs.
 
###### 2.0.1 - 2017-12-21
 - Initial release of 2.x and Split from PdfLayoutMgr.  This project is the logical continuation of that one.
   - Removed Glen Peterson from copyright holders
   - Adopted AGPL license
   - Added inline elements
   - Significantly improved all line- and page-breaking
   - Converted to Kotlin
   - Simplified interface with an eye to make it match the PDF spec maybe more than it matches PDFBox.
   - Fixed numerous bugs