package com.example.calculator

fun applyAssigns(resultTree: Expression?, scope: VariableScope) {
    if (resultTree == null) return
    if (resultTree.isVal()) return
    if (resultTree.operation!!.type != Operation.Type.ASSIGN) {
        applyAssigns(resultTree.x, scope)
        applyAssigns(resultTree.y, scope)
        return
    }
    applyAssigns(resultTree.y, scope)
    if (resultTree.x == null) return
    if (!resultTree.x.isVal()) return
    val lhs = resultTree.x.getVal()
    if (lhs !is Value.Variable) return
    try {
        val rhs = calculateResultFromTree(resultTree.y!!, scope)
        scope.addVariable(lhs.name, rhs)
    } catch(e: CalculationError) {}
}

// may throw
fun calculateResultFromTree(
    expression: Expression,
    scope: VariableScope
): Value.Number {
    if (expression.isVal()) {
        val exprVal: Value = expression.getVal()
        if (exprVal is Value.Variable) {
            val variableName = exprVal.name
            if (variableName in scope) {
                return scope.getValue(variableName)!!
            } else {
                throw CalculationError("variable $variableName not found")
            }
        }
        return exprVal as Value.Number
    }
    val lhs = calculateResultFromTree(expression.x!!, scope)
    val rhs = calculateResultFromTree(expression.y!!, scope)
    return expression.operation!!.apply(lhs, rhs)
}

class Operation(val type: Type) {
    constructor(oper: Token.Operator) : this(getType(oper.type)!!)

    enum class Type {
        ADD, SUBTRACT, MULTIPLY, DIVIDE, REVERT, ASSIGN
    }
    companion object {
        fun getType(c: String): Type? = when (c) {
                "+" -> Type.ADD
                "-" -> Type.SUBTRACT
                "*" -> Type.MULTIPLY
                "/" -> Type.DIVIDE
                ":" -> Type.DIVIDE
                "=" -> Type.ASSIGN
                else -> null
            }
    }

    fun apply(x: Value.Number, y: Value.Number): Value.Number = when (type) {
            Type.ADD -> add(x, y)
            Type.SUBTRACT -> substract(x, y)
            Type.MULTIPLY -> multiply(x, y)
            Type.DIVIDE -> divide(x, y)
            Type.REVERT -> revert(x, y)
            Type.ASSIGN -> assign(x, y)
        }

    fun add(x: Value.Number, y: Value.Number): Value.Number {
        if (x is Value.Number.RealNumber || y is Value.Number.RealNumber) {
            return Value.Number.RealNumber(x.toRealNumber().value + y.toRealNumber().value)
        }
        val a = x as Value.Number.Integer
        val b = y as Value.Number.Integer
        return Value.Number.Integer(a.value + b.value)
    }

    fun substract(x: Value.Number, y: Value.Number): Value.Number {
        if (x is Value.Number.RealNumber || y is Value.Number.RealNumber) {
            return Value.Number.RealNumber(x.toRealNumber().value - y.toRealNumber().value)
        }
        val a = x as Value.Number.Integer
        val b = y as Value.Number.Integer
        return Value.Number.Integer(a.value - b.value)
    }

    fun multiply(x: Value.Number, y: Value.Number): Value.Number {
        if (x is Value.Number.RealNumber || y is Value.Number.RealNumber) {
            return Value.Number.RealNumber(x.toRealNumber().value * y.toRealNumber().value)
        }
        val a = x as Value.Number.Integer
        val b = y as Value.Number.Integer
        return Value.Number.Integer(a.value * b.value)
    }

    fun divide(x: Value.Number, y: Value.Number): Value.Number {
        if (x is Value.Number.RealNumber || y is Value.Number.RealNumber) {
            if (y.toRealNumber().value == 0.0) {
                throw CalculationError("division by 0")
            }
            return Value.Number.RealNumber(x.toRealNumber().value / y.toRealNumber().value)
        }
        val a = x as Value.Number.Integer
        val b = y as Value.Number.Integer
        if (b.value == 0) {
            throw CalculationError("division by 0")
        }
        if (a.value % b.value == 0) {
            return Value.Number.Integer(a.value / b.value)
        }
        return Value.Number.RealNumber(a.value.toDouble() / b.value.toDouble())
    }

    fun revert(x: Value.Number, y: Value.Number): Value.Number = when(y) {
            is Value.Number.RealNumber -> Value.Number.RealNumber(-y.value)
            is Value.Number.Integer -> Value.Number.Integer(-y.value)
    }

    fun assign(x: Value.Number, y: Value.Number): Value.Number = y
}

