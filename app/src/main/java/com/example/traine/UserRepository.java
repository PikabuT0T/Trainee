package com.example.traine;

import android.app.Activity;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.concurrent.Executor;

import Models.User;

public class UserRepository {
    private FirebaseAuth auth;
    private DatabaseReference usersRef;
    private StorageReference storageRef;
    private ValueEventListener userEventListener;

    public UserRepository() {
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();

            usersRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
            storageRef = FirebaseStorage.getInstance().getReference("images").child("prof").child(uid).child("profile_image.jpg");
        }
    }

    public LiveData<User> getUser() {
        MutableLiveData<User> userLiveData = new MutableLiveData<>();
        if (usersRef != null) {
            userEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);
                    FirebaseUser currentUser = auth.getCurrentUser();
                    String uid = currentUser.getUid();
                    user.setUid(uid);
                    if (user != null) {
                        userLiveData.setValue(user);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle error
                }
            };
            usersRef.addValueEventListener(userEventListener);
        }
        return userLiveData;
    }

    public void detachListener() {
        if (usersRef != null && userEventListener != null) {
            usersRef.removeEventListener(userEventListener);
        }
    }

    public void updateUserInformation(String type, String newValue, String currentPassword, OnCompleteListener<Void> onCompleteListener) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            return;
        }

        Task<Void> updateTask;

        switch (type) {
            case "email":
                updateTask = user.updateEmail(newValue);
                break;
            case "password":
                updateTask = user.updatePassword(newValue);
                break;
            case "name":
                updateTask = usersRef.child("name").setValue(newValue);
                break;
            case "phone":
                updateTask = usersRef.child("phone").setValue(newValue);
                break;
            default:
                updateTask = usersRef.child(type).setValue(newValue);
                break;
        }

        updateTask.addOnCompleteListener(onCompleteListener);
    }

    public void uploadImage(Uri imageUri, OnCompleteListener<Uri> onCompleteListener) {
        if (storageRef != null) {
            storageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String newUri = uri.toString();
                    usersRef.child("profileUri").setValue(newUri).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            onCompleteListener.onComplete(new Task<Uri>() {
                                @Override
                                public boolean isComplete() {
                                    return true;
                                }

                                @Override
                                public boolean isSuccessful() {
                                    return true;
                                }

                                @Override
                                public boolean isCanceled() {
                                    return false;
                                }

                                @Override
                                public Uri getResult() {
                                    return uri;
                                }

                                @Override
                                public <X extends Throwable> Uri getResult(@NonNull Class<X> aClass) throws X {
                                    return uri;
                                }


                                @NonNull
                                @Override
                                public Task<Uri> addOnFailureListener(@NonNull OnFailureListener onFailureListener) {
                                    return null;
                                }

                                @NonNull
                                @Override
                                public Task<Uri> addOnFailureListener(@NonNull Activity activity, @NonNull OnFailureListener onFailureListener) {
                                    return null;
                                }

                                @NonNull
                                @Override
                                public Task<Uri> addOnFailureListener(@NonNull Executor executor, @NonNull OnFailureListener onFailureListener) {
                                    return null;
                                }

                                @NonNull
                                @Override
                                public Task<Uri> addOnSuccessListener(@NonNull OnSuccessListener<? super Uri> onSuccessListener) {
                                    return null;
                                }

                                @NonNull
                                @Override
                                public Task<Uri> addOnSuccessListener(@NonNull Activity activity, @NonNull OnSuccessListener<? super Uri> onSuccessListener) {
                                    return null;
                                }

                                @NonNull
                                @Override
                                public Task<Uri> addOnSuccessListener(@NonNull Executor executor, @NonNull OnSuccessListener<? super Uri> onSuccessListener) {
                                    return null;
                                }

                                @Override
                                public Exception getException() {
                                    return null;
                                }
                            });
                        }
                    });
                });
            });
        }
    }
}








