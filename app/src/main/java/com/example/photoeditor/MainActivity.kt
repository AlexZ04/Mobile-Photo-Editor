package com.example.photoeditor

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.photoeditor.Filter.VectorEditor
import com.example.photoeditor.Cube.Cube
import com.example.photoeditor.Saving.Saving
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var galleryButton: Button
    private lateinit var cameraButton: Button
    private lateinit var vectorEdit: FloatingActionButton
    private lateinit var cubeButton: FloatingActionButton
    private lateinit var photoUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        galleryButton = findViewById(R.id.upload)
        cameraButton = findViewById(R.id.uploadFromCam)
        vectorEdit = findViewById(R.id.vectorEditorId)
        cubeButton = findViewById(R.id.cubeButton)

        galleryButton.setOnClickListener {

            Toast.makeText(this, "Окунаемся с головой..", Toast.LENGTH_SHORT).show()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                        1
                    )
                } else {
                    val intent =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(intent, 1)
                }
            } else {
                // For versions below Android 13, request READ_EXTERNAL_STORAGE permission
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        1
                    )
                } else {
                    val intent =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(intent, 1)
                }
            }
        }

        cameraButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_MEDIA_IMAGES
                    ) == PackageManager.PERMISSION_GRANTED
                ) {

                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    photoUri = Saving.createImageUri(this)!!
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)

                    startActivityForResult(intent, 2)
                } else {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_MEDIA_IMAGES),
                        2
                    )
                }
            } else {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
                ) {

                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    photoUri = Saving.createImageUri(this)!!
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)

                    startActivityForResult(intent, 2)
                } else {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(
                            Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ),
                        2
                    )
                }
            }
        }

        cubeButton.setOnClickListener {
            val intent = Intent(this, Cube::class.java)
            startActivity(intent)
        }

        vectorEdit.setOnClickListener {
            val intent = Intent(this, VectorEditor::class.java)
            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            val uri: Uri? = data.data
            val intent = Intent(this, EditorActivity::class.java).apply {
                setData(uri)
            }
            startActivity(intent)
        }

        if (requestCode == 2 && resultCode == RESULT_OK) {
            val imageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, photoUri)
            val rotatedBitmap = rotateImageIfRequired(this, imageBitmap, photoUri)
            Saving.saveBitmapToUri(this, rotatedBitmap, photoUri)

            val intent = Intent(this, EditorActivity::class.java).apply {
                setData(photoUri)
            }
            startActivity(intent)
        }
    }

    private fun rotateImageIfRequired(context: Context, img: Bitmap, selectedImage: Uri): Bitmap {
        val input = context.contentResolver.openInputStream(selectedImage) ?: return img
        val exif = ExifInterface(input)
        val orientation =
            exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        input.close()

        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(img, 90)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(img, 180)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(img, 270)
            else -> img
        }
    }

    private fun rotateImage(img: Bitmap, degree: Int): Bitmap {
        val matrix = android.graphics.Matrix()
        matrix.postRotate(degree.toFloat())
        return Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
    }
}
