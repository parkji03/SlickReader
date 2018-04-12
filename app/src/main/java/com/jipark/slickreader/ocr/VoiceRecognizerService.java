package com.jipark.slickreader.ocr;

import android.app.Activity;
import android.app.IntentService;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.AsyncTask;
import android.speech.RecognizerIntent;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.jipark.slickreader.R;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;

import static android.support.v4.app.ActivityCompat.startActivityForResult;

public class VoiceRecognizerService extends IntentService implements RecognitionListener {

    private final int REQ_CODE_SPEECH_INPUT = 100;

    private static final String KWS_SEARCH = "wakeup";
    private static final String KEYPHRASE = "hey google";

    // PocketSphinx
    private SpeechRecognizer recognizer;
    private HashMap<String, Integer> captions;

    // Listeners
    private ArrayList<OnRecognizedListener> listeners;

    public VoiceRecognizerService(String name) {
        super(name);
        listeners = new ArrayList<>();

        new SetupTask(this).execute();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onEndOfSpeech() {
        if (!recognizer.getSearchName().equals(KWS_SEARCH)) {
//            recognizer.stop();
//            recognizer.startListening(KWS_SEARCH, 10000);
        }
//        Log.d("VOICE RECOG", "end of speech");

    }

    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis == null)
            return;

        String text = hypothesis.getHypstr();
        Log.d("VOICE RECOGNITION", text);

        if (text.equals(KEYPHRASE)) {
            recognizer.stop();
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speech_prompt));

            try {
//                startActivityForResult(ImageReaderActivity activity, intent, REQ_CODE_SPEECH_INPUT);
            } catch (ActivityNotFoundException a) {
                Toast.makeText(getApplicationContext(), "speech not supported", Toast.LENGTH_SHORT).show();
            }
//            recognizer.startListening(KWS_SEARCH, 10000);
        }
    }

    @Override
    public void onResult(Hypothesis hypothesis) {
//        ((TextView) findViewById(R.id.result_text)).setText("");
        if (hypothesis != null) {
            String text = hypothesis.getHypstr();
            Log.d("VOICE RECOG", text);
//            makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onError(Exception e) {
//        ((TextView) findViewById(R.id.caption_text)).setText(e.getMessage());
    }

    @Override
    public void onTimeout() {
        Log.d("VOICE RECOG", "Time out");
        recognizer.stop();
        recognizer.startListening(KWS_SEARCH, 10000);
    }

    private static class SetupTask extends AsyncTask<Void, Void, Exception> {
        WeakReference<ImageReaderActivity> activityReference;
        WeakReference<VoiceRecognizerService> serviceReference;

        SetupTask(VoiceRecognizerService service) {
            this.serviceReference = new WeakReference<>(service);
        }

        @Override
        protected Exception doInBackground(Void... params) {
            try {
                Assets assets = new Assets(activityReference.get());
                File assetDir = assets.syncAssets();
                serviceReference.get().setupRecognizer(assetDir);
            } catch (IOException e) {
                return e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Exception result) {
            if (result != null) {
                ((TextView) activityReference.get().findViewById(R.id.caption_text))
                        .setText("Failed to init recognizer " + result);
            } else {
                serviceReference.get().doSomething();
            }
        }
    }

    private void doSomething() {
        recognizer.stop();
        recognizer.startListening(KWS_SEARCH, 10000);
    }

    private void setupRecognizer(File assetsDir) throws IOException {
        recognizer = SpeechRecognizerSetup.defaultSetup()
                .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                .setDictionary(new File(assetsDir, "cmudict-en-us.dict"))
                .getRecognizer();
        recognizer.addListener(this);

        recognizer.addKeyphraseSearch(KWS_SEARCH, KEYPHRASE);
    }

    public void registerListener(OnRecognizedListener listener) {

    }

    public interface OnRecognizedListener {
        void onStart();
        void onEnd();
    }
}
