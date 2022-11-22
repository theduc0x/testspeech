package com.iodev.travel360.activity;

import static com.iodev.travel360.utilities.Constants.INTENT_CONTENT;
import static com.iodev.travel360.utilities.Constants.INTENT_MAIN;
import static com.iodev.travel360.utilities.Constants.INTENT_WEATHER;
import static com.iodev.travel360.utilities.Constants.PATH_IMAGE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

import com.iodev.travel360.R;
import com.iodev.travel360.apdater.DetailAdapter;
import com.iodev.travel360.databinding.ActivityDetailBinding;
import com.iodev.travel360.model.Content;
import com.iodev.travel360.model.weather.WeatherData;
import com.iodev.travel360.repository.GetWeatherRes;
import com.iodev.travel360.utilities.Constants;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class DetailActivity extends AppCompatActivity {
    private ActivityDetailBinding binding;
    private Content content;
    private WeatherData weather;
    private TextToSpeech tts;
    private MediaPlayer mMediaPlayer;
    private String mAudioFilename = "";
    private final String mUtteranceID = "totts";
    private File sddir;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initMain();
    }

    private void initMain() {
        Intent intent = getIntent();
        content = (Content) intent.getSerializableExtra(INTENT_MAIN);
        setWeatherAndTemperature();
        mMediaPlayer = new MediaPlayer();
        openMap();
        setData();
        setViewpagerImage();
        textToSpeech();
    }

    private void setData() {
        binding.tvContent.setText(content.getContent());
        binding.tvName.setText(content.getName());
        binding.tvLocation.setText(content.getLocation());
    }

    private void openMap() {
        binding.ivLocation.setOnClickListener(v -> {
            Intent intent = new Intent(this, MapActivity.class);
            intent.putExtra(INTENT_CONTENT, content);
            startActivity(intent);
        });
    }

    private void setWeatherAndTemperature() {
        String[] coordinate = content.getCoordinates().split(" ");
        Double lat = Math.ceil((Double.parseDouble(coordinate[0]) * 10000)) / 10000;
        Double lon = Math.ceil((Double.parseDouble(coordinate[1]) * 10000)) / 10000;
        String strLat = String.valueOf(lat);
        String strLon = String.valueOf(lon);
        GetWeatherRes getWeatherRes = new GetWeatherRes();
        getWeatherRes.getWeather(
                strLat, strLon,
                weathers -> {
                    weather = weathers;
                    binding.tvWeather.setText(supportTranslate(weather.getCurrent().getWeather().get(0).getDescription()));
                    int temperature = (int) (weather.getCurrent().getTemp());
                    binding.tvTemperature.setText(String.valueOf(temperature));
                }, () -> {
                    binding.ivWeather.setOnClickListener(v -> {
                        Intent intent = new Intent(this, WeatherActivity.class);
                        intent.putExtra(INTENT_WEATHER, weather);
                        startActivity(intent);
                    });
                    binding.ivTemperature.setOnClickListener(v->{
                        Intent intent = new Intent(this, WeatherActivity.class);
                        intent.putExtra(INTENT_WEATHER, weather);
                        startActivity(intent);
                    });
                }
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (new File(mAudioFilename).exists()) {
            new File(mAudioFilename).delete();
        }
        Toast.makeText(this, "Đã thoát", Toast.LENGTH_SHORT).show();
    }

    private void setViewpagerImage() {
        List<String> links = new ArrayList<>();
        for (int i = 1; i <= Integer.parseInt(content.getImage()); i++) {
            //https://ninhio.online/travel360/image/1_1.png
            String link = PATH_IMAGE + content.getCodeItem() + "_" + i + ".png";
            links.add(link);
        }
        DetailAdapter detailAdapter = new DetailAdapter(links);
        binding.vp.setAdapter(detailAdapter);
    }


    private void textToSpeech() {
        binding.ivPlay.setOnClickListener(v -> {
            tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    saveToAudioFile(binding.tvContent.getText().toString().trim());
                    CreateFile();

                    initializeMediaPlayer();
                    if(mMediaPlayer != null && mMediaPlayer.isPlaying()){
                        playMediaPlayer(1);
                        binding.ivPlay.setImageResource(R.drawable.ic_play);

                    } else {
                        playMediaPlayer(0);
                        binding.ivPlay.setImageResource(R.drawable.ic_pause);
                    }
                    mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            binding.ivPlay.setImageResource(R.drawable.ic_play);
                        }
                    });
                    Toast.makeText(DetailActivity.this, "Thời gian đọc là :" + mMediaPlayer.getDuration(), Toast.LENGTH_SHORT).show();
                }
            });

        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        per();
    }

    private void CreateFile() {
        // Perform the dynamic permission request
//        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
//                PackageManager.PERMISSION_GRANTED) requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 123);
        // Create audio file location
        sddir = new File(Environment.getExternalStorageDirectory() + "/My File/");
        if (!sddir.exists()) {
            sddir.mkdir();
        }
//        else {
//            sddir.delete();
//            sddir.mkdir();
//        }
        mAudioFilename = sddir.getAbsolutePath() + "/" + mUtteranceID + ".wav";
    }

    private void saveToAudioFile(String text) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.synthesizeToFile(text, null, new File(mAudioFilename), mUtteranceID);
//            Toast.makeText(DetailActivity.this, "Saved to 1" + mAudioFilename, Toast.LENGTH_LONG).show();
        } else {
            HashMap<String, String> hm = new HashMap();
            hm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,mUtteranceID);
            tts.synthesizeToFile(text, hm, mAudioFilename);
//            Toast.makeText(DetailActivity.this, "Saved to " + mAudioFilename, Toast.LENGTH_LONG).show();
        }
    }


    private void initializeMediaPlayer(){

        Uri uri  = Uri.parse("file://"+ mAudioFilename);

        mMediaPlayer.setAudioAttributes(
                new AudioAttributes
                        .Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build());

//        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

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

    private void per() {
        if (!Constants.isPermissionGranted(this)) {
            new AlertDialog.Builder(this)
                    .setTitle("All file")
                    .setMessage("Ok di")
                    .setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            takePermisstion();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    }).setIcon(android.R.drawable.ic_dialog_email).show();
        }
    }

    private void takePermisstion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivityForResult(intent, 101);
            } catch (Exception e) {
                e.printStackTrace();
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, 101);
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[] {
                    Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 101);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0) {
            if (requestCode == 101) {
                boolean readExt = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (!readExt) {
                    takePermisstion();
                }
            }
        }
    }

    private String supportTranslate(String name){
        if (name.equals("mây cụm")){
            return "nhiều mây";
        }
        return name;
    }
}