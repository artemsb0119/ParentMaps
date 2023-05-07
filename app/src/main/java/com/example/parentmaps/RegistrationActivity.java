package com.example.parentmaps;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegistrationActivity extends AppCompatActivity {

    private EditText editTextEmail;
    private EditText editTextPassword;
    private EditText editTextName;
    private EditText editTextLastName;
    private RadioGroup radioGroupReg;
    private RadioButton radioButtonParent;
    private RadioButton radioButtonChild;
    private Button buttonSignUp;

    private RegistrationViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        initViews();
        viewModel = new ViewModelProvider(this).get(RegistrationViewModel.class);
        observeViewModel();
        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!editTextEmail.getText().toString().equals("")&&
                        !editTextPassword.getText().toString().equals("")&&
                        !editTextName.getText().toString().equals("")&&
                        !editTextLastName.getText().toString().equals("")) {
                    String email = getTrimmedValue(editTextEmail);
                    String password = getTrimmedValue(editTextPassword);
                    String name = getTrimmedValue(editTextName);
                    String lastName = getTrimmedValue(editTextLastName);
                    int selectedId = radioGroupReg.getCheckedRadioButtonId();
                    RadioButton radioButton = findViewById(selectedId);
                    String selectedText = radioButton.getText().toString();
                    viewModel.signUp(email, password, name, lastName, selectedText);
                } else {
                    Toast.makeText(RegistrationActivity.this, getResources().getString(R.string.fill_in_all_fields), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void observeViewModel() {
        viewModel.getError().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if (s!=null) {
                    Toast.makeText(RegistrationActivity.this, s, Toast.LENGTH_SHORT).show();
                }
            }
        });
        viewModel.getUser().observe(this, new Observer<FirebaseUser>() {
            @Override
            public void onChanged(FirebaseUser firebaseUser) {
                if (firebaseUser != null) {
                    Intent intent = UsersActivity.newIntent(RegistrationActivity.this);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    private void initViews() {
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextName = findViewById(R.id.editTextName);
        editTextLastName = findViewById(R.id.editTextLastName);
        radioButtonParent = findViewById(R.id.radioButtonParent);
        radioButtonChild = findViewById(R.id.radioButtonChild);
        buttonSignUp = findViewById(R.id.buttonSignUp);
        radioGroupReg = findViewById(R.id.radioGroupReg);
    }

    private String getTrimmedValue(EditText editText) {
        return editText.getText().toString().trim();
    }

    public static Intent newIntent(Context context) {
        return new Intent(context, RegistrationActivity.class);
    }
}