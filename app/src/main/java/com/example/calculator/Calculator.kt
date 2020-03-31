package com.example.calculator

class Calculator(
    val scope: VariableScope = VariableScope()
) {
    var inputExpression: String = ""
    private var resultTree: IExpression? = null
    private var result: Value.Number? = null
    private var error: String? = null

    init {
        calculateResult()
    }

    fun getLiveResultString(): String =
        if (result == null || error != null)
            "..."
        else result.toString()

    fun getResultString(): String =
        if (error == null) result.toString() else error!!

    fun applyAssigns() {
        resultTree?.applyAssigns(scope)
    }

    fun reset() {
        inputExpression = ""
        calculateResult()
    }

    fun calculateResult() {
        resetResult()
        val errors: MutableList<String> = mutableListOf()
        resultTree = Parser(errors).parse(inputExpression)
        if (errors.isEmpty()) {
            try {
                result = resultTree!!.calcValue(scope)
            } catch(e: CalculationError) {
                result = null
                error = e.message
            }
        } else {
            result = null
            error = errors[0]
        }
    }

    private fun resetResult() {
        resultTree = null
        result = null
        error = null
    }
}

