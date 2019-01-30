package com.planbase.pdf.lm2.utils

/**
 * PDF 32000-1:2008 section 8.4.3.4 Line Join Style
 * The line join style shall specify the shape to be used at the corners of paths that are stroked.
 * Join styles shall be significant only at points where consecutive segments of a path
 * connect at an angle; segments that meet or intersect fortuitously shall receive no special treatment.
 */
enum class LineJoinStyle(val style: Int) {
    /**
     * "The outer edges of the strokes for the two segments shall be
     * extended until they meet at an angle, as in a picture frame. If the
     * segments meet at too sharp an angle (as defined by the miter limit
     * parameter—see 8.4.3.5, 'Miter Limit'), a bevel join shall be used
     * instead."
     * Source: p125 table 55 Line_Join_Styles
     */
    MITER(0),
    /**
     * "An arc of a circle with a diameter equal to the line width
     * shall be drawn around the point where the two segments meet,
     * connecting the outer edges of the strokes for the two segments. This
     * pieslice-shaped figure shall be filled in, producing a rounded corner.
     * NOTE: The definition of round join was changed in PDF 1.5.
     * In rare cases, the implementation of the previous specification could produce unexpected results."
     * Source: p125 table 55 Line_Join_Styles
     */
    ROUND(1),
    /**
     * "The two segments shall be finished with butt caps (see
     * 8.4.3.3, 'Line Cap Style') and the resulting notch beyond the ends of
     * the segments shall be filled with a triangle."
     * Source: p125 table 55 Line_Join_Styles
     */
    BEVEL(2)
}