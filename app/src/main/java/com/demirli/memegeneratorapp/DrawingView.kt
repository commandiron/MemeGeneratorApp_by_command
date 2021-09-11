package com.demirli.memegeneratorapp

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.get
import com.demirli.memegeneratorapp.utils.BitmapUtils
import ja.burhanrashid52.photoeditor.PhotoEditor

class DrawingView(context: Context, attrs: AttributeSet): View(context, attrs) {

    private var TOUCH_TOLERANCE = 10

    private var bitmap: Bitmap? = null
    private var bitmapOne: Bitmap? = null
    private var bitmapCanvas: Canvas? = null
    private var paintScreen: Paint? = null
    private var paintLine: Paint? = null
    var pathMap: HashMap<Int, Path>? = null
    private var previousPointMap: HashMap<Int, Point>? = null

    private var changeBitmapFlag = false

    init {
        paintScreen = Paint()

        paintLine = Paint()
        paintLine?.isAntiAlias = true
        paintLine?.color = Color.BLACK
        paintLine?.style = Paint.Style.STROKE
        paintLine?.strokeWidth = 7f
        paintLine?.strokeCap = Paint.Cap.ROUND

        pathMap = hashMapOf()
        previousPointMap = hashMapOf()

    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        if(changeBitmapFlag == false){
            bitmap = Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888)
            bitmapCanvas = Canvas(bitmap!!)
            bitmap?.eraseColor(Color.WHITE)
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if(changeBitmapFlag == false){
            canvas?.drawBitmap(bitmap!!,0f,0f,paintScreen)

        }else{
            canvas?.drawBitmap(bitmapOne!!,0f,0f,paintScreen)
        }

        for(key in pathMap!!.keys){
            canvas?.drawPath(pathMap!![key]!!, paintLine!!)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        var action = event?.actionMasked // event type
        var actionIndex = event?.actionIndex //pointer

        if(action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_UP){

            touchStarted(event?.getX(actionIndex!!),
                event?.getY(actionIndex!!),
                event?.getPointerId(actionIndex!!))
            Log.d("Test: ", event?.getPointerId(actionIndex!!).toString())

        }else if(action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP){
            touchEnded(event?.getPointerId(actionIndex!!))
        }else{
            touchMoved(event)
        }

        invalidate() // redraw the screen

        return true
    }

    private fun touchStarted(x: Float?, y: Float?, pointerId: Int?) {

        var path: Path  // store the path for given touch
        var point: Point // store the last point in path

        if(pathMap!!.containsKey(pointerId)){
            path = pathMap!![pointerId]!!
            point = previousPointMap!![pointerId]!!
        }else{
            path = Path()
            pathMap?.put(pointerId!!,path)
            point = Point()
            previousPointMap?.put(pointerId!!,point)
        }

        path.moveTo(x!!,y!!)
        point.x = x.toInt()
        point.y = y.toInt()
    }

    private fun touchMoved(event: MotionEvent?) {

        for(i in 0 until event!!.pointerCount){
            var pointerId = event.getPointerId(i)
            var pointerIndex = event.findPointerIndex(pointerId)

            if (pathMap!!.containsKey(pointerId)){
                var newX = event.getX(pointerIndex)
                var newY = event.getY(pointerIndex)

                var path = pathMap?.get(pointerId)
                var point = previousPointMap?.get(pointerId)

                //Calculate how far the user moved from the last update
                var deltaX = Math.abs(newX - point!!.x)
                var deltaY = Math.abs(newY - point!!.y)

                //
                if(deltaX >= TOUCH_TOLERANCE || deltaY >= TOUCH_TOLERANCE){

                    // move the path to new location
                    path?.quadTo(point.x.toFloat(), point.y.toFloat(), (newX + point.x) / 2, (newY + point.y) / 2)

                    //store the new coordinates
                    point.x = newX.toInt()
                    point.y = newY.toInt()
                }
            }

        }

    }

    fun clear(){
        pathMap?.clear() // removes all of the paths
        previousPointMap?.clear()
        bitmap?.eraseColor(Color.WHITE)
        invalidate() // reflesh the screen
    }


    private fun touchEnded(pointerId: Int?) {
        var path = pathMap!![pointerId!!] // get the corresponding Path
        bitmapCanvas?.drawPath(path!!, paintLine!!) // draw to bitmapCanvas



        path?.reset()
    }

    fun changeColor(color: Int){
        paintLine?.color = color
    }

    fun changeSizeOfTool(size: Float) {
        paintLine?.strokeWidth = size
    }

    fun changeBitmap(mBitmap: Bitmap){
        changeBitmapFlag = true
        bitmapOne = addWhitePaddingToBitmap(mBitmap).copy(Bitmap.Config.ARGB_8888, true)
        bitmapCanvas = Canvas(bitmapOne!!)
    }

    fun addWhitePaddingToBitmap(src: Bitmap): Bitmap{

        val padding_x = (width - src.width) / 2
        val padding_y = (height- src.height) / 2

        val outPutImage = Bitmap.createBitmap(src.width + padding_x, src.height + padding_y, Bitmap.Config.ARGB_8888)
        val can = Canvas(outPutImage)
        can.drawARGB(0,0,0,0)
        can.drawBitmap(src, padding_x.toFloat(), padding_y.toFloat(), null)

        val outPutImageSecond = Bitmap.createBitmap(outPutImage.width + padding_x, outPutImage.height + padding_y, Bitmap.Config.ARGB_8888)
        val canSecond = Canvas(outPutImageSecond)
        canSecond.drawARGB(0,0,0,0)
        canSecond.drawBitmap(outPutImage, 0f, 0f, null)

        return outPutImageSecond
    }

    fun getLastBitmap(): Bitmap{
        if(changeBitmapFlag == false){
            return bitmap!!
        }else{
            return bitmapOne!!
        }
    }

    fun addTextOnBitmap(text: String, textSize: Float, color: Int, x: Float, y:Float){
        paintScreen!!.textSize = textSize
        paintScreen!!.color = color
        bitmapCanvas!!.drawText(text, x, y+textSize, paintScreen!!)
    }
}