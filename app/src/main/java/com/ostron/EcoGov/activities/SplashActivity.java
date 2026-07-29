package com.ostron.EcoGov.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;


import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.karumi.dexter.BuildConfig;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.ostron.EcoGov.R;
import com.ostron.EcoGov.cbwtf.activities.CbwtfDashboardActivity;
import com.ostron.EcoGov.classes.AppStrings;
import com.ostron.EcoGov.classes.MSP;
import com.ostron.EcoGov.classes.VolleySingleton;
//import com.tonyodev.fetch2.Download;
//import com.tonyodev.fetch2.Error;
//import com.tonyodev.fetch2.Fetch;
//import com.tonyodev.fetch2.FetchConfiguration;
//import com.tonyodev.fetch2.FetchListener;
//import com.tonyodev.fetch2.NetworkType;
//import com.tonyodev.fetch2.Priority;
//import com.tonyodev.fetch2core.DownloadBlock;
//import com.tonyodev.fetch2core.Func;

import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    ImageView splash_image;
    int loginAs = 0;

   // Fetch fetch;

    int downloadProgress = 0;

    AlertDialog downloadAlert;

    TextView progressTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

//        FetchConfiguration fetchConfiguration = new FetchConfiguration.Builder(this).setDownloadConcurrentLimit(1).build();
//        fetch = Fetch.Impl.getInstance(fetchConfiguration);
//        fetch.addListener(fetchListener);

        downloadAlert = new AlertDialog.Builder(this).create();

        View downloadView = LayoutInflater.from(this).inflate(R.layout.download_alert_view,null,false);

        progressTv = downloadView.findViewById(R.id.download_progress_tv);

        downloadAlert.setView(downloadView);
        downloadAlert.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        downloadAlert.setCancelable(false);

        splash_image = findViewById(R.id.splash_image);

        Glide.with(this).load(R.drawable.uplogogpb).into(splash_image);

        startTimer();

       // getAppVersion();

    }

    private void startTimer(){

        Timer timer = new Timer();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                if (MSP.getInstance(SplashActivity.this).containsData(AppStrings.loginAs) && MSP.getInstance(SplashActivity.this).getStringData(AppStrings.loginAs).equalsIgnoreCase("cbwtf")){

                    loginAs = 0;

                    Map<String,String> map = new HashMap<>();
                    map.put("mobile",MSP.getInstance(SplashActivity.this).getStringData(AppStrings.userMobile));
                    map.put("password",MSP.getInstance(SplashActivity.this).getStringData(AppStrings.userPassword));
                    networkRequest(AppStrings.cbwtf_login,map);
                }
                else if (MSP.getInstance(SplashActivity.this).containsData(AppStrings.loginAs) && MSP.getInstance(SplashActivity.this).getStringData(AppStrings.loginAs).equalsIgnoreCase("hcf")){

                    loginAs = 1;
                    Map<String,String> map = new HashMap<>();
                    map.put("mobile",MSP.getInstance(SplashActivity.this).getStringData(AppStrings.userMobile));
                    map.put("password",MSP.getInstance(SplashActivity.this).getStringData(AppStrings.userPassword));
                    networkRequest(AppStrings.hcf_login,map);

                }else {
                    startActivity(new Intent(SplashActivity.this,LoginActivity.class));
                    finish();
                }

            }
        },2000);

    }

    public void networkRequest(String url, Map<String,String> map){
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {

                    JSONObject jsonObject = new JSONObject(response);

                    if (jsonObject.get("status").toString().equalsIgnoreCase("success")){

                        if (loginAs == 0){

                            String operatorID = jsonObject.getJSONArray("data").getJSONObject(0).get("operator_id").toString();
                            String operatorName = jsonObject.getJSONArray("data").getJSONObject(0).get("name").toString();
                            String operatorAddress = jsonObject.getJSONArray("data").getJSONObject(0).get("address").toString();
                            String operatorMobile = jsonObject.getJSONArray("data").getJSONObject(0).get("mobile").toString();
                            String operatorCbwtfID = jsonObject.getJSONArray("data").getJSONObject(0).get("cbwtf_id").toString();
                            String userPassword = jsonObject.getJSONArray("data").getJSONObject(0).get("password").toString();

                            MSP.getInstance(SplashActivity.this).setStringData(AppStrings.userPassword,userPassword);
                            MSP.getInstance(SplashActivity.this).setStringData(AppStrings.userName,operatorName);
                            MSP.getInstance(SplashActivity.this).setStringData(AppStrings.userMobile,operatorMobile);
                            MSP.getInstance(SplashActivity.this).setStringData(AppStrings.userAddress,operatorAddress);
                            MSP.getInstance(SplashActivity.this).setStringData(AppStrings.userID,operatorID);
                            MSP.getInstance(SplashActivity.this).setStringData(AppStrings.userCbwtfID,operatorCbwtfID);
                            MSP.getInstance(SplashActivity.this).setStringData(AppStrings.loginAs,"cbwtf");

                        }else{

                            String operatorID = jsonObject.getJSONArray("data").getJSONObject(0).get("hospital_code").toString();
                            String operatorName = jsonObject.getJSONArray("data").getJSONObject(0).get("name").toString();
                            String operatorAddress = jsonObject.getJSONArray("data").getJSONObject(0).get("address").toString();
                            String operatorMobile = jsonObject.getJSONArray("data").getJSONObject(0).get("mobile").toString();
                            String operatorCbwtfID = jsonObject.getJSONArray("data").getJSONObject(0).get("cbwtf_id").toString();
                            String userPassword = jsonObject.getJSONArray("data").getJSONObject(0).get("password").toString();
                            String hcfCode = jsonObject.getJSONArray("data").getJSONObject(0).get("hospital_code").toString();

                            MSP.getInstance(SplashActivity.this).setStringData(AppStrings.userPassword,userPassword);
                            MSP.getInstance(SplashActivity.this).setStringData(AppStrings.userName,operatorName);
                            MSP.getInstance(SplashActivity.this).setStringData(AppStrings.userMobile,operatorMobile);
                            MSP.getInstance(SplashActivity.this).setStringData(AppStrings.userAddress,operatorAddress);
                            MSP.getInstance(SplashActivity.this).setStringData(AppStrings.userID,operatorID);
                            MSP.getInstance(SplashActivity.this).setStringData(AppStrings.userCbwtfID,operatorCbwtfID);
                            MSP.getInstance(SplashActivity.this).setStringData(AppStrings.loginAs,"hcf");
                            MSP.getInstance(SplashActivity.this).setStringData(AppStrings.hcfCode,hcfCode);

                        }

                        startActivity(new Intent(SplashActivity.this, CbwtfDashboardActivity.class));
                        finish();

                    }else {
                        Snackbar.make(splash_image,"Your account is banned or removed",1000).show();
                        startActivity(new Intent(SplashActivity.this,LoginActivity.class));
                        finish();
                    }

                }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(SplashActivity.this, "Something went wrong try again", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SplashActivity.this,LoginActivity.class));
                    finish();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(SplashActivity.this, "Something went wrong try again", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(SplashActivity.this,LoginActivity.class));
                finish();
            }
        }){
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return map;
            }
        };

        VolleySingleton.getInstance(this).addToRequestQueue(request);

    }

    private void getAppVersion(){

        StringRequest appVersionRequest = new StringRequest(Request.Method.POST, AppStrings.get_app_version, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {

                    JSONObject versionObject = new JSONObject(response);

                    if (versionObject.get("status").toString().equalsIgnoreCase("success")){

                        String version = versionObject.getJSONArray("data").getJSONObject(0).get("version").toString();

                        checkAppVersion(Integer.parseInt(version.trim()));

                    }

                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        VolleySingleton.getInstance(this).addToRequestQueue(appVersionRequest);

    }

    private void checkAppVersion(int value){
        /*
        * for version check use version code
        * */
        if (BuildConfig.VERSION_CODE == value){
            startTimer();
        }else{
            new AlertDialog.Builder(this).setTitle("Update App")
                    .setMessage("This app version is outdated please update to latest version")
                    .setCancelable(false)
                    .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            Dexter.withContext(SplashActivity.this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE).withListener(new MultiplePermissionsListener() {
                                @Override
                                public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {

                                  //  checkApkFile();

                                }

                                @Override
                                public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {

                                }
                            }).check();

                        }
                    })
                    .show();
        }
    }

