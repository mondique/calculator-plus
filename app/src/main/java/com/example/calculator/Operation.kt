package com.example.calculator

import kotlin.math.abs

fun applyAssigns(resultTree: ExpressionNode?, scope: VariableScope) {
    if (resultTree == null) return
    if (resultTree!!.isVal()) return
    if (resultTree!!.operation!!.type != Operation.Type.ASSIGN) {
        applyAssigns(resultTree!!.x, scope)
        applyAssigns(resultTree!!.y, scope)
        return
    }
    if (resultTree!!.x == null) return
    if (!resultTree!!.x!!.isVal()) return
    if (resultTree!!.x!!.getVal().valType != Value.Type.NAME) return
    val rhs = calculateResultFromTree(resultTree.y, scope)
    if (rhs == null || rhs.isError()) return
    scope.addVariable(resultTree.x!!.getVal().toName(), rhs)
}

fun calculateResultFromTree(
    expression: ExpressionNode?,
    scope: VariableScope
): Value? {
    if (expression == null)
        return null
    if (expression.isVal()) {
        if (expression.getVal().valType == Value.Type.NAME) {
            val variableName = expression.getVal().toName()
            if (variableName in scope) {
                return scope.getValue(variableName)
            } else {
                return Value("ERROR: variable $variableName not found")
            }
        }
        return expression.getVal()
    }
    return expression.operation!!.apply(
        calculateResultFromTree(expression.x, scope),
        calculateResultFromTree(expression.y, scope)
    )
}

class Operation(val type: Type) {

    val EPS: Float = (1e-7).toFloat()

    enum class Type {
        ADD, SUBSTRACT, MULTIPLY, DIVIDE, REVERT, ASSIGN
    }
    companion object {
        fun getType(c: Char): Type? = when (c) {
            '+' -> Type.ADD
            '-' -> Type.SUBSTRACT
            '*' -> Type.MULTIPLY
            '/' -> Type.DIVIDE
            ':' -> Type.DIVIDE
            '=' -> Type.ASSIGN
            else -> null
        }
    }

    fun apply(x: Value?, y: Value?): Value? {
        return when (type) {
            Type.ADD -> add(x, y)
            Type.SUBSTRACT -> substract(x, y)
            Type.MULTIPLY -> multiply(x, y)
            Type.DIVIDE -> divide(x, y)
            Type.REVERT -> revert(x, y)
            Type.ASSIGN -> assign(x, y)
        }
    }

    fun add(x: Value?, y: Value?): Value? {
        if (x == null || y == null)
            return null
        if (x.isError())
            return x
        if (y.isError())
            return y
        if (x.valType == Value.Type.DOUBLE ||
            y.valType == Value.Type.DOUBLE) {
            return Value((x.toDouble()!! + y.toDouble()!!).toString())
        }
        return Value((x.toInt()!!.toLong() + y.toInt()!!.toLong()).toString())
    }

    fun substract(x: Value?, y: Value?): Value? {
        if (x == null || y == null)
            return null
        if (x.isError())
            return x
        if (y.isError())
            return y
        if (x.valType == Value.Type.DOUBLE ||
            y.valType == Value.Type.DOUBLE) {
            return Value((x.toDouble()!! - y.toDouble()!!).toString())
        }
        return Value((x.toInt()!!.toLong() - y.toInt()!!.toLong()).toString())
    }

    fun multiply(x: Value?, y: Value?): Value? {
        if (x == null || y == null)
            return null
        if (x.isError())
            return x
        if (y.isError())
            return y
        if (x.valType == Value.Type.DOUBLE ||
            y.valType == Value.Type.DOUBLE) {
            return Value((x.toDouble()!! * y.toDouble()!!).toString())
        }
        return Value((x.toInt()!!.toLong() * y.toInt()!!.toLong()).toString())
    }

    fun divide(x: Value?, y: Value?): Value? {
        if (x == null || y == null)
            return null
        if (x.isError())
            return x
        if (y.isError())
            return y
        if (x.valType == Value.Type.DOUBLE ||
            y.valType == Value.Type.DOUBLE) {
            val yDouble = y.toDouble()!!
            if (abs(yDouble) < EPS) //>
                return null
            return Value((x.toDouble()!! / yDouble).toString())
        }
        val yInt = y.toInt()!!
        if (yInt == 0)
            return null
        val xInt = x.toInt()!!
        if (xInt % yInt == 0)
            return Value((xInt / yInt).toString())
        return Value((x.toDouble()!! / yInt).toString())
    }

    fun revert(x: Value?, y: Value?): Value? {
        if (y == null)
            return null
        if (x?.isError() == true)
            return x
        if (y.isError())
            return y
        if (y.valType == Value.Type.DOUBLE)
            return Value((-(y.toDouble()!!)).toString())
        return Value((-(y.toInt()!!)).toString())
    }

    fun assign(x: Value?, y: Value?): Value? = y
}
