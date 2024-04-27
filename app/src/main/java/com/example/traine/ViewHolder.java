package com.example.traine;

import android.app.Application;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.squareup.picasso.Picasso;

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
//        videoSizeView = itemView.findViewById(R.id.video_size);
        videoDurationView = itemView.findViewById(R.id.video_duration);

    }

    public void setPlayerView(Application application,String videoUri, String videoName, String videoPreviewImageUri, String videoDuration){
        videoDurationView.setText(videoDuration);
        videoNameView.setText(videoName);
        //thumbnail.setImageURI(Uri.parse(videoPreviewImageUri));


        try{
            Uri image = Uri.parse(videoPreviewImageUri);
            Picasso.get()
                    .load(image)
                    .placeholder(R.drawable.ic_profile)
                    .resize(140, 140)
                    .into(thumbnail);
        }catch (Exception e){
            //Toast.makeText(ViewHolder.this, "Error:" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }


        Uri video = Uri.parse(videoUri);
        simpleExoPlayer = new SimpleExoPlayer.Builder(application).build();
        MediaItem mediaItem = MediaItem.fromUri(video);

        simpleExoPlayer.setMediaItem(mediaItem);
        simpleExoPlayer.prepare();

    }
}
