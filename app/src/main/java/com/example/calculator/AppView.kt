package com.example.calculator

import android.widget.*

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
        activity.respondToInput(input, result)
    }

    override fun updateInput(newInput: String, newInputPosition: InputPosition) {
        val inputView = activity.findViewById<EditText>(R.id.input)
        inputView.setText(newInput)
        inputView.setSelection(newInputPosition.start, newInputPosition.end)
    }
}

