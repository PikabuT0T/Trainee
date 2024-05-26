package com.example.traine;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.InetAddresses;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UploadVideoActivity extends AppCompatActivity {

    private static final int PICK_VIDEO = 1;
    Bitmap thumbnail;
    VideoView videoView;
    ImageButton buttonMain;
    private CheckBox checkBoxLegs, checkBoxArms, checkBoxAbs;
    private CheckBox pectoralis_major, pectoralis_minor, latissimus_dorsi, trapezius, rhomboids,
            anterior_deltoid, lateral_deltoid, posterior_deltoid, biceps_brachii, triceps_brachii,
            quadriceps, hamstrings, rectus_abdominis, external_obliques, internal_obliques, transverse_abdominis;
    TextView chooseVideoView, showVideoView, durationTime;
    ImageView videoImage;
    TextView buttonUpload;
    ProgressBar progressBar;
    EditText editText;
    private Uri videoUri;
    MediaController mediaController;
    FirebaseStorage storage;
    FirebaseDatabase database;
    StorageReference storageVideoReference, storageImageReference;
    DatabaseReference databaseReference;
    Member member;
    UploadTask uploadTaskVideo, uploadTaskImage;
    byte[] imageData;
    LayoutInflater inflater;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upload_video_layout);

        videoView = findViewById(R.id.videoview_upload);
        buttonUpload = findViewById(R.id.button_upload_video);
        progressBar = findViewById(R.id.progressBar_upload);
        editText = findViewById(R.id.upload_videoname);
        chooseVideoView = findViewById(R.id.choose_video_and_upload);
        showVideoView = findViewById(R.id.show_video_playlist);
        durationTime = findViewById(R.id.testShow);
        videoImage = findViewById(R.id.image_video);
