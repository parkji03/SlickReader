package com.jipark.slickreader.intro;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;
import com.jipark.slickreader.R;

import java.util.Objects;

public class IntroActivity extends AppIntro {

    private final int ASK_MULTIPLE_PERMISSION_REQUEST_CODE = 1003;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Objects.requireNonNull(getSupportActionBar()).setTitle("Slick Reader Instructions");

        addSlide(AppIntroFragment.newInstance("1. Take a photo of text", "Better quality photos will create better audio!", R.drawable.ic_photo_camera_white_100dp, getResources().getColor(R.color.colorAccent)));
        addSlide(AppIntroFragment.newInstance("2. Press play", "Pause, rewind, fast forward anytime!", R.drawable.ic_play_arrow_white_100dp, getResources().getColor(R.color.colorPrimaryLight)));
        addSlide(AppIntroFragment.newInstance("3. Listen", "Easy and simple.", R.drawable.ic_hearing_white_100dp, getResources().getColor(R.color.colorAccent)));

        // Ask for permissions
        requestPermissions(new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WAKE_LOCK,
                Manifest.permission.DISABLE_KEYGUARD,
        }, ASK_MULTIPLE_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        finish();
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        finish();
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
        // Do something when the slide changes.
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case ASK_MULTIPLE_PERMISSION_REQUEST_CODE: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                }
                else {
                    // Ask for permissions
                    requestPermissions(new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.CAMERA,
                    }, ASK_MULTIPLE_PERMISSION_REQUEST_CODE);
                }
            }
        }
    }
}
