/*
 * Copyright (C) 2012 Jake Wharton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.viewpagerindicator

import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.Parcelable
import android.support.v4.view.MotionEventCompat
import android.support.v4.view.ViewConfigurationCompat
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.util.FloatMath
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration

/**
 * Draws a line for each page. The current page line is colored differently
 * than the unselected page lines.
 */
class LinePageIndicator @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = R.attr.vpiLinePageIndicatorStyle) : View(context, attrs, defStyle), PageIndicator {

    private val mPaintUnselected = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mPaintSelected = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mViewPager: ViewPager? = null
    private var mListener: ViewPager.OnPageChangeListener? = null
    private var mCurrentPage: Int = 0
    private var mCentered: Boolean = false
    private var mLineWidth: Float = 0.toFloat()
    private var mGapWidth: Float = 0.toFloat()

    private var mTouchSlop: Int = 0
    private var mLastMotionX = -1f
    private var mActivePointerId = INVALID_POINTER
    private var mIsDragging: Boolean = false

    var isCentered: Boolean
        get() = mCentered
        set(centered) {
            mCentered = centered
            invalidate()
        }

    var unselectedColor: Int
        get() = mPaintUnselected.color
        set(unselectedColor) {
            mPaintUnselected.color = unselectedColor
            invalidate()
        }

    var selectedColor: Int
        get() = mPaintSelected.color
        set(selectedColor) {
            mPaintSelected.color = selectedColor
            invalidate()
        }

    var lineWidth: Float
        get() = mLineWidth
        set(lineWidth) {
            mLineWidth = lineWidth
            invalidate()
        }

    var strokeWidth: Float
        get() = mPaintSelected.strokeWidth
        set(lineHeight) {
            mPaintSelected.strokeWidth = lineHeight
            mPaintUnselected.strokeWidth = lineHeight
            invalidate()
        }

    var gapWidth: Float
        get() = mGapWidth
        set(gapWidth) {
            mGapWidth = gapWidth
            invalidate()
        }

    init {
        if (!isInEditMode) {

            val res = resources

            //Load defaults from resources
            val defaultSelectedColor = res.getColor(R.color.default_line_indicator_selected_color)
            val defaultUnselectedColor = res.getColor(R.color.default_line_indicator_unselected_color)
            val defaultLineWidth = res.getDimension(R.dimen.default_line_indicator_line_width)
            val defaultGapWidth = res.getDimension(R.dimen.default_line_indicator_gap_width)
            val defaultStrokeWidth = res.getDimension(R.dimen.default_line_indicator_stroke_width)
            val defaultCentered = res.getBoolean(R.bool.default_line_indicator_centered)

            //Retrieve styles attributes
            val a = context.obtainStyledAttributes(attrs, R.styleable.LinePageIndicator, defStyle, 0)

            mCentered = a.getBoolean(R.styleable.LinePageIndicator_centered, defaultCentered)
            mLineWidth = a.getDimension(R.styleable.LinePageIndicator_lineWidth, defaultLineWidth)
            mGapWidth = a.getDimension(R.styleable.LinePageIndicator_gapWidth, defaultGapWidth)
            strokeWidth = a.getDimension(R.styleable.LinePageIndicator_strokeWidth, defaultStrokeWidth)
            mPaintUnselected.color = a.getColor(R.styleable.LinePageIndicator_unselectedColor, defaultUnselectedColor)
            mPaintSelected.color = a.getColor(R.styleable.LinePageIndicator_selectedColor, defaultSelectedColor)

            val background = a.getDrawable(R.styleable.LinePageIndicator_android_background)
            if (background != null) {
                setBackground(background)
            }

            a.recycle()

            val configuration = ViewConfiguration.get(context)
            mTouchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(configuration)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (mViewPager == null) {
            return
        }
        val count = mViewPager!!.adapter.count
        if (count == 0) {
            return
        }

        if (mCurrentPage >= count) {
            setCurrentItem(count - 1)
            return
        }

        val lineWidthAndGap = mLineWidth + mGapWidth
        val indicatorWidth = count * lineWidthAndGap - mGapWidth
        val paddingTop = paddingTop.toFloat()
        val paddingLeft = paddingLeft.toFloat()
        val paddingRight = paddingRight.toFloat()

        val verticalOffset = paddingTop + (height.toFloat() - paddingTop - paddingBottom.toFloat()) / 2.0f
        var horizontalOffset = paddingLeft
        if (mCentered) {
            horizontalOffset += (width.toFloat() - paddingLeft - paddingRight) / 2.0f - indicatorWidth / 2.0f
        }

        //Draw stroked circles
        for (i in 0 until count) {
            val dx1 = horizontalOffset + i * lineWidthAndGap
            val dx2 = dx1 + mLineWidth
            canvas.drawLine(dx1, verticalOffset, dx2, verticalOffset, if (i == mCurrentPage) mPaintSelected else mPaintUnselected)
        }
    }

    override fun onTouchEvent(ev: android.view.MotionEvent): Boolean {
        if (super.onTouchEvent(ev)) {
            return true
        }
        if (mViewPager == null || mViewPager!!.adapter.count == 0) {
            return false
        }

        val action = ev.action and MotionEventCompat.ACTION_MASK
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0)
                mLastMotionX = ev.x
            }

