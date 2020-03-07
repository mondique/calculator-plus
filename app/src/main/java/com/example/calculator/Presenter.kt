package com.example.calculator

class Presenter(
    val calculator: Calculator,
    val history: InteractionList, 
    val viewSubscribers: MutableList<IPresenterObserver> = mutableListOf()
) {
    fun subscribeView(subscriber: AppView) {
        viewSubscribers.add(subscriber)
    }

    fun unsubscribeView(subscriber: AppView) {
        viewSubscribers.remove(subscriber)
    }

    fun onInputButtonPressed(buttonText: String, input: String, inputPosition: InputPosition) {
        val inputBeforeSelection = input.subSequence(0, inputPosition.start).toString()
        val inputSelection = input.subSequence(inputPosition.start, inputPosition.end).toString()
        val inputAfterSelection = input.subSequence(inputPosition.end, input.length).toString()
        val newInput: String
        val newInputPosition: InputPosition
        if (buttonText == "()".toString()) {
            if (inputPosition.isSelection()) {
                newInput = inputBeforeSelection + "(" + inputSelection + ")" + inputAfterSelection
                newInputPosition = InputPosition(inputPosition.start, inputPosition.end + 2)
            } else {
                newInput = "(" + inputBeforeSelection + ")" + inputAfterSelection
                newInputPosition = InputPosition(inputPosition.start + 2)
            }
        } else {
            newInput = inputBeforeSelection + buttonText + inputAfterSelection
            newInputPosition = InputPosition(inputPosition.start + buttonText.length)
        }
        notifyUpdateInput(newInput, newInputPosition)
    }

    fun onExpressionPressed(expressionText: String, input: String, inputPosition: InputPosition) {
        val inputBeforeSelection = input.subSequence(0, inputPosition.start).toString()
        val inputSelection = input.subSequence(inputPosition.start, inputPosition.end).toString()
        val inputAfterSelection = input.subSequence(inputPosition.end, input.length).toString()
        val newInput = inputBeforeSelection + expressionText + inputAfterSelection
        val newInputPosition = InputPosition(inputPosition.start + expressionText.length)
        notifyUpdateInput(newInput, newInputPosition)        
    }

    fun onInputChanged(newInput: String) {
        calculator.inputExpression = newInput
        calculator.calculateResult()
        notifyUpdateLiveResult()
    }

    fun notifyUpdateInput(newInput: String, newInputPosition: InputPosition) {
        for (subscriber in viewSubscribers) {
            subscriber.updateInput(newInput, newInputPosition)
        }
    }

    fun onDoneButtonPressed() {
        calculator.applyAssigns()
        notifyPushInput()
        notifyResetInput()
        calculator.resetInput()
        calculator.calculateResult()
        notifyUpdateLiveResult()
    }

    fun notifyUpdateLiveResult() {
        for (subscriber in viewSubscribers) {
            val result = calculator.getLiveResultString()
            subscriber.updateLiveResult(result)
        }    
    }    

    fun notifyPushInput() {
        for (subscriber in viewSubscribers) {
            subscriber.pushInteraction(calculator.inputExpression, calculator.getResultString())
        }
    }

    fun notifyResetInput() {
        for (subscriber in viewSubscribers) {
            subscriber.resetInput()
        }
    }
}

