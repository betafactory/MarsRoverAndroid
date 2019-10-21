package com.androidyug.marsrover.ui.intro


import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import android.database.DataSetObserver
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.os.SystemClock
import android.support.v4.os.ParcelableCompat
import android.support.v4.os.ParcelableCompatCreatorCallbacks
import android.support.v4.view.AccessibilityDelegateCompat
import android.support.v4.view.KeyEventCompat
import android.support.v4.view.MotionEventCompat
import android.support.v4.view.PagerAdapter
import android.support.v4.view.VelocityTrackerCompat
import android.support.v4.view.ViewCompat
import android.support.v4.view.ViewConfigurationCompat
import android.support.v4.view.ViewPager
import android.support.v4.view.accessibility.AccessibilityEventCompat
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat
import android.support.v4.view.accessibility.AccessibilityRecordCompat
import android.support.v4.widget.EdgeEffectCompat
import android.util.AttributeSet
import android.util.Log
import android.view.FocusFinder
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.SoundEffectConstants
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.ViewParent
import android.view.accessibility.AccessibilityEvent
import android.view.animation.Interpolator
import android.widget.Scroller
import com.androidyug.marsrover.ui.intro.VerticalViewPager.LayoutParams

import java.lang.reflect.Method
import java.util.ArrayList
import java.util.Collections
import java.util.Comparator

/**
 * Created by castorflex on 12/29/13.
 * Just a copy of the original ViewPager modified to support vertical Scrolling
 */
class VerticalViewPager : ViewGroup {

    /**
     * Used to track what the expected number of items in the adapter should be.
     * If the app changes this when we don't expect it, we'll throw a big obnoxious exception.
     */
    private var mExpectedAdapterCount: Int = 0

    private val mItems = ArrayList<ItemInfo>()
    private val mTempItem = ItemInfo()

    private val mTempRect = Rect()

    /**
     * Retrieve the current adapter supplying pages.
     *
     * @return The currently registered PagerAdapter
     */
    /**
     * Set a PagerAdapter that will supply views for this pager as needed.
     *
     * @param adapter Adapter to use
     */
    var adapter: PagerAdapter? = null
        set(adapter) {
            if (this.adapter != null) {
                this.adapter!!.unregisterDataSetObserver(mObserver)
                this.adapter!!.startUpdate(this)
                for (i in mItems.indices) {
                    val ii = mItems[i]
                    this.adapter!!.destroyItem(this, ii.position, ii.`object`)
                }
                this.adapter!!.finishUpdate(this)
                mItems.clear()
                removeNonDecorViews()
                mCurItem = 0
                scrollTo(0, 0)
            }

            val oldAdapter = this.adapter
            field = adapter
            mExpectedAdapterCount = 0

            if (this.adapter != null) {
                if (mObserver == null) {
                    mObserver = PagerObserver()
                }
                this.adapter!!.registerDataSetObserver(mObserver)
                mPopulatePending = false
                val wasFirstLayout = mFirstLayout
                mFirstLayout = true
                mExpectedAdapterCount = this.adapter!!.count
                if (mRestoredCurItem >= 0) {
                    this.adapter!!.restoreState(mRestoredAdapterState, mRestoredClassLoader)
                    setCurrentItemInternal(mRestoredCurItem, false, true)
                    mRestoredCurItem = -1
                    mRestoredAdapterState = null
                    mRestoredClassLoader = null
                } else if (!wasFirstLayout) {
                    populate()
                } else {
                    requestLayout()
                }
            }

            if (mAdapterChangeListener != null && oldAdapter !== adapter) {
                mAdapterChangeListener!!.onAdapterChanged(oldAdapter, adapter)
            }
        }
    private var mCurItem: Int = 0   // Index of currently displayed page.
    private var mRestoredCurItem = -1
    private var mRestoredAdapterState: Parcelable? = null
    private var mRestoredClassLoader: ClassLoader? = null
    private var mScroller: Scroller? = null
    private var mObserver: PagerObserver? = null

    /**
     * Return the margin between pages.
     *
     * @return The size of the margin in pixels
     */
    /**
     * Set the margin between pages.
     *
     * @param marginPixels Distance between adjacent pages in pixels
     * @see .getPageMargin
     * @see .setPageMarginDrawable
     * @see .setPageMarginDrawable
     */
    var pageMargin: Int = 0
        set(marginPixels) {
            val oldMargin = pageMargin
            field = marginPixels

            val height = height
            recomputeScrollPosition(height, height, marginPixels, oldMargin)

            requestLayout()
        }
    private var mMarginDrawable: Drawable? = null
    private var mLeftPageBounds: Int = 0
    private var mRightPageBounds: Int = 0

    // Offsets of the first and last items, if known.
    // Set during population, used to determine if we are at the beginning
    // or end of the pager data set during touch scrolling.
    private var mFirstOffset = -java.lang.Float.MAX_VALUE
    private var mLastOffset = java.lang.Float.MAX_VALUE

    private var mChildWidthMeasureSpec: Int = 0
    private var mChildHeightMeasureSpec: Int = 0
    private var mInLayout: Boolean = false

    private var mScrollingCacheEnabled: Boolean = false

    private var mPopulatePending: Boolean = false
    /**
     * Returns the number of pages that will be retained to either side of the
     * current page in the view hierarchy in an idle state. Defaults to 1.
     *
     * @return How many pages will be kept offscreen on either side
     * @see .setOffscreenPageLimit
     */
    /**
     * Set the number of pages that should be retained to either side of the
     * current page in the view hierarchy in an idle state. Pages beyond this
     * limit will be recreated from the adapter when needed.
     *
     *
     *
     * This is offered as an optimization. If you know in advance the number
     * of pages you will need to support or have lazy-loading mechanisms in place
     * on your pages, tweaking this setting can have benefits in perceived smoothness
     * of paging animations and interaction. If you have a small number of pages (3-4)
     * that you can keep active all at once, less time will be spent in layout for
     * newly created view subtrees as the user pages back and forth.
     *
     *
     *
     * You should keep this limit low, especially if your pages have complex layouts.
     * This setting defaults to 1.
     *
     * @param limit How many pages will be kept offscreen in an idle state.
     */
    var offscreenPageLimit = DEFAULT_OFFSCREEN_PAGES
        set(limit) {
            var limit = limit
            if (limit < DEFAULT_OFFSCREEN_PAGES) {
                Log.w(TAG, "Requested offscreen page limit " + limit + " too small; defaulting to " +
                        DEFAULT_OFFSCREEN_PAGES)
                limit = DEFAULT_OFFSCREEN_PAGES
            }
            if (limit != offscreenPageLimit) {
                field = limit
                populate()
            }
        }

    private var mIsBeingDragged: Boolean = false
    private var mIsUnableToDrag: Boolean = false
    private val mIgnoreGutter: Boolean = false
    private var mDefaultGutterSize: Int = 0
    private var mGutterSize: Int = 0
    private var mTouchSlop: Int = 0
    /**
     * Position of the last motion event.
     */
    private var mLastMotionX: Float = 0.toFloat()
    private var mLastMotionY: Float = 0.toFloat()
    private var mInitialMotionX: Float = 0.toFloat()
    private var mInitialMotionY: Float = 0.toFloat()
    /**
     * ID of the active pointer. This is used to retain consistency during
     * drags/flings if multiple pointers are used.
     */
    private var mActivePointerId = INVALID_POINTER

    /**
     * Determines speed during touch scrolling
     */
    private var mVelocityTracker: VelocityTracker? = null
    private var mMinimumVelocity: Int = 0
    private var mMaximumVelocity: Int = 0
    private var mFlingDistance: Int = 0
    private var mCloseEnough: Int = 0

    /**
     * Returns true if a fake drag is in progress.
     *
     * @return true if currently in a fake drag, false otherwise.
     * @see .beginFakeDrag
     * @see .fakeDragBy
     * @see .endFakeDrag
     */
    var isFakeDragging: Boolean = false
        private set
    private var mFakeDragBeginTime: Long = 0

    private var mTopEdge: EdgeEffectCompat? = null
    private var mBottomEdge: EdgeEffectCompat? = null

    private var mFirstLayout = true
    private var mNeedCalculatePageOffsets = false
    private var mCalledSuper: Boolean = false
    private var mDecorChildCount: Int = 0

    private var mOnPageChangeListener: ViewPager.OnPageChangeListener? = null
    private var mInternalPageChangeListener: ViewPager.OnPageChangeListener? = null
    private var mAdapterChangeListener: OnAdapterChangeListener? = null
    private var mPageTransformer: ViewPager.PageTransformer? = null
    private var mSetChildrenDrawingOrderEnabled: Method? = null
    private var mDrawingOrder: Int = 0
    private var mDrawingOrderedChildren: ArrayList<View>? = null

    private val mEndScrollRunnable = Runnable {
        setScrollState(SCROLL_STATE_IDLE)
        populate()
    }

    private var mScrollState = SCROLL_STATE_IDLE

    //    private int getClientWidth() {
    //        return getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
    //    }

    private val clientHeight: Int
        get() = measuredHeight - paddingTop - paddingBottom

    /**
     * Set the currently selected page. If the ViewPager has already been through its first
     * layout with its current adapter there will be a smooth animated transition between
     * the current item and the specified item.
     *
     * @param item Item index to select
     */
    var currentItem: Int
        get() = mCurItem
        set(item) {
            mPopulatePending = false
            setCurrentItemInternal(item, !mFirstLayout, false)
        }

    internal class ItemInfo {
        var `object`: Any? = null
        var position: Int = 0
        var scrolling: Boolean = false
        var heightFactor: Float = 0.toFloat()
        var offset: Float = 0.toFloat()
    }

    /**
     * Used internally to monitor when adapters are switched.
     */
    internal interface OnAdapterChangeListener {
        fun onAdapterChanged(oldAdapter: PagerAdapter?, newAdapter: PagerAdapter)
    }

    /**
     * Used internally to tag special types of child views that should be added as
     * pager decorations by default.
     */
    internal interface Decor
    constructor(context: Context) : super(context) {
        initViewPager()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initViewPager()
    }

    internal fun initViewPager() {
        setWillNotDraw(false)
        descendantFocusability = ViewGroup.FOCUS_AFTER_DESCENDANTS
        isFocusable = true
        val context = context
        mScroller = Scroller(context, sInterpolator)
        val configuration = ViewConfiguration.get(context)
        val density = context.resources.displayMetrics.density

        mTouchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(configuration)
        mMinimumVelocity = (MIN_FLING_VELOCITY * density).toInt()
        mMaximumVelocity = configuration.scaledMaximumFlingVelocity
        mTopEdge = EdgeEffectCompat(context)
        mBottomEdge = EdgeEffectCompat(context)

        mFlingDistance = (MIN_DISTANCE_FOR_FLING * density).toInt()
        mCloseEnough = (CLOSE_ENOUGH * density).toInt()
        mDefaultGutterSize = (DEFAULT_GUTTER_SIZE * density).toInt()

        ViewCompat.setAccessibilityDelegate(this, MyAccessibilityDelegate())

        if (ViewCompat.getImportantForAccessibility(this) == ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_AUTO) {
            ViewCompat.setImportantForAccessibility(this,
                    ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES)
        }
    }

