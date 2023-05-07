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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SearchViewModel extends ViewModel {

    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference usersReference;
    private DatabaseReference friendshipsReference;
    private MutableLiveData<FirebaseUser> user = new MutableLiveData<>();
    private MutableLiveData<List<User>> users = new MutableLiveData<>();

    public SearchViewModel() {
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
        usersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                FirebaseUser currentUser = auth.getCurrentUser();
                if (currentUser == null) {
                    return;
                }
                List<User> usersFromDb = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user == null) {
                        return;
                    }
                    if (!user.getId().equals(currentUser.getUid()) && user.getChildOrParent().equals("Ребенок")) {
                        final String userId = user.getId();
                        friendshipsReference.orderByChild("receiverId").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                boolean isFriend = false;
                                for (DataSnapshot friendshipSnapshot : snapshot.getChildren()) {
                                    Friendship friendship = friendshipSnapshot.getValue(Friendship.class);
                                    if (friendship == null || friendship.getSenderId() == null) {
                                        continue;
                                    }
                                    if (friendship.getSenderId().equals(currentUser.getUid()) && friendship.getStatus().equals("принят")) {
                                        isFriend = true;
                                        break;
                                    }
                                }
                                if (!isFriend) {
                                    usersFromDb.add(user);
                                }
                                users.setValue(usersFromDb);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
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

    public void logout() {
        auth.signOut();
    }

    public void request(User user) {
        friendshipsReference = database.getReference("FriendRequests");
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            return;
        }
        Friendship friendship = new Friendship(
                currentUser.getUid(),
                user.getId(),
                "в ожидании"
        );
        friendshipsReference.child(currentUser.getUid()).setValue(friendship);
    }
    public void search(String query) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            return;
        }
        Query queryRef = usersReference.orderByChild("childOrParent").equalTo("Ребенок");
        if (!query.isEmpty()) {
            queryRef = queryRef.orderByChild("name").startAt(query).endAt(query + "\uf8ff");
        }
        queryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<User> usersFromDb = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user == null || user.getId().equals(currentUser.getUid())) {
                        continue;
                    }
                    boolean isFriend = false;
                    for (DataSnapshot friendshipSnapshot : dataSnapshot.child("friendships").getChildren()) {
                        Friendship friendship = friendshipSnapshot.getValue(Friendship.class);
                        if (friendship == null || friendship.getSenderId() == null) {
                            continue;
                        }
                        if (friendship.getSenderId().equals(currentUser.getUid()) && friendship.getStatus().equals("принят")) {
                            isFriend = true;
                            break;
                        }
                    }
                    if (!isFriend) {
                        usersFromDb.add(user);
                    }
                }
                users.setValue(usersFromDb);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}
