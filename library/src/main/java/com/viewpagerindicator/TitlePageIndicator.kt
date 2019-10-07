/*
 * Copyright (C) 2011 Jake Wharton
 * Copyright (C) 2011 Patrik Akerfeldt
 * Copyright (C) 2011 Francisco Figueiredo Jr.
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
import android.graphics.Path
import android.graphics.Rect
import android.graphics.Typeface
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

import java.util.ArrayList

/**
 * A TitlePageIndicator is a PageIndicator which displays the title of left view
 * (if exist), the title of the current select view (centered) and the title of
 * the right view (if exist). When the user scrolls the ViewPager then titles are
 * also scrolled.
 */
class TitlePageIndicator @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = R.attr.vpiTitlePageIndicatorStyle) : View(context, attrs, defStyle), PageIndicator {

    private var mViewPager: ViewPager? = null
    private var mListener: ViewPager.OnPageChangeListener? = null
    private var mCurrentPage = -1
    private var mPageOffset: Float = 0.toFloat()
    private var mScrollState: Int = 0
    private val mPaintText = Paint()
    private var mBoldText: Boolean = false
    private var mColorText: Int = 0
    private var mColorSelected: Int = 0
    private val mPath = Path()
    private val mBounds = Rect()
    private val mPaintFooterLine = Paint()
    private var mFooterIndicatorStyle: IndicatorStyle? = null
    private var mLinePosition: LinePosition? = null
    private val mPaintFooterIndicator = Paint()
    private var mFooterIndicatorHeight: Float = 0.toFloat()
    private var mFooterIndicatorUnderlinePadding: Float = 0f
    private var mFooterPadding: Float = 0.toFloat()
    private var mTitlePadding: Float = 0.toFloat()
    private var mTopPadding: Float = 0.toFloat()
    /** Left and right side padding for not active view titles.  */
    private var mClipPadding: Float = 0.toFloat()
    private var mFooterLineHeight: Float = 0.toFloat()

    private var mTouchSlop: Int = 0
    private var mLastMotionX = -1f
    private var mActivePointerId = INVALID_POINTER
    private var mIsDragging: Boolean = false

    private var mCenterItemClickListener: OnCenterItemClickListener? = null


    var footerColor: Int
        get() = mPaintFooterLine.color
        set(footerColor) {
            mPaintFooterLine.color = footerColor
            mPaintFooterIndicator.color = footerColor
            invalidate()
        }

    var footerLineHeight: Float
        get() = mFooterLineHeight
        set(footerLineHeight) {
            mFooterLineHeight = footerLineHeight
            mPaintFooterLine.strokeWidth = mFooterLineHeight
            invalidate()
        }

    var footerIndicatorHeight: Float
        get() = mFooterIndicatorHeight
        set(footerTriangleHeight) {
            mFooterIndicatorHeight = footerTriangleHeight
            invalidate()
        }

    var footerIndicatorPadding: Float
        get() = mFooterPadding
        set(footerIndicatorPadding) {
            mFooterPadding = footerIndicatorPadding
            invalidate()
        }

    var footerIndicatorStyle: IndicatorStyle?
        get() = mFooterIndicatorStyle
        set(indicatorStyle) {
            mFooterIndicatorStyle = indicatorStyle
            invalidate()
        }

    var linePosition: LinePosition?
        get() = mLinePosition
        set(linePosition) {
            mLinePosition = linePosition
            invalidate()
        }

    var selectedColor: Int
        get() = mColorSelected
        set(selectedColor) {
            mColorSelected = selectedColor
            invalidate()
        }

    var isSelectedBold: Boolean
        get() = mBoldText
        set(selectedBold) {
            mBoldText = selectedBold
            invalidate()
        }

    var textColor: Int
        get() = mColorText
        set(textColor) {
            mPaintText.color = textColor
            mColorText = textColor
            invalidate()
        }

    var textSize: Float
        get() = mPaintText.textSize
        set(textSize) {
            mPaintText.textSize = textSize
            invalidate()
        }

