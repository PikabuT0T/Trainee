package com.example.traine;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;

import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;

public class VideoPlayerActivity extends AppCompatActivity {
    PlayerView playerView;
    SimpleExoPlayer exoPlayer;
    String videoUri;

    ImageView buttonMain;
    String videoTitle;
    TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        initViews();
        fetchDataFromIntent();
        initializePlayer();
    }

    private void initViews() {
        playerView = findViewById(R.id.exoplayer_view);
        title = findViewById(R.id.video_title);
        buttonMain = findViewById(R.id.buttonToMainActivity);
        buttonMain.setOnClickListener(view -> finish()); // Use finish() to return to previous activity
    }

    private void fetchDataFromIntent() {
        videoTitle = getIntent().getStringExtra("video_title");
        videoUri = getIntent().getStringExtra("video_uri");
        title.setText(videoTitle);
    }

    private void initializePlayer() {
        exoPlayer = new SimpleExoPlayer.Builder(this).build();
        Uri video = Uri.parse(videoUri);
        MediaItem mediaItem = MediaItem.fromUri(video);
        MediaSource mediaSource = new ProgressiveMediaSource.Factory(new DefaultHttpDataSource.Factory()).createMediaSource(mediaItem);

        playerView.setPlayer(exoPlayer);
        playerView.setKeepScreenOn(true);
        exoPlayer.setMediaSource(mediaSource);
        exoPlayer.prepare();
        exoPlayer.setPlayWhenReady(true);

        exoPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlayerError(PlaybackException error) {
                Toast.makeText(VideoPlayerActivity.this, "Video Playing Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(false);
            exoPlayer.getPlaybackState();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(true);
            exoPlayer.getPlaybackState();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
    }
}
//public class VideoPlayerActivity extends AppCompatActivity {
//    PlayerView playerView;
//    SimpleExoPlayer exoPlayer;
//    String videoUri;
//
//    ImageView buttonMain;
//
//    String videoTitle;
//    TextView title;
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_video_player);
//        playerView = findViewById(R.id.exoplayer_view);
//        buttonMain = findViewById(R.id.buttonToMainActivity);
//        buttonMain.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(VideoPlayerActivity.this, PlaylistActivity.class);
//                startActivity(intent);
//            }
//        });
//
//        //
//        videoTitle = getIntent().getStringExtra("video_title");
//        videoUri = getIntent().getStringExtra("video_uri");
//
//        title = findViewById(R.id.video_title);
//        title.setText(videoTitle);
//
//        playVideo();
//        //getSupportActionBar().hide();
//        // Устанавливаем флаги для полноэкранного режима
//
//
//
//    }
//
//    private void playVideo() {
//        Uri video = Uri.parse(videoUri);
//        exoPlayer = new SimpleExoPlayer.Builder(this).build();
////        DefaultDataSourceFactory defaultDataSourceFactory = new DefaultDataSourceFactory(
////                this, Util.getUserAgent(this, "app"));
////        concatenatingMediaSource = new ConcatenatingMediaSource();
//
//        //for(int i = 0; i < m)
//        DefaultHttpDataSource.Factory dataSourceFactory = new DefaultHttpDataSource.Factory();
//        MediaItem mediaItem = MediaItem.fromUri(video);
//        MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem);
//
//        playerView.setPlayer(exoPlayer);
//
//        playerView.setKeepScreenOn(true);
//        exoPlayer.prepare(mediaSource);
//        playError();
//    }
//
//    private void playError() {
//        exoPlayer.addListener(new Player.Listener() {
//            @Override
//            public void onPlayerError(PlaybackException error) {
//                Player.Listener.super.onPlayerError(error);
//                Toast.makeText(VideoPlayerActivity.this, "Video Playing Error" + error, Toast.LENGTH_SHORT).show();
//            }
//        });
//        exoPlayer.setPlayWhenReady(true);
//    }
//
//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//        if (exoPlayer.isPlaying()){
//            exoPlayer.stop();
//        }
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        exoPlayer.setPlayWhenReady(false);
//        exoPlayer.getPlaybackState();
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        exoPlayer.setPlayWhenReady(true);
//        exoPlayer.getPlaybackState();
//    }
//
//    @Override
//    protected void onRestart() {
//        super.onRestart();
//        exoPlayer.setPlayWhenReady(true);
//        exoPlayer.getPlaybackState();
//    }
//}