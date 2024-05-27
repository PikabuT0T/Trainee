package com.example.traine;

import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import Models.User;

public class ProfileViewModel extends ViewModel {
    private UserRepository userRepository;
    private MutableLiveData<User> userLiveData;

    public ProfileViewModel() {
        userRepository = new UserRepository();
        userLiveData = new MutableLiveData<>();
        loadUser();
    }

    public LiveData<User> getUser() {
        return userLiveData;
    }

    public void updateUserInformation(String type, String newValue, String currentPassword) {
        userRepository.updateUserInformation(type, newValue, currentPassword, task -> {
            if (task.isSuccessful()) {
                loadUser(); // Refresh user data
            }
        });
    }

    public void uploadImage(Uri imageUri) {
        userRepository.uploadImage(imageUri, task -> {
            if (task.isSuccessful()) {
                loadUser(); // Refresh user data
            }
        });
    }

    public void setUser(User user) {
        userLiveData.setValue(user);
    }

    private void loadUser() {
        userRepository.getUser().observeForever(user -> {
            if (user != null) {
                userLiveData.setValue(user);
            }
        });
    }

    public void detachListener() {
        userRepository.detachListener();
    }
}











//public class ProfileViewModel extends ViewModel {
//    private MutableLiveData<User> user = new MutableLiveData<>();
//    private UserModel userModel;
//
//    public ProfileViewModel() {
//        userModel = new UserModel();
//        attachDatabaseReadListener();
//    }
//
//    public LiveData<User> getUser() {
//        return user;
//    }
//
//    public void updateUser(User updatedUser) {
//        userModel.updateUser(updatedUser, new UserModel.UserDataListener() {
//            @Override
//            public void onDataLoaded(User user) {
//                ProfileViewModel.this.user.setValue(user);
//            }
//
//            @Override
//            public void onDataUpdated(String profileUri) {
//                User currentUser = ProfileViewModel.this.user.getValue();
//                if (currentUser != null) {
//                    currentUser.setProfileUri(profileUri);
//                    ProfileViewModel.this.user.setValue(currentUser);
//                }
//            }
//
//            @Override
//            public void onError(String errorMessage) {
//                // Обработка ошибок
//            }
//        });
//    }
//
//    public void uploadImage(Uri imageUri) {
//        userModel.uploadImage(imageUri, new UserModel.UserDataListener() {
//            @Override
//            public void onDataLoaded(User user) {
//                ProfileViewModel.this.user.setValue(user);
//            }
//
//            @Override
//            public void onDataUpdated(String profileUri) {
//                User currentUser = ProfileViewModel.this.user.getValue();
//                if (currentUser != null) {
//                    currentUser.setProfileUri(profileUri);
//                    ProfileViewModel.this.user.setValue(currentUser);
//                }
//            }
//
//            @Override
//            public void onError(String errorMessage) {
//                // Обработка ошибок загрузки изображения
//            }
//        });
//    }
//
//    private void attachDatabaseReadListener() {
//        userModel.attachDatabaseReadListener(new UserModel.UserDataListener() {
//            @Override
//            public void onDataLoaded(User user) {
//                ProfileViewModel.this.user.setValue(user);
//            }
//
//            @Override
//            public void onDataUpdated(String profileUri) {
//                User currentUser = ProfileViewModel.this.user.getValue();
//                if (currentUser != null) {
//                    currentUser.setProfileUri(profileUri);
//                    ProfileViewModel.this.user.setValue(currentUser);
//                }
//            }
//
//            @Override
//            public void onError(String errorMessage) {
//                // Обработка ошибок
//            }
//        });
//    }
//
//    @Override
//    protected void onCleared() {
//        userModel.detachDatabaseReadListener();
//        super.onCleared();
//    }
//}

