package com.akexorcist.screenshotdetection

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.ContentResolverCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import java.util.*

class ScreenshotDetectionDelegate(
    private val activityReference: WeakReference<Activity>,
    private val listener: ScreenshotDetectionListener
) {
    companion object {
        private const val TAG = "ScreenshotDetection"
    }

    constructor(
        activity: Activity,
        listener: ScreenshotDetectionListener
    ) : this(WeakReference(activity), listener)

    constructor(
        activity: Activity,
        onScreenCaptured: (path: String) -> Unit,
        onScreenCapturedWithDeniedPermission: () -> Unit
    ) : this(
        WeakReference(activity),
        object : ScreenshotDetectionListener {
            override fun onScreenCaptured(path: String) {
                onScreenCaptured(path)
            }

            override fun onScreenCapturedWithDeniedPermission() {
                onScreenCapturedWithDeniedPermission()
            }
        }
    )

    var job: Job? = null

    @FlowPreview
    @ExperimentalCoroutinesApi
    fun startScreenshotDetection() {
        job = GlobalScope.launch(Dispatchers.Main) {
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
    fun createContentObserverFlow() = channelFlow<Uri> {
        val contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                uri?.let { _ ->
                    offer(uri)
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
        } else {
            onScreenCapturedWithDeniedPermission()
        }
    }

    private fun onScreenCaptured(path: String) {
        listener.onScreenCaptured(path)
    }

    private fun onScreenCapturedWithDeniedPermission() {
        listener.onScreenCapturedWithDeniedPermission()
    }

    private fun isScreenshotPath(path: String?): Boolean {
        return path != null && path.toLowerCase(Locale.getDefault()).contains("screenshot")
    }

    private fun getFilePathFromContentResolver(context: Context, uri: Uri): String? {
        try {
            val dataColumn = "_data"
            context.contentResolver.query(
                uri,
                arrayOf(MediaStore.Images.Media.DISPLAY_NAME, dataColumn),
                null,
                null,
                null
            )?.let { cursor ->
                cursor.moveToFirst()
                val path = cursor.getString(cursor.getColumnIndex(dataColumn))
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

        fun onScreenCapturedWithDeniedPermission()
    }
}