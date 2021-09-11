package com.demirli.memegeneratorapp

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout

class DragableBox(context: Context, attributeSet: AttributeSet) : FrameLayout(context, attributeSet) {

    var startingPointerX: Float? = null
    var startingPointerY: Float? = null

    var startingViewX: Float? = null
    var startingViewY: Float? = null

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        when(event?.actionMasked){
            MotionEvent.ACTION_DOWN ->{
                startingViewX = x
                startingViewY = y
                startingPointerX = event.getRawX()
                startingPointerY = event.getRawY()
            }
            MotionEvent.ACTION_MOVE ->{
                val pointerX = event.getRawX()
                val pointerY = event.getRawY()

                val dx = pointerX - startingPointerX!!
                val dy = pointerY - startingPointerY!!

                val viewX = startingViewX!! + dx
                val viewY = startingViewY!! + dy

                x = viewX
                y = viewY
            }
        }
        return true
    }

    fun getCoordinates(): Pair<Float, Float>{
        val xyCoordinates = Pair(x,y)
        return xyCoordinates
    }
}