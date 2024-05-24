package com.example.photoeditor

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
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
import android.Manifest

//
//import org.opencv.android.OpenCVLoader;
//import org.opencv.android.Utils;
//import org.opencv.core.CvType;
//import org.opencv.core.Mat;
//import org.opencv.core.Size;
//import org.opencv.imgproc.Imgproc;

class MainActivity : AppCompatActivity() {

    private lateinit var galleryButton: Button
    private lateinit var cameraButton: Button
    private lateinit var vectorEdit: FloatingActionButton
    private lateinit var cubeButton: FloatingActionButton

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

        galleryButton.setOnClickListener{

            var intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, 1)


//            if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S
//                && ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED ||
//                android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S
//                && ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED){
//
//                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
//                startActivityForResult(intent, REQUEST_CODE)
//            }
//            else{
//                if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S){
//                    ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES), REQUEST_CODE)
//                }
//                else{
//                    ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_CODE)
//                }
//            }
        }

        cameraButton.setOnClickListener{

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

                startActivityForResult(takePictureIntent, 2)
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 2)
            }
        }

        cubeButton.setOnClickListener{

            val intent = Intent(this, Cube::class.java)
            startActivity(intent)
        }

        vectorEdit.setOnClickListener{

            val intent = Intent(this, VectorEditor::class.java)
            startActivity(intent)
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?){
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode==1 && resultCode == RESULT_OK && data != null){
            val uri: Uri? = data.data

            val intent = Intent(this, EditorActivity::class.java).apply {
                setData(uri)
            }
            startActivity(intent)
        }

        if (requestCode == 2 && resultCode == RESULT_OK) {

            val imageBitmap = data?.extras?.get("data") as? Bitmap
            requireNotNull(imageBitmap)

            val uri: Uri? = Saving.saveMediaToStorage(this, imageBitmap)

            val intent = Intent(this, EditorActivity::class.java).apply {
                setData(uri)
            }
            startActivity(intent)
        }
    }
}