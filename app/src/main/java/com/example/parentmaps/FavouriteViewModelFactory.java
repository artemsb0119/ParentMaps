package com.example.parentmaps;

import android.app.Application;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class FavouriteViewModelFactory implements ViewModelProvider.Factory {
    private Application mApplication;
    private String mParam;


    public FavouriteViewModelFactory(Application application, String param) {
        mApplication = application;
        mParam = param;
    }


    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        return (T) new FavouriteViewModel(mApplication, mParam);
    }
}