package com.example.calculator

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.*

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

