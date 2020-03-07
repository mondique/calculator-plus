package com.example.calculator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.*

const val TAG = "myApp"

lateinit var activity: MainActivity

class MainActivity : AppCompatActivity() {

    private lateinit var interactionHistory: InteractionList
    private lateinit var calculator: Calculator
    private lateinit var presenter: Presenter
    lateinit var view: AppView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        activity = this

        initApp()

        presenter.subscribeView(view)

        val inputView = findViewById<EditText>(R.id.input)
        val liveResultView = findViewById<TextView>(R.id.liveResult)

        addPastInteractions()
        showKeyboard()
        resetScrollView()

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

    private fun initApp() {
        initHistory()
        initCalculator()
        initPresenter()
        initView()
    }

    private fun initHistory() {
        interactionHistory = getSavedInteractionHistory()
    }

    private fun getSavedInteractionHistory(): InteractionList {
        val historyFile = File(filesDir, "history.json")
        val result = InteractionList(400)
        if (historyFile.exists()) {
            val resultArrayType = object : TypeToken<ArrayDeque<Interaction>>() {}.type
            val resultArray = Gson().fromJson<ArrayDeque<Interaction>>(
                FileReader(historyFile),
                resultArrayType
            )
            for (interaction in resultArray) {
                result.add(Interaction(interaction.input, interaction.output))
            }
        }
        return result
    }

    private fun initCalculator() {
        calculator = Calculator();
    }

    private fun initPresenter() {
        presenter = Presenter(calculator, interactionHistory)
    }

    private fun initView() {
        view = AppView(this, presenter);
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        saveInteractionHistory()
    }

    private fun saveInteractionHistory() {
        val historyJson = Gson().toJson(interactionHistory as LimitedQueue<Interaction>)
        val requiredSize: Long = historyJson.length.toLong() * Char.SIZE_BYTES.toLong()
        allocateSpace(requiredSize)
        val historyFile = File(filesDir, "history.json")
        val historyFileWriter = FileWriter(historyFile)
        historyFileWriter.write(historyJson)
        historyFileWriter.close()
    }

    private fun allocateSpace(size: Long) {}

    private fun addPastInteractions() {
        for (interaction in interactionHistory)
            addInteractionToLayout(interaction)
    }

    private fun addInteractionToLayout(interaction: Interaction) {
        addTextToLayout(">>> " + interaction.input)
        if (interaction.output != "") {
            val resultExpression = addTextToLayout(interaction.output)
            val resultBackground = getResources().getDrawable(R.drawable.expression_background, null)
            resultExpression.setBackground(resultBackground)
        }
    }

    private fun showKeyboard() {
        val editText = findViewById(R.id.input) as EditText
        editText.requestFocus()
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun resetScrollView() {
        val scrollView = findViewById<ScrollView>(R.id.expressionsScrollView)
        scrollView.post {
            scrollView.fullScroll(android.view.View.FOCUS_DOWN)
        }
    }

    private fun addInputButtons() {
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
        addInteraction(Interaction(inputExpression, result))
        resetInput()
        resetScrollView()
    }

    private fun addInteraction(interaction: Interaction) {
        addInteractionToLayout(interaction)
        interactionHistory.add(interaction)
    }

    private fun resetInput() {
        val inputView = findViewById<EditText>(R.id.input)
        inputView.setText("")
        // inputView.post {
        //     inputView.requestFocus();
        // }
    }

    private fun addTextToLayout(text: CharSequence): ExpressionView {
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

