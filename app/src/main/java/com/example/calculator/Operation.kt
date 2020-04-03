package com.example.calculator

import kotlin.math.pow

sealed class Operator()

sealed class IUnaryOperator() : Operator() {
    abstract fun apply(x: Value.Number): Value.Number

    object Add : IUnaryOperator() {
        override fun apply(x: Value.Number): Value.Number = x
    }

    object Revert : IUnaryOperator() {
        override fun apply(x: Value.Number): Value.Number = when(x) {
            is Value.Number.RealNumber -> Value.Number.RealNumber(-x.value)
            is Value.Number.Integer -> Value.Number.Integer(-x.value)
        }
    }

    object BitwiseRevert : IUnaryOperator() {
        override fun apply(x: Value.Number): Value.Number = when(x) {
            is Value.Number.RealNumber -> throw CalculationError("~ is not applicable to real numbers")
            is Value.Number.Integer -> Value.Number.Integer(x.value.inv())
        }
    }
}

sealed class IBinaryOperator() : Operator() {
    abstract fun apply(x: Value.Number, y: Value.Number): Value.Number

    object Xor : IBinaryOperator() {
        override fun apply(x: Value.Number, y: Value.Number): Value.Number {
            if (x is Value.Number.RealNumber || y is Value.Number.RealNumber) {
                throw CalculationError("^ is not applicable to real numbers")
            }
            val a = x as Value.Number.Integer
            val b = y as Value.Number.Integer
            return Value.Number.Integer(a.value xor b.value)
        }
    }

    object Add : IBinaryOperator() {
        override fun apply(x: Value.Number, y: Value.Number): Value.Number {
            if (x is Value.Number.RealNumber || y is Value.Number.RealNumber) {
                return Value.Number.RealNumber(x.toRealNumber().value + y.toRealNumber().value)
            }
            val a = x as Value.Number.Integer
            val b = y as Value.Number.Integer
            return Value.Number.Integer(a.value + b.value)
        }
    }

    object Subtract : IBinaryOperator() {
        override fun apply(x: Value.Number, y: Value.Number): Value.Number {
            if (x is Value.Number.RealNumber || y is Value.Number.RealNumber) {
                return Value.Number.RealNumber(x.toRealNumber().value - y.toRealNumber().value)
            }
            val a = x as Value.Number.Integer
            val b = y as Value.Number.Integer
            return Value.Number.Integer(a.value - b.value)
        }
    }

    object Multiply : IBinaryOperator() {
        override fun apply(x: Value.Number, y: Value.Number): Value.Number {
            if (x is Value.Number.RealNumber || y is Value.Number.RealNumber) {
                return Value.Number.RealNumber(x.toRealNumber().value * y.toRealNumber().value)
            }
            val a = x as Value.Number.Integer
            val b = y as Value.Number.Integer
            return Value.Number.Integer(a.value * b.value)
        }
    }

    object Divide : IBinaryOperator() {
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

    object Mod : IBinaryOperator() {
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

    object Power : IBinaryOperator() {
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
}

object Assign : Operator()

