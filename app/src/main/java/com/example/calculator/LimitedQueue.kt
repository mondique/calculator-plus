package com.example.calculator

import java.util.Queue
import java.util.LinkedList

open class LimitedQueue<T>(val maxSize: Int) : LinkedList<T>() {
    override fun add(element: T): Boolean {
        super.add(element)
        if (size > maxSize) {
            remove()
        }
        return true
    }
}

