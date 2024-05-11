package com.example.traine;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import Models.User;

public class MenuViewModel extends ViewModel {
    private MutableLiveData<User> userLiveData = new MutableLiveData<>();
    private UserModel userModel;
    public MenuViewModel() {
        userModel = new UserModel();
    }
    public void initFirebase() {

        userModel.initFirebase();
        userModel.attachDatabaseReadListener(new UserModel.UserDataListener() {
            @Override
            public void onDataLoaded(User user) {
                userLiveData.setValue(user);
            }

            @Override
            public void onDataUpdated(String profileUri) {

            }
            @Override
            public void onError(String successMessage) {
                // Обработка ошибки загрузки данных пользователя
            }
        });
    }

    public LiveData<User> getUser() {
        return userLiveData;
    }
    public LiveData<User> setUser(User user){
        userLiveData.setValue(user);
        return userLiveData;
    }

    @Override
    protected void onCleared() {
        userModel.detachDatabaseReadListener();
        super.onCleared();
    }
}

