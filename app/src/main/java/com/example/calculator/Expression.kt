package com.example.calculator

class Expression(
    val isValue: Boolean,
    val oper: Operator?,
    val x: Expression?,
    val y: Expression?,
    val value: Value?
) {

    constructor(operToken: Token.Operator, lhs: Expression?, rhs: Expression)
        : this(false,
               if (lhs == null)
                   operToken.toUnaryOperator()
               else
                   operToken.toBinaryOperator(),
               lhs, rhs, null) {}

    constructor(value: Value) : this(true, null, null, null, value) {}

    fun isVal(): Boolean = isValue

    fun getVal(): Value = value!!
}

