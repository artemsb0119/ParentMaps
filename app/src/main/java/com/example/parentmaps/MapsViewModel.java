package com.example.parentmaps;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MapsViewModel extends ViewModel {

    private FirebaseAuth auth;
    private MutableLiveData<FirebaseUser> user = new MutableLiveData<>();

    public MutableLiveData<FirebaseUser> getUser() {
        return user;
    }

    public MapsViewModel() {
        auth = FirebaseAuth.getInstance();
        auth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user.setValue(firebaseAuth.getCurrentUser());
            }
        });
    }

    public void logout() {
        auth.signOut();
    }
}
