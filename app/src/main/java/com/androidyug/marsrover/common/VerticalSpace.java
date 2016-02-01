package com.androidyug.marsrover.common;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by IAMONE on 1/3/2016.
 */
public class VerticalSpace extends RecyclerView.ItemDecoration {

    private final int mVerticalSpaceHeight;

    public VerticalSpace(int verticalHeight){
        this.mVerticalSpaceHeight = verticalHeight;
    }


    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        outRect.bottom = mVerticalSpaceHeight;


        /* to not insert the space below the
        *  last item
        */
        if (parent.getChildAdapterPosition(view)!= parent.getAdapter().getItemCount() -1){
            outRect.bottom = mVerticalSpaceHeight;
        }

    }


}
