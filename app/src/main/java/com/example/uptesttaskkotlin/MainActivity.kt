package com.example.uptesttaskkotlin

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.uptesttaskkotlin.databinding.ActivityMainBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var cameraExecutor: ExecutorService
    private var isCameraStarted = false
    private var cameraProvider: ProcessCameraProvider? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        cameraExecutor = Executors.newSingleThreadExecutor()

        setupRecyclerView()
        setupObservers()
        setupListeners()
    }

    private fun setupRecyclerView() {
        val adapter = BarcodeAdapter()
        binding.rvScanHistory.layoutManager = LinearLayoutManager(this)
        binding.rvScanHistory.adapter = adapter
    }

    private fun setupObservers() {
        // Observe history to submit to adapter
        viewModel.scanHistory.observe(this) { history ->
            (binding.rvScanHistory.adapter as? BarcodeAdapter)?.submitList(history)
        }

        // Observe current result text
        viewModel.currentScanResult.observe(this) { result ->
            binding.tvScanResult.text = result
        }

        // Observe scanning state
        viewModel.isScanning.observe(this) { isScanning ->
            if (isScanning) {
                binding.btnToggleScan.text = getString(R.string.stop_scanning)
                if (!isCameraStarted) {
                    checkPermissionsAndStartCamera()
                }
            } else {
                binding.btnToggleScan.text = getString(R.string.start_scanning)
                stopCamera()
            }
        }
    }

    private fun setupListeners() {
        binding.btnToggleScan.setOnClickListener {
            viewModel.toggleScanning()
        }

        binding.btnClearHistory.setOnClickListener {
            viewModel.clearHistory()
        }
    }

    private fun checkPermissionsAndStartCamera() {
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
    }

    private fun startCamera() {
        isCameraStarted = true
        Toast.makeText(this, "Camera mode activated (Stub)", Toast.LENGTH_SHORT).show()
    }

    private fun stopCamera() {
        isCameraStarted = false
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}
