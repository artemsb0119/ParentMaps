package com.example.parentmaps;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button buttonLogin;
    private TextView textViewForgotPassword;
    private TextView textViewRegister;
    private FirebaseDatabase database;
    private DatabaseReference usersReference;

    private LoginViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        initViews();
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        observeViewModel();
        setupClickListeners();
    }

    private void observeViewModel() {
        viewModel.getError().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                Toast.makeText(LoginActivity.this, s, Toast.LENGTH_SHORT).show();
            }
        });
        viewModel.getUser().observe(this, new Observer<FirebaseUser>() {
            @Override
            public void onChanged(FirebaseUser firebaseUser) {
                if (firebaseUser != null) {
                    database = FirebaseDatabase.getInstance();
                    usersReference = database.getReference("Users");
                    usersReference.child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.getValue(User.class);
                            if (user != null) {
                                if (user.getChildOrParent().equals("Ребенок")) {
                                    Intent intent = MapsActivity.newIntent(LoginActivity.this);
                                    startActivity(intent);
                                    finish();
                                } else if (user.getChildOrParent().equals("Родитель")) {
                                    Intent intent = UsersActivity.newIntent(LoginActivity.this);
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }
        });
    }

    private void setupClickListeners() {
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!editTextEmail.getText().toString().equals("") && !editTextPassword.getText().toString().equals("")) {
                    String email = editTextEmail.getText().toString().trim();
                    String password = editTextPassword.getText().toString().trim();
                    viewModel.login(email, password);
                } else {
                    Toast.makeText(LoginActivity.this, getResources().getString(R.string.fill_in_all_fields), Toast.LENGTH_SHORT).show();
                }
            }
        });
        textViewForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = ResetPasswordActivity.newIntent(LoginActivity.this, editTextEmail.getText().toString().trim());
                startActivity(intent);
            }
        });
        textViewRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = RegistrationActivity.newIntent(LoginActivity.this);
                startActivity(intent);
            }
        });
    }

    private void initViews() {
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewForgotPassword = findViewById(R.id.textViewForgotPassword);
        textViewRegister = findViewById(R.id.textViewRegister);
    }

    public static Intent newIntent(Context context) {
        return new Intent(context, LoginActivity.class);
    }
}