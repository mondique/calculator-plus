package com.example.calculator

abstract interface IExpression {
    abstract fun calcValue(scope: VariableScope): Value.Number
    abstract fun applyAssigns(scope: VariableScope)
}

class BinaryExpression(
    val oper: IBinaryOperator,
    val lhs: IExpression,
    val rhs: IExpression
) : IExpression {
    override fun calcValue(scope: VariableScope): Value.Number =
        oper.apply(lhs.calcValue(scope),
                   rhs.calcValue(scope))

    override fun applyAssigns(scope: VariableScope) {
        rhs.applyAssigns(scope)
        if (lhs is Value.Variable) {
            scope.addVariable(lhs.name, rhs.calcValue(scope))
        }
    }
}
class UnaryExpression(
    val oper: IUnaryOperator,
    val x: IExpression
) : IExpression {
    override fun calcValue(scope: VariableScope): Value.Number = oper.apply(x.calcValue(scope))

    override fun applyAssigns(scope: VariableScope) {
        x.applyAssigns(scope)
    }
}
sealed class Value : IExpression {
    override fun applyAssigns(scope: VariableScope) {}

    sealed class Number : Value() {
        abstract fun toRealNumber(): RealNumber
        abstract fun toInteger(): Integer

        override abstract fun toString(): String

        data class RealNumber(val value: Double) : Number() {
            constructor(intValue: Int) : this(intValue.toDouble())

            override fun calcValue(scope: VariableScope): Value.Number = this

            override fun toRealNumber(): RealNumber = this
            override fun toInteger(): Integer = Integer(value)

            override fun toString(): String = value.toString()
        }
        data class Integer(val value: Int) : Number() {
            constructor(doubleValue: Double) : this(doubleValue.toInt())

            override fun calcValue(scope: VariableScope): Value.Number = this

            override fun toRealNumber(): RealNumber = RealNumber(value)
            override fun toInteger(): Integer = this

            override fun toString(): String = value.toString()
        }
    }

    data class Variable(val name: String) : Value() {
        override fun calcValue(scope: VariableScope): Value.Number =
            if (name in scope) scope.getValue(name)!!
            else throw CalculationError("variable $name not found")
    }
}


