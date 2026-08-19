package com.appventurez.bmwms.cbwtf.activities;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.material.card.MaterialCardView;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanIntentResult;
import com.journeyapps.barcodescanner.ScanOptions;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.appventurez.bmwms.R;
import com.appventurez.bmwms.activities.LoginActivity;
import com.appventurez.bmwms.activities.PdfViewActivity;
import com.appventurez.bmwms.classes.AppStrings;
import com.appventurez.bmwms.classes.MSP;
import com.appventurez.bmwms.classes.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.Gson;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class CbwtfDashboardActivity extends AppCompatActivity {

    TextView guideline_tv,youtube_tv,header_tv,today_attempted,today_collected,otp_tv;
    ImageView bottom_img,hcf_img,report_img,profile_img,logout_img,header_logo_img,scanner_img,rescan_img;
    MaterialCardView hcf_card,report_card,profile_card,logout_card,rescan_card;
    LinearLayout ll_today,otp_ll;
    View view0;

    GoogleApiClient googleApiClient;

    boolean isLocationGranted = false;
    boolean isCameraPermissionGranted = false;
    boolean isbluetoothGranted = false;
    boolean isHCFLogin = false;

    Vibrator vibrator;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;


    private int f_Id = 0;

    private Boolean isFromScan = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        init();
        viewBinding();
        loadImages();
        clickListeners();

        getNotice();
        if (checkCameraPermission()) {

        } else {
            requestCameraPermission();
        }

       // getTodayData();

    }

    private  void getNotice(){
        StringRequest appNoticeRequest = new StringRequest(Request.Method.POST, AppStrings.get_app_notice, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {

                    JSONObject noticeObject = new JSONObject(response);

                    if (noticeObject.get("status").toString().equalsIgnoreCase("success")){

                        String url = noticeObject.getJSONArray("data").getJSONObject(0).get("notice_url").toString();
                        String noticeStatus = noticeObject.getJSONArray("data").getJSONObject(0).get("notice_status").toString();

                        if (noticeStatus.equalsIgnoreCase("1")){

                            WebView webView = new WebView(CbwtfDashboardActivity.this);

                            webView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

                            webView.loadUrl(url);

                            new AlertDialog.Builder(CbwtfDashboardActivity.this).setView(webView).show();

                        }else if (noticeStatus.equalsIgnoreCase("2")){
                            WebView webView = new WebView(CbwtfDashboardActivity.this);

                            webView.loadUrl(url);

                            new AlertDialog.Builder(CbwtfDashboardActivity.this).setCancelable(false).setView(webView).show();
                        }

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

        VolleySingleton.getInstance(this).addToRequestQueue(appNoticeRequest);

    }

    @Override
    protected void onStart() {
        super.onStart();
        checkLocationPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //checkLocationPermission();
        getCbwtfName();
        getTodayData();
        if (MSP.getInstance(this).getStringData(AppStrings.loginAs).equals("hcf")){
            generateOtp();
        }
    }

    public void getCbwtfName(){
        if (MSP.getInstance(this).getStringData(AppStrings.loginAs).equals("hcf")){
            header_tv.setText(MSP.getInstance(this).getStringData(AppStrings.userName));
        }else {
            Map<String,String> map = new HashMap<>();
            map.put("cbwtf_id",MSP.getInstance(this).getStringData(AppStrings.userCbwtfID));
            networkRequest(AppStrings.cbwtf_data,map);
        }
    }

    public void getTodayData(){

        String date = DateFormat.format("yyyy-MM-dd",new Date().getTime()).toString();

        if (MSP.getInstance(this).getStringData(AppStrings.loginAs).equals("hcf")){
            Map<String,String> map = new HashMap<>();
            map.put("hospital_id",MSP.getInstance(this).getStringData(AppStrings.userID));
            map.put("date",date);
            todayDataRequest(AppStrings.get_today_data_hcf,map);
            scan_otp_status(true);

        }else {
            Map<String,String> map = new HashMap<>();
            map.put("operator_id",MSP.getInstance(this).getStringData(AppStrings.userID));
            map.put("date",date);
            todayDataRequest(AppStrings.get_today_data,map);
            scan_otp_status(false);

        }
    }

    public boolean getBluethoothPermission(){
        Dexter.withContext(this).withPermission(Manifest.permission.BLUETOOTH).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {

                isbluetoothGranted = true;

            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                isbluetoothGranted = false;
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                permissionToken.continuePermissionRequest();
            }
        }).check();

        return isbluetoothGranted;
    }

    public void clickListeners(){

        profile_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(1);
            }
        });

        report_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(10);
            }
        });



        scanner_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getCameraPermission()  ){
                    startScanner();
                }else {
                    cameraPermissionAlertDialog();

                }
            }
        });

        hcf_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!getBluethoothPermission()){

                    new AlertDialog.Builder(CbwtfDashboardActivity.this).setMessage("Grant camera permission to continue\nApp permissions > Camera > Allow").setPositiveButton("Setting", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            intent.setData(uri);
                            startActivity(intent);
                        }
                    }).show();
                }

                if (isLocationGranted){
                    startActivity(0);
                }else {
                    checkLocationPermission();
                }
            }
        });

        rescan_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLocationGranted){
                    startActivity(9);
                }else {
                    checkLocationPermission();
                }
            }
        });

        logout_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                onLogout();

            }
        });

        guideline_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGuidelines();
            }
        });

        youtube_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openYoutubeLink();
            }
        });

    }

    public void init(){

        vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);

    }

    public void loadImages(){

        Glide.with(this).load(R.drawable.img_011).into(bottom_img);
        Glide.with(this).load(R.drawable.qr_code).into(hcf_img);
        Glide.with(this).load(R.drawable.user_profile).into(profile_img);
        Glide.with(this).load(R.drawable.user_report).into(report_img);
        Glide.with(this).load(R.drawable.exit_img).into(logout_img);
        Glide.with(this).load(R.drawable.uplogogpb).into(header_logo_img);
        Glide.with(this).load(R.drawable.qr_scan).into(rescan_img);

    }

    public void viewBinding(){

        guideline_tv = findViewById(R.id.cbwtf_dashboard_guideline_link_tv);
        youtube_tv = findViewById(R.id.cbwtf_dashboard_youtube_link_tv);
        header_tv = findViewById(R.id.cbwtf_dashboard_header_tv);
        today_attempted = findViewById(R.id.cbwtf_dashboard_attempted_tv);
        today_collected = findViewById(R.id.cbwtf_dashboard_waste_collected_tv);

        scanner_img = findViewById(R.id.cbwtf_dashboard_header_scan_img);
        bottom_img = findViewById(R.id.cbwtf_dashboard_bottom_img);
        hcf_img = findViewById(R.id.cbwtf_dashboard_hcf_scan_img);
        report_img = findViewById(R.id.cbwtf_dashboard_reports_img);
        profile_img = findViewById(R.id.cbwtf_dashboard_profile_img);
        logout_img = findViewById(R.id.cbwtf_dashboard_logout_img);
        header_logo_img = findViewById(R.id.cbwtf_dashboard_header_logo_img);
        rescan_img = findViewById(R.id.cbwtf_dashboard_rescan_img);

        hcf_card = findViewById(R.id.cbwtf_dashboard_hcf_scan_card);
        report_card = findViewById(R.id.cbwtf_dashboard_reports_card);
        profile_card = findViewById(R.id.cbwtf_dashboard_profile_card);
        logout_card = findViewById(R.id.cbwtf_dashboard_logout_card);
        rescan_card = findViewById(R.id.cbwtf_dashboard_rescan_card);

        ll_today = findViewById(R.id.today_collected_ll);
        view0 = findViewById(R.id.view_0);
        otp_ll = findViewById(R.id.otp_ll);
        otp_tv = findViewById(R.id.otp_tv);

        if (MSP.getInstance(this).getStringData(AppStrings.loginAs).equals("hcf")){
            rescan_card.setVisibility(View.GONE);
            logout_card.setVisibility(View.VISIBLE);
            ll_today.setVisibility(View.GONE);
            view0.setVisibility(View.GONE);


        }

    }

    private void scan_otp_status(Boolean isHCFLogin){
        if(isHCFLogin){
            hcf_card.setVisibility(View.GONE);
            otp_ll.setVisibility(View.GONE);
            scanner_img.setVisibility(View.GONE);
        }else{
            hcf_card.setVisibility(View.VISIBLE);
            otp_ll.setVisibility(View.GONE);
            scanner_img.setVisibility(View.VISIBLE);
        }
    }

    public void cameraPermissionAlertDialog(){
        new AlertDialog.Builder(CbwtfDashboardActivity.this).setMessage("Grant camera permission to continue\nApp permissions > Camera > Allow").setPositiveButton("Setting", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        }).show();
    }

    public void blueThoothPermissionAlertDialog(){
        new AlertDialog.Builder(CbwtfDashboardActivity.this).setMessage("Grant camera permission to continue\nApp permissions > Bluetooth/Near By > Allow").setPositiveButton("Setting", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        }).show();
    }

    public void startScanner(){
        ScanOptions scanOptions = new ScanOptions();
        scanOptions.setOrientationLocked(true);
        scanOptions.setBarcodeImageEnabled(true);
        scanOptions.setPrompt("Scan Waste Barcode");
        launcher.launch(scanOptions);
    }


    public void startActivity(int value){
        f_Id = value;
        isFromScan = true;
        Log.d("TAG", "startActivity: "+f_Id);
        if (checkCameraPermission()) {
            Intent intent = new Intent(CbwtfDashboardActivity.this,CbwtfFragmentContainerActivity.class);
            intent.putExtra("f_id",value);
            startActivity(intent);
        } else {
            requestCameraPermission();
        }

    }

    public void onLogout(){
        MSP.getInstance(this).removeAll();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public void openGuidelines(){
        startActivity(new Intent(CbwtfDashboardActivity.this, PdfViewActivity.class));
    }

    public void openYoutubeLink(){
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:OcrMGvRdjaY"));
        Intent webIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://www.youtube.com/watch?v=OcrMGvRdjaY"));
        try {
            startActivity(appIntent);
        } catch (ActivityNotFoundException ex) {
            startActivity(webIntent);
        }
    }

    public void checkLocationPermission(){

        Dexter.withContext(this).withPermission(Manifest.permission.ACCESS_FINE_LOCATION).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                checkLocationSetting();
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                new AlertDialog.Builder(CbwtfDashboardActivity.this).setMessage("Grant location permission to continue\nApp permissions > Location > Allow").setPositiveButton("Setting", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    }
                }).show();
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
//                permissionToken.cancelPermissionRequest();
                permissionToken.continuePermissionRequest();
            }
        }).check();

