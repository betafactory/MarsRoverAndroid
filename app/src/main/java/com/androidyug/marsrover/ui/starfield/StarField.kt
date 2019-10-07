package com.androidyug.marsrover.ui.starfield

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.view.SurfaceHolder

class StarField(private val surfaceHolder: SurfaceHolder, private val mWidth: Int, private val mHeight: Int) {

    private val TAG = javaClass.simpleName
    private var mVisible = true
    private val mHandler = Handler()
    private var xWidth: Int = 0
    private var mOffsetSpan: Int = 0
    private val mOffset = 1
    private val mTiles = TILES_NORMAL
    private val mDirection = Stars.RIGHT
    private val mAmmount = AMMOUNT_NORMAL
    private var c: Canvas? = null
    private var stars: Array<Stars>? = null

    private val mDraw = object : Runnable {
        var flag: Boolean? = mVisible
        override fun run() {
            drawFrame()
        }
    }

    private var mPaintFill = Paint()
    private var mPaintStar = Paint()
    private var mPaintText = Paint()

    private var firstTime = true

    init {

        mPaintFill.style = Paint.Style.FILL
        mPaintFill.color = Color.BLACK

        mPaintStar.style = Paint.Style.FILL
        mPaintStar.color = Color.WHITE
        mPaintStar.isAntiAlias = true
    }

    fun start() {
        firstTime = true
        mVisible = true
        drawFrame()
    }

    fun stop() {
        mVisible = false
        mHandler.removeCallbacks(mDraw)
    }

    private fun updateXWidthAndOffsetSpan() {
        xWidth = mWidth * mTiles
        mOffsetSpan = mWidth * (mTiles - 1)
    }


    private fun drawFrame() {

        if (firstTime) {
            firstTime = false
            updateXWidthAndOffsetSpan()
            try {
                c = surfaceHolder.lockCanvas()
                if (c != null) {
                    c!!.drawRect(0f, 0f, mWidth.toFloat(), mHeight.toFloat(), mPaintFill)
                    c!!.drawText("Loading", 5f, 75f, mPaintText)
                }
            } finally {
                if (c != null) surfaceHolder.unlockCanvasAndPost(c)
                c = null
            }
            val far = Stars(1f, 1f, Math.round(3 * mAmmount), xWidth, mHeight)
            val middle = Stars(2.1f, 1.5f, Math.round(5 * mAmmount), xWidth, mHeight)
            val near = Stars(2.9f, 2.5f, Math.round(7 * mAmmount), xWidth, mHeight)
            val close = Stars(4f, 15f, Math.round(40 * mAmmount), xWidth, mHeight)

            stars = arrayOf(far, middle, near, close)

            val steps = mWidth * 2
            for (star in stars!!) {
                for (i in 0 until steps) {
                    star.step(mDirection)
                }
            }
        }

        try {
            c = surfaceHolder.lockCanvas()
            if (c != null) {
                c!!.drawRect(0f, 0f, mWidth.toFloat(), mHeight.toFloat(), mPaintFill)

                for (star in stars!!) {
                    star.step(mDirection)
                    star.draw(c!!, mWidth, mOffset, mPaintStar)
                }
            }
        } finally {
            if (c != null) surfaceHolder.unlockCanvasAndPost(c)
        }

        mHandler.removeCallbacks(mDraw)
        if (mVisible) {
            mHandler.postDelayed(mDraw, 40)
        }
    }

    companion object {

        private const val TILES_NORMAL = 1
        private const val TILES_LARGE = 5
        private const val TILES_HUGE = 7

        private const val AMMOUNT_FEW = 3f
        private const val AMMOUNT_NORMAL = 2f
        private const val AMMOUNT_LOTS = 0.2f
    }
}