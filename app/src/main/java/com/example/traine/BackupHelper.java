package com.example.traine;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

import Models.User;

public class BackupHelper {

    private Context context;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private User user;

    public BackupHelper(Context context, User user) {
        this.context = context;
        this.storage = FirebaseStorage.getInstance();
        this.storageReference = storage.getReference();
        this.user = user;
    }

    public void backupDatabaseToFirebase() {
        if (user == null) {
            Log.e("BackupHelper", "User is null");
            return;
        }

        File database = context.getDatabasePath("notes.db");

        if (database.exists()) {
            Uri file = Uri.fromFile(database);
            StorageReference databaseRef = storageReference.child("backups/" + user.getUid() + "/notes.db");

            databaseRef.putFile(file)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Log.d("Firebase", "Database backup successful");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("Firebase", "Database backup failed", e);
                        }
                    });
        } else {
            Log.e("BackupHelper", "Database file does not exist");
        }
    }

    public void downloadDatabaseFromFirebase(final BackupCallback callback) {
        if (user == null) {
            Log.e("BackupHelper", "User is null");
            callback.onFailure();
            return;
        }

        final File localFile = context.getDatabasePath("notes.db");
        StorageReference databaseRef = storageReference.child("backups/" + user.getUid() + "/notes.db");

        databaseRef.getFile(localFile)
                .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        Log.d("Firebase", "Database download successful");
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Firebase", "Database download failed", e);
                        callback.onFailure();
                    }
                });
    }

    public void checkDatabaseExists(final BackupCallback callback) {
        if (user == null) {
            Log.e("BackupHelper", "User is null");
            callback.onFailure();
            return;
        }

        StorageReference databaseRef = storageReference.child("backups/" + user.getUid() + "/notes.db");

        databaseRef.getMetadata()
                .addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                    @Override
                    public void onSuccess(StorageMetadata storageMetadata) {
                        // File exists
                        Log.d("Firebase", "Database file exists in storage");
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // File does not exist

                        Log.e("Firebase", "Database file does not exist in storage", e);
                        callback.onFailure();
                    }
                });
    }

    public interface BackupCallback {
        void onSuccess();
        void onFailure();
    }
}
