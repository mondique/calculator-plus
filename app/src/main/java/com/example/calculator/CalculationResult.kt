package com.example.calculator

sealed class CalculationResult() {
    abstract val type: String

    sealed class Value : CalculationResult() {
        abstract fun toRealNumber(): CalculationResult.Value.RealNumber

        class Integer(val value: Int) : Value() {
            override fun toString(): String = value.toString()
            override fun toRealNumber(): CalculationResult.Value.RealNumber =
                CalculationResult.Value.RealNumber(value.toDouble())
            override val type: String = "<integer>"
        }

        class RealNumber(val value: Double) : Value() {
            override fun toString(): String = value.toString()
            override fun toRealNumber(): CalculationResult.Value.RealNumber = this
            override val type: String = "<real number>"
        }
    }

    sealed class Function : CalculationResult() {
        abstract fun calcValue(args: List<CalculationResult>, scope: Scope): CalculationResult
        abstract override val type: String
        abstract val argsAmount: Int

        class UserDefinedFunction(
            val argNames: List<String>,
            val body: IRValue,
            val definitionScope: Scope
        ) : Function() {
            override fun toString(): String = "<function(\$${argNames.size})>"
            override val type: String = toString()
            override val argsAmount: Int = argNames.size

            override fun calcValue(args: List<CalculationResult>, scope: Scope): CalculationResult {
                if (args.size != argNames.size) {
                    throw CalculationError("invalid amount of arguments: ${args.size} instead of ${argNames.size}")
                }
                val internalScope: Scope = scope.overwrite(definitionScope)
                for (i in 0..argNames.lastIndex) {
                    internalScope.addVariable(argNames[i], args[i])
                }
                return body.calcValue(internalScope)
            }
        }

        class STLFunction(
            val name: String,
            override val argsAmount: Int,
            val function: (List<CalculationResult>) -> CalculationResult
        ) : Function() {
            override fun toString(): String = "<built-in function $name(\$$argsAmount)>"
            override val type: String = "<function(\$$argsAmount)>"

            override fun calcValue(args: List<CalculationResult>, scope: Scope): CalculationResult {
                if (args.size != argsAmount) {
                    throw CalculationError("invalid amount of arguments: ${args.size} instead of ${argsAmount}")
                }
                return function(args)
            }
        }
    }
}

