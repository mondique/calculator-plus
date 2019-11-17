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
import kotlin.math.abs

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
        calculator.subscribe(presenter)
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
        // val liveResultView = activity.findViewById<TextView>(R.id.liveResult)
        // liveResultView.text = CalculateResultFromString(
        //     s.toString(), activity.parser, true).toString()
        // if (liveResultView.text == "")
        //     liveResultView.text = activity.getResources().getString(R.string.defaultLiveResult)
    }
}

class ExpressionView(context: Context, attrs: AttributeSet) :
    RelativeLayout(context, attrs) {

    fun getTextView(): TextView =
        getChildAt(0) as TextView

    private val myListener = object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            val v = activity.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            v.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            return true
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            val text: String = getTextView().text.toString()
            val realText = if (text.length > 0 && text[0] == '>')
                text.subSequence(4, text.length).toString()
                else text
            val inputView = activity.findViewById<EditText>(R.id.input)
            val inputText = inputView.getText().toString()
            val inputSelection = InputPosition(
                inputView.getSelectionStart(),
                inputView.getSelectionEnd()
            )
            activity.view.onExpressionPressed(
                realText,
                inputText,
                inputSelection
            )
            return false
        }

        // override fun onDoubleTap(e: MotionEvent): Boolean = false

        // override fun onScroll(e1: MotionEvent, e2: MotionEvent, x: Float, y: Float): Boolean = true

        // override fun onFling(e1: MotionEvent, e2: MotionEvent, x: Float, y: Float): Boolean = true
    }

    private val mDetector: GestureDetector = GestureDetector(context, myListener)

    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)
        return mDetector.onTouchEvent(event)
    }
}

class ButtonView(context: Context, attrs: AttributeSet) :
    RelativeLayout(context, attrs) {
    
    fun getTextView(): TextView =
        getChildAt(0) as TextView

    private val myListener = object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            val v = activity.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            v.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            return true
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            val buttonText = getTextView().text
            val inputView = activity.findViewById<EditText>(R.id.input)
            val inputText = inputView.getText().toString()
            val inputSelection = InputPosition(
                inputView.getSelectionStart(),
                inputView.getSelectionEnd()
            )
            activity.view.onInputButtonPressed(
                buttonText.toString(),
                inputText,
                inputSelection
            )
            return false
        }
    }

    private val mDetector: GestureDetector = GestureDetector(context, myListener)

    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)
        return mDetector.onTouchEvent(event)
    }

}

