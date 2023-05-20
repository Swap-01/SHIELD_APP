package com.daluiamit.shield;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.daluiamit.shield.AppConfigUtils.CURRENT_LOGGED_IN_USER_KEY;
import static com.daluiamit.shield.AppConfigUtils.USER_LOGIN_STATE_KEY;
import static com.daluiamit.shield.AppConfigUtils.USER_LOGIN_STATE_STORAGE_FILE_NAME;
public class sendOTPActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final SharedPreferences sharedPreferences = getSharedPreferences(USER_LOGIN_STATE_STORAGE_FILE_NAME, Context.MODE_PRIVATE);

        final boolean isLoggedIn = sharedPreferences.getBoolean(USER_LOGIN_STATE_KEY, false);
        final String currentUser = sharedPreferences.getString(CURRENT_LOGGED_IN_USER_KEY, null);

        // Checking whether any user already logged in or not
        if (Objects.nonNull(currentUser) && isLoggedIn) {
            Log.d(currentUser, "The user is already logged in, redirecting to Main Activity page");

            Intent intent=new Intent(sendOTPActivity.this, MainActivity.class);
            //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return;
        }


        setContentView(R.layout.activity_send_otpactivity);
        final EditText inputMobile=findViewById(R.id.inputMobile);
        Button buttonGetOTP=findViewById(R.id.buttonGetOTP);
        final ProgressBar progressBar=findViewById(R.id.progressBar);

        buttonGetOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inputMobile.getText().toString().trim().isEmpty()) {
                    Toast.makeText(sendOTPActivity.this, "Enter Mobile number", Toast.LENGTH_SHORT).show();
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);
                buttonGetOTP.setVisibility(View.INVISIBLE);

                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                        "+91"+inputMobile.getText().toString(),
                        60,
                        TimeUnit.SECONDS,
                        sendOTPActivity.this,
                        new PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                                progressBar.setVisibility(View.GONE);
                                buttonGetOTP.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {
                                progressBar.setVisibility(View.GONE);
                                buttonGetOTP.setVisibility(View.VISIBLE);
                                Toast.makeText(sendOTPActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                progressBar.setVisibility(View.GONE);
                                buttonGetOTP.setVisibility(View.VISIBLE);
                                Intent intent=new Intent(getApplicationContext(),VerifyOTPActivity.class);
                                intent.putExtra("mobile",inputMobile.getText().toString());
                                intent.putExtra("verificationId",verificationId);
                                startActivity(intent);
                            }
                        }


                );

            }
        });

    }
}