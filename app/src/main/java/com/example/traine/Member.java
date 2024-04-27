package com.example.traine;

public class Member {
    String videoUri;
    String videoName;
    String videoPreviewImageUri;
    String videoDuration;

    public Member() {
    }

    public Member(String videoUri, String videoName, String videoDuration, String videoPreviewImageUri) {
        this.videoUri = videoUri;
        this.videoName = videoName;
        this.videoDuration = videoDuration;
        this.videoPreviewImageUri = videoPreviewImageUri;
    }

    public String getVideoUri() {
        return videoUri;
    }

    public void setVideoUri(String videoUri) {
        this.videoUri = videoUri;
    }

    public String getVideoName() {
        return videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    public String getVideoPreviewImage() {
        return videoPreviewImageUri;
    }

    public void setVideoPreviewImage(String videoPreviewImage) {
        this.videoPreviewImageUri = videoPreviewImage;
    }

    public String getVideoDuration() {
        return videoDuration;
    }

    public void setVideoDuration(String videoDuration) {
        this.videoDuration = videoDuration;
    }
}
