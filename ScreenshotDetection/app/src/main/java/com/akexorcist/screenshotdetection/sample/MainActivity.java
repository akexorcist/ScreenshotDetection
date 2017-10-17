package com.akexorcist.screenshotdetection.sample;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.View;

public class MainActivity extends ScreenshotDetectionActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onScreenCaptured(String path) {
        Snackbar.make(getRootView(), path, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onScreenCapturedWithDeniedPermission() {
        Snackbar.make(getRootView(), R.string.please_grant_read_external_storage_permission_for_screenshot_detection, Snackbar.LENGTH_SHORT).show();
    }

    private View getRootView() {
        return getWindow().getDecorView().findViewById(android.R.id.content);
    }
}
