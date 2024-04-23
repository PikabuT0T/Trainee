package com.example.traine;

import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class VideoPlayerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        VideoView videoView = findViewById(R.id.videoView);

        String vPath = "https://firebasestorage.googleapis.com/v0/b/traine-11a25.appspot.com/o/video%2Fabs%2FHow%20to%20Do_%20ABDOMINAL%20CRUNCHES.mp4?alt=media&token=1e8be587-1479-4744-9ffc-b3cbc4d612b6";

        Uri uriVideo = Uri.parse(vPath);
        videoView.setVideoURI(uriVideo);
        videoView.start();

        MediaController mediaController = new MediaController(this);
        videoView.setMediaController(mediaController);
    }
}