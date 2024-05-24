package com.example.photoeditor

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.example.photoeditor.Affine.AffineTransformations
import com.example.photoeditor.Filter.ColorFilters
import com.example.photoeditor.Filter.UnsharpMask
import com.example.photoeditor.Retouch.Retouching
import com.example.photoeditor.Translate.Resize
import com.example.photoeditor.Translate.Rotate
import com.example.photoeditor.neuron.FaceDetector
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.slider.Slider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.opencv.android.OpenCVLoader
import org.opencv.objdetect.CascadeClassifier
import java.io.File
import com.example.photoeditor.Saving.Saving


class EditorActivity : AppCompatActivity() {

    private lateinit var rotatingButton: Button
    private lateinit var rotationConfirmButton: Button
    private lateinit var rotationAngleValueText: EditText

    private lateinit var resizingButton: Button
    private lateinit var resizingConfirmButton: Button
    private lateinit var resizingAngleValueText: EditText
    private lateinit var strengthText: EditText
    private lateinit var sizeText: EditText

    private lateinit var faceDetectorButton: Button
    private lateinit var toggleGroup: MaterialButtonToggleGroup
    private lateinit var firstGroupButton: MaterialButton
    private lateinit var secondGroupButton: MaterialButton
    private lateinit var thirdGroupButton: MaterialButton

    private lateinit var toggleColorGroup : MaterialButtonToggleGroup
    private lateinit var firstGroupColorButton: MaterialButton
    private lateinit var secondGroupColorButton: MaterialButton
    private lateinit var thirdGroupColorButton: MaterialButton

//    private lateinit var colorConfirmButton : Button
    private lateinit var blackWhiteConfirmButton : Button
    private lateinit var mozaikConfirmButton : Button
    private lateinit var contrastConfirmButton : Button

    private lateinit var mozaikSlider : Slider
    private lateinit var contrastSlider : Slider

    private lateinit var filtersButton: Button

    private lateinit var affineButton: Button
    private lateinit var firstAffineImage: ImageView
    private lateinit var secondAffineImage: ImageView
    private lateinit var firstAffineChangeButton: Button
    private lateinit var secondAffineChangeButton: Button
    private lateinit var confirmAffineButton: Button
    private lateinit var strengthOfBrushSlider: Slider
    private lateinit var sizeOfBrushSlider: Slider

    private lateinit var retouchButton: Button

    private lateinit var unsharpMasking: Button
    private lateinit var unsharpMaskingConfirmButton: Button

    private lateinit var mainImage: ImageView
    private lateinit var choosePickButton: Button
    private lateinit var saveButton: Button
    private lateinit var colorFilterButton: Button
    private lateinit var faceDetectorConfirmButton: Button
    private lateinit var faceCascade: CascadeClassifier

    private lateinit var newImageBitmap: Bitmap

    private var currAlg: Int = 0
    private var strengthOfBrush: Int = 100
    private var sizeOfBrush: Int = 0
    private var stateOfDetector: Int = 0
    private var stateOfColorDetector = 0

    private var mozaikUserValue = 8
    private var contrastUserValue = 0

    private lateinit var animationView : LottieAnimationView

    private var canStart = false

