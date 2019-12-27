package com.example.calculator

import androidx.appcompat.app.AppCompatActivity
import android.content.Context
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.inputmethod.EditorInfo
import android.widget.*
import java.io.Console

const val TAG = "myApp"

lateinit var activity: MainActivity

class MainActivity : AppCompatActivity() {

    // val expressionViews: MutableList<TextView> = mutableListOf()
    val calculator = Calculator()
    val presenter = Presenter(calculator)
    val view = AppView(this, presenter)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        activity = this
        presenter.subscribeView(view)
        val inputView = findViewById<EditText>(R.id.input)
        val liveResultView = findViewById<TextView>(R.id.liveResult)
        
        inputView.addTextChangedListener(InputWatcher())
        inputView.setOnEditorActionListener { v, actionId, event ->
            return@setOnEditorActionListener when (actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    view.onDoneButtonPressed()
                    true
                }
                else -> false
            }
        }

        addInputButtons()
    }

    fun addInputButtons() {
        val buttonsList = findViewById<LinearLayout>(R.id.inputButtonsContainer)
        for (c in listOf("+", "-", "*", "/", "=", "_", "(", ")", "()")) {
            val expressionView = layoutInflater.inflate(
                R.layout.character_button,
                buttonsList,
                false
            ) as ButtonView
            expressionView.getTextView().append(c)
            buttonsList.addView(expressionView)
            val resultBackground = getResources().getDrawable(R.drawable.expression_background, null)
            expressionView.setBackground(resultBackground)
        }
    }

    fun respondToInput(inputExpression: String, result: String) {
        addTextToLayout(">>> " + inputExpression)
        val resultExpression = addTextToLayout(result)
        val resultBackground = getResources().getDrawable(R.drawable.expression_background, null)
        resultExpression.setBackground(resultBackground)
        resetInput()
        val scrollView = findViewById<ScrollView>(R.id.expressionsScrollView)
        scrollView.post {
            scrollView.fullScroll(android.view.View.FOCUS_DOWN)
        }
    }

    fun resetInput() {
        val inputView = findViewById<EditText>(R.id.input)
        inputView.setText("")
        // inputView.post {
        //     inputView.requestFocus();
        // }
    }

    fun addTextToLayout(text: CharSequence): ExpressionView {
        val expressionsList = findViewById<LinearLayout>(R.id.expressionsList)
        val expressionView = layoutInflater.inflate(
            R.layout.expression,
            expressionsList,
            false
        ) as ExpressionView
        expressionView.getTextView().append(text)
        expressionsList.addView(expressionView)
        return expressionView
    }
}

