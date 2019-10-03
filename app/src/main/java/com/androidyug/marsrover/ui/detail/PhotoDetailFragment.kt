package com.androidyug.marsrover.ui.detail

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.graphics.Matrix
import android.graphics.PointF
import android.widget.Toast

import butterknife.BindView
import com.androidyug.marsrover.R
import com.androidyug.marsrover.data.model.Photo
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

import butterknife.ButterKnife

/**
 * Created by IAMONE on 12/26/2015.
 */
class PhotoDetailFragment : Fragment(), View.OnClickListener {

    private var mBitmap: Bitmap? = null

    // These matrices will be used to scale points of the image
    internal var matrix = Matrix()
    internal var savedMatrix = Matrix()
    internal var mode = NONE

    // these PointF objects are used to record the point(s) the user is touching
    internal var start = PointF()
    internal var mid = PointF()
    internal var oldDist = 1f

    @BindView(R.id.iv_detail)
    lateinit var ivDetail: ImageView

    @BindView(R.id.toolbar_detail)
    lateinit var mToolbarDetail: Toolbar

    @BindView(R.id.ib_save)
    lateinit var ibSave: ImageButton

    @BindView(R.id.ib_share)
    lateinit var ibShare: ImageButton

    @BindView(R.id.ib_back)
    lateinit var ibBack: ImageButton

    private lateinit var mPhoto: Photo

    /* Checks if external storage is available for read and write */
    val isExternalStorageWritable: Boolean
        get() {
            val state = Environment.getExternalStorageState()
            return Environment.MEDIA_MOUNTED == state
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_photo_detail, container, false)
        ButterKnife.bind(this, v)
        mPhoto = activity.intent.getSerializableExtra(INTENT_PHOTO_DETAIL) as Photo
        initListener()


        val target = object : Target {

            override fun onPrepareLoad(drawable: Drawable) {
                ivDetail.setBackgroundResource(android.R.color.white)
                //spinner.setVisibility(View.VISIBLE);
            }

            override fun onBitmapLoaded(photo: Bitmap, from: Picasso.LoadedFrom) {
                ivDetail.setImageBitmap(photo)
                //spinner.setVisibility(View.GONE);

                // TODO: 1/6/2016 set the visibility of save button to show
                // btnSave.enable();

                // and initilize the bitmap variable so that it can be saved on click
                mBitmap = photo
            }

            override fun onBitmapFailed(drawable: Drawable) {
                ivDetail.setBackgroundResource(android.R.color.white)
            }
        }

        Picasso.with(activity).load(mPhoto.getImgSrc()).into(target)



