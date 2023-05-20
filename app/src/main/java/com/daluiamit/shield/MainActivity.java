package com.daluiamit.shield;

import static com.daluiamit.shield.AppConfigUtils.CURRENT_LOGGED_IN_USER_KEY;
import static com.daluiamit.shield.AppConfigUtils.USER_LOGIN_STATE_KEY;
import static com.daluiamit.shield.AppConfigUtils.USER_LOGIN_STATE_STORAGE_FILE_NAME;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.ServerError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private static final String LOCATION_SAVE_URL = "https://shield.cyclic.app/users";
    private static final String TIME_FORMAT = "dd/MM/YYYY hh:mm:ss";

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(TIME_FORMAT);

    private FusedLocationProviderClient fusedLocationProviderClient;
    private Button getLocationBtn;
    private final static int REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getLocationBtn = findViewById(R.id.getLocationBtn);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        getLocationBtn.setOnClickListener(v -> {
            final SharedPreferences sharedPreferences = getSharedPreferences(USER_LOGIN_STATE_STORAGE_FILE_NAME, Context.MODE_PRIVATE);

            final boolean isLoggedIn = sharedPreferences.getBoolean(USER_LOGIN_STATE_KEY, false);
            final String currentUser = sharedPreferences.getString(CURRENT_LOGGED_IN_USER_KEY, null);

            // Checking whether any user already logged in or not
            if (Objects.nonNull(currentUser) && isLoggedIn) {
                Log.d(currentUser, "Getting the current location fot this user");

                getLastLocation(currentUser);
            } else {
                Log.e("system", "Could not find any user in local storage.");

                Toast.makeText(MainActivity.this, "Opps!! Something went wrong, please try again later.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getLastLocation(String phoneNo) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation()
                    .addOnFailureListener(e -> Log.e("amit", e.getMessage()))
                    .addOnSuccessListener((OnSuccessListener<Location>) location -> {
                        if (location != null) {
                            try {
                                Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

                                RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
                                Map<String, String> requestParams = new HashMap<>();

                                // Adding the location details into request param
                                requestParams.put("phone", phoneNo);
                                requestParams.put("latitude", String.valueOf(addresses.get(0).getLatitude()));
                                requestParams.put("longitude", String.valueOf(addresses.get(0).getLongitude()));
                                requestParams.put("time", DATE_FORMAT.format(new Date()));

                                queue.start();

                                queue.add(getLocationSaveRequestObj(phoneNo, requestParams));
                            } catch (IOException e) {
                                Log.e(phoneNo, e.getMessage(), e);
                            }
                        } else {
                            Log.e(phoneNo, "No location found!!");
                        }
                    });
        } else {
            askPermission();
        }
    }

    @NonNull
    private JsonObjectRequest getLocationSaveRequestObj(String phoneNo, Map<String, String> requestParams) {
        return new
                JsonObjectRequest(Request.Method.POST,
                LOCATION_SAVE_URL,
                new JSONObject(requestParams),
                response -> {
                    Toast.makeText(this, "Your current location is already shared with nearest police station. You will get an assistance shortly.",
                            Toast.LENGTH_LONG).show();
                }, error -> {
            Toast.makeText(this, "Opps!! Something went wrong. Please try again.", Toast.LENGTH_SHORT).show();
            NetworkResponse response = error.networkResponse;
            if (error instanceof ServerError && response != null) {
                try {
                    String res = new String(response.data,
                            HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                    // Now you can use any deserializer to make sense of data
                    JSONObject obj = new JSONObject(res);

                } catch (UnsupportedEncodingException | JSONException ex) {
                    Log.e(phoneNo, ex.getMessage(), ex);
                }
            }
        });
    }

    private void askPermission() {
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @org.jetbrains.annotations.NotNull String[] permissions, @NonNull @org.jetbrains.annotations.NotNull int[] grantResults) {
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                final SharedPreferences sharedPreferences = getSharedPreferences(USER_LOGIN_STATE_STORAGE_FILE_NAME, Context.MODE_PRIVATE);

                final boolean isLoggedIn = sharedPreferences.getBoolean(USER_LOGIN_STATE_KEY, false);
                final String currentUser = sharedPreferences.getString(CURRENT_LOGGED_IN_USER_KEY, null);

                // Checking whether any user already logged in or not
                if (Objects.nonNull(currentUser) && isLoggedIn) {
                    Log.d(currentUser, "Permission granted. Getting the current location fot this user");
                    getLastLocation(currentUser);
                } else {
                    Log.e("system", "Could not find any user in local storage.");

                    Toast.makeText(MainActivity.this, "Opps!! Something went wrong, please try again later.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MainActivity.this, "Please provide the required permission", Toast.LENGTH_SHORT).show();
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void Logout() {
        final SharedPreferences sharedPreferences = getSharedPreferences(USER_LOGIN_STATE_STORAGE_FILE_NAME, Context.MODE_PRIVATE);

        final boolean isLoggedIn = sharedPreferences.getBoolean(USER_LOGIN_STATE_KEY, false);
        final String currentUser = sharedPreferences.getString(CURRENT_LOGGED_IN_USER_KEY, null);

        // Checking whether any user already logged in or not
        if (Objects.nonNull(currentUser) && isLoggedIn) {
            Log.d(currentUser, "The user is logging out from the app, redirecting to SignIn Page");

            final SharedPreferences.Editor editor = sharedPreferences.edit();

            editor.putString(CURRENT_LOGGED_IN_USER_KEY, null);
            editor.putBoolean(USER_LOGIN_STATE_KEY, false);

            editor.apply();

            startActivity(new Intent(MainActivity.this, sendOTPActivity.class));
        } else {
            Log.e(currentUser, "There is no user login state found in the system, redirecting to SignIn Page only");
            startActivity(new Intent(MainActivity.this, sendOTPActivity.class));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logoutMenu: {
                Logout();
                break;
            }
        }

        return super.onOptionsItemSelected(item);
    }
}


