package com.github.YspDemo;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.YspDemo.jni.FFmpegCmd;
import com.github.YspDemo.util.MediaTool;

import java.io.File;

public class Main2Activity extends AppCompatActivity {
    private Button ffmpegButton;
    private Button shuaxing;
    private TextView ffmpegText;
    private static final int MAFF = 0x2;
    private String vidioPath;
    private String outVidioPath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        ffmpegButton = findViewById(R.id.button_ffmpeg);
        shuaxing = findViewById(R.id.button_ffmpeg2);
        ffmpegText = findViewById(R.id.text_ffmpeg);
        ffmpegButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, MAFF);
            }
        });
        shuaxing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaTool.insertMedia(getApplicationContext(),outVidioPath);
            }
        });
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/ffmpeg" );
        if (!file.exists()){
            file.mkdirs();
        }
        File extractorFile = new File(file,"ffmpegOut.mp4");
        outVidioPath = extractorFile.getAbsolutePath();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == MAFF && resultCode == RESULT_OK && null != data) {
            Uri selectedVideo = data.getData();
            String[] filePathColumn = {MediaStore.Video.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedVideo,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            vidioPath = cursor.getString(columnIndex);
            cursor.close();
            ffmpegText.setText(vidioPath);
            FFmpegCmd.look(vidioPath,outVidioPath);
        }
    }
}
