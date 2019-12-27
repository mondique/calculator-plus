package com.example.calculator

class ExpressionNode(
    val isValue: Boolean,
    val operation: Operation?,
    val x: ExpressionNode?,
    val y: ExpressionNode?,
    val value: Value?
) {

    constructor(value: Value) : this(true, null, null, null, value) {}

    fun isVal(): Boolean = isValue

    fun getVal(): Value = value!!
}

