package com.jipark.slickreader.ocr;

import android.app.KeyguardManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
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
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.jipark.slickreader.R;
import com.jipark.slickreader.intro.IntroActivity;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;

import static android.widget.Toast.makeText;

public class ImageReaderActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private final String TAG = "ImageReaderActivity";

    private final int PICK_IMAGE_REQUEST = 71;
    private final int REQ_CODE_SPEECH_INPUT = 100;

    private final String ROOT_DIR = Environment.getExternalStorageDirectory().getAbsolutePath();
    private final String destinationFileName = ROOT_DIR + "/slickreader.wav";

//    private static final String KWS_SEARCH = "wakeup";
//    private static final String KEYPHRASE = "hey google";

    private FloatingActionButton mSelectImageFab;
    private AppBarLayout mAppBarLayout;
    private ImageView mOcrImage;
    private Toolbar mToolbar;
    private TextView mOcrText;
    private TextView mChooseImageText;
    private ImageButton mPlayButton;
    private ImageButton mRewindButton;
    private ImageButton mFastForwardButton;
    private ProgressBar mLoadingOcrText;
    private ProgressBar mLoadingTts;

    private TextToSpeech tts;
    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;

    // PocketSphinx
//    private SpeechRecognizer recognizer;
//    private HashMap<String, Integer> captions;
//
//    // WakeLock
//    private PowerManager.WakeLock wakeLock;
//
//    KeyguardManager.KeyguardLock keyguard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_reader);

        // Binding views to activity
        bindActivity();

        isLoadingTts(true);

        // Binding support toolbar
        bindToolbar();

        // Binding onClick listeners for available buttons
        bindOnClickListeners();

        // Hide or show FAB on scroll
        showFab();

        // Disable play buttons
        enablePlayButtons(false);

//        // TODO: fix this later
//
//        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
//        wakeLock = powerManager.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "TAG");
//        wakeLock.acquire();
////        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
////        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WAKELOCK_TAG");
////        wakeLock.acquire();
//
//        getWindow().addFlags(
//                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
//                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
//                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
//                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
//
//        KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
//        keyguard = km.newKeyguardLock("myapp");
//        keyguard.disableKeyguard();
//
//        // TODO: fix this later
//        // Prepare the data for UI
//        captions = new HashMap<>();
//        captions.put(KWS_SEARCH, R.string.kws_caption);
//
//        new SetupTask(this).execute();

    }

    @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("REQUEST", requestCode + " ");
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri filePath = data.getData();
            try {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        mPlayButton.setImageResource(R.drawable.ic_play_arrow_white_24dp);
                        isPlaying = false;
                    }
                });

                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                mOcrImage.setImageBitmap(bitmap);
                mChooseImageText.setVisibility(View.INVISIBLE);
                convertImageToText(bitmap);
            } catch (IOException err) {
                err.printStackTrace();
            }
        }
//        else if (requestCode == REQ_CODE_SPEECH_INPUT && resultCode == RESULT_OK && data != null) {
//            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
//            for (String i : result) {
//                Log.d("REQ_CODE_SPEECH_INPUT", i);
//            }
//        }
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
                isLoadingTts(false);
                if (getIntent().hasExtra("IMAGE_PATH")) {
                    String fromExtra = getIntent().getExtras().getString("IMAGE_PATH");
                    if (fromExtra != null && !fromExtra.isEmpty()) {
                        Uri filePath = Uri.fromFile(new File(fromExtra));
                        try {
                            Matrix matrix = new Matrix();
                            matrix.postRotate(90);
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                            mOcrImage.setImageBitmap(rotatedBitmap);
                            mChooseImageText.setVisibility(View.INVISIBLE);
                            convertImageToText(rotatedBitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
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
            HashMap<String, String> hashRender = new HashMap<>();
            hashRender.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, mOcrText.getText().toString());
            File soundFile = new File(destinationFileName);
            if (soundFile.exists())
                soundFile.delete();

            if (tts.synthesizeToFile(mOcrText.getText().toString(), hashRender, destinationFileName) == TextToSpeech.SUCCESS) {
                mPlayButton.setVisibility(View.GONE);
                mRewindButton.setVisibility(View.GONE);
                mFastForwardButton.setVisibility(View.GONE);
                mLoadingOcrText.setVisibility(View.VISIBLE);

                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String s) {

                    }

                    @Override
                    public void onDone(String s) {
                        try {
                            mediaPlayer.setDataSource(destinationFileName);
                            mediaPlayer.prepare();

                            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                @Override
                                public void onPrepared(MediaPlayer mediaPlayer) {
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mPlayButton.setVisibility(View.VISIBLE);
                                mRewindButton.setVisibility(View.VISIBLE);
                                mFastForwardButton.setVisibility(View.VISIBLE);
                                mLoadingOcrText.setVisibility(View.GONE);
                                enablePlayButtons(true);
                            }
                        });
                    }

                    @Override
                    public void onError(String s) {

                    }
                });
            }
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
        mLoadingOcrText = findViewById(R.id.progress_loading_ocr_text);
        mLoadingTts = findViewById(R.id.progress_loading_tts);

        tts = new TextToSpeech(this, this);
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mPlayButton.setImageResource(R.drawable.ic_play_arrow_white_24dp);
                isPlaying = false;
            }
        });
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
                    mediaPlayer.pause();
                }
                else {
                    mPlayButton.setImageResource(R.drawable.ic_pause_white_24dp);
                    mediaPlayer.start();
                }
                isPlaying = !isPlaying;
            }
        });

        mRewindButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isPlaying) {
                    int rewindPosition = mediaPlayer.getCurrentPosition() - 3000;
                    if (rewindPosition < 0) {
                        mediaPlayer.seekTo(0);
                    } else {
                        mediaPlayer.seekTo(rewindPosition);
                    }
                }
            }
        });

        mFastForwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isPlaying) {
                    int forwardPosition = mediaPlayer.getCurrentPosition() + 3000;
                    mediaPlayer.seekTo(forwardPosition);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }

