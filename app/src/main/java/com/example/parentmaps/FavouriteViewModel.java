package com.example.parentmaps;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

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

public class FavouriteViewModel extends AndroidViewModel {
    private FirebaseDatabase database;
    private DatabaseReference childReference;
    private MutableLiveData<List<String>> addresses = new MutableLiveData<>();

    private FirebaseAuth auth;

    public FavouriteViewModel(Application application, String childId) {
        super(application);
        database = FirebaseDatabase.getInstance();
        childReference = database.getReference("Notificate").child(childId);
        List<String> adressesDB = new ArrayList<>();
        childReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                snapshot.child("Locate").getChildren().forEach((addressSnapshot) -> {
                    String address = addressSnapshot.getValue(String.class);
                    adressesDB.add(address);
                });
                addresses.setValue(adressesDB);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public LiveData<List<String>> getAddresses() {
        return addresses;
    }
}