    override fun onDetachedFromWindow() {
        removeCallbacks(mEndScrollRunnable)
        super.onDetachedFromWindow()
    }

    private fun setScrollState(newState: Int) {
        if (mScrollState == newState) {
            return
        }

        mScrollState = newState
        if (mPageTransformer != null) {
            // PageTransformers can do complex things that benefit from hardware layers.
            enableLayers(newState != SCROLL_STATE_IDLE)
        }
        if (mOnPageChangeListener != null) {
            mOnPageChangeListener!!.onPageScrollStateChanged(newState)
        }
    }

    private fun removeNonDecorViews() {
        var i = 0
        while (i < childCount) {
            val child = getChildAt(i)
            val lp = child.layoutParams as LayoutParams
            if (!lp.isDecor) {
                removeViewAt(i)
                i--
            }
            i++
        }
    }

    internal fun setOnAdapterChangeListener(listener: OnAdapterChangeListener) {
        mAdapterChangeListener = listener
    }

    /**
     * Set the currently selected page.
     *
     * @param item         Item index to select
     * @param smoothScroll True to smoothly scroll to the new item, false to transition immediately
     */
    fun setCurrentItem(item: Int, smoothScroll: Boolean) {
        mPopulatePending = false
        setCurrentItemInternal(item, smoothScroll, false)
    }

    @JvmOverloads
    internal fun setCurrentItemInternal(item: Int, smoothScroll: Boolean, always: Boolean, velocity: Int = 0) {
        var item = item
        if (adapter == null || adapter!!.count <= 0) {
            setScrollingCacheEnabled(false)
            return
        }
        if (!always && mCurItem == item && mItems.size != 0) {
            setScrollingCacheEnabled(false)
            return
        }

        if (item < 0) {
            item = 0
        } else if (item >= adapter!!.count) {
            item = adapter!!.count - 1
        }
        val pageLimit = offscreenPageLimit
        if (item > mCurItem + pageLimit || item < mCurItem - pageLimit) {
            // We are doing a jump by more than one page.  To avoid
            // glitches, we want to keep all current pages in the view
            // until the scroll ends.
            for (i in mItems.indices) {
                mItems[i].scrolling = true
            }
        }
        val dispatchSelected = mCurItem != item

        if (mFirstLayout) {
            // We don't have any idea how big we are yet and shouldn't have any pages either.
            // Just set things up and let the pending layout handle things.
            mCurItem = item
            if (dispatchSelected && mOnPageChangeListener != null) {
                mOnPageChangeListener!!.onPageSelected(item)
            }
            if (dispatchSelected && mInternalPageChangeListener != null) {
                mInternalPageChangeListener!!.onPageSelected(item)
            }
            requestLayout()
        } else {
            populate(item)
            scrollToItem(item, smoothScroll, velocity, dispatchSelected)
        }
    }

    private fun scrollToItem(item: Int, smoothScroll: Boolean, velocity: Int,
                             dispatchSelected: Boolean) {
        val curInfo = infoForPosition(item)
        var destY = 0
        if (curInfo != null) {
            val height = clientHeight
            destY = (height * Math.max(mFirstOffset,
                    Math.min(curInfo.offset, mLastOffset))).toInt()
        }
        if (smoothScroll) {
            smoothScrollTo(0, destY, velocity)
            if (dispatchSelected && mOnPageChangeListener != null) {
                mOnPageChangeListener!!.onPageSelected(item)
            }
            if (dispatchSelected && mInternalPageChangeListener != null) {
                mInternalPageChangeListener!!.onPageSelected(item)
            }
        } else {
            if (dispatchSelected && mOnPageChangeListener != null) {
                mOnPageChangeListener!!.onPageSelected(item)
            }
            if (dispatchSelected && mInternalPageChangeListener != null) {
                mInternalPageChangeListener!!.onPageSelected(item)
            }
            completeScroll(false)
            scrollTo(0, destY)
            pageScrolled(destY)
        }
    }

    /**
     * Set a listener that will be invoked whenever the page changes or is incrementally
     * scrolled. See [ViewPager.OnPageChangeListener].
     *
     * @param listener Listener to set
     */
    fun setOnPageChangeListener(listener: ViewPager.OnPageChangeListener) {
        mOnPageChangeListener = listener
    }

    /**
     * Set a [ViewPager.PageTransformer] that will be called for each attached page whenever
     * the scroll position is changed. This allows the application to apply custom property
     * transformations to each page, overriding the default sliding look and feel.
     *
     *
     *
     * *Note:* Prior to Android 3.0 the property animation APIs did not exist.
     * As a result, setting a PageTransformer prior to Android 3.0 (API 11) will have no effect.
     *
     * @param reverseDrawingOrder true if the supplied PageTransformer requires page views
     * to be drawn from last to first instead of first to last.
     * @param transformer         PageTransformer that will modify each page's animation properties
     */
    fun setPageTransformer(reverseDrawingOrder: Boolean, transformer: ViewPager.PageTransformer?) {
        if (Build.VERSION.SDK_INT >= 11) {
            val hasTransformer = transformer != null
            val needsPopulate = hasTransformer != (mPageTransformer != null)
            mPageTransformer = transformer
            setChildrenDrawingOrderEnabledCompat(hasTransformer)
            if (hasTransformer) {
                mDrawingOrder = if (reverseDrawingOrder) DRAW_ORDER_REVERSE else DRAW_ORDER_FORWARD
            } else {
                mDrawingOrder = DRAW_ORDER_DEFAULT
            }
            if (needsPopulate) populate()
        }
    }

    internal fun setChildrenDrawingOrderEnabledCompat(enable: Boolean) {
        if (Build.VERSION.SDK_INT >= 7) {
            if (mSetChildrenDrawingOrderEnabled == null) {
                try {
                    mSetChildrenDrawingOrderEnabled = ViewGroup::class.java.getDeclaredMethod(
                            "setChildrenDrawingOrderEnabled", *arrayOf<Class<*>>(java.lang.Boolean.TYPE))
                } catch (e: NoSuchMethodException) {
                    Log.e(TAG, "Can't find setChildrenDrawingOrderEnabled", e)
                }

            }
            try {
                mSetChildrenDrawingOrderEnabled!!.invoke(this, enable)
            } catch (e: Exception) {
                Log.e(TAG, "Error changing children drawing order", e)
            }

        }
    }

    override fun getChildDrawingOrder(childCount: Int, i: Int): Int {
        val index = if (mDrawingOrder == DRAW_ORDER_REVERSE) childCount - 1 - i else i
        return (mDrawingOrderedChildren!![index].layoutParams as LayoutParams).childIndex
    }

    /**
     * Set a separate OnPageChangeListener for internal use by the support library.
     *
     * @param listener Listener to set
     * @return The old listener that was set, if any.
     */
    internal fun setInternalPageChangeListener(listener: ViewPager.OnPageChangeListener): ViewPager.OnPageChangeListener? {
        val oldListener = mInternalPageChangeListener
        mInternalPageChangeListener = listener
        return oldListener
    }

    /**
     * Set a drawable that will be used to fill the margin between pages.
     *
     * @param d Drawable to display between pages
     */
    fun setPageMarginDrawable(d: Drawable?) {
        mMarginDrawable = d
        if (d != null) refreshDrawableState()
        setWillNotDraw(d == null)
        invalidate()
    }

    /**
     * Set a drawable that will be used to fill the margin between pages.
     *
     * @param resId Resource ID of a drawable to display between pages
     */
    fun setPageMarginDrawable(resId: Int) {
        setPageMarginDrawable(context.resources.getDrawable(resId))
    }

    override fun verifyDrawable(who: Drawable): Boolean {
        return super.verifyDrawable(who) || who === mMarginDrawable
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        val d = mMarginDrawable
        if (d != null && d.isStateful) {
            d.state = drawableState
        }
    }

    // We want the duration of the page snap animation to be influenced by the distance that
    // the screen has to travel, however, we don't want this duration to be effected in a
    // purely linear fashion. Instead, we use this method to moderate the effect that the distance
    // of travel has on the overall snap duration.
    internal fun distanceInfluenceForSnapDuration(f: Float): Float {
        var f = f
        f -= 0.5f // center the values about 0.
        f *= (0.3f * Math.PI / 2.0f).toFloat()
        return Math.sin(f.toDouble()).toFloat()
    }

    /**
     * Like [View.scrollBy], but scroll smoothly instead of immediately.
     *
     * @param x        the number of pixels to scroll by on the X axis
     * @param y        the number of pixels to scroll by on the Y axis
     * @param velocity the velocity associated with a fling, if applicable. (0 otherwise)
     */
    @JvmOverloads
    internal fun smoothScrollTo(x: Int, y: Int, velocity: Int = 0) {
        var velocity = velocity
        if (childCount == 0) {
            // Nothing to do.
            setScrollingCacheEnabled(false)
            return
        }
        val sx = scrollX
        val sy = scrollY
        val dx = x - sx
        val dy = y - sy
        if (dx == 0 && dy == 0) {
            completeScroll(false)
            populate()
            setScrollState(SCROLL_STATE_IDLE)
            return
        }

        setScrollingCacheEnabled(true)
        setScrollState(SCROLL_STATE_SETTLING)

        val height = clientHeight
        val halfHeight = height / 2
        val distanceRatio = Math.min(1f, 1.0f * Math.abs(dx) / height)
        val distance = halfHeight + halfHeight * distanceInfluenceForSnapDuration(distanceRatio)

        var duration = 0
        velocity = Math.abs(velocity)
        if (velocity > 0) {
            duration = 4 * Math.round(1000 * Math.abs(distance / velocity))
        } else {
            val pageHeight = height * adapter!!.getPageWidth(mCurItem)
            val pageDelta = Math.abs(dx).toFloat() / (pageHeight + pageMargin)
            duration = ((pageDelta + 1) * 100).toInt()
        }
        duration = Math.min(duration, MAX_SETTLE_DURATION)

        mScroller!!.startScroll(sx, sy, dx, dy, duration)
        ViewCompat.postInvalidateOnAnimation(this)
    }

    internal fun addNewItem(position: Int, index: Int): ItemInfo {
        val ii = ItemInfo()
        ii.position = position
        ii.`object` = adapter!!.instantiateItem(this, position)
        ii.heightFactor = adapter!!.getPageWidth(position)
        if (index < 0 || index >= mItems.size) {
            mItems.add(ii)
        } else {
            mItems.add(index, ii)
        }
        return ii
    }