            MotionEvent.ACTION_MOVE -> {
                val activePointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId)
                val x = MotionEventCompat.getX(ev, activePointerIndex)
                val deltaX = x - mLastMotionX

                if (!mIsDragging) {
                    if (Math.abs(deltaX) > mTouchSlop) {
                        mIsDragging = true
                    }
                }

                if (mIsDragging) {
                    mLastMotionX = x
                    if (mViewPager!!.isFakeDragging || mViewPager!!.beginFakeDrag()) {
                        mViewPager!!.fakeDragBy(deltaX)
                    }
                }
            }

            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                if (!mIsDragging) {
                    val count = mViewPager!!.adapter.count
                    val width = width
                    val halfWidth = width / 2f
                    val sixthWidth = width / 6f

                    if (mCurrentPage > 0 && ev.x < halfWidth - sixthWidth) {
                        if (action != MotionEvent.ACTION_CANCEL) {
                            mViewPager!!.currentItem = mCurrentPage - 1
                        }
                        return true
                    } else if (mCurrentPage < count - 1 && ev.x > halfWidth + sixthWidth) {
                        if (action != MotionEvent.ACTION_CANCEL) {
                            mViewPager!!.currentItem = mCurrentPage + 1
                        }
                        return true
                    }
                }

                mIsDragging = false
                mActivePointerId = INVALID_POINTER
                if (mViewPager!!.isFakeDragging) mViewPager!!.endFakeDrag()
            }

            MotionEventCompat.ACTION_POINTER_DOWN -> {
                val index = MotionEventCompat.getActionIndex(ev)
                mLastMotionX = MotionEventCompat.getX(ev, index)
                mActivePointerId = MotionEventCompat.getPointerId(ev, index)
            }

            MotionEventCompat.ACTION_POINTER_UP -> {
                val pointerIndex = MotionEventCompat.getActionIndex(ev)
                val pointerId = MotionEventCompat.getPointerId(ev, pointerIndex)
                if (pointerId == mActivePointerId) {
                    val newPointerIndex = if (pointerIndex == 0) 1 else 0
                    mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex)
                }
                mLastMotionX = MotionEventCompat.getX(ev, MotionEventCompat.findPointerIndex(ev, mActivePointerId))
            }
        }

        return true
    }

    override fun setViewPager(viewPager: ViewPager) {
        if (mViewPager === viewPager) {
            return
        }
        if (mViewPager != null) {
            //Clear us from the old pager.
            mViewPager!!.setOnPageChangeListener(null)
        }
        kotlin.checkNotNull(viewPager.adapter) { "ViewPager does not have adapter instance." }
        mViewPager = viewPager
        mViewPager!!.setOnPageChangeListener(this)
        invalidate()
    }

    override fun setViewPager(view: ViewPager, initialPosition: Int) {
        setViewPager(view)
        setCurrentItem(initialPosition)
    }

    override fun setCurrentItem(item: Int) {
        kotlin.checkNotNull(mViewPager) { "ViewPager has not been bound." }
        mViewPager!!.currentItem = item
        mCurrentPage = item
        invalidate()
    }

    override fun notifyDataSetChanged() {
        invalidate()
    }

    override fun onPageScrollStateChanged(state: Int) {
        if (mListener != null) {
            mListener!!.onPageScrollStateChanged(state)
        }
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        if (mListener != null) {
            mListener!!.onPageScrolled(position, positionOffset, positionOffsetPixels)
        }
    }

    override fun onPageSelected(position: Int) {
        mCurrentPage = position
        invalidate()

        if (mListener != null) {
            mListener!!.onPageSelected(position)
        }
    }

    override fun setOnPageChangeListener(listener: ViewPager.OnPageChangeListener) {
        mListener = listener
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec))
    }

    /**
     * Determines the width of this view
     *
     * @param measureSpec
     * A measureSpec packed into an int
     * @return The width of the view, honoring constraints from measureSpec
     */
    private fun measureWidth(measureSpec: Int): Int {
        var result: Float
        val specMode = View.MeasureSpec.getMode(measureSpec)
        val specSize = View.MeasureSpec.getSize(measureSpec)

        if (specMode == View.MeasureSpec.EXACTLY || mViewPager == null) {
            //We were told how big to be
            result = specSize.toFloat()
        } else {
            //Calculate the width according the views count
            val count = mViewPager!!.adapter.count
            result = paddingLeft.toFloat() + paddingRight.toFloat() + count * mLineWidth + (count - 1) * mGapWidth
            //Respect AT_MOST value if that was what is called for by measureSpec
            if (specMode == View.MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize.toFloat())
            }
        }
        return Math.ceil(result.toDouble()).toInt()
    }

    /**
     * Determines the height of this view
     *
     * @param measureSpec
     * A measureSpec packed into an int
     * @return The height of the view, honoring constraints from measureSpec
     */
    private fun measureHeight(measureSpec: Int): Int {
        var result: Float
        val specMode = View.MeasureSpec.getMode(measureSpec)
        val specSize = View.MeasureSpec.getSize(measureSpec)

        if (specMode == View.MeasureSpec.EXACTLY) {
            //We were told how big to be
            result = specSize.toFloat()
        } else {
            //Measure the height
            result = mPaintSelected.strokeWidth + paddingTop.toFloat() + paddingBottom.toFloat()
            //Respect AT_MOST value if that was what is called for by measureSpec
            if (specMode == View.MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize.toFloat())
            }
        }
        return Math.ceil(result.toDouble()).toInt()
    }

    public override fun onRestoreInstanceState(state: Parcelable) {
        val savedState = state as SavedState
        super.onRestoreInstanceState(savedState.superState)
        mCurrentPage = savedState.currentPage
        requestLayout()
    }

    public override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        val savedState = SavedState(superState)
        savedState.currentPage = mCurrentPage
        return savedState
    }

    internal class SavedState : View.BaseSavedState {
        var currentPage: Int = 0

        constructor(superState: Parcelable) : super(superState) {}

        private constructor(`in`: Parcel) : super(`in`) {
            currentPage = `in`.readInt()
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            dest.writeInt(currentPage)
        }

        companion object {

            @JvmField
            val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(`in`: Parcel): SavedState {
                    return SavedState(`in`)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }

    companion object {
        private val INVALID_POINTER = -1
    }
}