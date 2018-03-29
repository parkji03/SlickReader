package com.jipark.slickreader.intro;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;
import com.jipark.slickreader.R;

public class IntroActivity extends AppIntro {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addSlide(AppIntroFragment.newInstance("Slide 1", "Description", R.mipmap.ic_launcher, getResources().getColor(R.color.colorAccent)));
        addSlide(AppIntroFragment.newInstance("Slide 2", "Description", R.mipmap.ic_launcher, getResources().getColor(R.color.colorAccent)));
        addSlide(AppIntroFragment.newInstance("Slide 3", "Description", R.mipmap.ic_launcher, getResources().getColor(R.color.colorAccent)));
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
}
