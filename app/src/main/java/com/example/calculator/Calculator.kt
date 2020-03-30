package com.example.calculator

class Calculator(
    val scope: VariableScope = VariableScope()
) {
    var inputExpression: String = ""
    private var resultTree: Expression? = null
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
        val errors: MutableList<String> = mutableListOf()
        resultTree = Parser(errors).parse(inputExpression)
        if (errors.isEmpty()) {
            result = calculateResultFromTree(resultTree, scope)
        } else {
            result = Value(errors[0])
        }
    }
}

