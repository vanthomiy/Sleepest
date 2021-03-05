package com.doitstudio.restapitests;

import android.Manifest;
import android.accounts.Account;
import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.data.Subscription;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.SessionReadRequest;
import com.google.android.gms.fitness.result.DailyTotalResult;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.fitness.result.DataReadResult;
import com.google.android.gms.fitness.result.SessionReadResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.ListConnectionsResponse;
import com.google.api.services.people.v1.model.Person;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.google.android.gms.fitness.data.Field.FIELD_STEPS;
import static java.util.concurrent.TimeUnit.SECONDS;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = "MainActivity";

    Button authButton;
    Button dataButton;

    FitnessOptions fitnessOptions;
    GoogleSignInAccount account;
    private int extractionCounter = 0;

    String currentDate;
    long totalSleepTime;
    private List sleepDataArray;
    private JSONObject json;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        authButton = findViewById(R.id.authButton);
        dataButton = findViewById(R.id.Databutton);
        authButton.setOnClickListener(this);
        dataButton.setOnClickListener(this);

        //SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
        //currentDate = sdf.format(new Date());


    }

    private void authentificate() {
        fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_SLEEP_SEGMENT, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_SLEEP_SEGMENT, FitnessOptions.ACCESS_WRITE)
                .build();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .addExtension(fitnessOptions)
                .requestEmail()
                .build();

        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 9001);


    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == 9001) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            account = completedTask.getResult(ApiException.class);
            Toast.makeText(MainActivity.this, "JOOOO", Toast.LENGTH_LONG).show();

            // Signed in successfully, show authenticated UI.
            //updateUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            //updateUI(null);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void readSession() {
        extractionCounter ++;
        sleepDataArray = new ArrayList();

        Calendar midnight = Calendar.getInstance();

        midnight.set(Calendar.HOUR_OF_DAY, 0);
        midnight.set(Calendar.MINUTE, 0);
        midnight.set(Calendar.SECOND, 0);
        midnight.set(Calendar.MILLISECOND, 0);

        long startTime = midnight.getTimeInMillis() - 14400000; //start from 20:00 of prev day

        // Note: The android.permission.ACTIVITY_RECOGNITION permission is
        // required to read DataType.TYPE_ACTIVITY_SEGMENT
        SessionReadRequest request = new SessionReadRequest.Builder()
                .readSessionsFromAllApps()
                // Activity segment data is required for details of the fine-
                // granularity sleep, if it is present.
                .read(DataType.TYPE_ACTIVITY_SEGMENT)
                .setTimeInterval(startTime, System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .build();

        Task<SessionReadResponse> task = Fitness.getSessionsClient(MainActivity.this,
                GoogleSignIn.getLastSignedInAccount(MainActivity.this))
                .readSession(request);

        task.addOnSuccessListener(response -> {

            // Filter the resulting list of sessions to just those that are sleep.
            List<Session> sleepSessions = response.getSessions().stream()
                    //.filter(s -> s.getActivity().equals(FitnessActivities.SLEEP))
                    .collect(Collectors.toList());

            if (sleepSessions.size() == 0){
                if (extractionCounter < 3) {
                    readSession();
                }
                return;
            }

            extractionCounter = 0;

            for (Session session : sleepSessions) {

                totalSleepTime = session.getEndTime(TimeUnit.MILLISECONDS) - session.getStartTime(TimeUnit.MILLISECONDS);

                // If the sleep session has finer granularity sub-components, extract them:
                List<DataSet> dataSets = response.getDataSet(session);
                for (DataSet dataSet : dataSets) {
                    for (DataPoint point : dataSet.getDataPoints()) {
                        // The Activity defines whether this segment is light, deep, REM or awake.
                        String sleepStage = point.getValue(Field.FIELD_ACTIVITY).asActivity();

                        //ignore non sleeping data
                        if (!sleepStage.equals("sleep.deep") && !sleepStage.equals("sleep.light"))
                            continue;


                        int stateAsInt = point.getValue(Field.FIELD_ACTIVITY).asInt();

                        switch (stateAsInt) {
                            case 109:
                                sleepStage = "SLEEP_LIGHT";
                                break;
                            case 110:
                                sleepStage = "SLEEP_DEEP";
                                break;
                            case 112:
                                sleepStage = "SLEEP_AWAKE";
                                break;
                        }

                        long start = point.getStartTime(TimeUnit.MILLISECONDS);
                        long end = point.getEndTime(TimeUnit.MILLISECONDS);
                        Log.d(TAG, String.format("\t* %s between %d and %d", sleepStage, start, end));

                        JSONObject json = new JSONObject();
                        try {
                            json.put("StartTime", start);
                            json.put("EndTime", end);
                            json.put("State", sleepStage);
                            if (!this.sleepDataArray.contains(json))
                                this.sleepDataArray.add(json);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            // create a json for sending data to server
            makeBodyJson(System.currentTimeMillis());
            //sendDataToServer(HttpRequests.getInstance(context));
        })
                .addOnFailureListener(response -> {
                    Log.e(TAG, "extractSleepData: failed to extract sleeping data");
                    if (extractionCounter < 3){
                        Log.i(TAG, "extractSleepData: retry extract sleeping data. counter value = " + extractionCounter);
                        readSession();
                    }
                    else{
                        extractionCounter = 0;
                    }
                });
    }

    public void makeBodyJson(long time) {
        JSONObject jsonobj = new JSONObject();
        JSONArray array = new JSONArray(sleepDataArray);
        try {
            jsonobj.put("ValidTime", time);
            jsonobj.put("Data", array);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        json = jsonobj;
    }

    private void startRecordData() {
        fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_SLEEP_SEGMENT, FitnessOptions.ACCESS_READ)
                .build();
        Fitness.getRecordingClient(this, GoogleSignIn.getAccountForExtension(this, fitnessOptions))
                .subscribe(DataType.TYPE_SLEEP_SEGMENT)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        safeActualTime("Starttime");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this,"There was a problem subscribing.", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void stopRecordData() {
        fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_SLEEP_SEGMENT, FitnessOptions.ACCESS_READ)
                .build();
        Fitness.getRecordingClient(this, GoogleSignIn.getAccountForExtension(this, fitnessOptions))
                .unsubscribe(DataType.TYPE_SLEEP_SEGMENT)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        safeActualTime("Endtime");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Failed to unsubscribe for data type: ", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void readRecordedSleepData() {

    }

    private void safeActualTime(String key) {
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long time = cal.getTimeInMillis();
        SharedPreferences.Editor sharedPref = getSharedPreferences("Time", Context.MODE_PRIVATE).edit();
        sharedPref.putLong(key, time);
        sharedPref.apply();
    }

    private long getSafedTime(String key) {
        SharedPreferences sharedPref = getSharedPreferences("Time", Context.MODE_PRIVATE);
        return sharedPref.getLong(key, 0);
    }

    private void getData() {
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.WEEK_OF_YEAR, -1);
        long startTime = cal.getTimeInMillis();


        DataReadRequest readRequest = new DataReadRequest.Builder()
                .read(DataType.TYPE_SLEEP_SEGMENT)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .bucketByTime(365, TimeUnit.DAYS)
                .build();



        Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .readData(readRequest)
                .addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
                    @Override
                    public void onSuccess(DataReadResponse dataReadResponse) {
                        Log.d(TAG, "onSuccess()");

                        Toast.makeText(MainActivity.this, "Success", Toast.LENGTH_SHORT).show();

                        for (Bucket bucket : dataReadResponse.getBuckets()) {
                            Log.e("History", "Data returned for Data type: " + bucket.getDataSets());

                            List<DataSet> dataSets = bucket.getDataSets();
                            for (DataSet dataSet : dataSets) {

                                for (DataPoint dp : dataSet.getDataPoints()) {


                                    DateFormat timeFormat = DateFormat.getTimeInstance();
                                    DateFormat dateFormat = DateFormat.getDateInstance();


                                    if(currentDate.equals(dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)))) {

                                        Log.e("History", "Data point:");
                                        Log.e("History", "\tType: " + dp.getDataType().getName());
                                        Log.e("History", "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)) + " " + timeFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
                                        Log.e("History", "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)) + " " + timeFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)));



                                        //dailyPojos.add(new DailyPojo(timeFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)), timeFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)), dp.getValue(dp.getDataType().getFields().get(0))));
                                    }

                                }
                            }
                            //printData();
                            //adapter = new DailyDataAdapter(dailyPojos , MainActivity.this);
                            //adapter.notifyDataSetChanged();
                            //recyclerView.setAdapter(adapter);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure()", e);
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<DataReadResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<DataReadResponse> task) {
                        Log.d(TAG, "onComplete()");
                    }
                });
    }


    @Override
    public void onStart() {
        super.onStart();

    }


    @Override
    protected void onResume() {
        super.onResume();


    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.Databutton:

                //getData();
                //readSession();
                startRecordData();

                break;
            case R.id.authButton:

                //authentificate();
                stopRecordData();

                break;
        }


    }

}