    private fun changeVisibility(elems: Array<View>, isActive: Boolean){

        if(isActive){
            elems.forEach {elem ->
                elem.visibility = View.VISIBLE
            }
        }
        else{
            elems.forEach {elem ->
                elem.visibility = View.INVISIBLE
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        OpenCVLoader.initDebug()
        setContentView(R.layout.activity_editor)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        rotatingButton = findViewById(R.id.rotationButton)
        rotationConfirmButton = findViewById(R.id.rotationConfirmButton)
        rotationAngleValueText = findViewById(R.id.rotationAngleInput)

        resizingButton = findViewById(R.id.resizingButton)
        resizingConfirmButton = findViewById(R.id.resizingConfirmButton)
        resizingAngleValueText = findViewById(R.id.resizingAngleInput)

        faceDetectorButton = findViewById(R.id.faceDetectorButton)
        filtersButton = findViewById(R.id.filtersButton)
        strengthOfBrushSlider = findViewById(R.id.strengthOfBrushSlider)
        sizeOfBrushSlider = findViewById(R.id.sizeOfBrushSlider)
        affineButton = findViewById(R.id.affineButton)
        firstAffineImage = findViewById(R.id.affineFirst)
        secondAffineImage = findViewById(R.id.affineSecond)
        firstAffineChangeButton = findViewById(R.id.firstAffineChangeButton)
        secondAffineChangeButton = findViewById(R.id.secondAffineChangeButton)
        confirmAffineButton = findViewById(R.id.confirmAffineButton)
        toggleGroup = findViewById(R.id.toggleButton)
        firstGroupButton = findViewById(R.id.firstGroupButton)
        secondGroupButton = findViewById(R.id.secondGroupButton)
        thirdGroupButton = findViewById(R.id.thirdGroupButton)

        toggleColorGroup = findViewById(R.id.toggleColorButton)
        firstGroupColorButton = findViewById(R.id.firstGroupColorButton)
        secondGroupColorButton = findViewById(R.id.secondGroupColorButton)
        thirdGroupColorButton = findViewById(R.id.thirdGroupColorButton)

//        colorConfirmButton = findViewById(R.id.colorConfirmButton)
        blackWhiteConfirmButton = findViewById(R.id.blackWhiteConfirmButton)
        mozaikConfirmButton = findViewById(R.id.mozaikConfirmButton)
        contrastConfirmButton = findViewById(R.id.contrastConfirmButton)

        mozaikSlider = findViewById(R.id.mozaikSlider)
        contrastSlider = findViewById(R.id.contrastSlider)

        retouchButton = findViewById(R.id.retouchButton)

        unsharpMasking = findViewById(R.id.unsharpMasking)
        unsharpMaskingConfirmButton = findViewById(R.id.unsharpMaskingConfirmButton)

        mainImage = findViewById(R.id.mainImage)
        choosePickButton = findViewById(R.id.choosePickButton)
        saveButton = findViewById(R.id.saveButton)
        colorFilterButton = findViewById(R.id.colorFilterButton)
        faceDetectorConfirmButton = findViewById(R.id.faceDetectorConfirmButton)

        animationView = findViewById(R.id.animationView)
        animationView.repeatMode = LottieDrawable.RESTART
        animationView.repeatCount = LottieDrawable.INFINITE

        startAnimation()

        val views = arrayOf<Array<View>>(

            arrayOf<View>(
                mainImage,
                rotationConfirmButton,
                rotationAngleValueText
            ),

            arrayOf<View>(
                mainImage,
                toggleColorGroup,
                firstGroupColorButton,
                secondGroupColorButton,
                thirdGroupColorButton,
                blackWhiteConfirmButton,
                mozaikConfirmButton,
                contrastConfirmButton,
                mozaikSlider,
                contrastSlider
//                colorConfirmButton
//                colorFilterButton
            ),

            arrayOf<View>(
                mainImage,
                resizingConfirmButton,
                resizingAngleValueText),

            arrayOf<View>(
                mainImage,
                faceDetectorConfirmButton,
                toggleGroup,
                firstGroupButton,
                secondGroupButton,
                thirdGroupButton
            ),

            arrayOf<View>(
                mainImage,
                strengthOfBrushSlider,
                sizeOfBrushSlider
            ),

            arrayOf<View>(
                mainImage,
                unsharpMaskingConfirmButton
            ),

            arrayOf<View>(
                mainImage,
                firstAffineChangeButton,
                secondAffineChangeButton,
                confirmAffineButton
            )
        )

        val changeAlgorithmButtons = arrayOf<Button>(
            rotatingButton, filtersButton, resizingButton,
            faceDetectorButton, retouchButton, unsharpMasking, affineButton
        )

        val changeButtonsViews = arrayOf<Array<View>>(

            arrayOf<View>(
                blackWhiteConfirmButton
            ),

            arrayOf<View>(
                mozaikConfirmButton,
                mozaikSlider
            ),

            arrayOf<View>(
                contrastConfirmButton,
                contrastSlider
            )
        )

        for(i in changeAlgorithmButtons.indices){
            changeAlgorithmButtons[i].setOnClickListener(){

                changeVisibility(views[currAlg], false)
                currAlg = i
                changeVisibility(views[currAlg], true)

                if (i != 1) {
                    changeVisibility(changeButtonsViews[0], false)
                    changeVisibility(changeButtonsViews[1], false)
                    changeVisibility(changeButtonsViews[2], false)
                }

                if (i == 1) {

                    if (stateOfColorDetector == 0) {
                        changeVisibility(changeButtonsViews[0], false)
                        changeVisibility(changeButtonsViews[1], false)
                        changeVisibility(changeButtonsViews[2], false)
                    }

                    else if (stateOfColorDetector == 1) {
                        changeVisibility(changeButtonsViews[0], true)
                        changeVisibility(changeButtonsViews[1], false)
                        changeVisibility(changeButtonsViews[2], false)
                    }

                    else if (stateOfColorDetector == 2) {
                        changeVisibility(changeButtonsViews[1], true)
                        changeVisibility(changeButtonsViews[0], false)
                        changeVisibility(changeButtonsViews[2], false)
                    }

                    else if (stateOfColorDetector == 3) {
                        changeVisibility(changeButtonsViews[2], true)
                        changeVisibility(changeButtonsViews[0], false)
                        changeVisibility(changeButtonsViews[1], false)
                    }

                }
            }
        }

        val uri: Uri = intent.data!!

        var bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)

        val maxDimension = bitmap.width.coerceAtLeast(bitmap.height).toFloat() / 2
        val maxSliderValue = maxDimension.coerceAtMost(500.0f)
        sizeOfBrushSlider.valueTo = maxSliderValue
        sizeOfBrushSlider.value = maxSliderValue / 5
        sizeOfBrush = (maxSliderValue / 5).toInt()

        val exif = ExifInterface(contentResolver.openInputStream(uri)!!)
        val orientation: Int =
            exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        lifecycleScope.launch(Dispatchers.Main){
            when (orientation) {

                ExifInterface.ORIENTATION_ROTATE_90 -> bitmap = Rotate.rotate(bitmap,90.0)
                ExifInterface.ORIENTATION_ROTATE_180 -> bitmap = Rotate.rotate(bitmap,180.0)
                ExifInterface.ORIENTATION_ROTATE_270 -> bitmap = Rotate.rotate(bitmap,270.0)
            }

            mainImage.setImageBitmap(bitmap)
            stopAnimation()
        }

//        mainImage.setImageBitmap(bitmap)
        firstAffineImage.setImageBitmap(bitmap)
        secondAffineImage.setImageBitmap(bitmap)

        choosePickButton.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        rotationConfirmButton.setOnClickListener{

            lifecycleScope.launch(Dispatchers.Main) {

                if (canStart) {
                    startAnimation()
                    bitmap = Rotate.rotate(bitmap, -rotationAngleValueText.text.toString().toDouble())
                    mainImage.setImageBitmap(bitmap)
                    stopAnimation()
                }

            }
        }

        resizingConfirmButton.setOnClickListener{

            if (canStart) {
                bitmap = Resize.resize(bitmap, resizingAngleValueText.text.toString().toDouble())
                mainImage.setImageBitmap(bitmap)
            }

        }

        var affineMod = 0
        val firstPoints = mutableListOf<Array<Float>>()
        val secondPoints = mutableListOf<Array<Float>>()

        firstAffineChangeButton.setOnClickListener{

            if (canStart) {
                affineMod = 1
                firstPoints.clear()
            }

        }
        secondAffineChangeButton.setOnClickListener{
            if (canStart) {
                affineMod = 2
                secondPoints.clear()
            }

        }

        confirmAffineButton.setOnClickListener{
            if (canStart) {
                bitmap = AffineTransformations.transform(bitmap, firstPoints, secondPoints)
                mainImage.setImageBitmap(bitmap)
            }
        }

        colorFilterButton.setOnClickListener{
            if (canStart) {
                bitmap = ColorFilters.mozaik(bitmap, 20)
                mainImage.setImageBitmap(bitmap)
            }
        }

        blackWhiteConfirmButton.setOnClickListener{
            if (canStart) {
                bitmap = ColorFilters.blackWhiteFilter(bitmap)
                mainImage.setImageBitmap(bitmap)
            }

        }

        mozaikConfirmButton.setOnClickListener{
            if (canStart) {
                bitmap = ColorFilters.mozaik(bitmap, mozaikUserValue)
                mainImage.setImageBitmap(bitmap)
            }

        }

        contrastConfirmButton.setOnClickListener{
            if (canStart) {
                bitmap = ColorFilters.contrast(bitmap, contrastUserValue)
                mainImage.setImageBitmap(bitmap)
            }
        }

        unsharpMaskingConfirmButton.setOnClickListener{
            if (canStart) {
                Toast.makeText(this, "Данный фильтр работает очень долго...", Toast.LENGTH_SHORT).show()
                bitmap = UnsharpMask.unsharpMaskAlg(bitmap, 1.0)
                mainImage.setImageBitmap(bitmap)
            }
        }

        faceDetectorConfirmButton.setOnClickListener{
            if (canStart) {
                val inputStream = resources.openRawResource(R.raw.haarcascade_frontalface_default)
                val file = File(cacheDir, "haarcascade_frontalface_default.xml")
                inputStream.use { input -> file.outputStream().use { output -> input.copyTo(output) } }
                faceCascade = CascadeClassifier(file.absolutePath)
                val detector = FaceDetector()
                bitmap = detector.processImage(faceCascade, bitmap, stateOfDetector)
                mainImage.setImageBitmap(bitmap)
            }
        }
        strengthOfBrushSlider.addOnChangeListener { slider, value, fromUser ->
            strengthOfBrush = value.toInt()
        }
        sizeOfBrushSlider.addOnChangeListener { slider, value, fromUser ->
            sizeOfBrush = value.toInt()
        }
        mainImage.setOnTouchListener { v, event ->
            if (canStart && (currAlg == 4 || currAlg == 6) && (event.action == MotionEvent.ACTION_MOVE || event.action == MotionEvent.ACTION_DOWN)) {
                val imageView = v as ImageView
                val drawable = imageView.drawable
                val intrinsicWidth = drawable.intrinsicWidth
                val intrinsicHeight = drawable.intrinsicHeight

                val imageWidth = imageView.width
                val imageHeight = imageView.height
                val scaleX = intrinsicWidth.toFloat() / imageWidth
                val scaleY = intrinsicHeight.toFloat() / imageHeight

                val scaledTouchX = event.x * scaleX
                val scaledTouchY = event.y * scaleY

                val paddingLeft = (imageWidth - intrinsicWidth / scaleX) / 2
                val paddingTop = (imageHeight - intrinsicHeight / scaleY) / 2

                val centerX = (scaledTouchX - paddingLeft).toInt()
                val centerY = (scaledTouchY - paddingTop).toInt()

                if(currAlg == 4){
                        val retouching = Retouching(bitmap)
                        bitmap = retouching.startRetouching(centerX, centerY, sizeOfBrush, strengthOfBrush)
                        mainImage.setImageBitmap(bitmap)
                }

                else{

                    if ((affineMod == 1 || affineMod == 2) && event.action == MotionEvent.ACTION_DOWN) {

                        val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)

                        val canvas = Canvas(mutableBitmap)

                        val paint = Paint().apply {
                            color = Color.RED
                            style = Paint.Style.FILL
                        }
                        canvas.drawCircle(centerX.toFloat(), centerY.toFloat(), 15f, paint)

                        mainImage.setImageBitmap(mutableBitmap)
                        mainImage.invalidate()

                        if(affineMod == 1){

                            firstPoints.add(arrayOf(centerX.toFloat(), centerY.toFloat()))

                            if(firstPoints.size == 3){
                                affineMod = 0
                            }
                        }
                        if(affineMod == 2){

                            secondPoints.add(arrayOf(centerX.toFloat(), centerY.toFloat()))

                            if(secondPoints.size == 3){
                                affineMod = 0
                            }
                        }
                    }
                }
            }
            true
        }

        toggleGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (canStart && isChecked) {
                when (checkedId) {
                    R.id.firstGroupButton -> {
                        stateOfDetector = 1
                    }

                    R.id.secondGroupButton -> {
                        stateOfDetector = 2
                    }

                    R.id.thirdGroupButton -> {
                        stateOfDetector = 3
                    }
                }
            }
        }

