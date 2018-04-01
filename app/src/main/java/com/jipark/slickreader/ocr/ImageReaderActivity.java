package com.jipark.slickreader.ocr;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.jipark.slickreader.R;
import com.jipark.slickreader.intro.IntroActivity;

import java.io.IOException;
import java.util.Locale;
import java.util.Objects;

public class ImageReaderActivity extends AppCompatActivity implements TextToSpeech.OnInitListener{

    private final String TAG = "ImageReaderActivity";
    private final int PICK_IMAGE_REQUEST = 71;

    private FloatingActionButton mSelectImageFab;
    private AppBarLayout mAppBarLayout;
    private ImageView mOcrImage;
    private Toolbar mToolbar;
    private TextView mOcrText;
    private TextView mChooseImageText;
    private ImageButton mPlayButton;
    private ImageButton mRewindButton;
    private ImageButton mFastForwardButton;

    private TextToSpeech tts;
    private boolean isPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_reader);

        // Binding views to activity
        bindActivity();

        // Binding support toolbar
        bindToolbar();

        // Binding onClick listeners for available buttons
        bindOnClickListeners();

        // Toolbar setup
        bindToolbar();

        // Hide or show FAB on scroll
        showFab();

        // Disable play buttons
        enablePlayButtons(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                mOcrImage.setImageBitmap(bitmap);
                mChooseImageText.setVisibility(View.INVISIBLE);
                convertImageToText(bitmap);
                enablePlayButtons(true);
            } catch (IOException err) {
                err.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.image_reader_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.info:
                startActivity(new Intent(this, IntroActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * TextToSpeech.OnInitListener
     * Gets called when the activity starts
     * @param i
     */
    @Override
    public void onInit(int i) {
        if (i == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.d(TAG, "Language is not supported");
            } else {
            }
        } else {
            // Disable the play buttons until mOcrText is not null
            Log.d(TAG, "TTS onInit failed");
        }
    }

    private void convertImageToText(Bitmap bitmap) {
        TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
        if (textRecognizer.isOperational()) {
            Frame frame = new Frame.Builder().setBitmap(bitmap).build();
            SparseArray<TextBlock> items = textRecognizer.detect(frame);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < items.size(); i++) {
                TextBlock item = items.valueAt(i);
                sb.append(item.getValue());
                sb.append('\n');
            }
            mOcrText.setText(sb.toString());
            enablePlayButtons(true);
        } else {
            Log.d(TAG, "Text recognizer is not operational.");
        }
    }

    private void bindActivity() {
        mAppBarLayout = findViewById(R.id.layout_app_bar);
        mSelectImageFab = findViewById(R.id.btn_select_image);
        mToolbar = findViewById(R.id.toolbar);
        mOcrImage = findViewById(R.id.img_ocr_text);
        mOcrText = findViewById(R.id.txt_ocr_text);
        mChooseImageText = findViewById(R.id.txt_choose_image);
        mPlayButton = findViewById(R.id.btn_play);
        mFastForwardButton = findViewById(R.id.btn_forward);
        mRewindButton = findViewById(R.id.btn_rewind);

        tts = new TextToSpeech(this, this);
    }

    private void setImageButtonEnabled(Context context, boolean enabled, ImageButton imageButton, int iconResId) {
        imageButton.setEnabled(enabled);
        Drawable originalIcon = context.getResources().getDrawable(iconResId);
        Drawable icon = enabled ? originalIcon : convertDrawableToGrayScale(originalIcon);
        imageButton.setImageDrawable(icon);
    }

    private Drawable convertDrawableToGrayScale(Drawable drawable) {
        if (drawable == null)
            return null;

        Drawable res = drawable.mutate();
        res.setColorFilter(getResources().getColor(R.color.colorDisabledButton), PorterDuff.Mode.SRC_IN);
        return res;
    }

    private void enablePlayButtons(boolean enable) {
        setImageButtonEnabled(this, enable, mPlayButton, R.drawable.ic_play_arrow_white_24dp);
        setImageButtonEnabled(this, enable, mRewindButton, R.drawable.ic_fast_rewind_white_24dp);
        setImageButtonEnabled(this, enable, mFastForwardButton, R.drawable.ic_fast_forward_white_24dp);
    }

    private void bindOnClickListeners() {
        mSelectImageFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            }
        });
    }

    private void bindToolbar() {
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    private void showFab() {
        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (Math.abs(verticalOffset) - appBarLayout.getTotalScrollRange() > -300) {
                    mSelectImageFab.hide();
                } else {
                    mSelectImageFab.show();
                }
            }
        });

        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isPlaying) {
                    mPlayButton.setImageResource(R.drawable.ic_play_arrow_white_24dp);
                    // Pause
//                    tts.stop();
                    tts.playSilentUtterance(2000, TextToSpeech.QUEUE_FLUSH, null);

                }
                else {
                    mPlayButton.setImageResource(R.drawable.ic_pause_white_24dp);
                    tts.speak(mOcrText.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
                }
                isPlaying = !isPlaying;
            }
        });

        mRewindButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        mFastForwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // tts.setSpeechRate(2);
            }
        });
    }
}