    var titlePadding: Float
        get() = this.mTitlePadding
        set(titlePadding) {
            mTitlePadding = titlePadding
            invalidate()
        }

    var topPadding: Float
        get() = this.mTopPadding
        set(topPadding) {
            mTopPadding = topPadding
            invalidate()
        }

    var clipPadding: Float
        get() = this.mClipPadding
        set(clipPadding) {
            mClipPadding = clipPadding
            invalidate()
        }

    var typeface: Typeface
        get() = mPaintText.typeface
        set(typeface) {
            mPaintText.typeface = typeface
            invalidate()
        }

    /**
     * Interface for a callback when the center item has been clicked.
     */
    interface OnCenterItemClickListener {
        /**
         * Callback when the center item has been clicked.
         *
         * @param position Position of the current center item.
         */
        fun onCenterItemClick(position: Int)
    }

    enum class IndicatorStyle private constructor(val value: Int) {
        None(0), Triangle(1), Underline(2);


        companion object {

            fun fromValue(value: Int): IndicatorStyle? {
                for (style in IndicatorStyle.values()) {
                    if (style.value == value) {
                        return style
                    }
                }
                return null
            }
        }
    }

    enum class LinePosition private constructor(val value: Int) {
        Bottom(0), Top(1);


        companion object {

            fun fromValue(value: Int): LinePosition? {
                for (position in LinePosition.values()) {
                    if (position.value == value) {
                        return position
                    }
                }
                return null
            }
        }
    }