        mozaikSlider.addOnChangeListener { slider, value, fromUser ->
            mozaikUserValue = value.toInt()
        }
        contrastSlider.addOnChangeListener { slider, value, fromUser ->
            contrastUserValue = value.toInt()
        }

        toggleColorGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (canStart && isChecked) {
                when (checkedId) {
                    R.id.firstGroupColorButton -> {
                        stateOfColorDetector = 1
                        changeVisibility(changeButtonsViews[1], false)
                        changeVisibility(changeButtonsViews[2], false)
                        changeVisibility(changeButtonsViews[0], true)
                    }

                    R.id.secondGroupColorButton -> {
                        stateOfColorDetector = 2
                        changeVisibility(changeButtonsViews[0], false)
                        changeVisibility(changeButtonsViews[2], false)
                        changeVisibility(changeButtonsViews[1], true)
                    }

                    R.id.thirdGroupColorButton -> {
                        stateOfColorDetector = 3
                        changeVisibility(changeButtonsViews[1], false)
                        changeVisibility(changeButtonsViews[0], false)
                        changeVisibility(changeButtonsViews[2], true)
                    }
                }
            }
        }

        saveButton.setOnClickListener{
            if (canStart) {
                val uri: Uri? = Saving.createImageUri(this)
                if (uri != null) {
                    Saving.saveBitmapToUri(this, bitmap, uri)
                }
                Toast.makeText(this, "Сохранено", Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun startAnimation() {
        animationView.playAnimation()
        animationView.visibility = View.VISIBLE
        canStart = false
    }

    private fun stopAnimation() {
        animationView.pauseAnimation()
        animationView.visibility = View.INVISIBLE
        canStart = true
    }
}