package com.example.calculator

import kotlin.math.pow

sealed class Operator

sealed class IUnaryOperator : Operator() {
    fun apply(x: CalculationResult): CalculationResult {
        if (x is CalculationResult.Value) {
            return applyToValues(x)
        }
        val f: CalculationResult.Function = x as CalculationResult.Function
        return CalculationResult.Function(f.argNames, UnaryExpression(this, f.body), f.definitionScope)
    }

    abstract fun applyToValues(x: CalculationResult.Value): CalculationResult.Value

    object Add : IUnaryOperator() {
        override fun applyToValues(x: CalculationResult.Value): CalculationResult.Value = x
    }

    object Revert : IUnaryOperator() {
        override fun applyToValues(x: CalculationResult.Value): CalculationResult.Value = when(x) {
            is CalculationResult.Value.RealNumber ->
                CalculationResult.Value.RealNumber(-x.value)
            is CalculationResult.Value.Integer ->
                CalculationResult.Value.Integer(-x.value)
        }
    }

    object BitwiseRevert : IUnaryOperator() {
        override fun applyToValues(x: CalculationResult.Value): CalculationResult.Value = when(x) {
            is CalculationResult.Value.RealNumber ->
                throw CalculationError("~ is not applicable to real numbers")
            is CalculationResult.Value.Integer ->
                CalculationResult.Value.Integer(x.value.inv())
        }
    }
}

sealed class IBinaryOperator : Operator() {
    fun apply(x: CalculationResult, y: CalculationResult): CalculationResult {
        if (x is CalculationResult.Value
            && y is CalculationResult.Value) {
            return applyToValues(x, y)
        }
        if (x is CalculationResult.Value
            || y is CalculationResult.Value) {
            throw CalculationError("attempted to apply binary operator to objects with different signatures")
        }
        val f: CalculationResult.Function = x as CalculationResult.Function
        val g: CalculationResult.Function = y as CalculationResult.Function
        if (f.argNames.size != g.argNames.size) {
            throw CalculationError("attempted to apply binary operator to objects with different signatures")
        }
        throw CalculationError("binary operations on functions are currently not supported")
        // return CalculationResult.Function(f.argSignatures, UnaryExpression(this, f.body))
    }

    abstract fun applyToValues(x: CalculationResult.Value, y: CalculationResult.Value): CalculationResult.Value

    object Xor : IBinaryOperator() {
        override fun applyToValues(x: CalculationResult.Value, y: CalculationResult.Value): CalculationResult.Value {
            if (x !is CalculationResult.Value.Integer
                || y !is CalculationResult.Value.Integer) {
                throw CalculationError("^ is not applicable to real numbers")
            }
            return CalculationResult.Value.Integer(x.value xor y.value)
        }
    }

    object Add : IBinaryOperator() {
        override fun applyToValues(x: CalculationResult.Value, y: CalculationResult.Value): CalculationResult.Value {
            if (x is CalculationResult.Value.RealNumber
                || y is CalculationResult.Value.RealNumber) {
                return CalculationResult.Value.RealNumber(x.toRealNumber().value + y.toRealNumber().value)
            }
            val a = x as CalculationResult.Value.Integer
            val b = y as CalculationResult.Value.Integer
            return CalculationResult.Value.Integer(a.value + b.value)
        }
    }

    object Subtract : IBinaryOperator() {
        override fun applyToValues(x: CalculationResult.Value, y: CalculationResult.Value): CalculationResult.Value {
            if (x is CalculationResult.Value.RealNumber
                || y is CalculationResult.Value.RealNumber) {
                return CalculationResult.Value.RealNumber(x.toRealNumber().value - y.toRealNumber().value)
            }
            val a = x as CalculationResult.Value.Integer
            val b = y as CalculationResult.Value.Integer
            return CalculationResult.Value.Integer(a.value - b.value)
        }
    }

    object Multiply : IBinaryOperator() {
        override fun applyToValues(x: CalculationResult.Value, y: CalculationResult.Value): CalculationResult.Value {
            if (x is CalculationResult.Value.RealNumber
                || y is CalculationResult.Value.RealNumber) {
                return CalculationResult.Value.RealNumber(x.toRealNumber().value * y.toRealNumber().value)
            }
            val a = x as CalculationResult.Value.Integer
            val b = y as CalculationResult.Value.Integer
            return CalculationResult.Value.Integer(a.value * b.value)
        }
    }

    object Divide : IBinaryOperator() {
        override fun applyToValues(x: CalculationResult.Value, y: CalculationResult.Value): CalculationResult.Value {
            if (x is CalculationResult.Value.RealNumber || y is CalculationResult.Value.RealNumber) {
                if (y.toRealNumber().value == 0.0) {
                    throw CalculationError("division by 0")
                }
                return CalculationResult.Value.RealNumber(x.toRealNumber().value / y.toRealNumber().value)
            }
            val a = x as CalculationResult.Value.Integer
            val b = y as CalculationResult.Value.Integer
            if (b.value == 0) {
                throw CalculationError("division by 0")
            }
            if (a.value % b.value == 0) {
                return CalculationResult.Value.Integer(a.value / b.value)
            }
            return CalculationResult.Value.RealNumber(a.value.toDouble() / b.value.toDouble())
        }
    }

    object Mod : IBinaryOperator() {
        override fun applyToValues(x: CalculationResult.Value, y: CalculationResult.Value): CalculationResult.Value {
            if (x is CalculationResult.Value.RealNumber || y is CalculationResult.Value.RealNumber) {
                throw CalculationError("% is not applicable to real numbers")
            }
            val a = x as CalculationResult.Value.Integer
            val b = y as CalculationResult.Value.Integer
            if (b.value == 0) {
                throw CalculationError("division by 0")
            }
            return CalculationResult.Value.Integer(a.value % b.value)
        }
    }

    object Power : IBinaryOperator() {
        override fun applyToValues(x: CalculationResult.Value, y: CalculationResult.Value): CalculationResult.Value {
            if (y.toRealNumber().value == 0.0) {
                return if (x is CalculationResult.Value.Integer && y is CalculationResult.Value.Integer)
                    CalculationResult.Value.Integer(1) else CalculationResult.Value.RealNumber(1.0)
            }
            if (y.toRealNumber().value < 0.0 && x.toRealNumber().value == 0.0) {
                throw CalculationError("division by 0")
            }
            if (x is CalculationResult.Value.Integer && y is CalculationResult.Value.Integer && y.value > 0) {
                return CalculationResult.Value.Integer(x.value.toDouble().pow(y.value).toInt())
            }
            val a = x.toRealNumber()
            if (y is CalculationResult.Value.Integer) {
                return CalculationResult.Value.RealNumber(a.value.pow(y.value))
            }
            val b = y as CalculationResult.Value.RealNumber
            return CalculationResult.Value.RealNumber(a.value.pow(b.value))
        }
    }
}

