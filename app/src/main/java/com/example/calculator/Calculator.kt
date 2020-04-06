package com.example.calculator

class Calculator(
    val scope: Scope = Scope()
) {
    var inputExpression: String = ""
    private var resultTree: IRValue? = null
    private var result: CalculationResult? = null
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
        if (error == null) {
            resultTree?.applyAssigns(scope)
        }
    }

    fun reset() {
        inputExpression = ""
        calculateResult()
    }

    fun calculateResult() {
        resetResult()
        try {
            resultTree = Parser().parse(inputExpression)
            result = resultTree!!.calcValue(scope)
        } catch(parserError: ParserError) {
            resultTree = null
            result = null
            error = parserError.message
        } catch(calcError: CalculationError) {
            result = null
            error = calcError.message
        }
    }

    private fun resetResult() {
        resultTree = null
        result = null
        error = null
    }
}

