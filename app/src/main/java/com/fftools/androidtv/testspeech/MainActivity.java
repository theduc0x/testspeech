package com.fftools.androidtv.testspeech;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;

import aoto.com.mylibrary.MediaTTSManager;
import aoto.com.mylibrary.WhyTTS;

public class MainActivity extends AppCompatActivity{
    private EditText etText;
    private Button btOk;

    private TextToSpeech mTts;
    private int mStatus = 0;
    private MediaPlayer mMediaPlayer;

    private String mAudioFilename = "";
    private final String mUtteranceID = "totts";

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etText = findViewById(R.id.et_text);
        btOk = findViewById(R.id.bt_ok);
        mMediaPlayer = new MediaPlayer();
        btOk.setOnClickListener(v -> {

            mTts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    saveToAudioFile(etText.getText().toString().trim());
                    CreateFile();

                    initializeMediaPlayer();
                    if(mMediaPlayer != null && mMediaPlayer.isPlaying()){
                        playMediaPlayer(1);
                        btOk.setText("Speak");

                    } else {
                        playMediaPlayer(0);
                        btOk.setText("Pause");
                    }
                    mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            btOk.setText("Speak");
                        }
                    });
                    Toast.makeText(MainActivity.this, "Thời gian đọc là :" + mMediaPlayer.getDuration(), Toast.LENGTH_SHORT).show();
                }
            });



        });


    }
    private void CreateFile() {
        // Perform the dynamic permission request
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 123);
        // Create audio file location
        File sddir = new File(Environment.getExternalStorageDirectory() + "/My File/");
        if (!sddir.exists()) {
            sddir.mkdir();
        } else {
            sddir.delete();
            sddir.mkdir();
        }


        mAudioFilename = sddir.getAbsolutePath() + "/" + mUtteranceID + ".wav";
    }

    private void saveToAudioFile(String text) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mTts.synthesizeToFile(text, null, new File(mAudioFilename), mUtteranceID);
            Toast.makeText(MainActivity.this, "Saved to 1" + mAudioFilename, Toast.LENGTH_LONG).show();
        } else {
            HashMap<String, String> hm = new HashMap();
            hm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,mUtteranceID);
            mTts.synthesizeToFile(text, hm, mAudioFilename);
            Toast.makeText(MainActivity.this, "Saved to " + mAudioFilename, Toast.LENGTH_LONG).show();
        }
    }

    private void initializeMediaPlayer(){

        Uri uri  = Uri.parse("file://"+ mAudioFilename);

        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {
            mMediaPlayer.setDataSource(getApplicationContext(), uri);
            mMediaPlayer.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void playMediaPlayer(int status){

        // Start Playing
        if(status==0){
            mMediaPlayer.start();
        }

        // Pause Playing
        if(status==1){
            mMediaPlayer.pause();
        }
    }
}