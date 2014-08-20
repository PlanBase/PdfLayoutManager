// Copyright 2013-03-03 PlanBase Inc. & Glen Peterson
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.planbase.pdf.layoutmanager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 A styled table cell or layout block with a pre-set horizontal width.  Vertical height is calculated 
 based on how the content is rendered with regard to line-breaks and page-breaks.
 */
public class Cell implements Renderable {

    // These are limits of the cell, not the contents.
    private final CellStyle cellStyle;
    private final float width;

    // A list of the contents.  It's pretty limiting to have one item per row.
    private final List<Renderable> rows;

    private Cell(CellStyle cs, float w, List<Renderable> r) {
        if (w < 0) {
            throw new IllegalArgumentException("A cell cannot have a negative width");
        }
        cellStyle = cs; width = w; rows = r;
    }

    /**
     Creates a new cell.

     @param w the width (height will be calculated based on how objects can be rendered within this
         width).
     @param cs the cell style
     @return a cell suitable for rendering.
     */

//    @param s the style to show any text objects in.
    //      @param r the text (String) and/or pictures (Jpegs as BufferedImages) to render in this cell.
//         Pictures are assumed to print 300DPI with 72 document units per inch.  A null in this list
//         adds a little vertical space, like a half-line between paragraphs.

    public static Cell of(CellStyle cs, float w) { //, final Object... r) {
        return new Cell(cs, w, Collections.<Renderable>emptyList());
//                        (r == null) ? Collections.emptyList()
//                                    : Arrays.asList(r));
    }

    // Simple case of a single styled String
    public static Cell of(CellStyle cs, float w, TextStyle ts, String s) {
        List<Renderable> ls = new ArrayList<Renderable>(1);
        ls.add(Text.of(ts, s));
        return new Cell(cs, w, ls);
    }

    // Simple case of a single styled String
    public static Cell of(CellStyle cs, float w, Text t) {
        List<Renderable> ls = new ArrayList<Renderable>(1);
        ls.add(t);
        return new Cell(cs, w, ls);
    }

    public static Cell of(CellStyle cs, float w, ScaledJpeg j) {
        List<Renderable> ls = new ArrayList<Renderable>(1);
        ls.add(j);
        return new Cell(cs, w, ls);
    }

    public static Cell of(CellStyle cs, float w, Renderable r) {
        List<Renderable> ls = new ArrayList<Renderable>(1);
        ls.add(r);
        return new Cell(cs, w, ls);
    }

    // Simple case of a single styled String
    public static Cell of(CellStyle cs, float w, Cell c) {
        List<Renderable> ls = new ArrayList<Renderable>(1);
        ls.add(c);
        return new Cell(cs, w, ls);
    }

    public CellStyle cellStyle() { return cellStyle; }
    // public BorderStyle border() { return borderStyle; }
    public float width() { return width; }
    // public Color bgColor() { return bgColor; }

//    /**
//     Shows text without any boxing or background.
//
//     @return the final y-value
//     @throws java.io.IOException if there is an error reading the font metrics from the underlying font
//     file.  I think with a built-in font this is not possible, but it's in the signature of
//     the PDFBox class, so I have to throw it too.
//
//     @param x the left-most (least) x-value.
//     @param origY the top-most (greatest) y-value.
//     @param allPages set to true if this should be treated as a header or footer for all pages.
//     @param mgr the page manager this Cell belongs to.  Probably should be set at creation
//     time.
//     */
//    float processRows(final float x, final float origY, boolean allPages, PdfLayoutMgr mgr) {
//        // Note: Always used as: y = origY - TextStyle.BREADCRUMB.height,
//        if ( (rows == null) || (rows.size() < 1) ) {
//            return 0;
//        }
//        // Text is displayed based on its baseline, but this method takes a top-left corner of the
//        // "cell" that contains the text.  This is the translation:
//
//        float y = origY - cellStyle.padding().top();
//        for (Renderable renderable : rows) {
//            if (renderable == null) {
//                y -= 4;
//                continue;
//            }
//
//            XyPair p = renderable.render(XyPair.of(x, y), allPages, mgr, width);
//            y = p.y();
//        } // end for each row
//
//        return origY - y - cellStyle.padding().bottom(); // numLines * height;
//    } // end processRows();

    public XyPair calcDimensions(float maxWidth) {
        //         maxHeight += 2; // padding.
        XyPair maxWidthHeight = XyPair.of(0,0);
        for (Renderable row : rows) {
            XyPair rowDim = row.calcDimensions(maxWidth);
            maxWidthHeight = maxWidthHeight.maxXandY(rowDim);
            System.out.println("\trowDim = " + rowDim);
            System.out.println("\trow = " + row);
            System.out.println("\tmaxWidthHeight = " + maxWidthHeight);

        }
        return maxWidthHeight;
    }

