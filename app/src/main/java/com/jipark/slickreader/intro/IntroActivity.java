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

public class IntroActivity extends AppIntro {

    private final int ASK_MULTIPLE_PERMISSION_REQUEST_CODE = 1003;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addSlide(AppIntroFragment.newInstance("Slide 1", "Description", R.mipmap.ic_launcher, getResources().getColor(R.color.colorAccent)));
        addSlide(AppIntroFragment.newInstance("Slide 2", "Description", R.mipmap.ic_launcher, getResources().getColor(R.color.colorAccent)));
        addSlide(AppIntroFragment.newInstance("Slide 3", "Description", R.mipmap.ic_launcher, getResources().getColor(R.color.colorAccent)));

        // Ask for permissions
        requestPermissions(new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
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