//        if (recognizer != null) {
//            recognizer.cancel();
//            recognizer.shutdown();
//        }
//
//        wakeLock.release();
    }

    private void isLoadingTts(boolean isLoading) {
        if (isLoading) {
            mChooseImageText.setVisibility(View.GONE);
            mLoadingTts.setVisibility(View.VISIBLE);

        } else {
            mChooseImageText.setVisibility(View.VISIBLE);
            mLoadingTts.setVisibility(View.GONE);
        }
    }

//    @Override
//    public void onBeginningOfSpeech() {
//
//    }
//
//    @Override
//    public void onEndOfSpeech() {
//        if (!recognizer.getSearchName().equals(KWS_SEARCH)) {
////            recognizer.stop();
////            recognizer.startListening(KWS_SEARCH, 10000);
//        }
////        Log.d("VOICE RECOG", "end of speech");
//
//    }
//
//    @Override
//    public void onPartialResult(Hypothesis hypothesis) {
//        if (hypothesis == null)
//            return;
//
//        String text = hypothesis.getHypstr();
//        Log.d("VOICE RECOGNITION", text);
//
//        if (text.equals(KEYPHRASE)) {
//            recognizer.stop();
//
//            final Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
//            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
//            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US);
//            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speech_prompt));
//            try {
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
//                    }
//                }, 4000);
//            } catch (ActivityNotFoundException a) {
//                Toast.makeText(getApplicationContext(), "speech not supported", Toast.LENGTH_SHORT).show();
//            }
////            recognizer.startListening(KWS_SEARCH, 10000);
//        }
//    }
//
//    @Override
//    public void onResult(Hypothesis hypothesis) {
//        ((TextView) findViewById(R.id.result_text)).setText("");
//        if (hypothesis != null) {
//            String text = hypothesis.getHypstr();
//            Log.d("VOICE RECOG", text);
//            makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    @Override
//    public void onError(Exception e) {
//        ((TextView) findViewById(R.id.caption_text)).setText(e.getMessage());
//    }
//
//    @Override
//    public void onTimeout() {
//        Log.d("VOICE RECOG", "Time out");
//        recognizer.stop();
//        recognizer.startListening(KWS_SEARCH, 10000);
//    }
//
//    private static class SetupTask extends AsyncTask<Void, Void, Exception> {
//        WeakReference<ImageReaderActivity> activityReference;
//
//        SetupTask(ImageReaderActivity activity) {
//            this.activityReference = new WeakReference<>(activity);
//        }
//
//        @Override
//        protected Exception doInBackground(Void... params) {
//            try {
//                Assets assets = new Assets(activityReference.get());
//                File assetDir = assets.syncAssets();
//                activityReference.get().setupRecognizer(assetDir);
//            } catch (IOException e) {
//                return e;
//            }
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Exception result) {
//            if (result != null) {
//                ((TextView) activityReference.get().findViewById(R.id.caption_text))
//                        .setText("Failed to init recognizer " + result);
//            } else {
//                activityReference.get().doSomething();
//            }
//        }
//    }
//
//    private void doSomething() {
//        recognizer.stop();
//        recognizer.startListening(KWS_SEARCH, 10000);
//    }
//
//    private void setupRecognizer(File assetsDir) throws IOException {
//        recognizer = SpeechRecognizerSetup.defaultSetup()
//                .setAcousticModel(new File(assetsDir, "en-us-ptm"))
//                .setDictionary(new File(assetsDir, "cmudict-en-us.dict"))
//                .getRecognizer();
//        recognizer.addListener(this);
//
//        recognizer.addKeyphraseSearch(KWS_SEARCH, KEYPHRASE);
//    }
}
