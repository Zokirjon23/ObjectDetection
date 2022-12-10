package com.example.objectdetection

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.widget.Toast
import androidx.camera.core.CameraProvider
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.objectdetection.databinding.ActivityMainBinding
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.ObjectDetector
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions
import com.permissionx.guolindev.PermissionX

class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding? = null

    private lateinit var cameraProviderFuture : ListenableFuture<ProcessCameraProvider>
    private lateinit var objectDetector : ObjectDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        PermissionX.init(this)
            .permissions(Manifest.permission.CAMERA)
            .request { allGranted, _, deniedList ->
                if (allGranted) {

                    cameraProviderFuture = ProcessCameraProvider.getInstance(this)


                    val localModel =
                        LocalModel.Builder().setAssetFilePath("object_detection.tflite").build()

                    val customObjectDetectorOptions =
                        CustomObjectDetectorOptions.Builder(localModel)
                            .setDetectorMode(CustomObjectDetectorOptions.STREAM_MODE)
                            .enableClassification()
                            .setClassificationConfidenceThreshold(0.5f)
                            .setMaxPerObjectLabelCount(3)
                            .build()

                    objectDetector =
                        ObjectDetection.getClient(customObjectDetectorOptions)
                    cameraProviderFuture.addListener({
                        bindCamera()
                    }, ContextCompat.getMainExecutor(this))
                } else {
                    Toast.makeText(
                        this,
                        "These permissions are denied: $deniedList",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun bindCamera() {
        // Camera provider is now guaranteed to be available
        val cameraProvider = cameraProviderFuture.get()

        // Set up the preview use case to display camera preview.
        val preview = Preview.Builder().build()

        preview.setSurfaceProvider(
            binding!!.preview.surfaceProvider
        )

        // Set up the capture use case to allow users to take photos.
        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(Size(1280, 720))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this)) { imageProxy ->
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val image =
                    InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                objectDetector.process(image)
                    .addOnSuccessListener { detectedObjects ->
                        binding!!.overlayView.clear()
                        binding!!.overlayView.setResults(
                            detectedObjects
                        )

                        binding!!.overlayView.invalidate()
//                        for (detectedObject in detectedObjects) {
//
//                            if (binding!!.parent.childCount > 1) binding!!.parent.removeViewAt(1)
//
//                            val element = Draw(
//                                this,
//                                detectedObject.boundingBox,
//                                detectedObject.labels.firstOrNull()?.text ?: "Unknow",
//                                detectedObject.labels.first().confidence.apply {
//                                    times(this)
//                                }.toString()
//                            )
//
//                            binding!!.parent.addView(element)
//                        }
                        imageProxy.close()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                        Log.d("AAA", "detect: ${e.message}")
                        imageProxy.close()
                    }
            }
        }


        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()


        cameraProvider.bindToLifecycle(
            this as LifecycleOwner,
            cameraSelector,
            imageAnalysis,
            preview
        )

    }
}
