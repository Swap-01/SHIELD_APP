package com.daluiamit.shield;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.daluiamit.shield.AppConfigUtils.CURRENT_LOGGED_IN_USER_KEY;
import static com.daluiamit.shield.AppConfigUtils.USER_LOGIN_STATE_KEY;
import static com.daluiamit.shield.AppConfigUtils.USER_LOGIN_STATE_STORAGE_FILE_NAME;

public class VerifyOTPActivity extends AppCompatActivity {
    private EditText inputCode1, inputCode2, inputCode3, inputCode4, inputCode5, inputCode6;
    private String verificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final SharedPreferences sharedPreferences = getSharedPreferences(USER_LOGIN_STATE_STORAGE_FILE_NAME, Context.MODE_PRIVATE);

        final boolean isLoggedIn = sharedPreferences.getBoolean(USER_LOGIN_STATE_KEY, false);
        final String currentUser = sharedPreferences.getString(CURRENT_LOGGED_IN_USER_KEY, null);

        // Checking whether any user already logged in or not
        if (Objects.nonNull(currentUser) && isLoggedIn) {
            Log.d(currentUser, "The user is already logged in, redirecting to Main Activity page");

            Intent intent=new Intent(VerifyOTPActivity.this, MainActivity.class);
            //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return;
        }

        setContentView(R.layout.activity_verify_otpactivity);
        TextView textMobile=findViewById(R.id.textMobile);
        textMobile.setText(String.format(
                "+91-%s",getIntent().getStringExtra("mobile")
        ));
        inputCode1 = findViewById(R.id.inputCode1);
        inputCode2 = findViewById(R.id.inputCode2);
        inputCode3 = findViewById(R.id.inputCode3);
        inputCode4 = findViewById(R.id.inputCode4);
        inputCode5 = findViewById(R.id.inputCode5);
        inputCode6 = findViewById(R.id.inputCode6);

        setupOTPInputs();
        final ProgressBar progressBar=findViewById(R.id.progressBar1);
        final Button buttonVerify=findViewById(R.id.buttonVerifyOTP);

        verificationId=getIntent().getStringExtra("verificationId");

        buttonVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inputCode1.getText().toString().trim().isEmpty()
                        || inputCode2.getText().toString().trim().isEmpty()
                        || inputCode3.getText().toString().trim().isEmpty()
                        || inputCode4.getText().toString().trim().isEmpty()
                        || inputCode5.getText().toString().trim().isEmpty()
                        || inputCode6.getText().toString().trim().isEmpty()){
                    Toast.makeText(VerifyOTPActivity.this, "Please enter valid code", Toast.LENGTH_SHORT).show();
                    return;
                }
                String code=
                        inputCode1.getText().toString()+
                                inputCode2.getText().toString()+
                                inputCode3.getText().toString()+
                                inputCode4.getText().toString()+
                                inputCode5.getText().toString()+
                                inputCode6.getText().toString();

                if (verificationId != null) {
                    progressBar.setVisibility(View.VISIBLE);
                    buttonVerify.setVisibility(View.INVISIBLE);
                    PhoneAuthCredential phoneAuthCredential= PhoneAuthProvider.getCredential(verificationId,code);
                    FirebaseAuth.getInstance().signInWithCredential(phoneAuthCredential)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    progressBar.setVisibility(View.VISIBLE);
                                    buttonVerify.setVisibility(View.INVISIBLE);
                                    if(task.isSuccessful()){
                                        // After verifying, we need keep the user state as logged in into the system
                                        final String loggedInUser = getIntent().getStringExtra("mobile");

                                        final SharedPreferences sharedPreferences = getSharedPreferences(USER_LOGIN_STATE_STORAGE_FILE_NAME, Context.MODE_PRIVATE);

                                        final SharedPreferences.Editor editor = sharedPreferences.edit();

                                        editor.putString(CURRENT_LOGGED_IN_USER_KEY, loggedInUser);
                                        editor.putBoolean(USER_LOGIN_STATE_KEY, true);

                                        editor.apply();

                                        Intent intent=new Intent(VerifyOTPActivity.this,MainActivity.class);
                                        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                    }
                                    else {
                                        Toast.makeText(VerifyOTPActivity.this, "The verification code entered was invalid", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                }
            }
        });

        findViewById(R.id.textResendOTP).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                        "+91"+getIntent().getStringExtra("mobile"),
                        60,
                        TimeUnit.SECONDS,
                        VerifyOTPActivity.this,
                        new PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                            }

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {

                                Toast.makeText(VerifyOTPActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onCodeSent(@NonNull String newverificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                verificationId=newverificationId;
                                Toast.makeText(VerifyOTPActivity.this, "otp sent", Toast.LENGTH_SHORT).show();
                            }
                        }


                );

            }
        });
    }


    private void setupOTPInputs() {
        inputCode1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()) {
                    inputCode2.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        inputCode2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()) {
                    inputCode3.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        inputCode3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()) {
                    inputCode4.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        inputCode4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()) {
                    inputCode5.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        inputCode5.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()) {
                    inputCode6.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
}
