package com.example.calculator

class InternalVariableNameGenerator {
    companion object {
        private var lastVariableNumber: Int = 0

        fun generateInternalVariableName(): String {
            lastVariableNumber++
            return "#$lastVariableNumber"
        }
    }
}

