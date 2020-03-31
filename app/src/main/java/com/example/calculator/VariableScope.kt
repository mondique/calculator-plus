package com.example.calculator

class VariableScope(val valueByVariableName: MutableMap<String, Value.Number> = mutableMapOf()) {
    fun getValue(variableName: String): Value.Number? =
        if (variableName in valueByVariableName)
            valueByVariableName[variableName]
        else
            null

    fun addVariable(variableName: String, value: Value.Number) {
        valueByVariableName[variableName] = value
    }

    operator fun contains(variableName: String): Boolean = variableName in valueByVariableName
}

