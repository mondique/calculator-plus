package com.example.calculator

class InputWatcher() : TextWatcher {
    override fun afterTextChanged(s: Editable) {}

    override fun beforeTextChanged(
        s: CharSequence,
        start: Int, count: Int, after: Int
    ) {}

    override fun onTextChanged(
        s: CharSequence,
        start: Int, before: Int, count: Int
    ) {
        activity.view.onInputChanged(s.toString())
    }
}

