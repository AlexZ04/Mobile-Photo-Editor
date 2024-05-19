package com.example.photoeditor

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import com.example.photoeditor.R.id.cubeButton
import java.net.URI
//
//import org.opencv.android.OpenCVLoader;
//import org.opencv.android.Utils;
//import org.opencv.core.CvType;
//import org.opencv.core.Mat;
//import org.opencv.core.Size;
//import org.opencv.imgproc.Imgproc;

class MainActivity : AppCompatActivity() {

    val REQUEST_CODE = 1
    private lateinit var button: Button
    private lateinit var vectorEdit: Button
    private lateinit var cubeButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        button = findViewById(R.id.upload)
        vectorEdit = findViewById(R.id.vectorEditorId)
        cubeButton = findViewById(R.id.cubeButton)

        button.setOnClickListener{

            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, REQUEST_CODE)
        }

        cubeButton.setOnClickListener{

            val intent = Intent(this, Cube::class.java)
            startActivity(intent)
        }
        button.setOnClickListener{

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

            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, REQUEST_CODE)
        }

        vectorEdit.setOnClickListener{

            val intent = Intent(this, VectorEditor::class.java)
            startActivity(intent)
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?){
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==REQUEST_CODE && resultCode == RESULT_OK && data != null){
            val uri: Uri? = data.data

            val intent = Intent(this, EditorActivity::class.java).apply {
                setData(uri)
            }
            startActivity(intent)
        }
    }
}