package com.example.traine;

public class Member {
    String videoUri;
    String videoName;

    public Member() {
    }

    public Member(String videoUri, String videoName) {
        this.videoUri = videoUri;
        this.videoName = videoName;
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
}
