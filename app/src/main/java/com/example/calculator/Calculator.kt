package com.example.calculator

class Calculator(
    val scope: VariableScope = VariableScope()
) {
    var inputExpression: String = ""
    private var resultTree: ExpressionNode? = null
    private var result: Value? = null

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
        resultTree = Parser(scope).parseString(inputExpression)
        result = calculateResultFromTree(resultTree, scope)
    }
}

