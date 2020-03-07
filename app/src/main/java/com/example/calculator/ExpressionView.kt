package com.example.calculator

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView

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
            val realText = if (text.isNotEmpty() && text[0] == '>')
                text.subSequence(4, text.length).toString()
                else text
            val inputView = activity.findViewById<EditText>(R.id.input)
            val inputText = inputView.text.toString()
            val inputSelection = InputPosition(
                inputView.selectionStart,
                inputView.selectionEnd
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

