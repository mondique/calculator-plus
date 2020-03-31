package com.example.calculator

import kotlin.math.pow

fun applyAssigns(resultTree: Expression?, scope: VariableScope) {
    if (resultTree == null) return
    if (resultTree.isVal()) return
    if (resultTree.oper!! !is BinaryOperator.Assign) {
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
    if (expression.x == null) {
        val rhs = calculateResultFromTree(expression.y!!, scope)
        return (expression.oper!! as UnaryOperator).apply(rhs)
    }
    val lhs = calculateResultFromTree(expression.x, scope)
    val rhs = calculateResultFromTree(expression.y!!, scope)
    return (expression.oper!! as BinaryOperator).apply(lhs, rhs)
}

sealed class Operator()

sealed class UnaryOperator() : Operator() {
    abstract fun apply(x: Value.Number): Value.Number

    class Add : UnaryOperator() {
        override fun apply(x: Value.Number): Value.Number = x
    }

    class Revert : UnaryOperator() {
        override fun apply(x: Value.Number): Value.Number = when(x) {
            is Value.Number.RealNumber -> Value.Number.RealNumber(-x.value)
            is Value.Number.Integer -> Value.Number.Integer(-x.value)
        }
    }

    class BitwiseRevert : UnaryOperator() {
        override fun apply(x: Value.Number): Value.Number = when(x) {
            is Value.Number.RealNumber -> throw CalculationError("~ is not applicable to real numbers")
            is Value.Number.Integer -> Value.Number.Integer(x.value.inv())
        }
    }
}

sealed class BinaryOperator() : Operator() {
    abstract fun apply(x: Value.Number, y: Value.Number): Value.Number

    class Xor : BinaryOperator() {
        override fun apply(x: Value.Number, y: Value.Number): Value.Number {
            if (x is Value.Number.RealNumber || y is Value.Number.RealNumber) {
                throw CalculationError("^ is not applicable to real numbers")
            }
            val a = x as Value.Number.Integer
            val b = y as Value.Number.Integer
            return Value.Number.Integer(a.value xor b.value)
        }
    }

    class Add : BinaryOperator() {
        override fun apply(x: Value.Number, y: Value.Number): Value.Number {
            if (x is Value.Number.RealNumber || y is Value.Number.RealNumber) {
                return Value.Number.RealNumber(x.toRealNumber().value + y.toRealNumber().value)
            }
            val a = x as Value.Number.Integer
            val b = y as Value.Number.Integer
            return Value.Number.Integer(a.value + b.value)
        }
    }

    class Subtract : BinaryOperator() {
        override fun apply(x: Value.Number, y: Value.Number): Value.Number {
            if (x is Value.Number.RealNumber || y is Value.Number.RealNumber) {
                return Value.Number.RealNumber(x.toRealNumber().value - y.toRealNumber().value)
            }
            val a = x as Value.Number.Integer
            val b = y as Value.Number.Integer
            return Value.Number.Integer(a.value - b.value)
        }
    }

    class Multiply : BinaryOperator() {
        override fun apply(x: Value.Number, y: Value.Number): Value.Number {
            if (x is Value.Number.RealNumber || y is Value.Number.RealNumber) {
                return Value.Number.RealNumber(x.toRealNumber().value * y.toRealNumber().value)
            }
            val a = x as Value.Number.Integer
            val b = y as Value.Number.Integer
            return Value.Number.Integer(a.value * b.value)
        }
    }

    class Divide : BinaryOperator() {
        override fun apply(x: Value.Number, y: Value.Number): Value.Number {
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
    }

    class Mod : BinaryOperator() {
        override fun apply(x: Value.Number, y: Value.Number): Value.Number {
            if (x is Value.Number.RealNumber || y is Value.Number.RealNumber) {
                throw CalculationError("% is not applicable to real numbers")
            }
            val a = x as Value.Number.Integer
            val b = y as Value.Number.Integer
            if (b.value == 0) {
                throw CalculationError("division by 0")
            }
            return Value.Number.Integer(a.value % b.value)
        }
    }

    class Power : BinaryOperator() {
        override fun apply(x: Value.Number, y: Value.Number): Value.Number {
            if (y.toRealNumber().value == 0.0) {
                return if (x is Value.Number.Integer && y is Value.Number.Integer)
                    Value.Number.Integer(1) else Value.Number.RealNumber(1.0)
            }
            if (y.toRealNumber().value < 0.0 && x.toRealNumber().value == 0.0) {
                throw CalculationError("division by 0")
            }
            if (x is Value.Number.Integer && y is Value.Number.Integer) {
                return Value.Number.Integer(x.value.toDouble().pow(y.value))
            }
            if (y is Value.Number.Integer) {
                val a = x as Value.Number.RealNumber
                return Value.Number.Integer(a.value.pow(y.value))
            }
            val a = x as Value.Number.RealNumber
            val b = y as Value.Number.RealNumber
            return Value.Number.RealNumber(a.value.pow(b.value))
        }
    }

    class Assign : BinaryOperator() {
        override fun apply(x: Value.Number, y: Value.Number): Value.Number = y
    }
}

