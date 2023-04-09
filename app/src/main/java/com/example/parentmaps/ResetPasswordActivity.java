package com.example.parentmaps;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

public class ResetPasswordActivity extends AppCompatActivity {

    private static final String EXTRA_EMAIL = "email";

    private EditText editTextEmail;
    private Button buttonResetPassword;

    private ResetPasswordViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        initViews();
        viewModel = new ViewModelProvider(this).get(ResetPasswordViewModel.class);
        observeViewModel();
        String email = getIntent().getStringExtra(EXTRA_EMAIL);
        editTextEmail.setText(email);
        buttonResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!editTextEmail.getText().toString().equals("")) {
                    String email = editTextEmail.getText().toString().trim();
                    viewModel.resetPassword(email);
                } else {
                    Toast.makeText(ResetPasswordActivity.this, getResources().getString(R.string.fill_in_all_fields), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void observeViewModel() {
        viewModel.getError().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if (s != null) {
                    Toast.makeText(ResetPasswordActivity.this, s, Toast.LENGTH_SHORT).show();
                }
            }
        });
        viewModel.isSuccess().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean){
                    Toast.makeText(ResetPasswordActivity.this, R.string.reset_link, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void initViews() {
        editTextEmail = findViewById(R.id.editTextEmail);
        buttonResetPassword = findViewById(R.id.buttonResetPassword);
    }

    public static Intent newIntent(Context context, String email) {
        Intent intent = new Intent(context, ResetPasswordActivity.class);
        intent.putExtra(EXTRA_EMAIL, email);
        return intent;
    }
}