package com.example.traine;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import Models.User;

//public class UserModel {
//    private DatabaseReference users;
//    private ValueEventListener usersEventListener;
//    private StorageReference storageRef;
//    private FirebaseAuth auth;
//
//    public interface UserDataListener {
//        void onDataLoaded(User user);
//        void onDataUpdated(String profileUri);
//        void onError(String errorMessage);
//    }
//
//    public UserModel() {
//        initFirebase();
//    }
//
//    public void initFirebase() {
//        auth = FirebaseAuth.getInstance();
//        String uid = auth.getCurrentUser().getUid();
//        users = FirebaseDatabase.getInstance().getReference("Users").child(uid);
//        storageRef = FirebaseStorage.getInstance().getReference("images").child("prof").child(uid).child("profile_image.jpg");
//    }
//
//    public void attachDatabaseReadListener(UserDataListener listener) {
//        if (usersEventListener == null) {
//            usersEventListener = new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                    User currentUser = dataSnapshot.getValue(User.class);
//                    listener.onDataLoaded(currentUser);
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError error) {
//                    listener.onError(error.getMessage());
//                }
//            };
//            users.addValueEventListener(usersEventListener);
//        }
//    }
//
//    public void detachDatabaseReadListener() {
//        if (usersEventListener != null) {
//            users.removeEventListener(usersEventListener);
//            usersEventListener = null;
//        }
//    }
//
//    public void uploadImage(Uri imageUri, UserDataListener listener) {
//        storageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
//            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
//                String newUri = uri.toString();
//                users.child("profileUri").setValue(newUri).addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        listener.onDataUpdated(newUri);
//                    } else {
//                        listener.onError(task.getException().getMessage());
//                    }
//                });
//            }).addOnFailureListener(e -> listener.onError(e.getMessage()));
//        }).addOnFailureListener(e -> listener.onError(e.getMessage()));
//    }
//
//    public void updateUser(User updatedUser, UserDataListener listener) {
//        users.setValue(updatedUser).addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                listener.onDataLoaded(updatedUser);
//            } else {
//                listener.onError(task.getException().getMessage());
//            }
//        });
//    }
//}

