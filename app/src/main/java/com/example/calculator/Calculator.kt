package com.example.calculator

class Calculator(
    val scope: VariableScope = VariableScope()
) {
    var inputExpression: String = ""
    private var resultTree: ExpressionNode? = null
    var result: Value? = null

    fun setInput(newInput: String) {
        inputExpression = newInput
    }

    fun getLiveResultString(): String =
        if (result == null || result!!.isError() == true)
            "..."
        else result!!.toString()

    fun getResultString(): String = result?.toString() ?: ""

    fun applyAssigns() {
        applyAssigns(resultTree, scope)
    }

    fun resetInput() {
        inputExpression = ""
    }

    fun calculateResult() {
        resultTree = parseString(inputExpression, scope)
        result = calculateResultFromTree(resultTree, scope)
    }
}