//        Dexter.withContext(this).withPermissions(Manifest.permission.ACCESS_FINE_LOCATION).withListener(new MultiplePermissionsListener() {
//            @Override
//            public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
//
//                if (multiplePermissionsReport.areAllPermissionsGranted()){
//                    checkLocationSetting();
//                }else{
//                    vibrator.vibrate(100);
//                    //Toast.makeText(CbwtfDashboardActivity.this, "Grant location permission", Toast.LENGTH_SHORT).show();
//                    new AlertDialog.Builder(CbwtfDashboardActivity.this).setMessage("Grant location permission to continue\nApp permissions > Location > Allow").setPositiveButton("Setting", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialogInterface, int i) {
//                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                            Uri uri = Uri.fromParts("package", getPackageName(), null);
//                            intent.setData(uri);
//                            startActivity(intent);
//                        }
//                    }).show();
//                }
//
//            }
//
//            @Override
//            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
//                //permissionToken.continuePermissionRequest():
//            }
//
//        }).check();

    }

    public void checkLocationSetting(){
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(CbwtfDashboardActivity.this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(@Nullable Bundle bundle) {

                        }

                        @Override
                        public void onConnectionSuspended(int i) {

                        }
                    })
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                        }
                    }).build();

            googleApiClient.connect();

            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(30 * 1000);
            locationRequest.setFastestInterval(5 * 1000);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);

            //**************************
            builder.setAlwaysShow(true); //this is the key ingredient
            //**************************

            PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @SuppressLint("MissingPermission")
                @Override
                public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    final LocationSettingsStates state = result.getLocationSettingsStates();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:
                            isLocationGranted = true;
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the user
                            // a dialog.
                            try {
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                status.startResolutionForResult(
                                        CbwtfDashboardActivity.this, 1000);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.
                            break;
                    }
                }
            });
        }else {

            googleApiClient.connect();

            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(30 * 1000);
            locationRequest.setFastestInterval(5 * 1000);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);

            //**************************
            builder.setAlwaysShow(true);

            PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @SuppressLint("MissingPermission")
                @Override
                public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    final LocationSettingsStates state = result.getLocationSettingsStates();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:
                            isLocationGranted = true;
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the user
                            // a dialog.
                            try {
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                status.startResolutionForResult(
                                        CbwtfDashboardActivity.this, 1000);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.
                            break;
                    }
                }
            });
        }
    }

    ActivityResultLauncher<ScanOptions> launcher = registerForActivityResult(new ScanContract(), new ActivityResultCallback<ScanIntentResult>() {
        @Override
        public void onActivityResult(ScanIntentResult result) {

            if (result.getContents() != null){
                try {
                    String[] data = result.getContents().split("&",2);

                    String hcfCode = data[0];

                    String[] data1 = data[1].split("/",3);

                    String hospitalName = data1[1];
                    String qrCbwtfId = data1[2];

                    String[] data2 = data1[0].split("-",2);

                    String qrCode = data2[0];
                    String qrColor = data2[1];

                    Map<String,String> map = new HashMap<>();

                    map.put("hospital_code",hcfCode);

                    hospitalDataRequest(AppStrings.hospital_data,map);

                }catch (Exception e){
                    e.printStackTrace();
                    Log.d("TAG", "onActivityResult: "+e.getMessage());
                    Toast.makeText(CbwtfDashboardActivity.this, "Wrong QR Code", Toast.LENGTH_SHORT).show();
                }
            }

        }
    });


    public boolean getCameraPermission(){
        Dexter.withContext(this).withPermission(Manifest.permission.CAMERA).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {

                isCameraPermissionGranted = true;

            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                isCameraPermissionGranted = false;
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                permissionToken.continuePermissionRequest();
            }
        }).check();

        return isCameraPermissionGranted;
    }


    public void networkRequest(String url, Map<String,String> map){
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {

                    JSONObject jsonObject = new JSONObject(response);

                    if (jsonObject.get("status").toString().equalsIgnoreCase("success")){

                        String cbwtfName = "";
                        String attendace_compulsory = "";

                        for (int i=0;i<jsonObject.getJSONArray("data").length();i++){
                            cbwtfName = jsonObject.getJSONArray("data").getJSONObject(i).get("name").toString();
                            attendace_compulsory = jsonObject.getJSONArray("data").getJSONObject(i).get("attendace_compulsory").toString();
                            MSP.getInstance(CbwtfDashboardActivity.this).setStringData(AppStrings.attendance_compulsory,attendace_compulsory);
                        }

                        header_tv.setText(cbwtfName);

                    }

                }catch (Exception e){
                    e.printStackTrace();

                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

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

    public void hospitalDataRequest(String url, Map<String,String> map){
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {

                    JSONObject jsonObject = new JSONObject(response);

                    if (jsonObject.get("status").toString().equalsIgnoreCase("success")){

                        String hospitalName = "";
                        String hospitalAddress = "";

                        for (int i=0;i<jsonObject.getJSONArray("data").length();i++){
                            hospitalName = jsonObject.getJSONArray("data").getJSONObject(i).get("name").toString();
                            hospitalAddress = jsonObject.getJSONArray("data").getJSONObject(i).get("address").toString();
                        }

                        new AlertDialog.Builder(CbwtfDashboardActivity.this).setMessage(
                                "Hospital name: "+hospitalName.concat("\n\n").concat("Hospital address: "+hospitalAddress)
                        ).show();

                    }

                }catch (Exception e){
                    e.printStackTrace();

                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

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

//    public void todayDataRequest(String url, Map<String,String> map){
//        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
//            @Override
//            public void onResponse(String response) {
//                Log.i("res_p",response);
//                try {
//
//                    JSONObject jsonObject = new JSONObject(response);
//
//                    if (jsonObject.get("status").toString().equalsIgnoreCase("success")){
//
//                        String hospitalCode = "";
//                        String weight = "";
//
//                        double w_weight = 0;
//                        int count = 0;
//
//                        for (int i=0;i<jsonObject.getJSONArray("data").length();i++){
//
//                            if (hospitalCode.isEmpty()){
//
//                                hospitalCode = jsonObject.getJSONArray("data").getJSONObject(i).get("hospital_code").toString();
//                                count++;
//
//                                weight = jsonObject.getJSONArray("data").getJSONObject(i).get("hcf_weight").toString();
//                                w_weight = w_weight+Double.parseDouble(weight.trim());
//
//                            }else if (hospitalCode.equalsIgnoreCase(jsonObject.getJSONArray("data").getJSONObject(i).get("hospital_code").toString())){
//                                hospitalCode = jsonObject.getJSONArray("data").getJSONObject(i).get("hospital_code").toString();
//                                weight = jsonObject.getJSONArray("data").getJSONObject(i).get("hcf_weight").toString();
//                                w_weight = w_weight+Double.parseDouble(weight.trim());
//                            }else if (!hospitalCode.equalsIgnoreCase(jsonObject.getJSONArray("data").getJSONObject(i).get("hospital_code").toString())){
//                                hospitalCode = jsonObject.getJSONArray("data").getJSONObject(i).get("hospital_code").toString();
//                                count++;
//
//                                weight = jsonObject.getJSONArray("data").getJSONObject(i).get("hcf_weight").toString();
//                                w_weight = w_weight+Double.parseDouble(weight.trim());
//                            }
//
//                        }
//
//                        today_attempted.setText(""+count);
//                        today_collected.setText(new DecimalFormat("000.000").format(w_weight));
//
//                    }
//
//                }catch (Exception e){
//                    e.printStackTrace();
//
//                }
//
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//
//            }
//        }){
//            @Nullable
//            @Override
//            protected Map<String, String> getParams() throws AuthFailureError {
//                return map;
//            }
//        };
//
//        VolleySingleton.getInstance(this).addToRequestQueue(request);
//    }

    public void todayDataRequest(String url, Map<String,String> map){
        StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("res_p", response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            if (jsonObject.getString("status").equalsIgnoreCase("success")) {

                                Set<String> uniqueHospitals = new HashSet<>();
                                double totalWeight = 0;
                                List<Map<String, String>> customDataList = new ArrayList<>();

                                JSONArray dataArray = jsonObject.getJSONArray("data");
                                for (int i = 0; i < dataArray.length(); i++) {
                                    JSONObject item = dataArray.getJSONObject(i);
                                    String hospitalCode = item.getString("hospital_code").trim();
                                    String weightStr = item.getString("hcf_weight").trim();

                                    uniqueHospitals.add(hospitalCode);

                                    try {
                                        totalWeight += Double.parseDouble(weightStr);
                                    } catch (NumberFormatException e) {
                                        Log.e("parse_error", "Invalid weight: " + weightStr);
                                    }

                                    // Create custom map for persistence
                                    Map<String, String> dataMap = new HashMap<>();
                                    dataMap.put("qr_data_id", item.optString("qr_data_id"));
                                    dataMap.put("qr_id", item.optString("qr_id"));
                                    dataMap.put("hospital_code", hospitalCode);
                                    dataMap.put("operator_id", item.optString("operator_id"));
                                    dataMap.put("cbwtf_weight", item.optString("cbwtf_weight"));
                                    dataMap.put("hcf_weight", weightStr);
                                    customDataList.add(dataMap);
                                }

                                // Store in SharedPreferences using Gson
                                String jsonCustomData = new Gson().toJson(customDataList);
                                MSP.getInstance(CbwtfDashboardActivity.this).setStringData("today_qr_custom_data", jsonCustomData);

                                today_attempted.setText(String.valueOf(uniqueHospitals.size()));
                                today_collected.setText(new DecimalFormat("000.000").format(totalWeight));
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Volley", "Error: " + error.getMessage());
                    }
                }) {
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return map;
            }
        };

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    public void generateOtp()
    {
        String otpValue = String.format("%04d",new Random().nextInt(10000));
        String date = String.valueOf(DateFormat.format("yyyy-MM-dd",new Date().getTime()));

        StringRequest generateOtpRequest = new StringRequest(Request.Method.POST, AppStrings.generate_otp, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {

                    JSONObject otpObject = new JSONObject(response);

                    if (otpObject.get("status").toString().equalsIgnoreCase("success")){

                        String otp = otpObject.getJSONArray("data").getJSONObject(0).get("otp").toString();

                        otp_tv.setText(otp);

                    }else {

                    }

                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                generateOtp();
            }
        }){
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> otp = new HashMap<>();
                otp.put("hcf_code",MSP.getInstance(CbwtfDashboardActivity.this).getStringData(AppStrings.userID));
                otp.put("hcf_name",MSP.getInstance(CbwtfDashboardActivity.this).getStringData(AppStrings.userName));
                otp.put("hcf_contact",MSP.getInstance(CbwtfDashboardActivity.this).getStringData(AppStrings.userMobile));
                otp.put("otp",otpValue);
                otp.put("date",date);
                return otp;
            }
        };

        VolleySingleton.getInstance(this).addToRequestQueue(generateOtpRequest);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start the fragment

                if (isFromScan){
                    Intent intent = new Intent(CbwtfDashboardActivity.this,CbwtfFragmentContainerActivity.class);
                    intent.putExtra("f_id",f_Id);
                    startActivity(intent);
                }

            } else {
                // Permission denied, handle accordingly (e.g., show a message)
            }
        }
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
    }
}