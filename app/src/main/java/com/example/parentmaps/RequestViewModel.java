package com.example.parentmaps;

import androidx.annotation.NonNull;
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
import java.util.List;

public class RequestViewModel extends ViewModel {

    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference usersReference;
    private DatabaseReference friendshipsReference;
    private DatabaseReference friendReference;
    private MutableLiveData<FirebaseUser> user = new MutableLiveData<>();
    private MutableLiveData<List<User>> users = new MutableLiveData<>();

    public RequestViewModel() {
        auth = FirebaseAuth.getInstance();
        auth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user.setValue(firebaseAuth.getCurrentUser());
            }
        });
        database = FirebaseDatabase.getInstance();
        usersReference = database.getReference("Users");
        friendshipsReference = database.getReference("FriendRequests");
        friendshipsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                FirebaseUser currentUser = auth.getCurrentUser();
                if (currentUser == null) {
                    return;
                }
                List<Friendship> friendships = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Friendship friendship = dataSnapshot.getValue(Friendship.class);
                    if (friendship == null) {
                        continue;
                    }
                    if (friendship.getReceiverId().equals(currentUser.getUid())&&friendship.getStatus().equals("в ожидании")) {
                        friendships.add(friendship);
                    }
                }
                List<User> usersFromDb = new ArrayList<>();
                for (Friendship friendship : friendships) {
                    usersReference.child(friendship.getSenderId()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            User user = snapshot.getValue(User.class);
                            if (user == null) {
                                return;
                            }
                            usersFromDb.add(user);
                            users.setValue(usersFromDb);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public LiveData<List<User>> getUsers() {
        return users;
    }

    public MutableLiveData<FirebaseUser> getUser() {
        return user;
    }

    public void addUser(User user) {
        DatabaseReference friendshipsStatus = database.getReference("FriendRequests").child(user.getId()).child("status");
        friendshipsStatus.setValue("принят");

        FirebaseUser currentUser = auth.getCurrentUser();
        String currentUserId = currentUser.getUid();
        String addedUserId = user.getId();

        Friends friends = new Friends(currentUserId, addedUserId);
        database.getReference("Friends").child(currentUserId).setValue(friends);

        List<User> currentUsers = users.getValue();
        currentUsers.remove(user);
        users.setValue(currentUsers);
    }

    public void deleteUser(User user) {
        DatabaseReference friendshipsStatus = database.getReference("FriendRequests").child(user.getId()).child("status");
        friendshipsStatus.setValue("отклонен");

        List<User> currentUsers = users.getValue();
        currentUsers.remove(user);
        users.setValue(currentUsers);
    }
}
