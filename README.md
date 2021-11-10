[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-Screenshot%20Detection-brightgreen.svg?style=flat)](https://android-arsenal.com/details/1/8241)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.akexorcist/screenshot-detection/badge.svg)](https://search.maven.org/artifact/com.akexorcist/screenshot-detection) 
![Minimum SDK Version](https://img.shields.io/badge/minSdkVersion-16-brightgreen) 
[![Workflow Status](https://github.com/akexorcist/ScreenshotDetection/actions/workflows/android.yml/badge.svg)](https://github.com/akexorcist/ScreenshotDetection/actions)

# Android-ScreenshotDetection

Screenshot Detection Library

# Download

Since version 1.0.1 will [move from JCenter to MavenCentral](https://developer.android.com/studio/build/jcenter-migration)
```groovy
// build.gradle (project)
allprojects {
    repositories {
        mavenCentral()
        /* ... */
    }
}
```

**Gradle**
```
implementation 'com.akexorcist:screenshot-detection:1.0.2'
```

# Permission in this library

This library has declared the permission to read the external storage. So you need to handle the runtime permission by yourself. If not, app will not crash and screenshot detection still work but no file path.

# Usage

Implement the library to your activity or your base activity.

```kotlin
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import com.akexorcist.screenshotdetection.ScreenshotDetectionDelegate

open class ScreenshotDetectionActivity : AppCompatActivity(), ScreenshotDetectionDelegate.ScreenshotDetectionListener {
    private val screenshotDetectionDelegate = ScreenshotDetectionDelegate(this, this)

    override fun onStart() {
        super.onStart()
        screenshotDetectionDelegate.startScreenshotDetection()
    }

    override fun onStop() {
        super.onStop()
        screenshotDetectionDelegate.stopScreenshotDetection()
    }

    override fun onScreenCaptured(path: String) {
        // Do something when screen was captured
    }

    override fun onScreenCapturedWithDeniedPermission() {
        // Do something when screen was captured but read external storage permission has denied
    }
}
```

But above example will not work because read the external storage permission has denied. To fix this, you need to add the code for runtime permission request.

```kotlin
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

open class ScreenshotDetectionActivity : AppCompatActivity(), ScreenshotDetectionDelegate.ScreenshotDetectionListener {
    /* ... */

    companion object {
        private const val REQUEST_CODE_READ_EXTERNAL_STORAGE_PERMISSION = 3009
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkReadExternalStoragePermission()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CODE_READ_EXTERNAL_STORAGE_PERMISSION -> {
                if (grantResults.getOrNull(0) == PackageManager.PERMISSION_DENIED) {
                    showReadExternalStoragePermissionDeniedMessage()
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun checkReadExternalStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestReadExternalStoragePermission()
        }
    }

    private fun requestReadExternalStoragePermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_CODE_READ_EXTERNAL_STORAGE_PERMISSION)
    }

    private fun showReadExternalStoragePermissionDeniedMessage() {
        Toast.makeText(this, "Read external storage permission has denied", Toast.LENGTH_SHORT).show()
    }
}
```

Then extends your target activity with that base activity class and declare `onScreenCaptured(path: String)` and `onScreenCapturedWithDeniedPermission()` when you want to detect the screenshot.

```kotlin
import android.os.Bundle
import android.widget.Toast

class MainActivity : ScreenshotDetectionActivity() {
	/* ... */

    override fun onScreenCaptured(path: String) {
        Toast.make(this, path, Toast.LENGTH_SHORT).show();
        // Do something when screen was captured
    }

    override fun onScreenCapturedWithDeniedPermission() {
        Toast.make(this, "Please grant read external storage permission for screenshot detection", Toast.LENGTH_SHORT).show()
        // Do something when screen was captured but read external storage permission has denied
    }
}
```

# Demo

![Demo](https://raw.githubusercontent.com/akexorcist/Android-ScreenshotDetection/master/Images/screenshot_001.gif)

# Licence

Copyright 2021 Akexorcist

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this work except in compliance with the License. You may obtain a copy of the License in the LICENSE file, or at:

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
