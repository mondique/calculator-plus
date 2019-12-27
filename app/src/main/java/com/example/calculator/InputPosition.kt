package com.example.calculator

class InputPosition(val start: Int, val end: Int = start) {
    fun isSelection(): Boolean = start != end

    fun isCursor(): Boolean = !isSelection()

    fun getCursor(): Int {
        assert(isCursor())
        return start
    }
}

