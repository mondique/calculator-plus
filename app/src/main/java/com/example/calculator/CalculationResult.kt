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

    class Function(val argNames: List<String>, val body: IRValue, val definitionScope: Scope) : CalculationResult() {
        override fun toString(): String = "<function(\$${argNames.size})>"
        override val type: String = toString()

        fun calcValue(args: List<CalculationResult>, scope: Scope): CalculationResult {
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
}

