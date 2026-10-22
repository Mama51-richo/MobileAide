package com.mobileaide

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var statusText: TextView
    private lateinit var roleText: TextView
    private lateinit var switchCameraButton: Button
    private lateinit var toggleMonitoringButton: Button

    private val roleManager by lazy { RoleManager(applicationContext) }
    private val secureEventStore by lazy { SecureEventStore(applicationContext) }
    private val backendClient by lazy { BackendClient("https://YOUR_HOST/api/events") }
    private val cameraExecutor = Executors.newSingleThreadExecutor()

    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private var monitoringEnabled = false

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startCamera()
        } else {
            statusText.text = "Camera permission is required to run the demo."
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        previewView = PreviewView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }

        statusText = TextView(this).apply {
            text = "Camera ready."
            textSize = 18f
            setPadding(0, 16, 0, 16)
        }

        roleText = TextView(this).apply {
            text = "Role: ${roleManager.getRole().name}"
            textSize = 16f
            setPadding(0, 0, 0, 16)
        }

        switchCameraButton = Button(this).apply {
            text = "Switch camera"
            setOnClickListener { toggleCamera() }
        }

        toggleMonitoringButton = Button(this).apply {
            text = "Start monitoring"
            setOnClickListener { toggleMonitoring() }
        }

        rootLayout.addView(previewView)
        rootLayout.addView(statusText)
        rootLayout.addView(roleText)
        rootLayout.addView(switchCameraButton)
        rootLayout.addView(toggleMonitoringButton)

        setContentView(rootLayout)

        if (hasCameraPermission()) {
            startCamera()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun hasCameraPermission(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview)
                statusText.text = "Live preview active."
            } catch (exception: Exception) {
                statusText.text = "Unable to open camera."
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun toggleCamera() {
        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
            CameraSelector.LENS_FACING_FRONT
        } else {
            CameraSelector.LENS_FACING_BACK
        }
        if (hasCameraPermission()) {
            startCamera()
        }
    }

    private fun toggleMonitoring() {
        monitoringEnabled = !monitoringEnabled
        toggleMonitoringButton.text = if (monitoringEnabled) "Stop monitoring" else "Start monitoring"

        if (monitoringEnabled) {
            statusText.text = "Monitoring enabled. Motion heuristic running."

            val event = EventRecord(
                id = "event-${System.currentTimeMillis()}",
                timestamp = System.currentTimeMillis(),
                type = "MOTION",
                confidence = 0.82f,
                detail = "Demo motion heuristic trigger"
            )

            secureEventStore.saveEvent(event)

            lifecycleScope.launch {
                backendClient.sendEvent(event, token = null)
            }
        } else {
            statusText.text = "Monitoring paused."
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
