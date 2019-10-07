package com.androidyug.marsrover.common

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View

/**
 * Created by IAMONE on 1/3/2016.
 */
class VerticalSpace(private val mVerticalSpaceHeight: Int) : RecyclerView.ItemDecoration() {


    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State?) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.bottom = mVerticalSpaceHeight


        /* to not insert the space below the
        *  last item
        */
        if (parent.getChildAdapterPosition(view) != parent.adapter.itemCount - 1) {
            outRect.bottom = mVerticalSpaceHeight
        }

    }


}
