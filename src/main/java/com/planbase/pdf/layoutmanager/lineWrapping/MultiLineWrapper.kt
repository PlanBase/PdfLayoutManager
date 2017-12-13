package com.planbase.pdf.layoutmanager.lineWrapping

// This is a generic stackable line wrapper (a line wrapper that wraps an Iterable of line wrappers)
// This works and was used for wrapping cell contents, until it was discovered that cells need to be pre-wrapped.
class MultiLineWrapper(private val items: Iterator<LineWrappable>) : LineWrapper {
    private var internLineWrapper: LineWrapper =
            if (items.hasNext()) {
                items.next().lineWrapper()
            } else {
                LineWrapper.EmptyLineWrapper
            }

    private fun ensureValidInternLineWrapper() {
        if ( !internLineWrapper.hasMore() &&
             (items.hasNext()) ) {
            internLineWrapper = items.next().lineWrapper()
        }
    }

    override fun hasMore():Boolean {
        ensureValidInternLineWrapper()
        return (items.hasNext()) ||
               internLineWrapper.hasMore()
    }

    override fun getSomething(maxWidth: Float): ConTerm {
        if (maxWidth < 0) {
            throw IllegalArgumentException("Illegal negative width: " + maxWidth)
        }
        ensureValidInternLineWrapper()
        return internLineWrapper.getSomething(maxWidth)
    }

    override fun getIfFits(remainingWidth: Float): ConTermNone {
        if (remainingWidth < 0) {
            return None
        }
        ensureValidInternLineWrapper()
        return internLineWrapper.getIfFits(remainingWidth)
    }
}