//        checkBoxLegs = findViewById(R.id.checkBox_legs);
//        checkBoxArms = findViewById(R.id.checkBox_arms);
//        checkBoxAbs = findViewById(R.id.checkBox_abs);
        mediaController = new MediaController(this);
        inflater = LayoutInflater.from(this);

        member = new Member();
        storage = FirebaseStorage.getInstance();
        storageImageReference = storage.getReference("images").child("videoPreview");
        storageVideoReference = storage.getReference("video");

        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("Video");

        buttonMain = findViewById(R.id.buttonToMainActivity);
        buttonMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UploadVideoActivity.this, PlaylistActivity.class);
                startActivity(intent);
            }
        });




        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                int duration = mediaPlayer.getDuration();
                String durationString = timeConversion(duration);
                member.setVideoDuration(durationString);
                // Выводим длительность в TextView
                durationTime.setText("Длительность: " + durationString);
            }
        });

        videoView.setMediaController(mediaController);
        videoView.start();

        chooseVideoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChooseVideo();
            }
        });

        showVideoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowVideo();
            }
        });

        buttonUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UploadVideo();
            }
        });

        findViewById(R.id.textView4).setOnClickListener(view -> showMusclesWindow());
    }

    private void showMusclesWindow() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        View sign_in_window = inflater.inflate(R.layout.find_muscles_grupe_form, null);

        pectoralis_major = sign_in_window.findViewById(R.id.checkBox_pectoralis_major);
        pectoralis_minor = sign_in_window.findViewById(R.id.checkBox_pectoralis_minor);
        latissimus_dorsi = sign_in_window.findViewById(R.id.checkBox_latissimus_dorsi);
        trapezius = sign_in_window.findViewById(R.id.checkBox_trapezius);
        rhomboids = sign_in_window.findViewById(R.id.checkBox_rhomboids);
        anterior_deltoid = sign_in_window.findViewById(R.id.checkBox_anterior_deltoid);
        lateral_deltoid = sign_in_window.findViewById(R.id.checkBox_lateral_deltoid);
        posterior_deltoid = sign_in_window.findViewById(R.id.checkBox_posterior_deltoid);
        biceps_brachii = sign_in_window.findViewById(R.id.checkBox_biceps_brachii);
        triceps_brachii = sign_in_window.findViewById(R.id.checkBox_triceps_brachii);
        quadriceps = sign_in_window.findViewById(R.id.checkBox_quadriceps);
        hamstrings = sign_in_window.findViewById(R.id.checkBox_hamstrings);
        rectus_abdominis = sign_in_window.findViewById(R.id.checkBox_rectus_abdominis);
        external_obliques = sign_in_window.findViewById(R.id.checkBox_external_obliques);
        internal_obliques = sign_in_window.findViewById(R.id.checkBox_internal_obliques);
        transverse_abdominis = sign_in_window.findViewById(R.id.checkBox_transverse_abdominis);

        dialog.setView(sign_in_window);
        dialog.setPositiveButton("Застосувати", (dialogInterface, i) -> UploadVideo());
        dialog.setNegativeButton("Назад", (dialogInterface, i) -> dialogInterface.dismiss());

        dialog.setView(sign_in_window);
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_VIDEO || requestCode == RESULT_OK ||
                data != null || data.getData() != null){
            videoUri = data.getData();

            videoView.setVideoURI(videoUri);

            try {
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(this, videoUri);
                thumbnail = retriever.getFrameAtTime(6000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                retriever.release();
                videoImage.setImageBitmap(thumbnail);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                imageData = baos.toByteArray();
            }catch (IOException e){

            }
            videoView.start();
        }
    }

    public void ChooseVideo(){

        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_VIDEO);

    }

    private String getExt(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    public void ShowVideo(){
        Intent intent = new Intent(UploadVideoActivity.this, PlaylistActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void UploadVideo(){
        String videoName = editText.getText().toString();
        List<String> tags = new ArrayList<>();

        if (pectoralis_major.isChecked()) tags.add("pectoralis_major");
        if (pectoralis_minor.isChecked()) tags.add("pectoralis_minor");
        if (latissimus_dorsi.isChecked()) tags.add("latissimus_dorsi");
        if (trapezius.isChecked()) tags.add("trapezius");
        if (rhomboids.isChecked()) tags.add("rhomboids");
        if (anterior_deltoid.isChecked()) tags.add("anterior_deltoid");
        if (lateral_deltoid.isChecked()) tags.add("lateral_deltoid");
        if (posterior_deltoid.isChecked()) tags.add("posterior_deltoid");
        if (biceps_brachii.isChecked()) tags.add("biceps_brachii");
        if (triceps_brachii.isChecked()) tags.add("triceps_brachii");
        if (quadriceps.isChecked()) tags.add("quadriceps");
        if (hamstrings.isChecked()) tags.add("hamstrings");
        if (rectus_abdominis.isChecked()) tags.add("rectus_abdominis");
        if (external_obliques.isChecked()) tags.add("external_obliques");
        if (internal_obliques.isChecked()) tags.add("internal_obliques");
        if (transverse_abdominis.isChecked()) tags.add("transverse_abdominis");

        String tagsString = String.join(",", tags);
        Log.d("Search Exercises", tagsString);
        member.setVideoTags(tagsString);

        if (videoUri != null || imageData != null){
            if(TextUtils.isEmpty(editText.getText().toString())){
                Toast.makeText(UploadVideoActivity.this, "Вкажіть назву для відео", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.INVISIBLE);
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            //завантажуємо "прев'ю" для відео
            final StorageReference referenceImage = storageImageReference.child(videoName + "_" + System.currentTimeMillis() +
                    ".JPEG");
            uploadTaskImage = referenceImage.putBytes(imageData);

            uploadTaskImage.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    referenceImage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String thumbnailUrl = uri.toString();
                            member.setVideoPreviewImage(thumbnailUrl);
                        }
                    });
                }
            });


            //завантажуємо саме відео
            final StorageReference referenceVideo = storageVideoReference.child(System.currentTimeMillis() +
                    "." + getExt(videoUri));
            uploadTaskVideo = referenceVideo.putFile(videoUri);

            Task<Uri> uriVideoTask = uploadTaskVideo.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if(!task.isSuccessful()){
                        throw task.getException();
                    }
                    return referenceVideo.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(UploadVideoActivity.this, "Data saved", Toast.LENGTH_SHORT).show();
                        member.setVideoName(videoName);
                        member.setVideoUri(downloadUri.toString());
                        member.getVideoDuration();
                        member.getVideoPreviewImage();
                        member.getVideoTags();
                        String i = databaseReference.push().getKey();
                        databaseReference.child(i).setValue(member);
                    } else {
                        Toast.makeText(UploadVideoActivity.this, "Failed. Check your connections", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        } else {
            Toast.makeText(UploadVideoActivity.this, "Оберіть відео та вкажіть назву для нього", Toast.LENGTH_SHORT).show();
        }
    }

    public String timeConversion(int value) {
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

        return videoTime;
    }
}
