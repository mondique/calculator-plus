package com.example.calculator

class Expression(
    val isValue: Boolean,
    val operation: Operation?,
    val x: Expression?,
    val y: Expression?,
    val value: Value?
) {

    constructor(oper: Token.Operator, lhs: Expression?, rhs: Expression) : this(false, Operation(oper), lhs, rhs, null) {}

    constructor(value: Value) : this(true, null, null, null, value) {}

    fun isVal(): Boolean = isValue

    fun getVal(): Value = value!!
}

