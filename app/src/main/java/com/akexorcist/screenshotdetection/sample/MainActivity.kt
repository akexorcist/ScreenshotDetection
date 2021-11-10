package com.akexorcist.screenshotdetection.sample

import android.os.Bundle
import com.akexorcist.screenshotdetection.sample.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar

class MainActivity : ScreenshotDetectionActivity() {
    private val binding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }

    override fun onScreenCaptured(path: String) {
        Snackbar.make(binding.root, path, Snackbar.LENGTH_SHORT).show()
    }
}
