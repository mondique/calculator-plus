package com.example.calculator

class VariableScope(val valueByVariableName: MutableMap<String, Value> = mutableMapOf()) {
    fun getValue(variableName: String): Value? =
        if (variableName in valueByVariableName)
            valueByVariableName[variableName]
        else
            null
    
    fun addVariable(variableName: String, value: Value) {
        valueByVariableName[variableName] = value
    }

    operator fun contains(variableName: String): Boolean = variableName in valueByVariableName
}

