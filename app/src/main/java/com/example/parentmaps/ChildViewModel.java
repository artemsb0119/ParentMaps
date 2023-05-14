package com.example.parentmaps;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChildViewModel extends AndroidViewModel {
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference childReference;
    private MutableLiveData<FirebaseUser> user = new MutableLiveData<>();
    private MutableLiveData<String> nowLocate = new MutableLiveData<>();
    private MutableLiveData<List<String>> addresses = new MutableLiveData<>();

    public ChildViewModel(Application application, String childId) {
        super(application);
        auth = FirebaseAuth.getInstance();
        auth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user.setValue(firebaseAuth.getCurrentUser());
            }
        });
        database = FirebaseDatabase.getInstance();
        childReference = database.getReference("ChildLocation");
        List<String> adressesDB = new ArrayList<>();
        childReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                snapshot.child(childId).child("Adress").getChildren().forEach((addressSnapshot) -> {
                    Log.d("aaaaa", childId);
                    String address = addressSnapshot.getValue(String.class);
                    adressesDB.add(address);
                });
                if (!adressesDB.isEmpty()) {
                    nowLocate.setValue(adressesDB.get(adressesDB.size()-1).substring(12));
                    adressesDB.remove(adressesDB.size() - 1);
                    Collections.reverse(adressesDB);
                    addresses.setValue(adressesDB);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    public MutableLiveData<FirebaseUser> getUser() {
        return user;
    }

    public LiveData<List<String>> getAddresses() {
        return addresses;
    }
    public LiveData<String> getLocation() {
        return nowLocate;
    }
}
