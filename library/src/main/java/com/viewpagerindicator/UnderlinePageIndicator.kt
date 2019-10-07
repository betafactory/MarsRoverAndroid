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
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration

/**
 * Draws a line for each page. The current page line is colored differently
 * than the unselected page lines.
 */
class UnderlinePageIndicator @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = R.attr.vpiUnderlinePageIndicatorStyle) : View(context, attrs, defStyle), PageIndicator {

    private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    var fades: Boolean = false
        set(fades) {
            if (fades != this.fades) {
                field = fades
                if (fades) {
                    post(mFadeRunnable)
                } else {
                    removeCallbacks(mFadeRunnable)
                    mPaint.alpha = 0xFF
                    invalidate()
                }
            }
        }
    var fadeDelay: Int = 0
    var fadeLength: Int = 0
        set(fadeLength) {
            field = fadeLength
            mFadeBy = 0xFF / (this.fadeLength / FADE_FRAME_MS)
        }
    private var mFadeBy: Int = 0

    private var mViewPager: ViewPager? = null
    private var mListener: ViewPager.OnPageChangeListener? = null
    private var mScrollState: Int = 0
    private var mCurrentPage: Int = 0
    private var mPositionOffset: Float = 0.toFloat()

    private var mTouchSlop: Int = 0
    private var mLastMotionX = -1f
    private var mActivePointerId = INVALID_POINTER
    private var mIsDragging: Boolean = false

    private val mFadeRunnable = object : Runnable {
        override fun run() {
            if (!fades) return

            val alpha = Math.max(mPaint.alpha - mFadeBy, 0)
            mPaint.alpha = alpha
            invalidate()
            if (alpha > 0) {
                postDelayed(this, FADE_FRAME_MS.toLong())
            }
        }
    }

    var selectedColor: Int
        get() = mPaint.color
        set(selectedColor) {
            mPaint.color = selectedColor
            invalidate()
        }

    init {
        if (!isInEditMode) {

            val res = resources

            //Load defaults from resources
            val defaultFades = res.getBoolean(R.bool.default_underline_indicator_fades)
            val defaultFadeDelay = res.getInteger(R.integer.default_underline_indicator_fade_delay)
            val defaultFadeLength = res.getInteger(R.integer.default_underline_indicator_fade_length)
            val defaultSelectedColor = res.getColor(R.color.default_underline_indicator_selected_color)

            //Retrieve styles attributes
            val a = context.obtainStyledAttributes(attrs, R.styleable.UnderlinePageIndicator, defStyle, 0)

            fades = a.getBoolean(R.styleable.UnderlinePageIndicator_fades, defaultFades)
            selectedColor = a.getColor(R.styleable.UnderlinePageIndicator_selectedColor, defaultSelectedColor)
            fadeDelay = a.getInteger(R.styleable.UnderlinePageIndicator_fadeDelay, defaultFadeDelay)
            fadeLength = a.getInteger(R.styleable.UnderlinePageIndicator_fadeLength, defaultFadeLength)

            val background = a.getDrawable(R.styleable.UnderlinePageIndicator_android_background)
            if (background != null) {
                setBackgroundDrawable(background)
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

        val paddingLeft = paddingLeft
        val pageWidth = (width - paddingLeft - paddingRight) / (1f * count)
        val left = paddingLeft + pageWidth * (mCurrentPage + mPositionOffset)
        val right = left + pageWidth
        val top = paddingTop.toFloat()
        val bottom = (height - paddingBottom).toFloat()
        canvas.drawRect(left, top, right, bottom, mPaint)
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
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
        post {
            if (fades) {
                post(mFadeRunnable)
            }
        }
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
        mScrollState = state

        if (mListener != null) {
            mListener!!.onPageScrollStateChanged(state)
        }
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        mCurrentPage = position
        mPositionOffset = positionOffset
        if (fades) {
            if (positionOffsetPixels > 0) {
                removeCallbacks(mFadeRunnable)
                mPaint.alpha = 0xFF
            } else if (mScrollState != ViewPager.SCROLL_STATE_DRAGGING) {
                postDelayed(mFadeRunnable, fadeDelay.toLong())
            }
        }
        invalidate()

        if (mListener != null) {
            mListener!!.onPageScrolled(position, positionOffset, positionOffsetPixels)
        }
    }

    override fun onPageSelected(position: Int) {
        if (mScrollState == ViewPager.SCROLL_STATE_IDLE) {
            mCurrentPage = position
            mPositionOffset = 0f
            invalidate()
            mFadeRunnable.run()
        }
        if (mListener != null) {
            mListener!!.onPageSelected(position)
        }
    }

    override fun setOnPageChangeListener(listener: ViewPager.OnPageChangeListener) {
        mListener = listener
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
        private val FADE_FRAME_MS = 30
    }
}