    init {
        if (!isInEditMode) {

            //Load defaults from resources
            val res = resources
            val defaultFooterColor = res.getColor(R.color.default_title_indicator_footer_color)
            val defaultFooterLineHeight = res.getDimension(R.dimen.default_title_indicator_footer_line_height)
            val defaultFooterIndicatorStyle = res.getInteger(R.integer.default_title_indicator_footer_indicator_style)
            val defaultFooterIndicatorHeight = res.getDimension(R.dimen.default_title_indicator_footer_indicator_height)
            val defaultFooterIndicatorUnderlinePadding = res.getDimension(R.dimen.default_title_indicator_footer_indicator_underline_padding)
            val defaultFooterPadding = res.getDimension(R.dimen.default_title_indicator_footer_padding)
            val defaultLinePosition = res.getInteger(R.integer.default_title_indicator_line_position)
            val defaultSelectedColor = res.getColor(R.color.default_title_indicator_selected_color)
            val defaultSelectedBold = res.getBoolean(R.bool.default_title_indicator_selected_bold)
            val defaultTextColor = res.getColor(R.color.default_title_indicator_text_color)
            val defaultTextSize = res.getDimension(R.dimen.default_title_indicator_text_size)
            val defaultTitlePadding = res.getDimension(R.dimen.default_title_indicator_title_padding)
            val defaultClipPadding = res.getDimension(R.dimen.default_title_indicator_clip_padding)
            val defaultTopPadding = res.getDimension(R.dimen.default_title_indicator_top_padding)

            //Retrieve styles attributes
            val a = context.obtainStyledAttributes(attrs, R.styleable.TitlePageIndicator, defStyle, 0)

            //Retrieve the colors to be used for this view and apply them.
            mFooterLineHeight = a.getDimension(R.styleable.TitlePageIndicator_footerLineHeight, defaultFooterLineHeight)
            mFooterIndicatorStyle = IndicatorStyle.fromValue(a.getInteger(R.styleable.TitlePageIndicator_footerIndicatorStyle, defaultFooterIndicatorStyle))
            mFooterIndicatorHeight = a.getDimension(R.styleable.TitlePageIndicator_footerIndicatorHeight, defaultFooterIndicatorHeight)
            mFooterIndicatorUnderlinePadding = a.getDimension(R.styleable.TitlePageIndicator_footerIndicatorUnderlinePadding, defaultFooterIndicatorUnderlinePadding)
            mFooterPadding = a.getDimension(R.styleable.TitlePageIndicator_footerPadding, defaultFooterPadding)
            mLinePosition = LinePosition.fromValue(a.getInteger(R.styleable.TitlePageIndicator_linePosition, defaultLinePosition))
            mTopPadding = a.getDimension(R.styleable.TitlePageIndicator_topPadding, defaultTopPadding)
            mTitlePadding = a.getDimension(R.styleable.TitlePageIndicator_titlePadding, defaultTitlePadding)
            mClipPadding = a.getDimension(R.styleable.TitlePageIndicator_clipPadding, defaultClipPadding)
            mColorSelected = a.getColor(R.styleable.TitlePageIndicator_selectedColor, defaultSelectedColor)
            mColorText = a.getColor(R.styleable.TitlePageIndicator_android_textColor, defaultTextColor)
            mBoldText = a.getBoolean(R.styleable.TitlePageIndicator_selectedBold, defaultSelectedBold)

            val textSize = a.getDimension(R.styleable.TitlePageIndicator_android_textSize, defaultTextSize)
            val footerColor = a.getColor(R.styleable.TitlePageIndicator_footerColor, defaultFooterColor)
            mPaintText.textSize = textSize
            mPaintText.isAntiAlias = true
            mPaintFooterLine.style = Paint.Style.FILL_AND_STROKE
            mPaintFooterLine.strokeWidth = mFooterLineHeight
            mPaintFooterLine.color = footerColor
            mPaintFooterIndicator.style = Paint.Style.FILL_AND_STROKE
            mPaintFooterIndicator.color = footerColor

            val background = a.getDrawable(R.styleable.TitlePageIndicator_android_background)
            if (background != null) {
                setBackgroundDrawable(background)
            }

            a.recycle()

            val configuration = ViewConfiguration.get(context)
            mTouchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(configuration)
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see android.view.View#onDraw(android.graphics.Canvas)
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (mViewPager == null) {
            return
        }
        val count = mViewPager!!.adapter.count
        if (count == 0) {
            return
        }

        // mCurrentPage is -1 on first start and after orientation changed. If so, retrieve the correct index from viewpager.
        if (mCurrentPage == -1 && mViewPager != null) {
            mCurrentPage = mViewPager!!.currentItem
        }

        //Calculate views bounds
        val bounds = calculateAllBounds(mPaintText)
        val boundsSize = bounds.size

        //Make sure we're on a page that still exists
        if (mCurrentPage >= boundsSize) {
            setCurrentItem(boundsSize - 1)
            return
        }

        val countMinusOne = count - 1
        val halfWidth = width / 2f
        val left = left
        val leftClip = left + mClipPadding
        val width = width
        var height = height
        val right = left + width
        val rightClip = right - mClipPadding

        var page = mCurrentPage
        val offsetPercent: Float
        if (mPageOffset <= 0.5) {
            offsetPercent = mPageOffset
        } else {
            page += 1
            offsetPercent = 1 - mPageOffset
        }
        val currentSelected = offsetPercent <= SELECTION_FADE_PERCENTAGE
        val currentBold = offsetPercent <= BOLD_FADE_PERCENTAGE
        val selectedPercent = (SELECTION_FADE_PERCENTAGE - offsetPercent) / SELECTION_FADE_PERCENTAGE

        //Verify if the current view must be clipped to the screen
        val curPageBound = bounds[mCurrentPage]
        val curPageWidth = (curPageBound.right - curPageBound.left).toFloat()
        if (curPageBound.left < leftClip) {
            //Try to clip to the screen (left side)
            clipViewOnTheLeft(curPageBound, curPageWidth, left)
        }
        if (curPageBound.right > rightClip) {
            //Try to clip to the screen (right side)
            clipViewOnTheRight(curPageBound, curPageWidth, right)
        }

        //Left views starting from the current position
        if (mCurrentPage > 0) {
            for (i in mCurrentPage - 1 downTo 0) {
                val bound = bounds[i]
                //Is left side is outside the screen
                if (bound.left < leftClip) {
                    val w = bound.right - bound.left
                    //Try to clip to the screen (left side)
                    clipViewOnTheLeft(bound, w.toFloat(), left)
                    //Except if there's an intersection with the right view
                    val rightBound = bounds[i + 1]
                    //Intersection
                    if (bound.right + mTitlePadding > rightBound.left) {
                        bound.left = (rightBound.left.toFloat() - w.toFloat() - mTitlePadding).toInt()
                        bound.right = bound.left + w
                    }
                }
            }
        }
        //Right views starting from the current position
        if (mCurrentPage < countMinusOne) {
            for (i in mCurrentPage + 1 until count) {
                val bound = bounds[i]
                //If right side is outside the screen
                if (bound.right > rightClip) {
                    val w = bound.right - bound.left
                    //Try to clip to the screen (right side)
                    clipViewOnTheRight(bound, w.toFloat(), right)
                    //Except if there's an intersection with the left view
                    val leftBound = bounds[i - 1]
                    //Intersection
                    if (bound.left - mTitlePadding < leftBound.right) {
                        bound.left = (leftBound.right + mTitlePadding).toInt()
                        bound.right = bound.left + w
                    }
                }
            }
        }

        //Now draw views
        val colorTextAlpha = mColorText.ushr(24)
        for (i in 0 until count) {
            //Get the title
            val bound = bounds[i]
            //Only if one side is visible
            if (bound.left > left && bound.left < right || bound.right > left && bound.right < right) {
                val currentPage = i == page
                val pageTitle = getTitle(i)

                //Only set bold if we are within bounds
                mPaintText.isFakeBoldText = currentPage && currentBold && mBoldText

                //Draw text as unselected
                mPaintText.color = mColorText
                if (currentPage && currentSelected) {
                    //Fade out/in unselected text as the selected text fades in/out
                    mPaintText.alpha = colorTextAlpha - (colorTextAlpha * selectedPercent).toInt()
                }

                //Except if there's an intersection with the right view
                if (i < boundsSize - 1) {
                    val rightBound = bounds[i + 1]
                    //Intersection
                    if (bound.right + mTitlePadding > rightBound.left) {
                        val w = bound.right - bound.left
                        bound.left = (rightBound.left.toFloat() - w.toFloat() - mTitlePadding).toInt()
                        bound.right = bound.left + w
                    }
                }
                canvas.drawText(pageTitle, 0, pageTitle.length, bound.left.toFloat(), bound.bottom + mTopPadding, mPaintText)

                //If we are within the selected bounds draw the selected text
                if (currentPage && currentSelected) {
                    mPaintText.color = mColorSelected
                    mPaintText.alpha = (mColorSelected.ushr(24) * selectedPercent).toInt()
                    canvas.drawText(pageTitle, 0, pageTitle.length, bound.left.toFloat(), bound.bottom + mTopPadding, mPaintText)
                }
            }
        }

        //If we want the line on the top change height to zero and invert the line height to trick the drawing code
        var footerLineHeight = mFooterLineHeight
        var footerIndicatorLineHeight = mFooterIndicatorHeight
        if (mLinePosition == LinePosition.Top) {
            height = 0
            footerLineHeight = -footerLineHeight
            footerIndicatorLineHeight = -footerIndicatorLineHeight
        }

        //Draw the footer line
        mPath.reset()
        mPath.moveTo(0f, height - footerLineHeight / 2f)
        mPath.lineTo(width.toFloat(), height - footerLineHeight / 2f)
        mPath.close()
        canvas.drawPath(mPath, mPaintFooterLine)

        val heightMinusLine = height - footerLineHeight
        when (mFooterIndicatorStyle) {
            TitlePageIndicator.IndicatorStyle.Triangle -> {
                mPath.reset()
                mPath.moveTo(halfWidth, heightMinusLine - footerIndicatorLineHeight)
                mPath.lineTo(halfWidth + footerIndicatorLineHeight, heightMinusLine)
                mPath.lineTo(halfWidth - footerIndicatorLineHeight, heightMinusLine)
                mPath.close()
                canvas.drawPath(mPath, mPaintFooterIndicator)
            }

            TitlePageIndicator.IndicatorStyle.Underline -> {
                if (!currentSelected || page >= boundsSize) {
                    return
                }

                val underlineBounds = bounds[page]
                val rightPlusPadding = underlineBounds.right + mFooterIndicatorUnderlinePadding
                val leftMinusPadding = underlineBounds.left - mFooterIndicatorUnderlinePadding
                val heightMinusLineMinusIndicator = heightMinusLine - footerIndicatorLineHeight

                mPath.reset()
                mPath.moveTo(leftMinusPadding, heightMinusLine)
                mPath.lineTo(rightPlusPadding, heightMinusLine)
                mPath.lineTo(rightPlusPadding, heightMinusLineMinusIndicator)
                mPath.lineTo(leftMinusPadding, heightMinusLineMinusIndicator)
                mPath.close()

                mPaintFooterIndicator.alpha = (0xFF * selectedPercent).toInt()
                canvas.drawPath(mPath, mPaintFooterIndicator)
                mPaintFooterIndicator.alpha = 0xFF
            }
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
                    val leftThird = halfWidth - sixthWidth
                    val rightThird = halfWidth + sixthWidth
                    val eventX = ev.x

                    if (eventX < leftThird) {
                        if (mCurrentPage > 0) {
                            if (action != MotionEvent.ACTION_CANCEL) {
                                mViewPager!!.currentItem = mCurrentPage - 1
                            }
                            return true
                        }
                    } else if (eventX > rightThird) {
                        if (mCurrentPage < count - 1) {
                            if (action != MotionEvent.ACTION_CANCEL) {
                                mViewPager!!.currentItem = mCurrentPage + 1
                            }
                            return true
                        }
                    } else {
                        //Middle third
                        if (mCenterItemClickListener != null && action != MotionEvent.ACTION_CANCEL) {
                            mCenterItemClickListener!!.onCenterItemClick(mCurrentPage)
                        }
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

    /**
     * Set bounds for the right textView including clip padding.
     *
     * @param curViewBound
     * current bounds.
     * @param curViewWidth
     * width of the view.
     */
    private fun clipViewOnTheRight(curViewBound: Rect, curViewWidth: Float, right: Int) {
        curViewBound.right = (right - mClipPadding).toInt()
        curViewBound.left = (curViewBound.right - curViewWidth).toInt()
    }

    /**
     * Set bounds for the left textView including clip padding.
     *
     * @param curViewBound
     * current bounds.
     * @param curViewWidth
     * width of the view.
     */
    private fun clipViewOnTheLeft(curViewBound: Rect, curViewWidth: Float, left: Int) {
        curViewBound.left = (left + mClipPadding).toInt()
        curViewBound.right = (mClipPadding + curViewWidth).toInt()
    }

    /**
     * Calculate views bounds and scroll them according to the current index
     *
     * @param paint
     * @return
     */
    private fun calculateAllBounds(paint: Paint): ArrayList<Rect> {
        val list = ArrayList<Rect>()
        //For each views (If no values then add a fake one)
        val count = mViewPager!!.adapter.count
        val width = width
        val halfWidth = width / 2
        for (i in 0 until count) {
            val bounds = calcBounds(i, paint)
            val w = bounds.right - bounds.left
            val h = bounds.bottom - bounds.top
            bounds.left = (halfWidth - w / 2f + (i.toFloat() - mCurrentPage.toFloat() - mPageOffset) * width).toInt()
            bounds.right = bounds.left + w
            bounds.top = 0
            bounds.bottom = h
            list.add(bounds)
        }

        return list
    }

    /**
     * Calculate the bounds for a view's title
     *
     * @param index
     * @param paint
     * @return
     */
    private fun calcBounds(index: Int, paint: Paint): Rect {
        //Calculate the text bounds
        val bounds = Rect()
        val title = getTitle(index)
        bounds.right = paint.measureText(title, 0, title.length).toInt()
        bounds.bottom = (paint.descent() - paint.ascent()).toInt()
        return bounds
    }

    override fun setViewPager(view: ViewPager) {
        if (mViewPager === view) {
            return
        }
        if (mViewPager != null) {
            mViewPager!!.setOnPageChangeListener(null)
        }
        kotlin.checkNotNull(view.adapter) { "ViewPager does not have adapter instance." }
        mViewPager = view
        mViewPager!!.setOnPageChangeListener(this)
        invalidate()
    }

    override fun setViewPager(view: ViewPager, initialPosition: Int) {
        setViewPager(view)
        setCurrentItem(initialPosition)
    }

    override fun notifyDataSetChanged() {
        invalidate()
    }

    /**
     * Set a callback listener for the center item click.
     *
     * @param listener Callback instance.
     */
    fun setOnCenterItemClickListener(listener: OnCenterItemClickListener) {
        mCenterItemClickListener = listener
    }

    override fun setCurrentItem(item: Int) {
        kotlin.checkNotNull(mViewPager) { "ViewPager has not been bound." }
        mViewPager!!.currentItem = item
        mCurrentPage = item
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
        mPageOffset = positionOffset
        invalidate()

        if (mListener != null) {
            mListener!!.onPageScrolled(position, positionOffset, positionOffsetPixels)
        }
    }

    override fun onPageSelected(position: Int) {
        if (mScrollState == ViewPager.SCROLL_STATE_IDLE) {
            mCurrentPage = position
            invalidate()
        }

        if (mListener != null) {
            mListener!!.onPageSelected(position)
        }
    }

    override fun setOnPageChangeListener(listener: ViewPager.OnPageChangeListener) {
        mListener = listener
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        //Measure our width in whatever mode specified
        val measuredWidth = View.MeasureSpec.getSize(widthMeasureSpec)

        //Determine our height
        var height: Float
        val heightSpecMode = View.MeasureSpec.getMode(heightMeasureSpec)
        if (heightSpecMode == View.MeasureSpec.EXACTLY) {
            //We were told how big to be
            height = View.MeasureSpec.getSize(heightMeasureSpec).toFloat()
        } else {
            //Calculate the text bounds
            mBounds.setEmpty()
            mBounds.bottom = (mPaintText.descent() - mPaintText.ascent()).toInt()
            height = (mBounds.bottom - mBounds.top).toFloat() + mFooterLineHeight + mFooterPadding + mTopPadding
            if (mFooterIndicatorStyle != IndicatorStyle.None) {
                height += mFooterIndicatorHeight
            }
        }
        val measuredHeight = height.toInt()

        setMeasuredDimension(measuredWidth, measuredHeight)
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

            @JvmField val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(`in`: Parcel): SavedState {
                    return SavedState(`in`)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }

    private fun getTitle(i: Int): CharSequence {
        var title: CharSequence? = mViewPager!!.adapter.getPageTitle(i)
        if (title == null) {
            title = EMPTY_TITLE
        }
        return title
    }

    companion object {
        /**
         * Percentage indicating what percentage of the screen width away from
         * center should the underline be fully faded. A value of 0.25 means that
         * halfway between the center of the screen and an edge.
         */
        private val SELECTION_FADE_PERCENTAGE = 0.25f

        /**
         * Percentage indicating what percentage of the screen width away from
         * center should the selected text bold turn off. A value of 0.05 means
         * that 10% between the center and an edge.
         */
        private val BOLD_FADE_PERCENTAGE = 0.05f

        /**
         * Title text used when no title is provided by the adapter.
         */
        private val EMPTY_TITLE = ""

        private val INVALID_POINTER = -1
    }
}
