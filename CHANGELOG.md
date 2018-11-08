# Changelog

Bigger headings mean more stable releases!


## 2.2.3 2018-11-07 "Fixed space-after-last-word issue"
 - Fixed a bug in Multi-line-wrapped that reaturned a trailing space at the end of a line.
 The line-break measurer needs to return a space at the end of a Text item because it can't know if there's something else on the line after this text.
 Only the multi-line-wrapper knows what item actually ends the line, so it has to check the final thing for trailing spaces and remove them.

### 2.2.2 2018-11-02 "Fixed space-before-last-word issue"
 - Fixed a bug in the line-break measurer that it wasn't returning a trailing space in a corner case.

### 2.2.1 2018-10-19 "Improved Text Wrapping"
 - Changed wordspacing/charSpacing ratio from 3/1 to 2/1 to minimize rivers while still looking peaceful.

### 2.2.0 2018-10-19 "Improved Text Wrapping"
 - Rewrote Text.Companion.tryGettingText()
 - Renamed TextStyle.withFont() to .withFontAndLineHeight() because it now takes the default line height of the new
   font.
 - Upgraded PDFBox dependency to 2.0.12 (You should too, due to
 [CVE-2018-11797](https://nvd.nist.gov/vuln/detail/CVE-2018-11797))

## 2.1.12 2018-10-09 "Fixed: hyphenated word longer than a whole line goes into infinite loop."
 - Silly error - had forgotten to update an index when breaking out of a loop.
 Added tests to ensure this stays fixed.  We published two 200-page books with this code before finding this bug.

### 2.1.11 2018-10-05 "Fixed: SinglePage.add() takes X coordinate from edge of page (should take from edge of body)"
 - When a word is too big to fit on any line it has to overflow, but we weren't returning the correct word-length
 (idx) in Text.tryGettingText().
 This made it return the same too-long word every time it was asked for more text
 which put MultiLineWrapped.wrapLines() into a loop.
 This issue is now fixed.
 - Bumped Kotlin to 1.2.51

### 2.1.10 2018-07-11 "Fixed: infinite loop on too-long text"
 - When a word is too big to fit on any line it has to overflow, but we weren't returning the correct word-length
 (idx) in Text.tryGettingText().
 This made it return the same too-long word every time it was asked for more text
 which put MultiLineWrapped.wrapLines() into a loop.
 This issue is now fixed.
 - Bumped Kotlin to 1.2.51

### 2.1.9 2018-06-08 "Fixed: Free font file descriptors"
 - PdfLayoutManger now caches all TrueTypeFont objects and tells them to free their file descriptors once the
 document is closed.
 This fixes a file descriptor leak.
 We also de-duplicate so that if you request that the same font be loaded multiple times, we only load it
 once per PdfLayoutMgr.

### 2.1.8 2018-06-07 "Fixed: missing space before last word"
 - In a cell, on a line with multiple items (2 styles of text, an image followed by text, etc.) where
 the last item was text, and the text was longer than the line length by less than the width of a single space,
 the space before the last word was removed.
 A rare issue, but fixed now.

### 2.1.7 2018-06-01 "Fix"
 - 2.1.6 was a mistake, which this undoes.

### 2.1.5 2018-05-31 "TextStyle.withFont()"
 - Added .withFont() method to return a copy of the immutable TextStyle with a different font.

### 2.1.4 2018-05-31 "Draw-Image Z-Index"
 - Added Z-Index parameter to RenderTarget.drawImage().
 If we're willing to add some complexity to WrappedCell.render() (we aren't today) then we could do away with
 z-indexing.  For now, use lower numbers for background items and higher for foreground.
 If you aren't doing complicated layering, don't specify any z-indexes and it should all work out. 
 - Moved DEFAULT_Z_INDEX from SinglePage to RenderTarget.
 - Made minimum line-width for justified text 0.7 instead of 0.75 because I think the new algorithm allows it to look
 ok a little shorter than before, and a single line of unjustified text is pretty ugly.

##### 2.1.3 2018-05-29 "Orphan Prevntion phase I"
 - Prevented orphans in a few situations;
   the single line of a longer paragraph or bullet at the bottom of the page now sometimes starts on the next page
   instead.

## 2.1.2 2018-05-29 "Mitered Cell Borders"
 - Added mitering for Cell borders.
   For a cell with four identical border styles, this uses the mitering built into PDF with
   a "closed path" of lines.
   For cells with uneven border styles, this butts the ends of each border up next to the edge of the two adjoining
   borders.
 - Added RenderTarget.drawLineLoop() to make a closed path.
 Such a path can only use a single LineStyle for all segments - this is a limitation of the PDF spec.

## 2.1.1 2018-05-11 "Bulleted and Numbered Lists"
 - Added real lists (bulleted and numbered).
 Previously there were only tables that you could make lists manually with.
 But to make page-breaking *and* widow prevention work with lists, they couldn't
 be done in tables.  Now you should use BulletList and NumberList instead.
 They are simpler, easier to use, and eliminate widows.

### 2.1.0 2018-05-11 "Double Precision"
 - Changed all Floats to Doubles
 - Aded PdfLayoutMgr.getFileIdentifiers() and .setFileIdentifiers() to set ID manually for each file.
 - Started saving PDF files so that we can see differences.
 - Changed PageGrouping.appendCell() to take an x-offset as the first parameter (usually set to zero)
 - Removed LineWrapped.lineHeight because it entirely duplicated .dim.height.
 - Renamed Coord.x() .y() to .withX() and .withY() and Dim.width() .height() to .withWidth() and .withHeight() because it's clearer.
 - Made WrappedText a top-level class like WrappedCell.
 - Added handwriting font to Alice test.
 - Slightly improved text justification for full lines of text.
 - Added widow prevention to cells (only cells not associated with tables).

Upgrade Instructions
```
YOU MUST MANUALLY ACCEPT EACH CHANGE!
Colors still use floats and your code may use floats for other reasons!

Replace Regex fix float literals with a decimal point:
([0-9]+)[.]([0-9]+)f
$1.$2

Replace Regex to fix any float literals without a decimal point:
([0-9]+)f
$1.0

Replace Words:
Float
Double
```

## 2.0.10 - 2018-03-29 "Fill-Rect after many pages"
 - Fixed bug where after a hundred or more pages, the edge of a fill-rect in a PageGrouping could slip between the
 floating point gaps and cause an exception.

### 2.0.9 - 2018-03-23 "Page for Cursor"
 - Added PageGrouping.pageForCursor()
 - Fixed bug where an extra page was sometimes added to the document when merely measuring items for fit.
 - renamed DimAndPages to DimAndPageNums.

#### 2.0.8 - 2018-03-23 "Room Below Cursor"
 - Added roomBelowCursor() to PageGrouping.
 - Bumped Kotlin version
 - IDEA reports 87% test coverage by line for the entire project.
 - Added numPages to LayoutMgr (not sure this is a good idea, but it's useful)

#### 2.0.7 - 2018-02-01 "Table of Contents"
Together these changes should allow the creation of a table of contents and an index.
Instead of adding each paragraph to a single cell for a whole chapter, you can now add each top-level LineWrapped element directly to the page.
Each add returns the page number range that the final rendered element so that you can store it and later insert page numbers.
 - Changed RenderTarget methods to return a HeightAndPage instead of Float and  IntRange instead of RenderTarget or void.
 - Changed LineWrapped.render to return DimAndPages instead of just Dim.
 - Changed PageGrouping constructor to take the top-left of the Body (was the lower-left).
 - Added PageGrouping.add() which returns an adjusted height and page range.
 - Moved commit() from PageGrouping to PdfLayoutMgr and made PdfLayoutMgr.logicalPageEnd() private.
 - Added PageGrouping cursor with cursorToNewPage() function for adding pages.

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