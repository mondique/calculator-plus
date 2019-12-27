package com.example.calculator

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

