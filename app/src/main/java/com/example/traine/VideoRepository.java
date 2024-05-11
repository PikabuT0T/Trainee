package com.example.traine;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class VideoRepository {
    private DatabaseReference videoReference;

    public VideoRepository() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        videoReference = db.getReference("Video");
    }

    public Query getVideoQuery() {
        return videoReference;
    }
}
