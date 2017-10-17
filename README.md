[![Build Status](https://travis-ci.org/akexorcist/Android-ScreenshotDetection.svg?branch=master)](https://travis-ci.org/akexorcist/Android-ScreenshotDetection) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.akexorcist/screenshotdetection/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.akexorcist/screenshotdetection)

# Android-ScreenshotDetection
Screenshot Detection Library

Download
===============================

Maven
```
<dependency>
  <groupId>com.akexorcist</groupId>
  <artifactId>screenshotdetection</artifactId>
  <version>1.0.0</version>
</dependency>
```

Gradle
```
compile 'com.akexorcist:screenshotdetection:1.0.0'
```


Usage
===========================
Implement the library to activity that you need to detect the screenshot. Use base activity class for clean code

```java
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.akexorcist.screenshotdetection.ScreenshotDetectionDelegate;

public abstract class ScreenshotDetectionActivity extends AppCompatActivity implements ScreenshotDetectionDelegate.ScreenshotDetectionListener {
    private ScreenshotDetectionDelegate screenshotDetectionDelegate = new ScreenshotDetectionDelegate(this, this);

    @Override
    protected void onStart() {
        super.onStart();
        screenshotDetectionDelegate.startScreenshotDetection();
    }

    @Override
    protected void onStop() {
        super.onStop();
        screenshotDetectionDelegate.stopScreenshotDetection();
    }

    @Override
    public void onScreenCaptured(String path) {
        // Do something when screen was captured
    }

    @Override
    public void onScreenCapturedWithDeniedPermission() {
        // Do something when screen was captured but read external storage permission has denied
    }
}
```

Then extends your target activity with that base activity class

```java
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends ScreenshotDetectionActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onScreenCaptured(String path) {
        Toast.make(this, path, Toast.LENGTH_SHORT).show();
        // Do something when screen was captured
    }

    @Override
    public void onScreenCapturedWithDeniedPermission() {
        Toast.make(this, "Please grant read external storage permission for screenshot detection", Toast.LENGTH_SHORT).show();
        // Do something when screen was captured but read external storage permission has denied
    }
}
```


Demo 
===========================
![Demo](https://raw.githubusercontent.com/akexorcist/Android-ScreenshotDetection/master/Image/screenshot_001.gif)


Licence
===========================
Copyright 2017 Akexorcist

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this work except in compliance with the License. You may obtain a copy of the License in the LICENSE file, or at:

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

