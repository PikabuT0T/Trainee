package com.example.traine;

import android.app.Application;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.drm.DrmSessionEventListener;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.MediaPeriod;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MediaSourceEventListener;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.Allocator;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.TransferListener;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ViewHolder extends RecyclerView.ViewHolder {

    SimpleExoPlayer simpleExoPlayer;
    PlayerView playerView;
    TextView videoNameView, videoSizeView, videoDurationView;
    ImageView thumbnail, menu_more;

    public ViewHolder(@NonNull View itemView) {

        super(itemView);
        thumbnail = itemView.findViewById(R.id.thumbnail);
        menu_more = itemView.findViewById(R.id.video_menu_more);
        videoNameView = itemView.findViewById(R.id.video_name);
        videoSizeView = itemView.findViewById(R.id.video_size);
        videoDurationView = itemView.findViewById(R.id.video_duration);

    }

    public void setPlayerView(Application application,String videoUri, String videoName){
        TextView textView = itemView.findViewById(R.id.video_name);
        textView.setText(videoName);

        Uri video = Uri.parse(videoUri);
        simpleExoPlayer = new SimpleExoPlayer.Builder(application).build();
        MediaItem mediaItem = MediaItem.fromUri(video);

        simpleExoPlayer.setMediaItem(mediaItem);
        simpleExoPlayer.prepare();

        long durationMillis = simpleExoPlayer.getDuration();

        if (durationMillis != 0){
            videoDurationView.setText(timeConversion(durationMillis));
        } else{
            videoDurationView.setText("99:99");
            Log.d("Duration", "duration error");
        }
    }

    public String timeConversion(long value) {
        String videoTime;
        int duration = (int) value;
        int hrs = duration / 3600000;
        int mns = (duration / 60000) % 60;
        int scs = (duration % 60000) / 1000;

        if (hrs > 0) {
            videoTime = String.format("%02d:%02d:%02d", hrs, mns, scs);
        } else {
            videoTime = String.format("%02d:%02d", mns, scs);
        }

        //videoTime = String.format("%02d", value);
        videoTime= String.valueOf((int)value);

        return videoTime;
    }
}
