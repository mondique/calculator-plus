package com.example.calculator

interface IExpression


sealed class ILValue : IExpression {
    abstract val name: String
}

class VariableDeclaration(override val name: String) : ILValue()

class FunctionDeclaration(override val name: String, val argNames: List<String>) : ILValue()


interface IRValue : IExpression {
    fun calcValue(scope: Scope): CalculationResult
    fun applyAssigns(scope: Scope)
}

class BinaryExpression(
    private val oper: IBinaryOperator,
    private val lhs: IRValue,
    private val rhs: IRValue
) : IRValue {
    override fun calcValue(scope: Scope): CalculationResult =
        oper.apply(lhs.calcValue(scope),
                   rhs.calcValue(scope))

    override fun applyAssigns(scope: Scope) {
        lhs.applyAssigns(scope)
        rhs.applyAssigns(scope)
    }
}

class UnaryExpression(
    val oper: IUnaryOperator,
    val x: IRValue
) : IRValue {
    override fun calcValue(scope: Scope): CalculationResult = oper.apply(x.calcValue(scope))

    override fun applyAssigns(scope: Scope) {
        x.applyAssigns(scope)
    }
}

class FunctionCall(val functionTree: IRValue, val args: List<IRValue>) : IRValue {
    override fun calcValue(scope: Scope): CalculationResult {
        val function: CalculationResult = functionTree.calcValue(scope)
        if (function !is CalculationResult.Function) {
            throw CalculationError("variable is not a function")
        }
        val calculatedArgs: List<CalculationResult> = List(args.size, { args[it].calcValue(scope) })
        try {
            return function.calcValue(calculatedArgs, scope)
        } catch(calcError: CalculationError) {
            throw CalculationError("in function: ${calcError.message}")
        }
    }

    override fun applyAssigns(scope: Scope) {
        for (argument in args) {
            argument.applyAssigns(scope)
        }
    }
}

class Variable(val name: String) : IRValue {
    override fun calcValue(scope: Scope): CalculationResult {
        if (!scope.hasVariable(name)) {
            throw ParserError("variable $name not found")
        }
        return scope.getVariableValue(name)
    }

    override fun applyAssigns(scope: Scope) {}
}

sealed class Value : IRValue {
    override fun applyAssigns(scope: Scope) {}

    sealed class Number : Value() {
        abstract override fun toString(): String

        class RealNumber(val value: Double) : Number() {
            override fun calcValue(scope: Scope): CalculationResult.Value =
                CalculationResult.Value.RealNumber(value)

            override fun toString(): String = value.toString()
        }
        class Integer(val value: Int) : Number() {
            override fun calcValue(scope: Scope): CalculationResult.Value =
                CalculationResult.Value.Integer(value)

            override fun toString(): String = value.toString()
        }
    }
}

class Assignment(val lhs: ILValue, val rhs: IRValue) : IRValue {
    override fun calcValue(scope: Scope): CalculationResult = when(lhs) {
        is VariableDeclaration -> rhs.calcValue(scope)
        is FunctionDeclaration -> CalculationResult.Function.UserDefinedFunction(lhs.argNames, rhs, scope)
    }

    override fun applyAssigns(scope: Scope) {
        scope.addVariable(lhs.name, calcValue(scope))
    }
}

