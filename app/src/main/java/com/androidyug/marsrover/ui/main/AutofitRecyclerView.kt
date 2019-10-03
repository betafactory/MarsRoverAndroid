/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.androidyug.marsrover.ui.main

import android.content.Context
import android.content.res.TypedArray
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet

class AutofitRecyclerView : RecyclerView {

    private var mLayoutManager: GridLayoutManager? = null
    private var mColumnWidth = -1

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {

        if (attrs != null) {

            val attrsArray = intArrayOf(android.R.attr.columnWidth)

            val array = context.obtainStyledAttributes(attrs, attrsArray)
            mColumnWidth = array.getDimensionPixelSize(0, -1)
            array.recycle()
        }

        mLayoutManager = GridLayoutManager(getContext(), 2)
        layoutManager = mLayoutManager
    }

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {

        super.onMeasure(widthSpec, heightSpec)

        if (mColumnWidth > 0) {

            val spanCount = Math.max(1, measuredWidth / mColumnWidth)
            mLayoutManager!!.spanCount = spanCount
        }
    }
}