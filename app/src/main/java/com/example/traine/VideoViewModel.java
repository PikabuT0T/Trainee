package com.example.traine;

import android.content.Context;
import android.content.Intent;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.Query;

import java.util.List;

public class VideoViewModel extends ViewModel {
    private VideoRepository videoRepository;
    private MutableLiveData<List<Member>> videos = new MutableLiveData<>();

    public VideoViewModel() {
        videoRepository = new VideoRepository();
    }

    public LiveData<List<Member>> getVideos() {
        return videos;
    }

    public Query getVideoQuery() {
        return videoRepository.getVideoQuery();
    }

    public void launchVideoPlayer(Context context, Member member) {
        Intent intent = new Intent(context, VideoPlayerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("video_title", member.getVideoName());
        intent.putExtra("video_uri", member.getVideoUri());
        context.startActivity(intent);
    }
}