//    private void checkApkFile(){
//
//        File apkFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),"eco.apk");
//
//        if (apkFile.exists()){
//            if (apkFile.delete()){
//                enqueueDownload();
//            }
//            else {
//                enqueueDownload();
//            }
//        }else{
//            enqueueDownload();
//        }
//
//    }

//    private void enqueueDownload(){
//
//        String url = getResources().getString(R.string.site_url)+"app_apk/eco.apk";
//        String file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath()+"/eco.apk";
//
//        final com.tonyodev.fetch2.Request request = new com.tonyodev.fetch2.Request(url,file);
//        request.setPriority(Priority.HIGH);
//        request.setNetworkType(NetworkType.ALL);
//        request.addHeader("clientKey", "SD78DF93_3947&MVNGHE1WONG");
//
//        fetch.enqueue(request, new Func<com.tonyodev.fetch2.Request>() {
//            @Override
//            public void call(@NonNull com.tonyodev.fetch2.Request result) {
//
//            }
//        }, new Func<Error>() {
//            @Override
//            public void call(@NonNull Error result) {
//
//            }
//        });
//
//    }

    private void checkInstallPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!getPackageManager().canRequestPackageInstalls()) {
                new AlertDialog.Builder(SplashActivity.this)
                        .setTitle("Install permission")
                        .setMessage("Please grant unknown source permission to install the app")
                        .setCancelable(false)
                        .setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivityForResult(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).setData(Uri.parse(String.format("package:%s", getPackageName()))), 1234);
                            }
                        })
                        .show();
            }
            else {
                installAPK();
            }
        }else {
            installAPK();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1234){
            installAPK();
        }
    }

    void installAPK(){

        String PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath()+"/eco.apk";
        File file = new File(PATH);
        if(file.exists()) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uriFromFile(getApplicationContext(), new File(PATH)), "application/vnd.android.package-archive");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                getApplicationContext().startActivity(intent);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
                Log.e("TAG", "Error in opening the file!");
            }
        }else{
            Toast.makeText(getApplicationContext(),"installing",Toast.LENGTH_LONG).show();
        }
    }
    Uri uriFromFile(Context context, File file) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file);
        } else {
            return Uri.fromFile(file);
        }
    }

