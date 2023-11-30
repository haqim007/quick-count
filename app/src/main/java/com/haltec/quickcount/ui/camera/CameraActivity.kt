package com.haltec.quickcount.ui.camera

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.haltec.quickcount.R
import com.haltec.quickcount.util.createFile
import com.haltec.quickcount.databinding.ActivityCameraBinding
import com.haltec.quickcount.ui.uploadevidence.UploadEvidenceFragment.Companion.CAMERA_RESULT
import com.haltec.quickcount.ui.uploadevidence.UploadEvidenceFragment.Companion.FILE_NAME_EXTRA
import androidx.camera.core.Preview
import com.haltec.quickcount.util.createCacheFile

class CameraActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCameraBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val fileName: String? = intent.getStringExtra(FILE_NAME_EXTRA)
        
        binding.captureImage.setOnClickListener {
            if (fileName != null) {
                takePhoto(fileName)
            } else {
                Toast.makeText(
                    this@CameraActivity,
                    getString(R.string.please_choose_type_name),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        binding.switchCamera.setOnClickListener { startCamera() }
    }
    
    override fun onResume() {
        super.onResume()
        hideSystemUI()
        startCamera()
    }
    
    private fun takePhoto(typeName: String) {
        Toast.makeText(
            this@CameraActivity,
            getString(R.string.please_wait),
            Toast.LENGTH_SHORT
        ).show()
        binding.captureImage.isClickable = false
        binding.switchCamera.isClickable = false
        
        val imageCapture = imageCapture ?: return
        val photoFile = createCacheFile(applicationContext, typeName)
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Toast.makeText(
                        this@CameraActivity,
                        getString(R.string.failed_to_take_a_picture),
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.captureImage.isClickable = true
                    binding.switchCamera.isClickable = true
                }
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    Toast.makeText(
                        this@CameraActivity,
                        getString(R.string.success_taking_picture),
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.captureImage.isClickable = true
                    binding.switchCamera.isClickable = true

                    val intent = Intent()
                    intent.putExtra(PICTURE, photoFile)
                    intent.putExtra(
                        IS_BACK_CAMERA,
                        cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA
                    )
                    setResult(CAMERA_RESULT, intent)
                    finish()
                }
            }
        )
    }
    
    private var imageCapture: ImageCapture? = null
    private var cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    
    private fun startCamera() {
        
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }
            
            imageCapture = ImageCapture
                .Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()
            
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (exc: Exception) {
                Toast.makeText(
                    this@CameraActivity,
                    getString(R.string.failed_to_start_camera),
                    Toast.LENGTH_LONG
                ).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }
    
    private fun hideSystemUI() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    companion object{
        const val PICTURE = "picture"
        const val IS_BACK_CAMERA = "isBackCamera"
    }
}