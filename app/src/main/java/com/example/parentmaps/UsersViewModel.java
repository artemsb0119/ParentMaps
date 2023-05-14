package com.example.parentmaps;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UsersViewModel extends ViewModel {

    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference usersReference;
    private DatabaseReference friendReference;
    private DatabaseReference notificateReference;

    private MutableLiveData<FirebaseUser> user = new MutableLiveData<>();

    private MutableLiveData<String> notificate = new MutableLiveData<>();
    private MutableLiveData<List<User>> users = new MutableLiveData<>();

    public UsersViewModel() {
        auth = FirebaseAuth.getInstance();
        auth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user.setValue(firebaseAuth.getCurrentUser());
            }
        });
        database = FirebaseDatabase.getInstance();
        usersReference = database.getReference("Users");
        friendReference = database.getReference("Friends");
        notificateReference = database.getReference("SendNotificate");
        friendReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                FirebaseUser currentUser = auth.getCurrentUser();
                if (currentUser == null) {
                    return;
                }
                List<Friends> friend = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Friends friends = dataSnapshot.getValue(Friends.class);
                    if (friends == null) {
                        continue;
                    }
                    if (friends.getUserId2().equals(currentUser.getUid())) {
                        friend.add(friends);
                    }
                }
                List<User> usersFromDb = new ArrayList<>();
                for (Friends friends : friend) {
                    usersReference.child(friends.getUserId1()).addValueEventListener(new ValueEventListener() {
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
                    notificateReference.child(friends.getUserId1()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            notificate.setValue(snapshot.getValue(String.class));
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

    public MutableLiveData<String> getNotificate() {
        return notificate;
    }
    public void logout() {
        auth.signOut();
    }
}
