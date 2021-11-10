package com.akexorcist.screenshotdetection

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import java.lang.ref.WeakReference

class ScreenshotDetectionDelegate(
    private val activityReference: WeakReference<Activity>,
    private val listener: ScreenshotDetectionListener
) {
    companion object {
        private const val TAG = "ScreenshotDetection"
    }

    private var job: Job? = null

    constructor(
        activity: Activity,
        listener: ScreenshotDetectionListener
    ) : this(WeakReference(activity), listener)

    @Suppress("unused")
    constructor(
        activity: Activity,
        onScreenCaptured: (path: String) -> Unit
    ) : this(
        WeakReference(activity),
        object : ScreenshotDetectionListener {
            override fun onScreenCaptured(path: String) {
                onScreenCaptured(path)
            }
        }
    )

    @FlowPreview
    @ExperimentalCoroutinesApi
    fun startScreenshotDetection() {
        job = CoroutineScope(Dispatchers.Main).launch {
            createContentObserverFlow()
                .debounce(500)
                .collect { uri ->
                    activityReference.get()?.let { activity ->
                        onContentChanged(activity, uri)
                    }
                }
        }
    }

    fun stopScreenshotDetection() {
        job?.cancel()
    }

    @ExperimentalCoroutinesApi
    fun createContentObserverFlow() = channelFlow {
        val contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                uri?.let { _ ->
                    trySend(uri)
                }
            }
        }
        activityReference.get()
            ?.contentResolver
            ?.registerContentObserver(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                true,
                contentObserver
            )
        awaitClose {
            activityReference.get()
                ?.contentResolver
                ?.unregisterContentObserver(contentObserver)
        }
    }

    private fun onContentChanged(context: Context, uri: Uri) {
        if (isReadExternalStoragePermissionGranted()) {
            val path = getFilePathFromContentResolver(context, uri)

            path?.let { p ->
                if (isScreenshotPath(p)) {
                    onScreenCaptured(p)
                }
            }
        }
    }

    private fun onScreenCaptured(path: String) {
        listener.onScreenCaptured(path)
    }

    private fun isScreenshotPath(path: String?): Boolean {
        val lowercasePath = path?.lowercase()
        val screenshotDirectory = getPublicScreenshotDirectoryName()?.lowercase()
        return (screenshotDirectory != null &&
                lowercasePath?.contains(screenshotDirectory) == true) ||
                lowercasePath?.contains("screenshot") == true
    }

    @Suppress("DEPRECATION")
    private fun getPublicScreenshotDirectoryName() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_SCREENSHOTS).name
    } else null

    @Suppress("DEPRECATION")
    private fun getFilePathFromContentResolver(context: Context, uri: Uri): String? {
        try {
            context.contentResolver.query(
                uri,
                arrayOf(
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.DATA
                ),
                null,
                null,
                null
            )?.let { cursor ->
                cursor.moveToFirst()
                val path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
                cursor.close()
                return path
            }
        } catch (e: Exception) {
            Log.w(TAG, e.message ?: "")
        }
        return null
    }

    private fun isReadExternalStoragePermissionGranted(): Boolean {
        return activityReference.get()?.let { activity ->
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        } ?: run {
            false
        }
    }

    interface ScreenshotDetectionListener {
        fun onScreenCaptured(path: String)
    }
}