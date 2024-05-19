package com.example.traine;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.HashMap;
import java.util.Map;

public class VideoRepository {
    private DatabaseReference videoReference;

    public VideoRepository() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        videoReference = db.getReference("Video");
    }

    public Query getVideoQuery() {
        return videoReference;
    }

    public Query getFilteredVideoQuery(String tag) {
        return videoReference.orderByChild("videoTags").equalTo(tag);
    }

}
//public class VideoRepository {
//    private DatabaseReference videoReference;
//    private static final int LOAD_LIMIT = 1000;  // Можете настроить это значение в соответствии с вашими нуждами
//
//    public VideoRepository() {
//        FirebaseDatabase db = FirebaseDatabase.getInstance();
//        videoReference = db.getReference("Video");
//    }
//
//    // Метод для получения начального запроса
//    public Query getInitialVideoQuery() {
//        return videoReference.orderByKey().limitToFirst(LOAD_LIMIT);
//    }
//
//    // Метод для получения следующего запроса начиная с последнего загруженного ключа
//    public Query getNextVideoQuery(String lastLoadedVideoKey) {
//        if (lastLoadedVideoKey == null) {
//            return null;  // если ключ не задан, значит, это был последний элемент или ошибка
//        }
//        return videoReference.orderByKey().startAfter(lastLoadedVideoKey).limitToFirst(LOAD_LIMIT);
//    }
//
//}
