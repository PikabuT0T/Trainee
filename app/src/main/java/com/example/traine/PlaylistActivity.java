package com.example.traine;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class PlaylistActivity extends AppCompatActivity {
    private VideoViewModel viewModel;
    private RecyclerView recyclerView;
    private ImageButton buttonMain, buttonToUploadVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);
        initializeUI();

        viewModel = new ViewModelProvider(this).get(VideoViewModel.class);

        setupRecyclerView();
    }

    private void initializeUI() {
        buttonMain = findViewById(R.id.buttonToMainActivity);
        buttonToUploadVideo = findViewById(R.id.buttonToUploadVideo);

        buttonMain.setOnClickListener(view -> startActivity(new Intent(PlaylistActivity.this, MenuActivity.class)));
        buttonToUploadVideo.setOnClickListener(view -> startActivity(new Intent(PlaylistActivity.this, UploadVideoActivity.class)));

        recyclerView = findViewById(R.id.video_rv);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupRecyclerView() {
        FirebaseRecyclerOptions<Member> options = new FirebaseRecyclerOptions.Builder<Member>()
                .setQuery(viewModel.getVideoQuery(), Member.class)
                .build();

        FirebaseRecyclerAdapter<Member, ViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Member, ViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ViewHolder viewHolder, int position, @NonNull Member member) {
                viewHolder.bindData(member);
                viewHolder.itemView.setOnClickListener(view -> viewModel.launchVideoPlayer(PlaylistActivity.this, member));
            }

            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_item, parent, false);
                return new ViewHolder(view);
            }
        };

        firebaseRecyclerAdapter.startListening();
        recyclerView.setAdapter(firebaseRecyclerAdapter);
    }
}

//public class PlaylistActivity extends AppCompatActivity {
//    private ImageButton buttonMain, buttonToUploadVideo;
//    private RecyclerView recyclerView;
//    private DatabaseReference videoReference;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_playlist);
//        initializeUI();
//        setupFirebase();
//    }
//
//    private void initializeUI() {
//        buttonMain = findViewById(R.id.buttonToMainActivity);
//        buttonToUploadVideo = findViewById(R.id.buttonToUploadVideo);
//
//        buttonMain.setOnClickListener(view -> startActivity(new Intent(PlaylistActivity.this, MenuActivity.class)));
//        buttonToUploadVideo.setOnClickListener(view -> startActivity(new Intent(PlaylistActivity.this, UploadVideoActivity.class)));
//
//        recyclerView = findViewById(R.id.video_rv);
//        recyclerView.setHasFixedSize(true);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//    }
//
//    private void setupFirebase() {
//        FirebaseDatabase db = FirebaseDatabase.getInstance("https://traine-11a25-default-rtdb.europe-west1.firebasedatabase.app/");
//        videoReference = db.getReference("Video");
//    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//        setupRecyclerView();
//    }
//
//    private void setupRecyclerView() {
//        FirebaseRecyclerOptions<Member> options = new FirebaseRecyclerOptions.Builder<Member>()
//                .setQuery(videoReference, Member.class)
//                .build();
//
//        FirebaseRecyclerAdapter<Member, ViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Member, ViewHolder>(options) {
//            @Override
//            protected void onBindViewHolder(@NonNull ViewHolder viewHolder, int position, @NonNull Member member) {
//                viewHolder.bindData(member);
//                viewHolder.itemView.setOnClickListener(view -> launchVideoPlayer(member));
//            }
//
//            @NonNull
//            @Override
//            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_item, parent, false);
//                return new ViewHolder(view);
//            }
//        };
//
//        firebaseRecyclerAdapter.startListening();
//        recyclerView.setAdapter(firebaseRecyclerAdapter);
//    }
//
//    private void launchVideoPlayer(Member member) {
//        Intent intent = new Intent(PlaylistActivity.this, VideoPlayerActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.putExtra("video_title", member.getVideoName());
//        intent.putExtra("video_uri", member.getVideoUri());
//        startActivity(intent);
//    }
//}

//public class PlaylistActivity extends AppCompatActivity {
//    private FirebaseDatabase db;
//    private DatabaseReference users, videoReference;
//    ImageButton buttonMain, buttonToUploadVideo;
//    public String videoUri, videoName;
//
//    public RelativeLayout root;
//
//    public RecyclerView recyclerView;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_playlist);
//
//        //VideoView videoView = findViewById(R.id.v);
//        //root = findViewById(R.id.main);
//        buttonMain = findViewById(R.id.buttonToMainActivity);
//        buttonToUploadVideo = findViewById(R.id.buttonToUploadVideo);
//        buttonMain.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(PlaylistActivity.this, MenuActivity.class);
//                startActivity(intent);
//            }
//        });
//        buttonToUploadVideo.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(PlaylistActivity.this, UploadVideoActivity.class);
//                startActivity(intent);
//            }
//        });
//
//        recyclerView = findViewById(R.id.video_rv);
//        recyclerView.setHasFixedSize(true);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        db = FirebaseDatabase.getInstance("https://traine-11a25-default-rtdb.europe-west1.firebasedatabase.app/");
//        videoReference = db.getReference("Video");
//    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//
//        FirebaseRecyclerOptions<Member> options =
//                new FirebaseRecyclerOptions.Builder<Member>()
//                        .setQuery(videoReference, Member.class)
//                        .build();
//
//        FirebaseRecyclerAdapter<Member, ViewHolder> firebaseRecyclerAdapter =
//                new FirebaseRecyclerAdapter<Member, ViewHolder>(options) {
//                    @Override
//                    protected void onBindViewHolder(@NonNull ViewHolder viewHolder, int i, @NonNull Member member) {
//
//                        viewHolder.setPlayerView(getApplication(), member.getVideoUri(), member.getVideoName(), member.getVideoPreviewImage(), member.getVideoDuration());
//
//                        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View view) {
//                                //viewHolder.setExoplayer(getApplication(), member.getVideoUri(), member.getVideoName());
//                                Intent intent = new Intent(getApplication(), VideoPlayerActivity.class);
//                                intent.putExtra("video_title", member.getVideoName());
//                                intent.putExtra("video_uri", member.getVideoUri());
//
//                                getApplication().startActivity(intent);
//                            }
//                        });
//                    }
//
//                    @NonNull
//                    @Override
//                    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_item, parent, false);
//
//                        return new ViewHolder(view);
//                    }
//                };
//
//        firebaseRecyclerAdapter.startListening();
//        recyclerView.setAdapter(firebaseRecyclerAdapter);
//    }
//
//}
