package com.example.calculator

sealed class Value {
    sealed class Number : Value() {
        abstract fun toRealNumber(): RealNumber
        abstract fun toInteger(): Integer

        override abstract fun toString(): String

        data class RealNumber(val value: Double) : Number() {
            constructor(intValue: Int) : this(intValue.toDouble())

            override fun toRealNumber(): RealNumber = this
            override fun toInteger(): Integer = Integer(value)

            override fun toString(): String = value.toString()
        }
        data class Integer(val value: Int) : Number() {
            constructor(doubleValue: Double) : this(doubleValue.toInt())

            override fun toRealNumber(): RealNumber = RealNumber(value)
            override fun toInteger(): Integer = this

            override fun toString(): String = value.toString()
        }
    }

    data class Variable(val name: String) : Value()
}

