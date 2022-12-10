package com.example.objectdetection

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.graphics.Rect
import android.util.Log
import android.view.View

class Draw(context : Context,private val rect: Rect,private var text : String,val persent : String) : View(context) {

    private var textRect : Paint = Paint()
//    private var boundRect :Paint = Paint()

    init {
//        boundRect.color  = Color.WHITE
//        boundRect.strokeWidth = 10f
//        boundRect.style = Paint.Style.STROKE

        textRect.color  = Color.WHITE
        textRect.textSize = 50f
        textRect.style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawText("$text $persent",rect.left.toFloat(),rect.top.toFloat(),textRect)
//        canvas?.drawRect(rect.left.toFloat(),rect.top.toFloat(),rect.right.toFloat(),rect.bottom.toFloat(),boundRect)
    }

}