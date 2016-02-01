package com.androidyug.marsrover.ui.main;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.androidyug.marsrover.R;
import com.androidyug.marsrover.data.model.Camera;
import com.androidyug.marsrover.data.model.Photo;
import com.androidyug.marsrover.ui.detail.PhotoDetailActivity;
import com.androidyug.marsrover.ui.detail.PhotoDetailFragment;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * @author Nitin Anand (nitinnatural@gmail.com)
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
        ImageButton ibReload;
        Boolean isPhotoLoaded = false;
        ProgressBar lineProgressBar;

        public PhotoViewHolder(View view) {
            super(view);

            view.setOnClickListener(this);
            cv = (CardView) view.findViewById(R.id.cv_photo);
            ivPhoto = (ImageView) view.findViewById(R.id.iv_photo);
            tvCamera = (TextView) view.findViewById(R.id.tv_camera);
            tvCameraFullname = (TextView) view.findViewById(R.id.tv_camera_fullname);
            ibReload = (ImageButton) view.findViewById(R.id.ib_reload);
            lineProgressBar = (ProgressBar) view.findViewById(R.id.line_progress);
            ibReload.setOnClickListener(this);
        }

        public void setItem(Photo p, final Context ctx){
            this.photo = p;
            this.context = ctx;

            Camera c = p.getCamera();

            loadPhotoIntoView();

            tvCameraFullname.setText("Photo taken by " + c.getFullName());
            tvCamera.setText(c.getName());
        }

        void hideProgress(){
            if (lineProgressBar!=null){
                lineProgressBar.setVisibility(View.GONE);
            }
        }

        void showProgress(){
            lineProgressBar.setVisibility(View.VISIBLE);
        }

        void loadPhotoIntoView(){
            showProgress();
            Picasso.with(context).load(photo.getImgSrc()).into(ivPhoto, new Callback() {
                @Override
                public void onSuccess() {
                    // TODO: 1/6/2016  hide the progress

                    isPhotoLoaded = true;
                    ibReload.setVisibility(View.GONE);
                    hideProgress();
//                    Toast.makeText(context, "Image loaded", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError() {

                    hideProgress();
                    isPhotoLoaded = false;
                    ibReload.setVisibility(View.VISIBLE);
//                    Toast.makeText(context, "Image not able to load", Toast.LENGTH_SHORT).show();
                }
            });

        }

        @Override
        public void onClick(View v) {

            int id = v.getId();

            switch (id){
                case R.id.cv_photo:

                    if (!isPhotoLoaded){
                        Toast.makeText(context, "please let the photo to load", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Toast.makeText(context, "position "+ getLayoutPosition(), Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(context, PhotoDetailActivity.class);
                    i.putExtra(PhotoDetailFragment.INTENT_PHOTO_DETAIL, photo);
                    context.startActivity(i);
                    break;

                case R.id.ib_reload:
                    ibReload.setVisibility(View.GONE);
                   loadPhotoIntoView();
                    break;

            }


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
