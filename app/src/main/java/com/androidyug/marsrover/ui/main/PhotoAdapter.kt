package com.androidyug.marsrover.ui.main

import android.content.Context
import android.content.Intent
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast

import com.androidyug.marsrover.R
import com.androidyug.marsrover.data.model.Camera
import com.androidyug.marsrover.data.model.Photo
import com.androidyug.marsrover.ui.detail.PhotoDetailActivity
import com.androidyug.marsrover.ui.detail.PhotoDetailFragment
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

/**
 * @author Nitin Anand (nitinnatural@gmail.com)
 */
class PhotoAdapter internal constructor(private var ctx: Context, private var listPhoto: List<Photo>) : RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {


    class PhotoViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        private lateinit var photo: Photo
        private lateinit var context: Context
        private var cv: CardView
        private var ivPhoto: ImageView
        private var tvCamera: TextView
        private var tvCameraFullname: TextView
        private var ibReload: ImageButton
        private var isPhotoLoaded: Boolean = false
        private var lineProgressBar: ProgressBar

        init {

            view.setOnClickListener(this)
            cv = view.findViewById<View>(R.id.cv_photo) as CardView
            ivPhoto = view.findViewById<View>(R.id.iv_photo) as ImageView
            tvCamera = view.findViewById<View>(R.id.tv_camera) as TextView
            tvCameraFullname = view.findViewById<View>(R.id.tv_camera_fullname) as TextView
            ibReload = view.findViewById<View>(R.id.ib_reload) as ImageButton
            lineProgressBar = view.findViewById<View>(R.id.line_progress) as ProgressBar
            ibReload.setOnClickListener(this)
        }

        fun setItem(p: Photo, ctx: Context) {
            this.photo = p
            this.context = ctx

            val c = p.getCamera()

            loadPhotoIntoView()

            tvCameraFullname.text = "Photo taken by " + c!!.getFullName()
            tvCamera.text = c.getName()
        }

        private fun hideProgress() {
            if (lineProgressBar != null) {
                lineProgressBar.visibility = View.GONE
            }
        }

        private fun showProgress() {
            lineProgressBar.visibility = View.VISIBLE
        }

        private fun loadPhotoIntoView() {
            showProgress()
            Picasso.with(context).load(photo.getImgSrc()).into(ivPhoto, object : Callback {
                override fun onSuccess() {
                    // TODO: 1/6/2016  hide the progress

                    isPhotoLoaded = true
                    ibReload.visibility = View.GONE
                    hideProgress()
                    //                    Toast.makeText(context, "Image loaded", Toast.LENGTH_SHORT).show();
                }

                override fun onError() {

                    hideProgress()
                    isPhotoLoaded = false
                    ibReload.visibility = View.VISIBLE
                    //                    Toast.makeText(context, "Image not able to load", Toast.LENGTH_SHORT).show();
                }
            })

        }

        override fun onClick(v: View) {

            when (v.id) {
                R.id.cv_photo -> {

                    if (!isPhotoLoaded) {
                        Toast.makeText(context, "please let the photo to load", Toast.LENGTH_SHORT).show()
                        return
                    }

                    Toast.makeText(context, "position $layoutPosition", Toast.LENGTH_SHORT).show()
                    val i = Intent(context, PhotoDetailActivity::class.java)
                    i.putExtra(PhotoDetailFragment.INTENT_PHOTO_DETAIL, photo)
                    context.startActivity(i)
                }

                R.id.ib_reload -> {
                    ibReload.visibility = View.GONE
                    loadPhotoIntoView()
                }
            }


        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.list_view_row, parent, false)
        return PhotoViewHolder(v)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {

        holder.setItem(listPhoto[position], ctx)

    }

    override fun getItemCount(): Int {
        return listPhoto.size
    }

}
