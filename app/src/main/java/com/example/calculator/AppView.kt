package com.example.calculator

interface IPresenterObserver {
    fun updateLiveResult(result: String) {}
    fun pushInteraction(input: String, result: String) {}
    fun resetInput() {}
    fun updateInput(newInput: String, newInputPosition: InputPosition) {}
}

class AppView(
    val activity: MainActivity,
    val presenter: Presenter
) : IPresenterObserver {
    fun onInputChanged(newInput: String) {
        presenter.onInputChanged(newInput)
    }

    fun onInputButtonPressed(buttonText: String, input: String, inputPosition: InputPosition) {
        presenter.onInputButtonPressed(buttonText, input, inputPosition)
    }

    fun onExpressionPressed(expressionText: String, input: String, inputPosition: InputPosition) {
        presenter.onExpressionPressed(expressionText, input, inputPosition)
    }

    fun onDoneButtonPressed() {
        presenter.onDoneButtonPressed()
    }
    
    override fun updateLiveResult(result: String) {
        val resultView = activity.findViewById<TextView>(R.id.liveResult)
        resultView.setText(result)
    }

    override fun pushInteraction(input: String, result: String) {
        addTextToLayout(">>> " + input)
        if (result != "") {
            val resultExpression = addTextToLayout(result)
            val resultBackground = activity.getResources().getDrawable(
                R.drawable.expression_background, null)
            resultExpression.setBackground(resultBackground)
        }
        resetInput()
        val scrollView = activity.findViewById<ScrollView>(R.id.expressionsScrollView)
        scrollView.post {
            scrollView.fullScroll(android.view.View.FOCUS_DOWN)
        }
    }

    fun addTextToLayout(text: String): ExpressionView {
        val expressionsList = activity.findViewById<LinearLayout>(R.id.expressionsList)
        val expressionView = activity.layoutInflater.inflate(
            R.layout.expression,
            expressionsList,
            false
        ) as ExpressionView
        expressionView.getTextView().append(text)
        expressionsList.addView(expressionView)
        return expressionView
    }

    override fun resetInput() {
        activity.findViewById<EditText>(R.id.input).setText("")
    }

    override fun updateInput(newInput: String, newInputPosition: InputPosition) {
        val inputView = activity.findViewById<EditText>(R.id.input)
        inputView.setText(newInput)
        inputView.setSelection(newInputPosition.start, newInputPosition.end)
    }
}

