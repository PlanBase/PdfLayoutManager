# Changelog

## 2.0.3 - 2018-01-09
 - Fixed: Line-wrapping sometimes drops the space before the last word on a line.
   Only happened when the last word should have been too long to line wrap by a width of less than one space.
 - Fixed: When nesting tables, the inner table gets extra space beneath it after a page break.
   Went back to earlier idea of adding reallyRender boolean parameter to LineWrapped.render() so
   that we could share all the logic between measuring and actually rendering.

## 2.0.2 - 2018-01-01
 - Upgraded Apache PDFBox to 2.0.0 and Kotlin to 1.2.10
 - Added this changelog
 - Added Javadocs.
 
## 2.0.1 - 2017-12-21
 - Initial release of 2.x and Split from PdfLayoutMgr.  This project is the logical continuation of that one.
   - Removed Glen Peterson from copyright holders
   - Adopted AGPL license
   - Added inline elements
   - Significantly improved all line- and page-breaking
   - Converted to Kotlin
   - Simplified interface with an eye to make it match the PDF spec maybe more than it matches PDFBox.
   - Fixed numerous bugs