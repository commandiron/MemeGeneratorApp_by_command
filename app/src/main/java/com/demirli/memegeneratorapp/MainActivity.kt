package com.demirli.memegeneratorapp

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.TypedValue
import android.view.*
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.marginLeft
import androidx.core.view.marginTop
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.demirli.memegeneratorapp.Adapter.ColorAdapter
import com.demirli.memegeneratorapp.utils.BitmapUtils
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import com.squareup.picasso.Picasso
import com.squareup.picasso.RequestCreator
import com.squareup.picasso.Target
import ja.burhanrashid52.photoeditor.OnSaveBitmap
import ja.burhanrashid52.photoeditor.PhotoEditor
import kotlinx.android.synthetic.main.content_main.*
import java.io.OutputStream
import java.lang.Exception

class MainActivity : AppCompatActivity(), ColorAdapter.ColorAdapterClickListener {

    val SELECT_GALLERY_PERMISSION = 1000

    init {
        System.loadLibrary("NativeImageProcessor")
    }

    var colorSelected: Int = Color.parseColor("#000000")
    private var colorAdapter: ColorAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        recycler_color.setHasFixedSize(true)
        recycler_color.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        colorAdapter = ColorAdapter(this, this)
        recycler_color.adapter = colorAdapter

        add_btn.setOnClickListener {
            dragableBox.visibility = View.VISIBLE
            add_btn.visibility = View.GONE
            done_btn.visibility = View.VISIBLE

            dragable_Text.setTextColor(colorSelected)
            dragable_Text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
            dragable_Text.setText(edt_add_text.text.toString())
        }

        done_btn.setOnClickListener {
            dragableBox.visibility = View.GONE
            add_btn.visibility = View.VISIBLE
            done_btn.visibility = View.GONE

            val x = dragableBox.getCoordinates().first
            val y = dragableBox.getCoordinates().second

            drawing_view.addTextOnBitmap(edt_add_text.text.toString(),60f,colorSelected, x, y)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val id = item.itemId

        if(id == R.id.action_open){
            openImageFromGallery()
            return true
        }else if(id == R.id.action_save){
            saveImageToGallery()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun openImageFromGallery() {
        Dexter.withContext(this)
            .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            .withListener(object: PermissionListener{

                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                    val intent = Intent(Intent.ACTION_PICK)
                    intent.type = "image/"
                    startActivityForResult(intent,  SELECT_GALLERY_PERMISSION)
                }

                override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                    Toast.makeText(applicationContext, "Permision denied", Toast.LENGTH_SHORT).show()
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: PermissionRequest?,
                    p1: PermissionToken?
                ) {
                    p1!!.continuePermissionRequest()
                }

            }).check()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == Activity.RESULT_OK && requestCode == SELECT_GALLERY_PERMISSION){

            val bitmap = BitmapUtils.getBitmapFromGallery(this, data!!.data!!, 800, 800 )
            drawing_view.changeBitmap(bitmap)
        }
    }

    private fun saveImageToGallery() {

        Dexter.withContext(this)
            .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .withListener(object: PermissionListener{

                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {

                    saveImage(drawing_view.getLastBitmap(),this@MainActivity,"Meme Folder")
                    Toast.makeText(applicationContext, "Image saved to gallery", Toast.LENGTH_SHORT).show()
                }

                override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                    Toast.makeText(applicationContext, "Permision denied", Toast.LENGTH_SHORT).show()
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: PermissionRequest?,
                    p1: PermissionToken?
                ) {
                    p1!!.continuePermissionRequest()
                }

            }).check()
    }

    override fun onColorItemSelected(color: Int) {
        colorSelected = color
    }


    //SAVE IMAGE

    fun saveImage(bitmap: Bitmap, context: Context, folderName: String) {
        if (android.os.Build.VERSION.SDK_INT >= 29) {
            val values = contentValues()
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/" + folderName)
            values.put(MediaStore.Images.Media.IS_PENDING, true)
            // RELATIVE_PATH and IS_PENDING are introduced in API 29.

            val uri: Uri? =
                context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            if (uri != null) {
                saveImageToStream(bitmap, context.contentResolver.openOutputStream(uri))
                values.put(MediaStore.Images.Media.IS_PENDING, false)
                context.contentResolver.update(uri, values, null, null)
            }
        }
    }
    fun contentValues() : ContentValues {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        return values
    }

    fun saveImageToStream(bitmap: Bitmap, outputStream: OutputStream?) {
        if (outputStream != null) {
            try {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}