class LiveExpressionView(context: Context, attrs: AttributeSet) :
    RelativeLayout(context, attrs) {

    fun getTextView(): TextView =
        getChildAt(0) as TextView

    private val myListener =  object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            activity.view.onDoneButtonPressed()
            return false
        }

        // override fun onScroll(e1: MotionEvent, e2: MotionEvent, x: Float, y: Float): Boolean = true

        // override fun onFling(e1: MotionEvent, e2: MotionEvent, x: Float, y: Float): Boolean = true
    }

    private val mDetector: GestureDetector = GestureDetector(context, myListener)

    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)
        return mDetector.onTouchEvent(event)
    }
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
        val resultExpression = addTextToLayout(result)
        if (result != "") {
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

interface IPresenterObserver {
    fun updateLiveResult(result: String) {}
    fun pushInteraction(input: String, result: String) {}
    fun resetInput() {}
    fun updateInput(newInput: String, newInputPosition: InputPosition) {}
}

class Presenter(
    val calculator: Calculator,
    val viewSubscribers: MutableList<IPresenterObserver> = mutableListOf()
) : ICalculatorObserver {

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
        calculator.setInput(newInput)
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

class InputPosition(val start: Int, val end: Int = start) {
    fun isSelection(): Boolean = start != end

    fun isCursor(): Boolean = !isSelection()

    fun getCursor(): Int {
        assert(isCursor())
        return start
    }
}

class Calculator(
    val scope: VariableScope = VariableScope()
) {
    var inputExpression: String = ""
    private var resultTree: ExpressionNode? = null
    var result: Value? = null

    fun setInput(newInput: String) {
        inputExpression = newInput
    }

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
        resultTree = parseString(inputExpression, scope)
        result = calculateResultFromTree(resultTree, scope)
    }
}

class VariableScope(val valueByVariableName: MutableMap<String, Value> = mutableMapOf()) {
    fun getValue(variableName: String): Value? =
        if (variableName in valueByVariableName)
            valueByVariableName[variableName]
        else
            null
    
    fun addVariable(variableName: String, value: Value) {
        valueByVariableName[variableName] = value
    }

    operator fun contains(variableName: String): Boolean = variableName in valueByVariableName
}

fun applyAssigns(resultTree: ExpressionNode?, scope: VariableScope) {
    if (resultTree == null) return
    if (resultTree!!.isVal()) return
    if (resultTree!!.operation!!.type != ExpressionNode.Operation.Type.ASSIGN) {
        applyAssigns(resultTree!!.x, scope)
        applyAssigns(resultTree!!.y, scope)
        return
    }
    if (resultTree!!.x == null) return
    if (!resultTree!!.x!!.isVal()) return
    if (resultTree!!.x!!.getVal().valType != Value.Type.NAME) return
    val rhs = calculateResultFromTree(resultTree.y, scope)
    if (rhs == null || rhs.isError()) return
    scope.addVariable(resultTree.x!!.getVal().toName(), rhs)
}

fun calculateResultFromTree(
    expression: ExpressionNode?,
    scope: VariableScope
): Value? {
    if (expression == null)
        return null
    if (expression.isVal()) {
        if (expression.getVal().valType == Value.Type.NAME) {
            val variableName = expression.getVal().toName()
            if (variableName in scope) {
                return scope.getValue(variableName)
            } else {
                return Value("ERROR: variable $variableName not found")
            }
        }
        return expression.getVal()
    }
    return expression.operation!!.apply(
        calculateResultFromTree(expression.x, scope),
        calculateResultFromTree(expression.y, scope)
    )
}

fun parseString(text: String, scope: VariableScope): ExpressionNode? {
    return parseSubstring(text, 0, text.length - 1, scope)
}

fun parseSubstring(
    text: String,
    begin: Int,
    end: Int,
    scope: VariableScope
): ExpressionNode? {

    if (begin > end)
        return null

    if (!isValidBracketSequence(text, begin, end))
        return ExpressionNode(Value("ERROR: invalid bracket placement"))

    if (text[begin] == ' ')
        return parseSubstring(text, begin + 1, end, scope)
    if (text[end] == ' ')
        return parseSubstring(text, begin, end - 1, scope)

    if (canDiscardMarginalBrackets(text, begin, end))
        return parseSubstring(text, begin + 1, end - 1, scope)

    val substring = text.subSequence(begin, end + 1).toString()
    if (substring.toDoubleOrNull() != null)
        return ExpressionNode(Value(substring))

    val assignIndex = findLeftmostSymbolOnLevelZero(text, begin, end, "=")
    if (assignIndex != null) {
        val variableName = getVariableName(text, begin, assignIndex - 1)
        val rhs = parseSubstring(text, assignIndex + 1, end, scope)
        if (rhs == null)
            return ExpressionNode(Value("ERROR: no RHS"))
        if (variableName == null)
            return ExpressionNode(Value("ERROR: enter variable name"))
        if (!isValidName(variableName))
            return ExpressionNode(Value("ERROR: invalid variable name: $variableName"))
        return ExpressionNode(
            false,
            ExpressionNode.Operation(
                ExpressionNode.Operation.getType(text[assignIndex])!!),
            ExpressionNode(Value(variableName)),
            rhs,
            null
        )
    }

    val addSubstractIndex = findRightmostSymbolOnLevelZero(text, begin, end, "+-")
    if (addSubstractIndex != null && addSubstractIndex != begin) {
        val lhs = parseSubstring(text, begin, addSubstractIndex - 1, scope)
        val rhs = parseSubstring(text, addSubstractIndex + 1, end, scope)
        if (lhs == null)
            return ExpressionNode(Value("ERROR: no LHS"))
        Log.e(TAG, "LHS is not null")
        if (lhs.isVal() && lhs.getVal().isError())
            return lhs
        Log.e(TAG, "LHS is not error")
        if (rhs == null)
            return ExpressionNode(Value("ERROR: no RHS"))
        if (rhs.isVal() && rhs.getVal().isError())
            return rhs
        Log.e(TAG, "LHS and RHS OK")
        return ExpressionNode(
            false,
            ExpressionNode.Operation(ExpressionNode.Operation.getType(
                text[addSubstractIndex])!!), lhs, rhs,
            null
        )
    }

    val multiplyDivideIndex = findRightmostSymbolOnLevelZero(text, begin, end, "*/:")
    if (multiplyDivideIndex != null) {
        val lhs = parseSubstring(text, begin, multiplyDivideIndex - 1, scope)
        val rhs = parseSubstring(text, multiplyDivideIndex + 1, end, scope)
        if (lhs == null)
            return ExpressionNode(Value("ERROR: no LHS"))
        if (lhs.isVal() && lhs.getVal().isError())
            return lhs
        if (rhs == null)
            return ExpressionNode(Value("ERROR: no RHS"))
        if (rhs.isVal() && rhs.getVal().isError())
            return rhs
        return ExpressionNode(
            false,
            ExpressionNode.Operation(ExpressionNode.Operation.getType(
                text[multiplyDivideIndex])!!), lhs, rhs,
            null
        )
    }

    if (text[begin] == '-') {
        val rhs = parseSubstring(text, begin + 1, end, scope)
        if (rhs == null)
            return ExpressionNode(Value("ERROR: no RHS"))
        if (rhs.isVal() && rhs.getVal().isError())
            return rhs
        return ExpressionNode(
            false,
            ExpressionNode.Operation(ExpressionNode.Operation.Type.REVERT),
            null,
            rhs,
            null
        )
    }

    val inputText = text.subSequence(begin, end + 1).toString()
    val value: Value
    if (Value.getStringType(inputText) == Value.Type.ERROR) {
        value = Value("ERROR: \"$inputText\" is not a valid expression")
    } else {
        value = Value(inputText)
    }
    return ExpressionNode(value)
}

fun getVariableName(text: String, begin: Int, end: Int): String? {
    if (begin > end)
        return null

    if (text[begin] == ' ')
        return getVariableName(text, begin + 1, end)
    if (text[end] == ' ')
        return getVariableName(text, begin, end - 1)

    if (canDiscardMarginalBrackets(text, begin, end))
        return getVariableName(text, begin + 1, end - 1)

    return text.subSequence(begin, end + 1).toString()
}

fun isValidName(variableName: String): Boolean {
    if (variableName.length == 0 ||
        !IsValidFirstNameChar(variableName[0]) ||
        variableName == "ERROR")
        return false
    for (c in variableName) {
        if (!isValidNameChar(c))
            return false
    }
    return true
}

fun isValidNameChar(c: Char): Boolean {
    if (c == '_')
        return true
    if (0 <= c - '0' && '9' - c >= 0)
        return true
    if (0 <= c - 'a' && 'z' - c >= 0)
        return true
    if (0 <= c - 'A' && 'Z' - c >= 0)
        return true
    return false
}

fun IsValidFirstNameChar(c: Char): Boolean {
    if (c == '_')
        return true
    if (0 <= c - 'a' && 'z' - c >= 0)
        return true
    if (0 <= c - 'A' && 'Z' - c >= 0)
        return true
    return false
}

fun isDigit(c: Char): Boolean =
    (c - '0') >= 0 && ('9' - c) >= 0

fun isSign(c: Char): Boolean =
    ExpressionNode.Operation.getType(c) != null

fun isValidBracketSequence(
    text: String, begin: Int, end: Int): Boolean {
    var bracketsBalance = 0
    var index = end
    while (index >= begin) {
        bracketsBalance += when (text[index]) {
            '(' -> -1
            ')' -> 1
            else -> 0
        }
        if (bracketsBalance < 0)
            return false
        index -= 1
    }
    if (bracketsBalance != 0)
        return false
    return true
}

fun canDiscardMarginalBrackets(
    text: String, begin: Int, end: Int): Boolean {
    if (text[begin] != '(' || text[end] != ')')
        return false
    var bracketsBalance = 0
    var index = end
    while (index >= begin) {
        bracketsBalance += when (text[index]) {
            '(' -> -1
            ')' -> 1
            else -> 0
        }
        if (index != begin && bracketsBalance == 0)
            return false
        index -= 1
    }
    return true
}

fun findLeftmostSymbolOnLevelZero(
    text: String, begin: Int, end: Int, charset: String): Int? {
    var bracketsBalance = 0
    var index = begin
    while (index <= end) { //>
        bracketsBalance += when (text[index]) {
            '(' -> 1
            ')' -> -1
            else -> 0
        }
        if (bracketsBalance == 0 &&
            text[index] in charset)
            break
        index += 1
    }
    if (index > end)
        return null
    return index
}

fun findRightmostSymbolOnLevelZero(
    text: String, begin: Int, end: Int, charset: String): Int? {
    var bracketsBalance = 0
    var index = end
    while (index >= begin) {
        bracketsBalance += when (text[index]) {
            '(' -> -1
            ')' -> 1
            else -> 0
        }
        if (bracketsBalance == 0 &&
            text[index] in charset)
            break
        index -= 1
    }
    if (index < begin) //>
        return null
    return index
}

class ExpressionNode(
    val isValue: Boolean,
    val operation: Operation?,
    val x: ExpressionNode?,
    val y: ExpressionNode?,
    val value: Value?
) {

    constructor(value: Value) : this(true, null, null, null, value) {}

    class Operation(val type: Type) {

        val EPS: Float = (1e-7).toFloat()

        enum class Type {
            ADD, SUBSTRACT, MULTIPLY, DIVIDE, REVERT, ASSIGN
        }
        companion object {
            fun getType(c: Char): Type? = when (c) {
                '+' -> Type.ADD
                '-' -> Type.SUBSTRACT
                '*' -> Type.MULTIPLY
                '/' -> Type.DIVIDE
                ':' -> Type.DIVIDE
                '=' -> Type.ASSIGN
                else -> null
            }
        }

        fun apply(x: Value?, y: Value?): Value? {
            return when (type) {
                Type.ADD -> add(x, y)
                Type.SUBSTRACT -> substract(x, y)
                Type.MULTIPLY -> multiply(x, y)
                Type.DIVIDE -> divide(x, y)
                Type.REVERT -> revert(x, y)
                Type.ASSIGN -> assign(x, y)
            }
        }

        fun add(x: Value?, y: Value?): Value? {
            if (x == null || y == null)
                return null
            if (x.isError())
                return x
            if (y.isError())
                return y
            if (x.valType == Value.Type.DOUBLE ||
                y.valType == Value.Type.DOUBLE) {
                return Value((x.toDouble()!! + y.toDouble()!!).toString())
            }
            return Value((x.toInt()!!.toLong() + y.toInt()!!.toLong()).toString())
        }

        fun substract(x: Value?, y: Value?): Value? {
            if (x == null || y == null)
                return null
            if (x.isError())
                return x
            if (y.isError())
                return y
            if (x.valType == Value.Type.DOUBLE ||
                y.valType == Value.Type.DOUBLE) {
                return Value((x.toDouble()!! - y.toDouble()!!).toString())
            }
            return Value((x.toInt()!!.toLong() - y.toInt()!!.toLong()).toString())
        }

        fun multiply(x: Value?, y: Value?): Value? {
            if (x == null || y == null)
                return null
            if (x.isError())
                return x
            if (y.isError())
                return y
            if (x.valType == Value.Type.DOUBLE ||
                y.valType == Value.Type.DOUBLE) {
                return Value((x.toDouble()!! * y.toDouble()!!).toString())
            }
            return Value((x.toInt()!!.toLong() * y.toInt()!!.toLong()).toString())
        }

        fun divide(x: Value?, y: Value?): Value? {
            if (x == null || y == null)
                return null
            if (x.isError())
                return x
            if (y.isError())
                return y
            if (x.valType == Value.Type.DOUBLE ||
                y.valType == Value.Type.DOUBLE) {
                val yDouble = y.toDouble()!!
                if (abs(yDouble) < EPS) //>
                    return null
                return Value((x.toDouble()!! / yDouble).toString())
            }
            val yInt = y.toInt()!!
            if (yInt == 0)
                return null
            val xInt = x.toInt()!!
            if (xInt % yInt == 0)
                return Value((xInt / yInt).toString())
            return Value((x.toDouble()!! / yInt).toString())
        }

        fun revert(x: Value?, y: Value?): Value? {
            if (y == null)
                return null
            if (x?.isError() == true)
                return x
            if (y.isError())
                return y
            if (y.valType == Value.Type.DOUBLE)
                return Value((-(y.toDouble()!!)).toString())
            return Value((-(y.toInt()!!)).toString())
        }

        fun assign(x: Value?, y: Value?): Value? = y
    }

    fun isVal(): Boolean = isValue

    fun getVal(): Value = value!!
}

class Value(
    val valType: Type,
    val doubleVal: Double?,
    val intVal: Int?,
    val nameVal: String?,
    val errorVal: String?
) {
    enum class Type {
        INT, DOUBLE, NAME, ERROR
    }
    constructor(rawVal: String) : this(
        getStringType(rawVal),
        rawVal.toDoubleOrNull(),
        rawVal.toIntOrNull(),
        getVariableName(rawVal, 0, rawVal.length - 1),
        rawVal
    ) {}

    companion object {
        fun getStringType(string: String): Type {
            val intVal = string.toIntOrNull()
            if (intVal != null) {
                return Type.INT
            }
            val doubleVal = string.toDoubleOrNull()
            if (doubleVal != null) {
                return Type.DOUBLE
            }
            val nameVal = getVariableName(string, 0, string.length - 1)
            if (nameVal != null && isValidName(nameVal)) {
                return Type.NAME
            }
            return Type.ERROR
        }
    }

    fun isInt(): Boolean = valType == Type.INT
    fun isDouble(): Boolean = valType == Type.DOUBLE
    fun isName(): Boolean = valType == Type.NAME
    fun isError(): Boolean = valType == Type.ERROR

    override fun toString(): String = when(valType) {
        Type.INT -> intVal!!.toString()
        Type.DOUBLE -> doubleVal!!.toString()
        Type.NAME -> nameVal!!
        Type.ERROR -> errorVal!!
    }

    fun toName(): String = when(valType) {
        Type.NAME -> nameVal!!
        else -> {
            assert(false)
            ""
        }
    }

    fun toDouble(): Double = when(valType) {
        Type.INT -> intVal!!.toDouble()
        Type.DOUBLE -> doubleVal!!
        Type.NAME -> {
            assert(false)
            .0
        }
        Type.ERROR -> {
            assert(false)
            .0
        }
    }

    fun toInt(): Int? = when(valType) {
        Type.INT -> intVal
        Type.DOUBLE -> {
            assert(false)
            0
        }
        Type.NAME -> {
            assert(false)
            0
        }
        Type.ERROR -> {
            assert(false)
            0
        }
    }
}

