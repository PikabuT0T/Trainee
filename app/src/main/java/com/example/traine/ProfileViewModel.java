package com.example.traine;

import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.material.snackbar.Snackbar;

import Models.User;

public class ProfileViewModel extends ViewModel {
    private Context context;
    public static final int PICK_IMAGE = 1;
    private UserModel userModel;
    private MutableLiveData<User> user = new MutableLiveData<>();

    public ProfileViewModel() {
        userModel = new UserModel();
        userModel.attachDatabaseReadListener(new UserModel.UserDataListener() {
            @Override
            public void onDataLoaded(User user) {
                ProfileViewModel.this.user.setValue(user);
            }
            @Override
            public void onDataUpdated(String profileUri) {
                User currentUser = user.getValue();
                if (currentUser != null) {
                    currentUser.setProfileUri(profileUri);
                    user.setValue(currentUser); // Обновляем LiveData, что в свою очередь обновит UI
                }
            }
            @Override
            public void onError(String errorMessage) {
                // Обработка ошибок, например, через LiveData для ошибок
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    public LiveData<User> getUser() {
        return user;
    }


    public void uploadImage(Uri imageUri) {
        userModel.uploadImage(imageUri, new UserModel.UserDataListener() {
            @Override
            public void onDataLoaded(User user) {
                ProfileViewModel.this.user.setValue(user);
            }
            @Override
            public void onDataUpdated(String profileUri) {
                User currentUser = user.getValue();
                if (currentUser != null) {
                    currentUser.setProfileUri(profileUri);
                    user.setValue(currentUser); // Обновляем LiveData, что в свою очередь обновит UI
                }
            }
            @Override
            public void onError(String errorMessage) {
                // Обработка ошибок загрузки изображения
            }
        });
    }

    @Override
    protected void onCleared() {
        userModel.detachDatabaseReadListener(); // Отсоединяем слушателя при уничтожении ViewModel
    }
}