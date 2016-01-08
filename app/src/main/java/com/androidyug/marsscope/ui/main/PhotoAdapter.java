package com.androidyug.marsscope.ui.main;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidyug.marsscope.R;
import com.androidyug.marsscope.data.model.Camera;
import com.androidyug.marsscope.data.model.Photo;
import com.androidyug.marsscope.ui.detail.PhotoDetailActivity;
import com.androidyug.marsscope.ui.detail.PhotoDetailFragment;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by IAMONE on 12/26/2015.
 */
public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>{

    List<Photo> listPhoto;
    Context ctx;

    PhotoAdapter(Context context, List<Photo> photos){
        this.listPhoto = photos;
        this.ctx = context;
    }



    public  static class PhotoViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        Photo photo;
        Context context;
        CardView cv;
        ImageView ivPhoto;
        TextView tvCamera;
        TextView tvCameraFullname;

        public PhotoViewHolder(View view) {
            super(view);

            view.setOnClickListener(this);
            cv = (CardView) view.findViewById(R.id.cv_photo);
            ivPhoto = (ImageView) view.findViewById(R.id.iv_photo);
            tvCamera = (TextView) view.findViewById(R.id.tv_camera);
            tvCameraFullname = (TextView) view.findViewById(R.id.tv_camera_fullname);
        }

        public void setItem(Photo p, final Context ctx){
            this.photo = p;
            this.context = ctx;

            Camera c = p.getCamera();
            Picasso.with(ctx).load(p.getImgSrc()).error(R.drawable.ic_action_reload).into(ivPhoto, new Callback() {
                @Override
                public void onSuccess() {
                    // TODO: 1/6/2016  hide the progress
                    Toast.makeText(ctx, "Image loaded", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError() {
                    // TODO: 1/6/2016 show the button to retry
                    Toast.makeText(ctx, "Image not able to load", Toast.LENGTH_SHORT).show();
                }
            });
            tvCameraFullname.setText("Photo taken by " + c.getFullName());
            tvCamera.setText(c.getName());
        }

        @Override
        public void onClick(View v) {
            Toast.makeText(context, "position "+ getLayoutPosition(), Toast.LENGTH_SHORT).show();
            Intent i = new Intent(context, PhotoDetailActivity.class);
            i.putExtra(PhotoDetailFragment.INTENT_PHOTO_DETAIL,photo);
            context.startActivity(i);
        }
    }


    @Override
    public PhotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_view_row, parent, false);
        return new PhotoViewHolder(v);
    }

    @Override
    public void onBindViewHolder(PhotoViewHolder holder, int position) {

        holder.setItem(listPhoto.get(position), ctx);

    }

    @Override
    public int getItemCount() {
        return listPhoto.size();
    }

}