//    FetchListener fetchListener = new FetchListener() {
//        @Override
//        public void onAdded(@NonNull Download download) {
//
//        }
//
//        @Override
//        public void onQueued(@NonNull Download download, boolean b) {
//
//        }
//
//        @Override
//        public void onWaitingNetwork(@NonNull Download download) {
//
//        }
//
//        @Override
//        public void onCompleted(@NonNull Download download) {
//
//            downloadAlert.dismiss();
//
//            checkInstallPermission();
//
//        }
//
//        @Override
//        public void onError(@NonNull Download download, @NonNull Error error, @Nullable Throwable throwable) {
//
//        }
//
//        @Override
//        public void onDownloadBlockUpdated(@NonNull Download download, @NonNull DownloadBlock downloadBlock, int i) {
//
//        }
//
//        @Override
//        public void onStarted(@NonNull Download download, @NonNull List<? extends DownloadBlock> list, int i) {
//
//            downloadAlert.show();
//
//        }
//
//        @Override
//        public void onProgress(@NonNull Download download, long l, long l1) {
//            downloadProgress = download.getProgress();
//            progressTv.setText(String.valueOf(download.getProgress()));
//        }
//
//        @Override
//        public void onPaused(@NonNull Download download) {
//
//        }
//
//        @Override
//        public void onResumed(@NonNull Download download) {
//
//        }
//
//        @Override
//        public void onCancelled(@NonNull Download download) {
//
//        }
//
//        @Override
//        public void onRemoved(@NonNull Download download) {
//
//        }
//
//        @Override
//        public void onDeleted(@NonNull Download download) {
//
//        }
//    };
}