        return v
    }


    internal fun initListener() {
        //        ivDetail.setOnTouchListener(this);
        ibSave.setOnClickListener(this)
        ibShare.setOnClickListener(this)
        ibBack.setOnClickListener(this)
    }

    internal fun savePhotoToGallery(bitmap: Bitmap) {

        // TODO: 1/6/2016 save to MarsRover folder in the gallery


        val filename = mPhoto.getRover()!!.getName() + "_" + mPhoto.getId() + "_" + mPhoto.getEarthDate() // curiosity_20123_2015-12-1
        val path = Environment.getExternalStorageDirectory().toString()
        var fOut: OutputStream? = null
        val file = File(path, filename) // the File to save to


        try {
            fOut = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut) // bmp is your Bitmap instance
            MediaStore.Images.Media.insertImage(activity.contentResolver, file.absolutePath, file.name, file.name)
            Toast.makeText(activity, "photo saved to gallery", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                if (fOut != null) {
                    fOut.flush()
                    fOut.close()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }


    }


    //    @Override
    //    public boolean onTouch(View v, MotionEvent event) {
    //        ImageView view = (ImageView) v;
    //        view.setScaleType(ImageView.ScaleType.MATRIX);
    //        float scale;
    //
    //        dumpEvent(event);
    //        // Handle touch events here...
    //
    //        switch (event.getAction() & MotionEvent.ACTION_MASK) {
    //            case MotionEvent.ACTION_DOWN:   // first finger down only
    //                savedMatrix.set(matrix);
    //                start.set(event.getX(), event.getY());
    //                Log.d(TAG, "mode=DRAG"); // write to LogCat
    //                mode = DRAG;
    //                break;
    //
    //            case MotionEvent.ACTION_UP: // first finger lifted
    //
    //            case MotionEvent.ACTION_POINTER_UP: // second finger lifted
    //
    //                mode = NONE;
    //                Log.d(TAG, "mode=NONE");
    //                break;
    //
    //            case MotionEvent.ACTION_POINTER_DOWN: // first and second finger down
    //
    //                oldDist = spacing(event);
    //                Log.d(TAG, "oldDist=" + oldDist);
    //                if (oldDist > 5f) {
    //                    savedMatrix.set(matrix);
    //                    midPoint(mid, event);
    //                    mode = ZOOM;
    //                    Log.d(TAG, "mode=ZOOM");
    //                }
    //                break;
    //
    //            case MotionEvent.ACTION_MOVE:
    //
    //                if (mode == DRAG) {
    //                    matrix.set(savedMatrix);
    //                    matrix.postTranslate(event.getX() - start.x, event.getY() - start.y); // create the transformation in the matrix  of points
    //                } else if (mode == ZOOM) {
    //                    // pinch zooming
    //                    float newDist = spacing(event);
    //                    Log.d(TAG, "newDist=" + newDist);
    //                    if (newDist > 5f) {
    //                        matrix.set(savedMatrix);
    //                        scale = newDist / oldDist; // setting the scaling of the
    //                        // matrix...if scale > 1 means
    //                        // zoom in...if scale < 1 means
    //                        // zoom out
    //                        matrix.postScale(scale, scale, mid.x, mid.y);
    //                    }
    //                }
    //                break;
    //        }
    //
    //        view.setImageMatrix(matrix); // display the transformation on screen
    //
    //        return true; // indicate event was handled
    //    }
    //
    //
    //     /*
    //     * --------------------------------------------------------------------------
    //     * Method: spacing Parameters: MotionEvent Returns: float Description:
    //     * checks the spacing between the two fingers on touch
    //     * ----------------------------------------------------
    //     */
    //
    //    private float spacing(MotionEvent event)
    //    {
    //        float x = event.getX(0) - event.getX(1);
    //        float y = event.getY(0) - event.getY(1);
    //        return (float) Math.sqrt(x * x + y * y);
    //    }
    //
    //    /*
    //     * --------------------------------------------------------------------------
    //     * Method: midPoint Parameters: PointF object, MotionEvent Returns: void
    //     * Description: calculates the midpoint between the two fingers
    //     * ------------------------------------------------------------
    //     */
    //
    //    private void midPoint(PointF point, MotionEvent event)
    //    {
    //        float x = event.getX(0) + event.getX(1);
    //        float y = event.getY(0) + event.getY(1);
    //        point.set(x / 2, y / 2);
    //    }
    //
    //    /** Show an event in the LogCat view, for debugging */
    //    private void dumpEvent(MotionEvent event)
    //    {
    //        String names[] = { "DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE","POINTER_DOWN", "POINTER_UP", "7?", "8?", "9?" };
    //        StringBuilder sb = new StringBuilder();
    //        int action = event.getAction();
    //        int actionCode = action & MotionEvent.ACTION_MASK;
    //        sb.append("event ACTION_").append(names[actionCode]);
    //
    //        if (actionCode == MotionEvent.ACTION_POINTER_DOWN || actionCode == MotionEvent.ACTION_POINTER_UP)
    //        {
    //            sb.append("(pid ").append(action >> MotionEvent.ACTION_POINTER_ID_SHIFT);
    //            sb.append(")");
    //        }
    //
    //        sb.append("[");
    //        for (int i = 0; i < event.getPointerCount(); i++)
    //        {
    //            sb.append("#").append(i);
    //            sb.append("(pid ").append(event.getPointerId(i));
    //            sb.append(")=").append((int) event.getX(i));
    //            sb.append(",").append((int) event.getY(i));
    //            if (i + 1 < event.getPointerCount())
    //                sb.append(";");
    //        }
    //
    //        sb.append("]");
    //        Log.d("Touch Events ---------", sb.toString());
    //    }

    override fun onClick(v: View) {

        val id = v.id
        when (id) {
            R.id.ib_save -> {

                if (mBitmap == null) {
                    Toast.makeText(activity, "photo is not downloaded", Toast.LENGTH_SHORT).show()
                    return
                }

                if (mBitmap != null) {
                    savePhotoToGallery(mBitmap!!)
                }
            }
            R.id.ib_share -> {
                if (mBitmap == null) {
                    Toast.makeText(activity, "photo is not downloaded", Toast.LENGTH_SHORT).show()
                    return
                }

                if (mBitmap != null) {
                    onShareItem()
                }
            }
            R.id.ib_back -> activity.finish()
        }
    }


    // Can be triggered by a view event such as a button press
    fun onShareItem() {
        // Get access to bitmap image from view
        val ivImage = ivDetail
        // Get access to the URI for the bitmap
        val bmpUri = getLocalBitmapUri(ivImage!!)
        if (bmpUri != null) {
            // Construct a ShareIntent with link to image
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri)
            val toShareText = "#MarsRover " + "#" + mPhoto.getRover()!!.getName() + " #" + mPhoto.getEarthDate() //#MarsRover #Spirit #2015-12-21
            shareIntent.putExtra(Intent.EXTRA_TEXT, toShareText)
            shareIntent.type = "image/*"
            // Launch sharing dialog for image
            startActivity(Intent.createChooser(shareIntent, "Share Image"))
        } else {
            // ...sharing failed, handle error
        }
    }

    // Returns the URI path to the Bitmap displayed in specified ImageView
    fun getLocalBitmapUri(imageView: ImageView): Uri? {
        // Extract Bitmap from ImageView drawable
        val drawable = imageView.drawable
        var bmp: Bitmap? = null
        if (drawable is BitmapDrawable) {
            bmp = (imageView.drawable as BitmapDrawable).bitmap
        } else {
            return null
        }
        // Store image to default external storage directory
        var bmpUri: Uri? = null
        try {
            val file = File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS), "share_image_" + System.currentTimeMillis() + ".png")
            file.parentFile.mkdirs()
            val out = FileOutputStream(file)
            bmp!!.compress(Bitmap.CompressFormat.PNG, 90, out)
            out.close()
            bmpUri = Uri.fromFile(file)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return bmpUri
    }

    companion object {

        const val INTENT_PHOTO_DETAIL = "intent_photo_detail"

        private const val TAG = "Touch"
        private const val MIN_ZOOM = 1f
        private const val MAX_ZOOM = 1f

        // The 3 states (events) which the user is trying to perform
        private const val NONE = 0
        private const val DRAG = 1
        private const val ZOOM = 2
    }


}
