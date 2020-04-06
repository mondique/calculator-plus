package com.example.calculator

class Scope(private val valueByVariableName: MutableMap<String, CalculationResult> = mutableMapOf()) {
    fun getVariableValue(name: String): CalculationResult = valueByVariableName[name]!!

    fun addVariable(name: String, value: CalculationResult) {
        valueByVariableName[name] = value
    }

    fun hasVariable(name: String): Boolean =
        name in valueByVariableName

    fun copy(): Scope {
        val result = Scope()
        for ((name, value) in valueByVariableName) {
            result.addVariable(name, value)
        }
        return result
    }

    fun overwrite(other: Scope): Scope {
        val result: Scope = other
        for ((name, value) in valueByVariableName) {
            result.addVariable(name, value)
        }
        return result
    }
}