    internal fun dataSetChanged() {
        // This method only gets called if our observer is attached, so mAdapter is non-null.

        val adapterCount = adapter!!.count
        mExpectedAdapterCount = adapterCount
        var needPopulate = mItems.size < offscreenPageLimit * 2 + 1 && mItems.size < adapterCount
        var newCurrItem = mCurItem

        var isUpdating = false
        run {
            var i = 0
            while (i < mItems.size) {
                val ii = mItems[i]
                val newPos = adapter!!.getItemPosition(ii.`object`)

                if (newPos == PagerAdapter.POSITION_UNCHANGED) {
                    i++
                    continue
                }

                if (newPos == PagerAdapter.POSITION_NONE) {
                    mItems.removeAt(i)
                    i--

                    if (!isUpdating) {
                        adapter!!.startUpdate(this)
                        isUpdating = true
                    }

                    adapter!!.destroyItem(this, ii.position, ii.`object`)
                    needPopulate = true

                    if (mCurItem == ii.position) {
                        // Keep the current item in the valid range
                        newCurrItem = Math.max(0, Math.min(mCurItem, adapterCount - 1))
                        needPopulate = true
                    }
                    i++
                    continue
                }

                if (ii.position != newPos) {
                    if (ii.position == mCurItem) {
                        // Our current item changed position. Follow it.
                        newCurrItem = newPos
                    }

                    ii.position = newPos
                    needPopulate = true
                }
                i++
            }
        }

        if (isUpdating) {
            adapter!!.finishUpdate(this)
        }

        Collections.sort(mItems, COMPARATOR)

        if (needPopulate) {
            // Reset our known page widths; populate will recompute them.
            val childCount = childCount
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                val lp = child.layoutParams as LayoutParams
                if (!lp.isDecor) {
                    lp.heightFactor = 0f
                }
            }

            setCurrentItemInternal(newCurrItem, false, true)
            requestLayout()
        }
    }

    @JvmOverloads
    internal fun populate(newCurrentItem: Int = mCurItem) {
        var oldCurInfo: ItemInfo? = null
        var focusDirection = View.FOCUS_FORWARD
        if (mCurItem != newCurrentItem) {
            focusDirection = if (mCurItem < newCurrentItem) View.FOCUS_DOWN else View.FOCUS_UP
            oldCurInfo = infoForPosition(mCurItem)
            mCurItem = newCurrentItem
        }

        if (adapter == null) {
            sortChildDrawingOrder()
            return
        }

        // Bail now if we are waiting to populate.  This is to hold off
        // on creating views from the time the user releases their finger to
        // fling to a new position until we have finished the scroll to
        // that position, avoiding glitches from happening at that point.
        if (mPopulatePending) {
            if (DEBUG) Log.i(TAG, "populate is pending, skipping for now...")
            sortChildDrawingOrder()
            return
        }

        // Also, don't populate until we are attached to a window.  This is to
        // avoid trying to populate before we have restored our view hierarchy
        // state and conflicting with what is restored.
        if (windowToken == null) {
            return
        }

        adapter!!.startUpdate(this)

        val pageLimit = offscreenPageLimit
        val startPos = Math.max(0, mCurItem - pageLimit)
        val N = adapter!!.count
        val endPos = Math.min(N - 1, mCurItem + pageLimit)

        if (N != mExpectedAdapterCount) {
            var resName: String
            try {
                resName = resources.getResourceName(id)
            } catch (e: Resources.NotFoundException) {
                resName = Integer.toHexString(id)
            }

            throw IllegalStateException("The application's PagerAdapter changed the adapter's" +
                    " contents without calling PagerAdapter#notifyDataSetChanged!" +
                    " Expected adapter item count: " + mExpectedAdapterCount + ", found: " + N +
                    " Pager id: " + resName +
                    " Pager class: " + javaClass +
                    " Problematic adapter: " + adapter!!.javaClass)
        }

        // Locate the currently focused item or add it if needed.
        var curIndex = -1
        var curItem: ItemInfo? = null
        curIndex = 0
        while (curIndex < mItems.size) {
            val ii = mItems[curIndex]
            if (ii.position >= mCurItem) {
                if (ii.position == mCurItem) curItem = ii
                break
            }
            curIndex++
        }

        if (curItem == null && N > 0) {
            curItem = addNewItem(mCurItem, curIndex)
        }

        // Fill 3x the available width or up to the number of offscreen
        // pages requested to either side, whichever is larger.
        // If we have no current item we have no work to do.
        if (curItem != null) {
            var extraHeightTop = 0f
            var itemIndex = curIndex - 1
            var ii: ItemInfo? = if (itemIndex >= 0) mItems[itemIndex] else null
            val clientHeight = clientHeight
            val topHeightNeeded = if (clientHeight <= 0)
                0
            else
                2f - curItem.heightFactor + paddingLeft.toFloat() / clientHeight.toFloat()
            for (pos in mCurItem - 1 downTo 0) {
                if (extraHeightTop >= topHeightNeeded && pos < startPos) {
                    if (ii == null) {
                        break
                    }
                    if (pos == ii.position && !ii.scrolling) {
                        mItems.removeAt(itemIndex)
                        adapter!!.destroyItem(this, pos, ii.`object`)
                        if (DEBUG) {
                            Log.i(TAG, "populate() - destroyItem() with pos: " + pos +
                                    " view: " + ii.`object` as View?)
                        }
                        itemIndex--
                        curIndex--
                        ii = if (itemIndex >= 0) mItems[itemIndex] else null
                    }
                } else if (ii != null && pos == ii.position) {
                    extraHeightTop += ii.heightFactor
                    itemIndex--
                    ii = if (itemIndex >= 0) mItems[itemIndex] else null
                } else {
                    ii = addNewItem(pos, itemIndex + 1)
                    extraHeightTop += ii.heightFactor
                    curIndex++
                    ii = if (itemIndex >= 0) mItems[itemIndex] else null
                }
            }

            var extraHeightBottom = curItem.heightFactor
            itemIndex = curIndex + 1
            if (extraHeightBottom < 2f) {
                ii = if (itemIndex < mItems.size) mItems[itemIndex] else null
                val bottomHeightNeeded = if (clientHeight <= 0)
                    0
                else
                    paddingRight.toFloat() / clientHeight.toFloat() + 2f
                for (pos in mCurItem + 1 until N) {
                    if (extraHeightBottom >= bottomHeightNeeded && pos > endPos) {
                        if (ii == null) {
                            break
                        }
                        if (pos == ii.position && !ii.scrolling) {
                            mItems.removeAt(itemIndex)
                            adapter!!.destroyItem(this, pos, ii.`object`)
                            if (DEBUG) {
                                Log.i(TAG, "populate() - destroyItem() with pos: " + pos +
                                        " view: " + ii.`object` as View?)
                            }
                            ii = if (itemIndex < mItems.size) mItems[itemIndex] else null
                        }
                    } else if (ii != null && pos == ii.position) {
                        extraHeightBottom += ii.heightFactor
                        itemIndex++
                        ii = if (itemIndex < mItems.size) mItems[itemIndex] else null
                    } else {
                        ii = addNewItem(pos, itemIndex)
                        itemIndex++
                        extraHeightBottom += ii.heightFactor
                        ii = if (itemIndex < mItems.size) mItems[itemIndex] else null
                    }
                }
            }

            calculatePageOffsets(curItem, curIndex, oldCurInfo)
        }

        if (DEBUG) {
            Log.i(TAG, "Current page list:")
            for (i in mItems.indices) {
                Log.i(TAG, "#" + i + ": page " + mItems[i].position)
            }
        }

        adapter!!.setPrimaryItem(this, mCurItem, curItem?.`object`)

        adapter!!.finishUpdate(this)

        // Check width measurement of current pages and drawing sort order.
        // Update LayoutParams as needed.
        val childCount = childCount
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val lp = child.layoutParams as LayoutParams
            lp.childIndex = i
            if (!lp.isDecor && lp.heightFactor == 0f) {
                // 0 means requery the adapter for this, it doesn't have a valid width.
                val ii = infoForChild(child)
                if (ii != null) {
                    lp.heightFactor = ii.heightFactor
                    lp.position = ii.position
                }
            }
        }
        sortChildDrawingOrder()

        if (hasFocus()) {
            val currentFocused = findFocus()
            var ii: ItemInfo? = if (currentFocused != null) infoForAnyChild(currentFocused) else null
            if (ii == null || ii.position != mCurItem) {
                for (i in 0 until getChildCount()) {
                    val child = getChildAt(i)
                    ii = infoForChild(child)
                    if (ii != null && ii.position == mCurItem) {
                        if (child.requestFocus(focusDirection)) {
                            break
                        }
                    }
                }
            }
        }
    }

    private fun sortChildDrawingOrder() {
        if (mDrawingOrder != DRAW_ORDER_DEFAULT) {
            if (mDrawingOrderedChildren == null) {
                mDrawingOrderedChildren = ArrayList()
            } else {
                mDrawingOrderedChildren!!.clear()
            }
            val childCount = childCount
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                mDrawingOrderedChildren!!.add(child)
            }
            Collections.sort(mDrawingOrderedChildren!!, sPositionComparator)
        }
    }

    private fun calculatePageOffsets(curItem: ItemInfo, curIndex: Int, oldCurInfo: ItemInfo?) {
        val N = adapter!!.count
        val height = clientHeight
        val marginOffset = if (height > 0) pageMargin.toFloat() / height else 0
        // Fix up offsets for later layout.
        if (oldCurInfo != null) {
            val oldCurPosition = oldCurInfo.position
            // Base offsets off of oldCurInfo.
            if (oldCurPosition < curItem.position) {
                var itemIndex = 0
                var ii: ItemInfo? = null
                var offset = oldCurInfo.offset + oldCurInfo.heightFactor + marginOffset
                var pos = oldCurPosition + 1
                while (pos <= curItem.position && itemIndex < mItems.size) {
                    ii = mItems[itemIndex]
                    while (pos > ii!!.position && itemIndex < mItems.size - 1) {
                        itemIndex++
                        ii = mItems[itemIndex]
                    }
                    while (pos < ii.position) {
                        // We don't have an item populated for this,
                        // ask the adapter for an offset.
                        offset += adapter!!.getPageWidth(pos) + marginOffset
                        pos++
                    }
                    ii.offset = offset
                    offset += ii.heightFactor + marginOffset
                    pos++
                }
            } else if (oldCurPosition > curItem.position) {
                var itemIndex = mItems.size - 1
                var ii: ItemInfo? = null
                var offset = oldCurInfo.offset
                var pos = oldCurPosition - 1
                while (pos >= curItem.position && itemIndex >= 0) {
                    ii = mItems[itemIndex]
                    while (pos < ii!!.position && itemIndex > 0) {
                        itemIndex--
                        ii = mItems[itemIndex]
                    }
                    while (pos > ii.position) {
                        // We don't have an item populated for this,
                        // ask the adapter for an offset.
                        offset -= adapter!!.getPageWidth(pos) + marginOffset
                        pos--
                    }
                    offset -= ii.heightFactor + marginOffset
                    ii.offset = offset
                    pos--
                }
            }
        }

        // Base all offsets off of curItem.
        val itemCount = mItems.size
        var offset = curItem.offset
        var pos = curItem.position - 1
        mFirstOffset = if (curItem.position == 0) curItem.offset else -java.lang.Float.MAX_VALUE
        mLastOffset = if (curItem.position == N - 1)
            curItem.offset + curItem.heightFactor - 1
        else
            java.lang.Float.MAX_VALUE
        // Previous pages
        run {
            var i = curIndex - 1
            while (i >= 0) {
                val ii = mItems[i]
                while (pos > ii.position) {
                    offset -= adapter!!.getPageWidth(pos--) + marginOffset
                }
                offset -= ii.heightFactor + marginOffset
                ii.offset = offset
                if (ii.position == 0) mFirstOffset = offset
                i--
                pos--
            }
        }
        offset = curItem.offset + curItem.heightFactor + marginOffset
        pos = curItem.position + 1
        // Next pages
        var i = curIndex + 1
        while (i < itemCount) {
            val ii = mItems[i]
            while (pos < ii.position) {
                offset += adapter!!.getPageWidth(pos++) + marginOffset
            }
            if (ii.position == N - 1) {
                mLastOffset = offset + ii.heightFactor - 1
            }
            ii.offset = offset
            offset += ii.heightFactor + marginOffset
            i++
            pos++
        }

        mNeedCalculatePageOffsets = false
    }

    /**
     * This is the persistent state that is saved by ViewPager.  Only needed
     * if you are creating a sublass of ViewPager that must save its own
     * state, in which case it should implement a subclass of this which
     * contains that state.
     */
    class SavedState : View.BaseSavedState {
        internal var position: Int = 0
        internal var adapterState: Parcelable
        internal var loader: ClassLoader

        constructor(superState: Parcelable) : super(superState) {}

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(position)
            out.writeParcelable(adapterState, flags)
        }

        override fun toString(): String {
            return ("FragmentPager.SavedState{"
                    + Integer.toHexString(System.identityHashCode(this))
                    + " position=" + position + "}")
        }

        internal constructor(`in`: Parcel, loader: ClassLoader?) : super(`in`) {
            var loader = loader
            if (loader == null) {
                loader = javaClass.classLoader
            }
            position = `in`.readInt()
            adapterState = `in`.readParcelable(loader)
            this.loader = loader
        }

        companion object {

            val CREATOR: Parcelable.Creator<SavedState> = ParcelableCompat.newCreator(object : ParcelableCompatCreatorCallbacks<SavedState> {
                override fun createFromParcel(`in`: Parcel, loader: ClassLoader): SavedState {
                    return SavedState(`in`, loader)
                }

                override fun newArray(size: Int): Array<SavedState> {
                    return arrayOfNulls(size)
                }
            })
        }
    }

    public override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        val ss = SavedState(superState)
        ss.position = mCurItem
        if (adapter != null) {
            ss.adapterState = adapter!!.saveState()
        }
        return ss
    }

    public override fun onRestoreInstanceState(state: Parcelable) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }

        super.onRestoreInstanceState(state.superState)

        if (adapter != null) {
            adapter!!.restoreState(state.adapterState, state.loader)
            setCurrentItemInternal(state.position, false, true)
        } else {
            mRestoredCurItem = state.position
            mRestoredAdapterState = state.adapterState
            mRestoredClassLoader = state.loader
        }
    }

    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams) {
        var params = params
        if (!checkLayoutParams(params)) {
            params = generateLayoutParams(params)
        }
        val lp = params as LayoutParams
        lp.isDecor = lp.isDecor or (child is Decor)
        if (mInLayout) {
            check(!(lp != null && lp.isDecor)) { "Cannot add pager decor view during layout" }
            lp.needsMeasure = true
            addViewInLayout(child, index, params)
        } else {
            super.addView(child, index, params)
        }

        if (USE_CACHE) {
            if (child.visibility != View.GONE) {
                child.isDrawingCacheEnabled = mScrollingCacheEnabled
            } else {
                child.isDrawingCacheEnabled = false
            }
        }
    }

    override fun removeView(view: View) {
        if (mInLayout) {
            removeViewInLayout(view)
        } else {
            super.removeView(view)
        }
    }

    internal fun infoForChild(child: View): ItemInfo? {
        for (i in mItems.indices) {
            val ii = mItems[i]
            if (adapter!!.isViewFromObject(child, ii.`object`)) {
                return ii
            }
        }
        return null
    }

    internal fun infoForAnyChild(child: View): ItemInfo? {
        var child = child
        var parent: ViewParent?
        while ((parent = child.parent) !== this) {
            if (parent == null || parent !is View) {
                return null
            }
            child = parent
        }
        return infoForChild(child)
    }

    internal fun infoForPosition(position: Int): ItemInfo? {
        for (i in mItems.indices) {
            val ii = mItems[i]
            if (ii.position == position) {
                return ii
            }
        }
        return null
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mFirstLayout = true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // For simple implementation, our internal size is always 0.
        // We depend on the container to specify the layout size of
        // our view.  We can't really know what it is since we will be
        // adding and removing different arbitrary views and do not
        // want the layout to change as this happens.
        setMeasuredDimension(View.getDefaultSize(0, widthMeasureSpec),
                View.getDefaultSize(0, heightMeasureSpec))

        val measuredHeight = measuredHeight
        val maxGutterSize = measuredHeight / 10
        mGutterSize = Math.min(maxGutterSize, mDefaultGutterSize)

        // Children are just made to fill our space.
        var childWidthSize = measuredWidth - paddingLeft - paddingRight
        var childHeightSize = measuredHeight - paddingTop - paddingBottom

        /*
         * Make sure all children have been properly measured. Decor views first.
         * Right now we cheat and make this less complicated by assuming decor
         * views won't intersect. We will pin to edges based on gravity.
         */
        var size = childCount
        for (i in 0 until size) {
            val child = getChildAt(i)
            if (child.visibility != View.GONE) {
                val lp = child.layoutParams as LayoutParams
                if (lp != null && lp.isDecor) {
                    val hgrav = lp.gravity and Gravity.HORIZONTAL_GRAVITY_MASK
                    val vgrav = lp.gravity and Gravity.VERTICAL_GRAVITY_MASK
                    var widthMode = View.MeasureSpec.AT_MOST
                    var heightMode = View.MeasureSpec.AT_MOST
                    val consumeVertical = vgrav == Gravity.TOP || vgrav == Gravity.BOTTOM
                    val consumeHorizontal = hgrav == Gravity.LEFT || hgrav == Gravity.RIGHT

                    if (consumeVertical) {
                        widthMode = View.MeasureSpec.EXACTLY
                    } else if (consumeHorizontal) {
                        heightMode = View.MeasureSpec.EXACTLY
                    }

                    var widthSize = childWidthSize
                    var heightSize = childHeightSize
                    if (lp.width != LayoutParams.WRAP_CONTENT) {
                        widthMode = View.MeasureSpec.EXACTLY
                        if (lp.width != LayoutParams.FILL_PARENT) {
                            widthSize = lp.width
                        }
                    }
                    if (lp.height != LayoutParams.WRAP_CONTENT) {
                        heightMode = View.MeasureSpec.EXACTLY
                        if (lp.height != LayoutParams.FILL_PARENT) {
                            heightSize = lp.height
                        }
                    }
                    val widthSpec = View.MeasureSpec.makeMeasureSpec(widthSize, widthMode)
                    val heightSpec = View.MeasureSpec.makeMeasureSpec(heightSize, heightMode)
                    child.measure(widthSpec, heightSpec)

                    if (consumeVertical) {
                        childHeightSize -= child.measuredHeight
                    } else if (consumeHorizontal) {
                        childWidthSize -= child.measuredWidth
                    }
                }
            }
        }

        mChildWidthMeasureSpec = View.MeasureSpec.makeMeasureSpec(childWidthSize, View.MeasureSpec.EXACTLY)
        mChildHeightMeasureSpec = View.MeasureSpec.makeMeasureSpec(childHeightSize, View.MeasureSpec.EXACTLY)

        // Make sure we have created all fragments that we need to have shown.
        mInLayout = true
        populate()
        mInLayout = false

        // Page views next.
        size = childCount
        for (i in 0 until size) {
            val child = getChildAt(i)
            if (child.visibility != View.GONE) {
                if (DEBUG)
                    Log.v(TAG, "Measuring #" + i + " " + child
                            + ": " + mChildWidthMeasureSpec)

                val lp = child.layoutParams as LayoutParams
                if (lp == null || !lp.isDecor) {
                    val heightSpec = View.MeasureSpec.makeMeasureSpec(
                            (childHeightSize * lp.heightFactor).toInt(), View.MeasureSpec.EXACTLY)
                    child.measure(mChildWidthMeasureSpec, heightSpec)
                }
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        // Make sure scroll position is set correctly.
        if (h != oldh) {
            recomputeScrollPosition(h, oldh, pageMargin, pageMargin)
        }
    }

    private fun recomputeScrollPosition(height: Int, oldHeight: Int, margin: Int, oldMargin: Int) {
        if (oldHeight > 0 && !mItems.isEmpty()) {
            val heightWithMargin = height - paddingTop - paddingBottom + margin
            val oldHeightWithMargin = oldHeight - paddingTop - paddingBottom + oldMargin
            val ypos = scrollY
            val pageOffset = ypos.toFloat() / oldHeightWithMargin
            val newOffsetPixels = (pageOffset * heightWithMargin).toInt()

            scrollTo(scrollX, newOffsetPixels)
            if (!mScroller!!.isFinished) {
                // We now return to your regularly scheduled scroll, already in progress.
                val newDuration = mScroller!!.duration - mScroller!!.timePassed()
                val targetInfo = infoForPosition(mCurItem)
                mScroller!!.startScroll(0, newOffsetPixels,
                        0, (targetInfo!!.offset * height).toInt(), newDuration)
            }
        } else {
            val ii = infoForPosition(mCurItem)
            val scrollOffset = if (ii != null) Math.min(ii.offset, mLastOffset) else 0
            val scrollPos = (scrollOffset * (height - paddingTop - paddingBottom)).toInt()
            if (scrollPos != scrollY) {
                completeScroll(false)
                scrollTo(scrollX, scrollPos)
            }
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val count = childCount
        val width = r - l
        val height = b - t
        var paddingLeft = paddingLeft
        var paddingTop = paddingTop
        var paddingRight = paddingRight
        var paddingBottom = paddingBottom
        val scrollY = scrollY

        var decorCount = 0

        // First pass - decor views. We need to do this in two passes so that
        // we have the proper offsets for non-decor views later.
        for (i in 0 until count) {
            val child = getChildAt(i)
            if (child.visibility != View.GONE) {
                val lp = child.layoutParams as LayoutParams
                var childLeft = 0
                var childTop = 0
                if (lp.isDecor) {
                    val hgrav = lp.gravity and Gravity.HORIZONTAL_GRAVITY_MASK
                    val vgrav = lp.gravity and Gravity.VERTICAL_GRAVITY_MASK
                    when (hgrav) {
                        Gravity.LEFT -> {
                            childLeft = paddingLeft
                            paddingLeft += child.measuredWidth
                        }
                        Gravity.CENTER_HORIZONTAL -> childLeft = Math.max((width - child.measuredWidth) / 2,
                                paddingLeft)
                        Gravity.RIGHT -> {
                            childLeft = width - paddingRight - child.measuredWidth
                            paddingRight += child.measuredWidth
                        }
                        else -> childLeft = paddingLeft
                    }
                    when (vgrav) {
                        Gravity.TOP -> {
                            childTop = paddingTop
                            paddingTop += child.measuredHeight
                        }
                        Gravity.CENTER_VERTICAL -> childTop = Math.max((height - child.measuredHeight) / 2,
                                paddingTop)
                        Gravity.BOTTOM -> {
                            childTop = height - paddingBottom - child.measuredHeight
                            paddingBottom += child.measuredHeight
                        }
                        else -> childTop = paddingTop
                    }
                    childTop += scrollY
                    child.layout(childLeft, childTop,
                            childLeft + child.measuredWidth,
                            childTop + child.measuredHeight)
                    decorCount++
                }
            }
        }

        val childHeight = height - paddingTop - paddingBottom
        // Page views. Do this once we have the right padding offsets from above.
        for (i in 0 until count) {
            val child = getChildAt(i)
            if (child.visibility != View.GONE) {
                val lp = child.layoutParams as LayoutParams
                val ii: ItemInfo?
                if (!lp.isDecor && (ii = infoForChild(child)) != null) {
                    val toff = (childHeight * ii!!.offset).toInt()
                    val childLeft = paddingLeft
                    val childTop = paddingTop + toff
                    if (lp.needsMeasure) {
                        // This was added during layout and needs measurement.
                        // Do it now that we know what we're working with.
                        lp.needsMeasure = false
                        val widthSpec = View.MeasureSpec.makeMeasureSpec(
                                width - paddingLeft - paddingRight,
                                View.MeasureSpec.EXACTLY)
                        val heightSpec = View.MeasureSpec.makeMeasureSpec(
                                (childHeight * lp.heightFactor).toInt(),
                                View.MeasureSpec.EXACTLY)
                        child.measure(widthSpec, heightSpec)
                    }
                    if (DEBUG)
                        Log.v(TAG, "Positioning #" + i + " " + child + " f=" + ii.`object`
                                + ":" + childLeft + "," + childTop + " " + child.measuredWidth
                                + "x" + child.measuredHeight)
                    child.layout(childLeft, childTop,
                            childLeft + child.measuredWidth,
                            childTop + child.measuredHeight)
                }
            }
        }
        mLeftPageBounds = paddingLeft
        mRightPageBounds = width - paddingRight
        mDecorChildCount = decorCount

        if (mFirstLayout) {
            scrollToItem(mCurItem, false, 0, false)
        }
        mFirstLayout = false
    }

    override fun computeScroll() {
        if (!mScroller!!.isFinished && mScroller!!.computeScrollOffset()) {
            val oldX = scrollX
            val oldY = scrollY
            val x = mScroller!!.currX
            val y = mScroller!!.currY

            if (oldX != x || oldY != y) {
                scrollTo(x, y)
                if (!pageScrolled(y)) {
                    mScroller!!.abortAnimation()
                    scrollTo(x, 0)
                }
            }

            // Keep on drawing until the animation has finished.
            ViewCompat.postInvalidateOnAnimation(this)
            return
        }

        // Done with scroll, clean up state.
        completeScroll(true)
    }

    private fun pageScrolled(ypos: Int): Boolean {
        if (mItems.size == 0) {
            mCalledSuper = false
            onPageScrolled(0, 0f, 0)
            check(mCalledSuper) { "onPageScrolled did not call superclass implementation" }
            return false
        }
        val ii = infoForCurrentScrollPosition()
        val height = clientHeight
        val heightWithMargin = height + pageMargin
        val marginOffset = pageMargin.toFloat() / height
        val currentPage = ii!!.position
        val pageOffset = (ypos.toFloat() / height - ii.offset) / (ii.heightFactor + marginOffset)
        val offsetPixels = (pageOffset * heightWithMargin).toInt()

        mCalledSuper = false
        onPageScrolled(currentPage, pageOffset, offsetPixels)
        check(mCalledSuper) { "onPageScrolled did not call superclass implementation" }
        return true
    }

    /**
     * This method will be invoked when the current page is scrolled, either as part
     * of a programmatically initiated smooth scroll or a user initiated touch scroll.
     * If you override this method you must call through to the superclass implementation
     * (e.g. super.onPageScrolled(position, offset, offsetPixels)) before onPageScrolled
     * returns.
     *
     * @param position     Position index of the first page currently being displayed.
     * Page position+1 will be visible if positionOffset is nonzero.
     * @param offset       Value from [0, 1) indicating the offset from the page at position.
     * @param offsetPixels Value in pixels indicating the offset from position.
     */
    protected fun onPageScrolled(position: Int, offset: Float, offsetPixels: Int) {
        // Offset any decor views if needed - keep them on-screen at all times.
        if (mDecorChildCount > 0) {
            val scrollY = scrollY
            var paddingTop = paddingTop
            var paddingBottom = paddingBottom
            val height = height
            val childCount = childCount
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                val lp = child.layoutParams as LayoutParams
                if (!lp.isDecor) continue

                val vgrav = lp.gravity and Gravity.VERTICAL_GRAVITY_MASK
                var childTop = 0
                when (vgrav) {
                    Gravity.TOP -> {
                        childTop = paddingTop
                        paddingTop += child.height
                    }
                    Gravity.CENTER_VERTICAL -> childTop = Math.max((height - child.measuredHeight) / 2,
                            paddingTop)
                    Gravity.BOTTOM -> {
                        childTop = height - paddingBottom - child.measuredHeight
                        paddingBottom += child.measuredHeight
                    }
                    else -> childTop = paddingTop
                }
                childTop += scrollY

                val childOffset = childTop - child.top
                if (childOffset != 0) {
                    child.offsetTopAndBottom(childOffset)
                }
            }
        }

        if (mOnPageChangeListener != null) {
            mOnPageChangeListener!!.onPageScrolled(position, offset, offsetPixels)
        }
        if (mInternalPageChangeListener != null) {
            mInternalPageChangeListener!!.onPageScrolled(position, offset, offsetPixels)
        }

        if (mPageTransformer != null) {
            val scrollY = scrollY
            val childCount = childCount
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                val lp = child.layoutParams as LayoutParams

                if (lp.isDecor) continue

                val transformPos = (child.top - scrollY).toFloat() / clientHeight
                mPageTransformer!!.transformPage(child, transformPos)
            }
        }

        mCalledSuper = true
    }

    private fun completeScroll(postEvents: Boolean) {
        var needPopulate = mScrollState == SCROLL_STATE_SETTLING
        if (needPopulate) {
            // Done with scroll, no longer want to cache view drawing.
            setScrollingCacheEnabled(false)
            mScroller!!.abortAnimation()
            val oldX = scrollX
            val oldY = scrollY
            val x = mScroller!!.currX
            val y = mScroller!!.currY
            if (oldX != x || oldY != y) {
                scrollTo(x, y)
            }
        }
        mPopulatePending = false
        for (i in mItems.indices) {
            val ii = mItems[i]
            if (ii.scrolling) {
                needPopulate = true
                ii.scrolling = false
            }
        }
        if (needPopulate) {
            if (postEvents) {
                ViewCompat.postOnAnimation(this, mEndScrollRunnable)
            } else {
                mEndScrollRunnable.run()
            }
        }
    }

    private fun isGutterDrag(y: Float, dy: Float): Boolean {
        return y < mGutterSize && dy > 0 || y > height - mGutterSize && dy < 0
    }

    private fun enableLayers(enable: Boolean) {
        val childCount = childCount
        for (i in 0 until childCount) {
            val layerType = if (enable)
                ViewCompat.LAYER_TYPE_HARDWARE
            else
                ViewCompat.LAYER_TYPE_NONE
            ViewCompat.setLayerType(getChildAt(i), layerType, null)
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        /*
         * This method JUST determines whether we want to intercept the motion.
         * If we return true, onMotionEvent will be called and we do the actual
         * scrolling there.
         */

        val action = ev.action and MotionEventCompat.ACTION_MASK

        // Always take care of the touch gesture being complete.
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            // Release the drag.
            if (DEBUG) Log.v(TAG, "Intercept done!")
            mIsBeingDragged = false
            mIsUnableToDrag = false
            mActivePointerId = INVALID_POINTER
            if (mVelocityTracker != null) {
                mVelocityTracker!!.recycle()
                mVelocityTracker = null
            }
            return false
        }

        // Nothing more to do here if we have decided whether or not we
        // are dragging.
        if (action != MotionEvent.ACTION_DOWN) {
            if (mIsBeingDragged) {
                if (DEBUG) Log.v(TAG, "Intercept returning true!")
                return true
            }
            if (mIsUnableToDrag) {
                if (DEBUG) Log.v(TAG, "Intercept returning false!")
                return false
            }
        }

        when (action) {
            MotionEvent.ACTION_MOVE -> {
                /*
                 * mIsBeingDragged == false, otherwise the shortcut would have caught it. Check
                 * whether the user has moved far enough from his original down touch.
                 */

                /*
                 * Locally do absolute value. mLastMotionY is set to the y value
                 * of the down event.
                 */
                val activePointerId = mActivePointerId
                if (activePointerId == INVALID_POINTER) {
                    // If we don't have a valid id, the touch down wasn't on content.
                    break
                }

                val pointerIndex = MotionEventCompat.findPointerIndex(ev, activePointerId)
                val y = MotionEventCompat.getY(ev, pointerIndex)
                val dy = y - mLastMotionY
                val yDiff = Math.abs(dy)
                val x = MotionEventCompat.getX(ev, pointerIndex)
                val xDiff = Math.abs(x - mInitialMotionX)
                if (DEBUG) Log.v(TAG, "Moved x to $x,$y diff=$xDiff,$yDiff")

                if (dy != 0f && !isGutterDrag(mLastMotionY, dy) &&
                        canScroll(this, false, dy.toInt(), x.toInt(), y.toInt())) {
                    // Nested view has scrollable area under this point. Let it be handled there.
                    mLastMotionX = x
                    mLastMotionY = y
                    mIsUnableToDrag = true
                    return false
                }
                if (yDiff > mTouchSlop && yDiff * 0.5f > xDiff) {
                    if (DEBUG) Log.v(TAG, "Starting drag!")
                    mIsBeingDragged = true
                    requestParentDisallowInterceptTouchEvent(true)
                    setScrollState(SCROLL_STATE_DRAGGING)
                    mLastMotionY = if (dy > 0)
                        mInitialMotionY + mTouchSlop
                    else
                        mInitialMotionY - mTouchSlop
                    mLastMotionX = x
                    setScrollingCacheEnabled(true)
                } else if (xDiff > mTouchSlop) {
                    // The finger has moved enough in the vertical
                    // direction to be counted as a drag...  abort
                    // any attempt to drag horizontally, to work correctly
                    // with children that have scrolling containers.
                    if (DEBUG) Log.v(TAG, "Starting unable to drag!")
                    mIsUnableToDrag = true
                }
                if (mIsBeingDragged) {
                    // Scroll to follow the motion event
                    if (performDrag(y)) {
                        ViewCompat.postInvalidateOnAnimation(this)
                    }
                }
            }

            MotionEvent.ACTION_DOWN -> {
                /*
                 * Remember location of down touch.
                 * ACTION_DOWN always refers to pointer index 0.
                 */
                mInitialMotionX = ev.x
                mLastMotionX = mInitialMotionX
                mInitialMotionY = ev.y
                mLastMotionY = mInitialMotionY
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0)
                mIsUnableToDrag = false

                mScroller!!.computeScrollOffset()
                if (mScrollState == SCROLL_STATE_SETTLING && Math.abs(mScroller!!.finalY - mScroller!!.currY) > mCloseEnough) {
                    // Let the user 'catch' the pager as it animates.
                    mScroller!!.abortAnimation()
                    mPopulatePending = false
                    populate()
                    mIsBeingDragged = true
                    requestParentDisallowInterceptTouchEvent(true)
                    setScrollState(SCROLL_STATE_DRAGGING)
                } else {
                    completeScroll(false)
                    mIsBeingDragged = false
                }

                if (DEBUG)
                    Log.v(TAG, "Down at " + mLastMotionX + "," + mLastMotionY
                            + " mIsBeingDragged=" + mIsBeingDragged
                            + "mIsUnableToDrag=" + mIsUnableToDrag)
            }

            MotionEventCompat.ACTION_POINTER_UP -> onSecondaryPointerUp(ev)
        }

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain()
        }
        mVelocityTracker!!.addMovement(ev)

        /*
         * The only time we want to intercept motion events is if we are in the
         * drag mode.
         */
        return mIsBeingDragged
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (isFakeDragging) {
            // A fake drag is in progress already, ignore this real one
            // but still eat the touch events.
            // (It is likely that the user is multi-touching the screen.)
            return true
        }

        if (ev.action == MotionEvent.ACTION_DOWN && ev.edgeFlags != 0) {
            // Don't handle edge touches immediately -- they may actually belong to one of our
            // descendants.
            return false
        }

        if (adapter == null || adapter!!.count == 0) {
            // Nothing to present or scroll; nothing to touch.
            return false
        }

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain()
        }
        mVelocityTracker!!.addMovement(ev)

        val action = ev.action
        var needsInvalidate = false

        when (action and MotionEventCompat.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                mScroller!!.abortAnimation()
                mPopulatePending = false
                populate()

                // Remember where the motion event started
                mInitialMotionX = ev.x
                mLastMotionX = mInitialMotionX
                mInitialMotionY = ev.y
                mLastMotionY = mInitialMotionY
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0)
            }
            MotionEvent.ACTION_MOVE -> {
                if (!mIsBeingDragged) {
                    val pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId)
                    val y = MotionEventCompat.getY(ev, pointerIndex)
                    val yDiff = Math.abs(y - mLastMotionY)
                    val x = MotionEventCompat.getX(ev, pointerIndex)
                    val xDiff = Math.abs(x - mLastMotionX)
                    if (DEBUG)
                        Log.v(TAG, "Moved x to $x,$y diff=$xDiff,$yDiff")
                    if (yDiff > mTouchSlop && yDiff > xDiff) {
                        if (DEBUG) Log.v(TAG, "Starting drag!")
                        mIsBeingDragged = true
                        requestParentDisallowInterceptTouchEvent(true)
                        mLastMotionY = if (y - mInitialMotionY > 0)
                            mInitialMotionY + mTouchSlop
                        else
                            mInitialMotionY - mTouchSlop
                        mLastMotionX = x
                        setScrollState(SCROLL_STATE_DRAGGING)
                        setScrollingCacheEnabled(true)

                        // Disallow Parent Intercept, just in case
                        val parent = parent
                        parent?.requestDisallowInterceptTouchEvent(true)
                    }
                }
                // Not else! Note that mIsBeingDragged can be set above.
                if (mIsBeingDragged) {
                    // Scroll to follow the motion event
                    val activePointerIndex = MotionEventCompat.findPointerIndex(
                            ev, mActivePointerId)
                    val y = MotionEventCompat.getY(ev, activePointerIndex)
                    needsInvalidate = needsInvalidate or performDrag(y)
                }
            }
            MotionEvent.ACTION_UP -> if (mIsBeingDragged) {
                val velocityTracker = mVelocityTracker
                velocityTracker!!.computeCurrentVelocity(1000, mMaximumVelocity.toFloat())
                val initialVelocity = VelocityTrackerCompat.getYVelocity(
                        velocityTracker, mActivePointerId).toInt()
                mPopulatePending = true
                val height = clientHeight
                val scrollY = scrollY
                val ii = infoForCurrentScrollPosition()
                val currentPage = ii!!.position
                val pageOffset = (scrollY.toFloat() / height - ii.offset) / ii.heightFactor
                val activePointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId)
                val y = MotionEventCompat.getY(ev, activePointerIndex)
                val totalDelta = (y - mInitialMotionY).toInt()
                val nextPage = determineTargetPage(currentPage, pageOffset, initialVelocity,
                        totalDelta)
                setCurrentItemInternal(nextPage, true, true, initialVelocity)

                mActivePointerId = INVALID_POINTER
                endDrag()
                needsInvalidate = mTopEdge!!.onRelease() or mBottomEdge!!.onRelease()
            }
            MotionEvent.ACTION_CANCEL -> if (mIsBeingDragged) {
                scrollToItem(mCurItem, true, 0, false)
                mActivePointerId = INVALID_POINTER
                endDrag()
                needsInvalidate = mTopEdge!!.onRelease() or mBottomEdge!!.onRelease()
            }
            MotionEventCompat.ACTION_POINTER_DOWN -> {
                val index = MotionEventCompat.getActionIndex(ev)
                val y = MotionEventCompat.getY(ev, index)
                mLastMotionY = y
                mActivePointerId = MotionEventCompat.getPointerId(ev, index)
            }
            MotionEventCompat.ACTION_POINTER_UP -> {
                onSecondaryPointerUp(ev)
                mLastMotionY = MotionEventCompat.getY(ev,
                        MotionEventCompat.findPointerIndex(ev, mActivePointerId))
            }
        }
        if (needsInvalidate) {
            ViewCompat.postInvalidateOnAnimation(this)
        }
        return true
    }

    private fun requestParentDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        val parent = parent
        parent?.requestDisallowInterceptTouchEvent(disallowIntercept)
    }

    private fun performDrag(y: Float): Boolean {
        var needsInvalidate = false

        val deltaY = mLastMotionY - y
        mLastMotionY = y

        val oldScrollY = scrollY.toFloat()
        var scrollY = oldScrollY + deltaY
        val height = clientHeight

        var topBound = height * mFirstOffset
        var bottomBound = height * mLastOffset
        var topAbsolute = true
        var bottomAbsolute = true

        val firstItem = mItems[0]
        val lastItem = mItems[mItems.size - 1]
        if (firstItem.position != 0) {
            topAbsolute = false
            topBound = firstItem.offset * height
        }
        if (lastItem.position != adapter!!.count - 1) {
            bottomAbsolute = false
            bottomBound = lastItem.offset * height
        }

        if (scrollY < topBound) {
            if (topAbsolute) {
                val over = topBound - scrollY
                needsInvalidate = mTopEdge!!.onPull(Math.abs(over) / height)
            }
            scrollY = topBound
        } else if (scrollY > bottomBound) {
            if (bottomAbsolute) {
                val over = scrollY - bottomBound
                needsInvalidate = mBottomEdge!!.onPull(Math.abs(over) / height)
            }
            scrollY = bottomBound
        }
        // Don't lose the rounded component
        mLastMotionX += scrollY - scrollY.toInt()
        scrollTo(scrollX, scrollY.toInt())
        pageScrolled(scrollY.toInt())

        return needsInvalidate
    }

    /**
     * @return Info about the page at the current scroll position.
     * This can be synthetic for a missing middle page; the 'object' field can be null.
     */
    private fun infoForCurrentScrollPosition(): ItemInfo? {
        val height = clientHeight
        val scrollOffset = if (height > 0) scrollY.toFloat() / height else 0
        val marginOffset = if (height > 0) pageMargin.toFloat() / height else 0
        var lastPos = -1
        var lastOffset = 0f
        var lastHeight = 0f
        var first = true

        var lastItem: ItemInfo? = null
        var i = 0
        while (i < mItems.size) {
            var ii = mItems[i]
            val offset: Float
            if (!first && ii.position != lastPos + 1) {
                // Create a synthetic item for a missing page.
                ii = mTempItem
                ii.offset = lastOffset + lastHeight + marginOffset
                ii.position = lastPos + 1
                ii.heightFactor = adapter!!.getPageWidth(ii.position)
                i--
            }
            offset = ii.offset

            val bottomBound = offset + ii.heightFactor + marginOffset
            if (first || scrollOffset >= offset) {
                if (scrollOffset < bottomBound || i == mItems.size - 1) {
                    return ii
                }
            } else {
                return lastItem
            }
            first = false
            lastPos = ii.position
            lastOffset = offset
            lastHeight = ii.heightFactor
            lastItem = ii
            i++
        }

        return lastItem
    }

    private fun determineTargetPage(currentPage: Int, pageOffset: Float, velocity: Int, deltaY: Int): Int {
        var targetPage: Int
        if (Math.abs(deltaY) > mFlingDistance && Math.abs(velocity) > mMinimumVelocity) {
            targetPage = if (velocity > 0) currentPage else currentPage + 1
        } else {
            val truncator = if (currentPage >= mCurItem) 0.4f else 0.6f
            targetPage = (currentPage.toFloat() + pageOffset + truncator).toInt()
        }

        if (mItems.size > 0) {
            val firstItem = mItems[0]
            val lastItem = mItems[mItems.size - 1]

            // Only let the user target pages we have items for
            targetPage = Math.max(firstItem.position, Math.min(targetPage, lastItem.position))
        }

        return targetPage
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        var needsInvalidate = false

        val overScrollMode = ViewCompat.getOverScrollMode(this)
        if (overScrollMode == ViewCompat.OVER_SCROLL_ALWAYS || overScrollMode == ViewCompat.OVER_SCROLL_IF_CONTENT_SCROLLS &&
                adapter != null && adapter!!.count > 1) {
            if (!mTopEdge!!.isFinished) {
                val restoreCount = canvas.save()
                val height = height
                val width = width - paddingLeft - paddingRight

                canvas.translate(paddingLeft.toFloat(), mFirstOffset * height)
                mTopEdge!!.setSize(width, height)
                needsInvalidate = needsInvalidate or mTopEdge!!.draw(canvas)
                canvas.restoreToCount(restoreCount)
            }
            if (!mBottomEdge!!.isFinished) {
                val restoreCount = canvas.save()
                val height = height
                val width = width - paddingLeft - paddingRight

                canvas.rotate(180f)
                canvas.translate((-width - paddingLeft).toFloat(), -(mLastOffset + 1) * height)
                mBottomEdge!!.setSize(width, height)
                needsInvalidate = needsInvalidate or mBottomEdge!!.draw(canvas)
                canvas.restoreToCount(restoreCount)
            }
        } else {
            mTopEdge!!.finish()
            mBottomEdge!!.finish()
        }

        if (needsInvalidate) {
            // Keep animating
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw the margin drawable between pages if needed.
        if (pageMargin > 0 && mMarginDrawable != null && mItems.size > 0 && adapter != null) {
            val scrollY = scrollY
            val height = height

            val marginOffset = pageMargin.toFloat() / height
            var itemIndex = 0
            var ii = mItems[0]
            var offset = ii.offset
            val itemCount = mItems.size
            val firstPos = ii.position
            val lastPos = mItems[itemCount - 1].position
            for (pos in firstPos until lastPos) {
                while (pos > ii.position && itemIndex < itemCount) {
                    ii = mItems[++itemIndex]
                }

                val drawAt: Float
                if (pos == ii.position) {
                    drawAt = (ii.offset + ii.heightFactor) * height
                    offset = ii.offset + ii.heightFactor + marginOffset
                } else {
                    val heightFactor = adapter!!.getPageWidth(pos)
                    drawAt = (offset + heightFactor) * height
                    offset += heightFactor + marginOffset
                }

                if (drawAt + pageMargin > scrollY) {
                    mMarginDrawable!!.setBounds(mLeftPageBounds, drawAt.toInt(),
                            mRightPageBounds, (drawAt + pageMargin.toFloat() + 0.5f).toInt())
                    mMarginDrawable!!.draw(canvas)
                }

                if (drawAt > scrollY + height) {
                    break // No more visible, no sense in continuing
                }
            }
        }
    }

    /**
     * Start a fake drag of the pager.
     *
     *
     *
     * A fake drag can be useful if you want to synchronize the motion of the ViewPager
     * with the touch scrolling of another view, while still letting the ViewPager
     * control the snapping motion and fling behavior. (e.g. parallax-scrolling tabs.)
     * Call [.fakeDragBy] to simulate the actual drag motion. Call
     * [.endFakeDrag] to complete the fake drag and fling as necessary.
     *
     *
     *
     * During a fake drag the ViewPager will ignore all touch events. If a real drag
     * is already in progress, this method will return false.
     *
     * @return true if the fake drag began successfully, false if it could not be started.
     * @see .fakeDragBy
     * @see .endFakeDrag
     */
    fun beginFakeDrag(): Boolean {
        if (mIsBeingDragged) {
            return false
        }
        isFakeDragging = true
        setScrollState(SCROLL_STATE_DRAGGING)
        mLastMotionY = 0f
        mInitialMotionY = mLastMotionY
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain()
        } else {
            mVelocityTracker!!.clear()
        }
        val time = SystemClock.uptimeMillis()
        val ev = MotionEvent.obtain(time, time, MotionEvent.ACTION_DOWN, 0f, 0f, 0)
        mVelocityTracker!!.addMovement(ev)
        ev.recycle()
        mFakeDragBeginTime = time
        return true
    }

    /**
     * End a fake drag of the pager.
     *
     * @see .beginFakeDrag
     * @see .fakeDragBy
     */
    fun endFakeDrag() {
        check(isFakeDragging) { "No fake drag in progress. Call beginFakeDrag first." }

        val velocityTracker = mVelocityTracker
        velocityTracker!!.computeCurrentVelocity(1000, mMaximumVelocity.toFloat())
        val initialVelocity = VelocityTrackerCompat.getYVelocity(
                velocityTracker, mActivePointerId).toInt()
        mPopulatePending = true
        val height = clientHeight
        val scrollY = scrollY
        val ii = infoForCurrentScrollPosition()
        val currentPage = ii!!.position
        val pageOffset = (scrollY.toFloat() / height - ii.offset) / ii.heightFactor
        val totalDelta = (mLastMotionY - mInitialMotionY).toInt()
        val nextPage = determineTargetPage(currentPage, pageOffset, initialVelocity,
                totalDelta)
        setCurrentItemInternal(nextPage, true, true, initialVelocity)
        endDrag()

        isFakeDragging = false
    }

    /**
     * Fake drag by an offset in pixels. You must have called [.beginFakeDrag] first.
     *
     * @param yOffset Offset in pixels to drag by.
     * @see .beginFakeDrag
     * @see .endFakeDrag
     */
    fun fakeDragBy(yOffset: Float) {
        check(isFakeDragging) { "No fake drag in progress. Call beginFakeDrag first." }

        mLastMotionY += yOffset

        val oldScrollY = scrollY.toFloat()
        var scrollY = oldScrollY - yOffset
        val height = clientHeight

        var topBound = height * mFirstOffset
        var bottomBound = height * mLastOffset

        val firstItem = mItems[0]
        val lastItem = mItems[mItems.size - 1]
        if (firstItem.position != 0) {
            topBound = firstItem.offset * height
        }
        if (lastItem.position != adapter!!.count - 1) {
            bottomBound = lastItem.offset * height
        }

        if (scrollY < topBound) {
            scrollY = topBound
        } else if (scrollY > bottomBound) {
            scrollY = bottomBound
        }
        // Don't lose the rounded component
        mLastMotionY += scrollY - scrollY.toInt()
        scrollTo(scrollX, scrollY.toInt())
        pageScrolled(scrollY.toInt())

        // Synthesize an event for the VelocityTracker.
        val time = SystemClock.uptimeMillis()
        val ev = MotionEvent.obtain(mFakeDragBeginTime, time, MotionEvent.ACTION_MOVE,
                0f, mLastMotionY, 0)
        mVelocityTracker!!.addMovement(ev)
        ev.recycle()
    }

    private fun onSecondaryPointerUp(ev: MotionEvent) {
        val pointerIndex = MotionEventCompat.getActionIndex(ev)
        val pointerId = MotionEventCompat.getPointerId(ev, pointerIndex)
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            val newPointerIndex = if (pointerIndex == 0) 1 else 0
            mLastMotionY = MotionEventCompat.getY(ev, newPointerIndex)
            mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex)
            if (mVelocityTracker != null) {
                mVelocityTracker!!.clear()
            }
        }
    }

    private fun endDrag() {
        mIsBeingDragged = false
        mIsUnableToDrag = false

        if (mVelocityTracker != null) {
            mVelocityTracker!!.recycle()
            mVelocityTracker = null
        }
    }

    private fun setScrollingCacheEnabled(enabled: Boolean) {
        if (mScrollingCacheEnabled != enabled) {
            mScrollingCacheEnabled = enabled
            if (USE_CACHE) {
                val size = childCount
                for (i in 0 until size) {
                    val child = getChildAt(i)
                    if (child.visibility != View.GONE) {
                        child.isDrawingCacheEnabled = enabled
                    }
                }
            }
        }
    }

    fun internalCanScrollVertically(direction: Int): Boolean {
        if (adapter == null) {
            return false
        }

        val height = clientHeight
        val scrollY = scrollY
        return if (direction < 0) {
            scrollY > (height * mFirstOffset).toInt()
        } else if (direction > 0) {
            scrollY < (height * mLastOffset).toInt()
        } else {
            false
        }
    }

    /**
     * Tests scrollability within child views of v given a delta of dx.
     *
     * @param v      View to test for horizontal scrollability
     * @param checkV Whether the view v passed should itself be checked for scrollability (true),
     * or just its children (false).
     * @param dy     Delta scrolled in pixels
     * @param x      X coordinate of the active touch point
     * @param y      Y coordinate of the active touch point
     * @return true if child views of v can be scrolled by delta of dx.
     */
    protected fun canScroll(v: View, checkV: Boolean, dy: Int, x: Int, y: Int): Boolean {
        if (v is ViewGroup) {
            val scrollX = v.getScrollX()
            val scrollY = v.getScrollY()
            val count = v.childCount
            // Count backwards - let topmost views consume scroll distance first.
            for (i in count - 1 downTo 0) {
                // TODO: Add versioned support here for transformed views.
                // This will not work for transformed views in Honeycomb+
                val child = v.getChildAt(i)
                if (y + scrollY >= child.top && y + scrollY < child.bottom &&
                        x + scrollX >= child.left && x + scrollX < child.right &&
                        canScroll(child, true, dy, x + scrollX - child.left,
                                y + scrollY - child.top)) {
                    return true
                }
            }
        }

        return checkV && ViewCompat.canScrollVertically(v, -dy)
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        // Let the focused view and/or our descendants get the key first
        return super.dispatchKeyEvent(event) || executeKeyEvent(event)
    }

    /**
     * You can call this function yourself to have the scroll view perform
     * scrolling from a key event, just as if the event had been dispatched to
     * it by the view hierarchy.
     *
     * @param event The key event to execute.
     * @return Return true if the event was handled, else false.
     */
    fun executeKeyEvent(event: KeyEvent): Boolean {
        var handled = false
        if (event.action == KeyEvent.ACTION_DOWN) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_DPAD_LEFT -> handled = arrowScroll(View.FOCUS_LEFT)
                KeyEvent.KEYCODE_DPAD_RIGHT -> handled = arrowScroll(View.FOCUS_RIGHT)
                KeyEvent.KEYCODE_TAB -> if (Build.VERSION.SDK_INT >= 11) {
                    // The focus finder had a bug handling FOCUS_FORWARD and FOCUS_BACKWARD
                    // before Android 3.0. Ignore the tab key on those devices.
                    if (KeyEventCompat.hasNoModifiers(event)) {
                        handled = arrowScroll(View.FOCUS_FORWARD)
                    } else if (KeyEventCompat.hasModifiers(event, KeyEvent.META_SHIFT_ON)) {
                        handled = arrowScroll(View.FOCUS_BACKWARD)
                    }
                }
            }
        }
        return handled
    }

    fun arrowScroll(direction: Int): Boolean {
        var currentFocused: View? = findFocus()
        if (currentFocused === this) {
            currentFocused = null
        } else if (currentFocused != null) {
            var isChild = false
            run {
                var parent = currentFocused!!.parent
                while (parent is ViewGroup) {
                    if (parent === this) {
                        isChild = true
                        break
                    }
                    parent = parent.parent
                }
            }
            if (!isChild) {
                // This would cause the focus search down below to fail in fun ways.
                val sb = StringBuilder()
                sb.append(currentFocused.javaClass.simpleName)
                var parent = currentFocused.parent
                while (parent is ViewGroup) {
                    sb.append(" => ").append(parent.javaClass.simpleName)
                    parent = parent.parent
                }
                Log.e(TAG, "arrowScroll tried to find focus based on non-child " +
                        "current focused view " + sb.toString())
                currentFocused = null
            }
        }

        var handled = false

        val nextFocused = FocusFinder.getInstance().findNextFocus(this, currentFocused,
                direction)
        if (nextFocused != null && nextFocused !== currentFocused) {
            if (direction == View.FOCUS_UP) {
                // If there is nothing to the left, or this is causing us to
                // jump to the right, then what we really want to do is page left.
                val nextTop = getChildRectInPagerCoordinates(mTempRect, nextFocused).top
                val currTop = getChildRectInPagerCoordinates(mTempRect, currentFocused).top
                if (currentFocused != null && nextTop >= currTop) {
                    handled = pageUp()
                } else {
                    handled = nextFocused.requestFocus()
                }
            } else if (direction == View.FOCUS_DOWN) {
                // If there is nothing to the right, or this is causing us to
                // jump to the left, then what we really want to do is page right.
                val nextDown = getChildRectInPagerCoordinates(mTempRect, nextFocused).bottom
                val currDown = getChildRectInPagerCoordinates(mTempRect, currentFocused).bottom
                if (currentFocused != null && nextDown <= currDown) {
                    handled = pageDown()
                } else {
                    handled = nextFocused.requestFocus()
                }
            }
        } else if (direction == View.FOCUS_UP || direction == View.FOCUS_BACKWARD) {
            // Trying to move left and nothing there; try to page.
            handled = pageUp()
        } else if (direction == View.FOCUS_DOWN || direction == View.FOCUS_FORWARD) {
            // Trying to move right and nothing there; try to page.
            handled = pageDown()
        }
        if (handled) {
            playSoundEffect(SoundEffectConstants.getContantForFocusDirection(direction))
        }
        return handled
    }

    private fun getChildRectInPagerCoordinates(outRect: Rect?, child: View?): Rect {
        var outRect = outRect
        if (outRect == null) {
            outRect = Rect()
        }
        if (child == null) {
            outRect.set(0, 0, 0, 0)
            return outRect
        }
        outRect.left = child.left
        outRect.right = child.right
        outRect.top = child.top
        outRect.bottom = child.bottom

        var parent = child.parent
        while (parent is ViewGroup && parent !== this) {
            val group = parent
            outRect.left += group.left
            outRect.right += group.right
            outRect.top += group.top
            outRect.bottom += group.bottom

            parent = group.parent
        }
        return outRect
    }

    internal fun pageUp(): Boolean {
        if (mCurItem > 0) {
            setCurrentItem(mCurItem - 1, true)
            return true
        }
        return false
    }

    internal fun pageDown(): Boolean {
        if (adapter != null && mCurItem < adapter!!.count - 1) {
            setCurrentItem(mCurItem + 1, true)
            return true
        }
        return false
    }

    /**
     * We only want the current page that is being shown to be focusable.
     */
    override fun addFocusables(views: ArrayList<View>, direction: Int, focusableMode: Int) {
        val focusableCount = views.size

        val descendantFocusability = descendantFocusability

        if (descendantFocusability != ViewGroup.FOCUS_BLOCK_DESCENDANTS) {
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                if (child.visibility == View.VISIBLE) {
                    val ii = infoForChild(child)
                    if (ii != null && ii.position == mCurItem) {
                        child.addFocusables(views, direction, focusableMode)
                    }
                }
            }
        }

        // we add ourselves (if focusable) in all cases except for when we are
        // FOCUS_AFTER_DESCENDANTS and there are some descendants focusable.  this is
        // to avoid the focus search finding layouts when a more precise search
        // among the focusable children would be more interesting.
        if (descendantFocusability != ViewGroup.FOCUS_AFTER_DESCENDANTS ||
                // No focusable descendants
                focusableCount == views.size) {
            // Note that we can't call the superclass here, because it will
            // add all views in.  So we need to do the same thing View does.
            if (!isFocusable) {
                return
            }
            if (focusableMode and View.FOCUSABLES_TOUCH_MODE == View.FOCUSABLES_TOUCH_MODE &&
                    isInTouchMode && !isFocusableInTouchMode) {
                return
            }
            views?.add(this)
        }
    }

    /**
     * We only want the current page that is being shown to be touchable.
     */
    override fun addTouchables(views: ArrayList<View>) {
        // Note that we don't call super.addTouchables(), which means that
        // we don't call View.addTouchables().  This is okay because a ViewPager
        // is itself not touchable.
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility == View.VISIBLE) {
                val ii = infoForChild(child)
                if (ii != null && ii.position == mCurItem) {
                    child.addTouchables(views)
                }
            }
        }
    }

    /**
     * We only want the current page that is being shown to be focusable.
     */
    override fun onRequestFocusInDescendants(direction: Int,
                                             previouslyFocusedRect: Rect): Boolean {
        val index: Int
        val increment: Int
        val end: Int
        val count = childCount
        if (direction and View.FOCUS_FORWARD != 0) {
            index = 0
            increment = 1
            end = count
        } else {
            index = count - 1
            increment = -1
            end = -1
        }
        var i = index
        while (i != end) {
            val child = getChildAt(i)
            if (child.visibility == View.VISIBLE) {
                val ii = infoForChild(child)
                if (ii != null && ii.position == mCurItem) {
                    if (child.requestFocus(direction, previouslyFocusedRect)) {
                        return true
                    }
                }
            }
            i += increment
        }
        return false
    }

    override fun dispatchPopulateAccessibilityEvent(event: AccessibilityEvent): Boolean {
        // Dispatch scroll events from this ViewPager.
        if (event.eventType == AccessibilityEventCompat.TYPE_VIEW_SCROLLED) {
            return super.dispatchPopulateAccessibilityEvent(event)
        }

        // Dispatch all other accessibility events from the current page.
        val childCount = childCount
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility == View.VISIBLE) {
                val ii = infoForChild(child)
                if (ii != null && ii.position == mCurItem &&
                        child.dispatchPopulateAccessibilityEvent(event)) {
                    return true
                }
            }
        }

        return false
    }

    override fun generateDefaultLayoutParams(): ViewGroup.LayoutParams {
        return LayoutParams()
    }

    override fun generateLayoutParams(p: ViewGroup.LayoutParams): ViewGroup.LayoutParams {
        return generateDefaultLayoutParams()
    }

    override fun checkLayoutParams(p: ViewGroup.LayoutParams): Boolean {
        return p is LayoutParams && super.checkLayoutParams(p)
    }

    override fun generateLayoutParams(attrs: AttributeSet): ViewGroup.LayoutParams {
        return LayoutParams(context, attrs)
    }

    internal inner class MyAccessibilityDelegate : AccessibilityDelegateCompat() {

        override fun onInitializeAccessibilityEvent(host: View, event: AccessibilityEvent) {
            super.onInitializeAccessibilityEvent(host, event)
            event.className = ViewPager::class.java.name
            val recordCompat = AccessibilityRecordCompat.obtain()
            recordCompat.isScrollable = canScroll()
            if (event.eventType == AccessibilityEventCompat.TYPE_VIEW_SCROLLED && adapter != null) {
                recordCompat.itemCount = adapter!!.count
                recordCompat.fromIndex = mCurItem
                recordCompat.toIndex = mCurItem
            }
        }

        override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfoCompat) {
            super.onInitializeAccessibilityNodeInfo(host, info)
            info.className = ViewPager::class.java.name
            info.isScrollable = canScroll()
            if (internalCanScrollVertically(1)) {
                info.addAction(AccessibilityNodeInfoCompat.ACTION_SCROLL_FORWARD)
            }
            if (internalCanScrollVertically(-1)) {
                info.addAction(AccessibilityNodeInfoCompat.ACTION_SCROLL_BACKWARD)
            }
        }

        override fun performAccessibilityAction(host: View, action: Int, args: Bundle): Boolean {
            if (super.performAccessibilityAction(host, action, args)) {
                return true
            }
            when (action) {
                AccessibilityNodeInfoCompat.ACTION_SCROLL_FORWARD -> {
                    run {
                        if (internalCanScrollVertically(1)) {
                            currentItem = mCurItem + 1
                            return true
                        }
                    }
                    return false
                }
                AccessibilityNodeInfoCompat.ACTION_SCROLL_BACKWARD -> {
                    run {
                        if (internalCanScrollVertically(-1)) {
                            currentItem = mCurItem - 1
                            return true
                        }
                    }
                    return false
                }
            }
            return false
        }

        private fun canScroll(): Boolean {
            return adapter != null && adapter!!.count > 1
        }
    }

    private inner class PagerObserver : DataSetObserver() {
        override fun onChanged() {
            dataSetChanged()
        }

        override fun onInvalidated() {
            dataSetChanged()
        }
    }

    /**
     * Layout parameters that should be supplied for views added to a
     * ViewPager.
     */
    class LayoutParams : ViewGroup.LayoutParams {
        /**
         * true if this view is a decoration on the pager itself and not
         * a view supplied by the adapter.
         */
        var isDecor: Boolean = false

        /**
         * Gravity setting for use on decor views only:
         * Where to position the view page within the overall ViewPager
         * container; constants are defined in [Gravity].
         */
        var gravity: Int = 0

        /**
         * Width as a 0-1 multiplier of the measured pager width
         */
        internal var heightFactor = 0f

        /**
         * true if this view was added during layout and needs to be measured
         * before being positioned.
         */
        internal var needsMeasure: Boolean = false

        /**
         * Adapter position this view is for if !isDecor
         */
        internal var position: Int = 0

        /**
         * Current child index within the ViewPager that this view occupies
         */
        internal var childIndex: Int = 0

        constructor() : super(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT) {}

        constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {

            val a = context.obtainStyledAttributes(attrs, LAYOUT_ATTRS)
            gravity = a.getInteger(0, Gravity.TOP)
            a.recycle()
        }
    }

    internal class ViewPositionComparator : Comparator<View> {
        override fun compare(lhs: View, rhs: View): Int {
            val llp = lhs.layoutParams as LayoutParams
            val rlp = rhs.layoutParams as LayoutParams
            return if (llp.isDecor != rlp.isDecor) {
                if (llp.isDecor) 1 else -1
            } else llp.position - rlp.position
        }
    }

    companion object {

        private val TAG = "ViewPager"
        private val DEBUG = false

        private val USE_CACHE = false

        private val DEFAULT_OFFSCREEN_PAGES = 1
        private val MAX_SETTLE_DURATION = 600 // ms
        private val MIN_DISTANCE_FOR_FLING = 25 // dips

        private val DEFAULT_GUTTER_SIZE = 16 // dips

        private val MIN_FLING_VELOCITY = 400 // dips

        private val LAYOUT_ATTRS = intArrayOf(android.R.attr.layout_gravity)

        private val COMPARATOR = Comparator<ItemInfo> { lhs, rhs -> lhs.position - rhs.position }

        private val sInterpolator = Interpolator { t ->
            var t = t
            t -= 1.0f
            t * t * t * t * t + 1.0f
        }
        /**
         * Sentinel value for no current active pointer.
         * Used by [.mActivePointerId].
         */
        private val INVALID_POINTER = -1

        // If the pager is at least this close to its final position, complete the scroll
        // on touch down and let the user interact with the content inside instead of
        // "catching" the flinging pager.
        private val CLOSE_ENOUGH = 2 // dp

        private val DRAW_ORDER_DEFAULT = 0
        private val DRAW_ORDER_FORWARD = 1
        private val DRAW_ORDER_REVERSE = 2
        private val sPositionComparator = ViewPositionComparator()

        /**
         * Indicates that the pager is in an idle, settled state. The current page
         * is fully in view and no animation is in progress.
         */
        val SCROLL_STATE_IDLE = 0

        /**
         * Indicates that the pager is currently being dragged by the user.
         */
        val SCROLL_STATE_DRAGGING = 1

        /**
         * Indicates that the pager is in the process of settling to a final position.
         */
        val SCROLL_STATE_SETTLING = 2
    }
}
/**
 * Like [View.scrollBy], but scroll smoothly instead of immediately.
 *
 * @param x the number of pixels to scroll by on the X axis
 * @param y the number of pixels to scroll by on the Y axis
 */
