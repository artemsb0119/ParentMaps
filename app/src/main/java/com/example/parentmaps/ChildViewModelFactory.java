package com.example.parentmaps;

import android.app.Application;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class ChildViewModelFactory implements ViewModelProvider.Factory {
    private Application mApplication;
    private String mParam;


    public ChildViewModelFactory(Application application, String param) {
        mApplication = application;
        mParam = param;
    }


    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        return (T) new ChildViewModel(mApplication, mParam);
    }
}