    /*
    Renders item and all child-items with given width and returns the x-y pair of the
    lower-right-hand corner of the last line (e.g. of text).
    */
    public XyPair render(final PdfLayoutMgr mgr, final XyPair outerTopLeft,
                         final XyPair outerDimensions, final boolean allPages) {

        // Draw background first (if necessary) so that everything else ends up on top of it.
        if (cellStyle.bgColor() != null) {
            mgr.putRect(outerTopLeft, outerDimensions, cellStyle.bgColor());
        }

        XyPair innerTopLeft = XyPair.of((outerTopLeft.x() + cellStyle.padding().left()),
                                        (outerTopLeft.y() - cellStyle.padding().top()));

        XyPair innerDimensions = XyPair.of(
                (outerDimensions.x() - cellStyle.padding().left() - cellStyle.padding().right()),
                (outerDimensions.y() - cellStyle.padding().top() - cellStyle.padding().bottom()));

//        float x = outerTopLeft.x() + cellStyle.padding().left();
//        float y = outerTopLeft.y() - cellStyle.padding().top();
//        y -= this.processRows(x, y, allPages, mgr);
//        return XyPair.of(x + width + cellStyle.padding().right(), y - cellStyle.padding().bottom());

        XyPair outerLowerRight = outerTopLeft;
        for (Renderable row : rows) {
            outerLowerRight = row.render(mgr, innerTopLeft, innerDimensions, allPages);
            innerTopLeft = outerLowerRight.x(innerTopLeft.x());
        }

        // Draw border last to cover anything that touches it?
        BorderStyle border = cellStyle.borderStyle();
        if (border != null) {
            float origX = outerTopLeft.x();
            float origY = outerTopLeft.y();
            float rightX = outerTopLeft.x() + outerDimensions.x();
            float bottomY = outerTopLeft.y() - outerDimensions.y();
            // Like CSS it's listed Top, Right, Bottom, left
            if (border.top() != null) {
                mgr.putLine(origX, origY, rightX, origY, border.top());
            }
            if (border.right() != null) {
                mgr.putLine(rightX, origY, rightX, bottomY, border.right());
            }
            if (border.bottom() != null) {
                mgr.putLine(origX, bottomY, rightX, bottomY, border.bottom());
            }
            if (border.left() != null) {
                mgr.putLine(origX, origY, origX, bottomY, border.left());
            }
        }

        return outerLowerRight;
    }


//    public XyPair render(XyPair p, boolean allPages, PdfLayoutMgr mgr, float maxWidth) {
//        // TODO: This shouldn't ignore the internal cellstyle.
//        float x = p.x() + cellStyle.padding().left();
//        float y = p.y() - cellStyle.padding().top();
//        y -= this.processRows(x, y, allPages, mgr);
//        return XyPair.of(x + width + cellStyle.padding().right(), y - cellStyle.padding().bottom());
//    }

    public static Builder builder(CellStyle cellStyle, float width) {
        return new Builder(cellStyle, width);
    }

    public static class Builder {
        private final float width; // Both require this.
        private final CellStyle cellStyle; // Both require this.
        private final List<Renderable> rows = new ArrayList<Renderable>();
        private TextStyle textStyle;

        private Builder(CellStyle cs, float w) { width = w; cellStyle = cs; }

        public Builder add(Text t) { rows.add(t); return this; }
        public Builder addAll(TextStyle ts, List<String> ls) {
            if (ls != null) {
                for (String s : ls) {
                    rows.add(Text.of(ts, s));
                }
            }
            return this;
        }

        public Builder add(ScaledJpeg j) { rows.add(j); return this; }
        public Builder addAll(List<ScaledJpeg> js) {
            if (js != null) { rows.addAll(js); }
            return this;
        }
        public Builder textStyle(TextStyle x) { textStyle = x; return this; }

        public Builder add(Cell c) { rows.add(c); return this; }

        public Cell build() { return new Cell(cellStyle, width, rows); }
    }

    /*
    These are limits of the cell, not the contents.

    float width is a limit of the cell, not of the contents.
    CellStyle cellStyle is the over-all style of the cell, inherited by all contents for which
    it is relevant.

    public static interface CellContents {
        // This is just some junk to indicate that this method will handle anything of this type.
        // Don't go implementing your own stuff and passing it to this method.

    }

    public static class CellText implements CellContents {
        private final float width; // Both require this.
        private final CellStyle cellStyle; // Both require this.
        private final TextStyle textStyle; // Required for Strings.  Unnecessary for Images.
        private final int avgCharsForWidth; // Required for Strings.  Unnecessary for Images.

        private CellText(final TextStyle ts, final float w, CellStyle cs) {
            if (w < 0) {
                throw new IllegalArgumentException("A cell cannot have a negative width");
            }
            textStyle = ts; width = w; cellStyle = cs;
            avgCharsForWidth = (int) ((width * 1220) / textStyle.avgCharWidth());
        }

        public float width() { return width; }
        public CellStyle cellStyle() { return cellStyle; }
    }

    public static class CellImage implements CellContents {
        private final float width; // Both require this.
        private final CellStyle cellStyle; // Both require this.

        private CellImage(final float w, CellStyle cs) {
            if (w < 0) {
                throw new IllegalArgumentException("A cell cannot have a negative width");
            }
            width = w; cellStyle = cs;
        }
        public float width() { return width; }
        public CellStyle cellStyle() { return cellStyle; }
    }
     */
}
