package com.appventurez.EcoGov.cbwtf.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.os.Handler;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
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
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothStatus;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnTokenCanceledListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.BeepManager;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanIntentResult;
import com.journeyapps.barcodescanner.ScanOptions;
import com.journeyapps.barcodescanner.camera.CameraSettings;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;
import com.mukesh.OnOtpCompletionListener;
import com.mukesh.OtpView;
import com.appventurez.EcoGov.R;
import com.appventurez.EcoGov.adapters.BluetoothDevicesAdapter;
import com.appventurez.EcoGov.bluetooth.MyBluetoothService;
import com.appventurez.EcoGov.cbwtf.adapters.HospitalAdapter;
import com.appventurez.EcoGov.cbwtf.models.ReportsModel;
import com.appventurez.EcoGov.classes.AppStrings;
import com.appventurez.EcoGov.classes.MSP;
import com.appventurez.EcoGov.classes.VolleySingleton;
import com.appventurez.EcoGov.database.HospitalDao;
import com.appventurez.EcoGov.database.HospitalModel;
import com.appventurez.EcoGov.database.MyDatabase;
import com.appventurez.EcoGov.models.BluetoothDevicesModel;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.text.DecimalFormat;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import me.aflak.bluetooth.Bluetooth;

public class CbwtfHcfScanFragment extends Fragment implements BluetoothDevicesAdapter.EventListener, MyBluetoothService.MyEventListener,HospitalAdapter.MyHospitalEventListener{

    View view;

    ProgressDialog progressDialog;
    int loginAs = 0;

    TextView codeTv,appbar_tv,location_tv,hcf_name,total_bags,total_waste_weight,weight_hcf_name,empty_tv,attendanceStatusTv;
    MaterialButton scanButton,attendanceButton;
    ImageView appBarLogo,backButton,hcf_reset_button;
    MaterialCardView attendanceCard;
    SwitchMaterial switchMaterial;

    boolean isCameraPermissionGranted = false;

    boolean isLocationGranted = false;

    GoogleApiClient googleApiClient;

    FusedLocationProviderClient fusedLocationProviderClient;

    Geocoder geocoder;

    double latitude = 0;
    double longitude = 0;

    int scanMode = 0;

    //Bluetooth
    AlertDialog alertDialog;

    Bluetooth bluetooth;
    RecyclerView blRv;
    List<BluetoothDevicesModel> bluetoothDevicesModel = new ArrayList<>();
    BluetoothDevicesAdapter bluetoothDevicesAdapter;
    TextView weight_tv,weight_type;

    String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION};

    //ProgressBar loading;

    MyBluetoothService myBluetoothService;

    ConstraintLayout weightView;

    ImageView weight_img,weight_close,weight_type_img;

    boolean bluetoothConnected = false;
    //Bluetooth

    RecyclerView weight_rv;

    MyDatabase myDatabase;
    HospitalDao hospitalDao;

    MaterialButton add_weight_button,submit_button;

    List<HospitalModel> hospitalModels = new ArrayList<>();
    HospitalAdapter hospitalAdapter;

    String finalWeight = "000.000";

    String scannedQrCode = "0";
    String scannedHcfCode = "0";

    LinearLayout loading_ll;

    int dataSize = 0;

    int scanType = 0;

    List<ReportsModel> reportsModels = new ArrayList<>();

    private DecoratedBarcodeView barcodeView;
    private BeepManager beepManager;
    private String lastText;
    private Boolean isWeightAdded = true;

    private Boolean isFirstScanHCF = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null){
            view = inflater.inflate(R.layout.fragment_cbwtf_hcf_scan, container, false);

            myDatabase = Room.databaseBuilder(getContext(),MyDatabase.class,"hospital_data").fallbackToDestructiveMigration().build();
            hospitalDao = myDatabase.hospitalDao();

            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
            geocoder = new Geocoder(getContext());
            checkLocationPermission();

            scanType = getArguments().getInt("scanType");

            getQRData(AppStrings.get_all_report);

            attendanceButton = view.findViewById(R.id.cbwtf_hcf_scan_attendance_button);
            attendanceCard = view.findViewById(R.id.attendance_card);
            attendanceStatusTv = view.findViewById(R.id.attendance_status_tv);
            empty_tv = view.findViewById(R.id.empty_tv);
            weight_type_img = view.findViewById(R.id.weight_type_img);
            weight_hcf_name = view.findViewById(R.id.weight_hcf_name);
            hcf_reset_button = view.findViewById(R.id.cbwtf_hcf_scan_reset);
            total_bags = view.findViewById(R.id.weight_total_bags);
            total_waste_weight = view.findViewById(R.id.weight_total_waste_weight);
            add_weight_button = view.findViewById(R.id.weight_add_button);
            weight_rv = view.findViewById(R.id.cbwtf_hcf_scan_weight_rv);
            hcf_name = view.findViewById(R.id.cbwtf_hcf_scan_name_tv);
            location_tv = view.findViewById(R.id.cbwtf_hcf_scan_location_tv);
            appBarLogo = view.findViewById(R.id.cbwtf_hcf_scan_appBar_logo);
            codeTv = view.findViewById(R.id.cbwtf_hcf_scan_code_tv);
            scanButton = view.findViewById(R.id.cbwtf_hcf_scan_button);
            backButton = view.findViewById(R.id.cbwtf_hcf_scan_back_button);
            switchMaterial = view.findViewById(R.id.cbwtf_hcf_scan_appbar_switch);
            appbar_tv = view.findViewById(R.id.cbwtf_hcf_scan_appbar_tv);
            submit_button = view.findViewById(R.id.cbwtf_hcf_scan_submit_button);
            loading_ll = view.findViewById(R.id.loading_view_ll);


            progressDialog = new ProgressDialog(getContext());
            progressDialog.setCancelable(false);
            progressDialog.setMessage("Submitting...");
            progressDialog.create();

            Glide.with(getContext()).load(R.drawable.uplogogpb).into(appBarLogo);

            if (MSP.getInstance(getContext()).containsData(AppStrings.currentHcfCode)){
                codeTv.setText(MSP.getInstance(getContext()).getStringData(AppStrings.currentHcfCode));
                hcf_name.setText(MSP.getInstance(getContext()).getStringData(AppStrings.currentHcfName));
            }

            barcode(view);

            hospitalAdapter = new HospitalAdapter(getContext(),hospitalModels,CbwtfHcfScanFragment.this);
            weight_rv.setLayoutManager(new LinearLayoutManager(getContext(),RecyclerView.VERTICAL,false));
            weight_rv.setAdapter(hospitalAdapter);

            //Bluetooth

            myBluetoothService = new MyBluetoothService(getContext(),CbwtfHcfScanFragment.this);

            bluetooth = new Bluetooth(getContext());

            myBluetoothService.configService();

            weight_img = view.findViewById(R.id.weighing_img);
            weight_tv = view.findViewById(R.id.weight_tv);
            weightView = view.findViewById(R.id.weight_view);
            weight_close = view.findViewById(R.id.back_button_weight);
            weight_type = view.findViewById(R.id.weight_type);

            bluetoothDevicesAdapter = new BluetoothDevicesAdapter(getContext(),bluetoothDevicesModel, this::onClickBluetoothDevice);

            Glide.with(getContext()).load(R.drawable.weight_scale).fitCenter().into(weight_img);
            weight_close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    weightView.setVisibility(View.GONE);
                    isWeightAdded = true;
                }
            });
            //Bluetooth

            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getActivity().finish();
                }
            });

            switchMaterial.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                    if (b){
                        appbar_tv.setText("Auto");
                        scanMode = 1;
                    }else {
                        appbar_tv.setText("Manual");
                        scanMode = 0;
                    }

                }
            });


            switchMaterial.setChecked(false);
            appbar_tv.setText("Manual");
            scanMode = 0;

            scanButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (getCameraPermission()){
                        ScanOptions scanOptions = new ScanOptions();
                        scanOptions.setOrientationLocked(true);
                        scanOptions.setBarcodeImageEnabled(true);
                        scanOptions.setPrompt("Scan Waste Barcode");
                     //   launcher.launch(scanOptions);
                    }else {
                        new AlertDialog.Builder(getContext()).setMessage("Grant camera permission to continue\nApp permissions > Camera > Allow").setPositiveButton("Setting", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
                                intent.setData(uri);
                                startActivity(intent);
                            }
                        }).show();
                    }

                }
            });

            add_weight_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    String code = codeTv.getText().toString();
                    String hName = hcf_name.getText().toString();
                    String wType = weight_type.getText().toString();
                    weightView.setVisibility(View.GONE);

                    String[] ww_ww = finalWeight.trim().split("\\.");

                    hospitalModels.add(new HospitalModel(code,hName,wType,ww_ww[0],ww_ww[1],scannedQrCode));
                    hospitalAdapter.notifyDataSetChanged();

                }
            });

            submit_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("TAG", "onClick: "+"HCF Scan ");
                    if (dataSize < hospitalModels.size()){

                        progressDialog.show();

                        String date = DateFormat.format("yyyy-MM-dd",new Date().getTime()).toString();

                        if (scanType == 0){
                            /*
                            * this condition for HCF scan
                            * */
                            if (MSP.getInstance(getContext()).getStringData(AppStrings.loginAs).equals("hcf")){
                                Map<String,String> map = new HashMap<>();
                                map.put("weight",hospitalModels.get(dataSize).getWaste_weight()+"."+hospitalModels.get(dataSize).getWaste_weight_g());
                                map.put("admin_id",MSP.getInstance(getContext()).getStringData(AppStrings.userCbwtfID));
                                map.put("handhover_address",latitude+","+longitude);
                                map.put("type",hospitalModels.get(dataSize).getWaste_color());
                                map.put("hospital_id",hospitalModels.get(dataSize).getHcf_code());
                                map.put("seq_no",hospitalModels.get(dataSize).getQr_code());
                                map.put("year",date);
                                map.put("attenden_status","collected");
                                cbwtfScanSubmitRequest(AppStrings.hcf_scan_submit_hcf,map);

                            }else {

                                Map<String,String> map = new HashMap<>();
                                map.put("weight",hospitalModels.get(dataSize).getWaste_weight()+"."+hospitalModels.get(dataSize).getWaste_weight_g());
                                map.put("admin_id",MSP.getInstance(getContext()).getStringData(AppStrings.userCbwtfID));
                                map.put("operator_name",MSP.getInstance(getContext()).getStringData(AppStrings.userID));
                                map.put("handhover_address",latitude+","+longitude);
                                map.put("type",hospitalModels.get(dataSize).getWaste_color());
                                map.put("hospital_id",hospitalModels.get(dataSize).getHcf_code());
                                map.put("seq_no",hospitalModels.get(dataSize).getQr_code());
                                map.put("year",date);
                                map.put("attenden_status","collected");

                                deleteQRData(hospitalModels.get(dataSize).getQr_code(),map);

                            }

                        }else {
                            Map<String,String> map = new HashMap<>();
                            map.put("weight",hospitalModels.get(dataSize).getWaste_weight()+"."+hospitalModels.get(dataSize).getWaste_weight_g());
                            map.put("receiving_date",date);
                            map.put("operator_id",MSP.getInstance(getContext()).getStringData(AppStrings.userID));
                            map.put("receiving_address",latitude+","+longitude);
                            map.put("color",hospitalModels.get(dataSize).getWaste_color());
                            map.put("hospital_id",hospitalModels.get(dataSize).getHcf_code());
                            map.put("seq_no",hospitalModels.get(dataSize).getQr_code());

                            cbwtfScanSubmitRequest(AppStrings.cbwtf_scan_submit,map);
                        }

                    }
                }
            });

            hcf_reset_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MSP.getInstance(getContext()).removeData(AppStrings.currentHcfCode);
                    codeTv.setText("HCF QR Code");
                    hcf_name.setText("HCF Name");
                    total_bags.setText("0");
                    total_waste_weight.setText("000.000");
                    dataSize = 0;
                    hospitalModels.clear();
                    hospitalAdapter.notifyDataSetChanged();

                    if (scanType == 0){
                        if (MSP.getInstance(getContext()).getStringData(AppStrings.loginAs).equals("cbwtf")){
                            if (MSP.getInstance(getContext()).getStringData(AppStrings.attendance_compulsory).equals("1")){
                                submit_button.setVisibility(View.GONE);
                                scanButton.setVisibility(View.GONE);
                                attendanceButton.setVisibility(View.VISIBLE);
                                attendanceCard.setVisibility(View.VISIBLE);
                                attendanceStatusTv.setText("Pending");
                                attendanceStatusTv.setTextColor(getActivity().getResources().getColor(R.color.yellow));
                                attendanceStatusTv.setCompoundDrawablesWithIntrinsicBounds(0,0, R.drawable.ic_baseline_pending_actions_24,0);
                                setTextViewDrawableColor(attendanceStatusTv, R.color.yellow);

                            }
                        }
                    }
                }
            });

            attendanceButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (getCameraPermission()){
                        ScanOptions scanOptions = new ScanOptions();
                        scanOptions.setOrientationLocked(true);
                        scanOptions.setBarcodeImageEnabled(true);
                        scanOptions.setPrompt("Scan Attendance Barcode");
                        attendanceScanner.launch(scanOptions);
                    }else {
                        new AlertDialog.Builder(getContext()).setMessage("Grant camera permission to continue\nApp permissions > Camera > Allow").setPositiveButton("Setting", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
                                intent.setData(uri);
                                startActivity(intent);
                            }
                        }).show();
                    }
                }
            });

            if (scanType == 0){
                if (MSP.getInstance(getContext()).getStringData(AppStrings.loginAs).equals("cbwtf")){
                    if (MSP.getInstance(getContext()).getStringData(AppStrings.attendance_compulsory).equals("1")){
                        submit_button.setVisibility(View.GONE);
                        scanButton.setVisibility(View.GONE);
                        attendanceButton.setVisibility(View.VISIBLE);
                        attendanceCard.setVisibility(View.VISIBLE);
                        attendanceStatusTv.setText("Pending");
                        attendanceStatusTv.setTextColor(getActivity().getResources().getColor(R.color.yellow));
                        attendanceStatusTv.setCompoundDrawablesWithIntrinsicBounds(0,0, R.drawable.ic_baseline_pending_actions_24,0);
                        setTextViewDrawableColor(attendanceStatusTv, R.color.yellow);
                    }
                }
            }
        }
        return view;
    }

    ActivityResultLauncher<ScanOptions> attendanceScanner = registerForActivityResult(new ScanContract(), new ActivityResultCallback<ScanIntentResult>() {
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

                    if (qrCbwtfId.equals(MSP.getInstance(getContext()).getStringData(AppStrings.userCbwtfID))){
                        MSP.getInstance(getContext()).setStringData(AppStrings.currentHcfCode,hcfCode);
                        MSP.getInstance(getContext()).setStringData(AppStrings.currentHcfName,hospitalName);

                        hcf_name.setText(hospitalName);
                        codeTv.setText(hcfCode);

                        checkAttendance(hcfCode,hospitalName);
                    }else {
                        showToast("Wrong QR Code 1");
                    }

//                    MSP.getInstance(getContext()).setStringData(AppStrings.currentHcfCode,hcfCode);
//                    MSP.getInstance(getContext()).setStringData(AppStrings.currentHcfName,hospitalName);
//
//                    hcf_name.setText(hospitalName);
//                    codeTv.setText(hcfCode);
//
//                    checkAttendance(hcfCode,hospitalName);

                }catch (Exception e){
                    e.printStackTrace();
                    Log.d("TAG", "onActivityResult:2 "+e.getMessage());
                    showToast("Wrong QR Code 7");
                }

            }
        }
    });

    public void manualWeightInput(String w_color){
        AlertDialog manualWeightAlert = new AlertDialog.Builder(getContext()).create();

        View manualWeightView = LayoutInflater.from(getContext()).inflate(R.layout.manual_weight_view,null,false);

        TextView manualWeightColor = manualWeightView.findViewById(R.id.manual_weight_color);
        ImageView manualWeightImage = manualWeightView.findViewById(R.id.manual_weight_image);
        EditText manualWeightKilo = manualWeightView.findViewById(R.id.manual_weight_kilo);
        EditText manualWeightGram = manualWeightView.findViewById(R.id.manual_weight_gram);
        MaterialButton manualWeightAdd = manualWeightView.findViewById(R.id.manual_weight_add);

        manualWeightColor.setText(w_color.toUpperCase());

        if (w_color.trim().equalsIgnoreCase("red")){
            Glide.with(getContext()).load(R.drawable.red).into(manualWeightImage);
            manualWeightColor.setTextColor(getActivity().getResources().getColor(android.R.color.holo_red_light));
        }else if (w_color.trim().equalsIgnoreCase("blue")){
            Glide.with(getContext()).load(R.drawable.blue).into(manualWeightImage);
            manualWeightColor.setTextColor(getActivity().getResources().getColor(android.R.color.holo_blue_light));
        }else if (w_color.trim().equalsIgnoreCase("yellow")){
            Glide.with(getContext()).load(R.drawable.yellow).into(manualWeightImage);
            manualWeightColor.setTextColor(getActivity().getResources().getColor(android.R.color.holo_orange_light));
        }else if (w_color.trim().equalsIgnoreCase("yellow c")){
            Glide.with(getContext()).load(R.drawable.yellow).into(manualWeightImage);
            manualWeightColor.setTextColor(getActivity().getResources().getColor(android.R.color.holo_orange_light));
        }else if (w_color.trim().equalsIgnoreCase("white")){
            Glide.with(getContext()).load(R.drawable.gray).into(manualWeightImage);
            manualWeightColor.setTextColor(getActivity().getResources().getColor(android.R.color.darker_gray));
        }

        manualWeightAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String code = codeTv.getText().toString();
                String hName = hcf_name.getText().toString();
                String wType = manualWeightColor.getText().toString();

                String w_kilo = manualWeightKilo.getText().toString().trim();
                String w_gram = manualWeightGram.getText().toString().trim();

                if (!w_kilo.isEmpty() && !w_gram.isEmpty()){

                    int d_kilo = Integer.parseInt(w_kilo.trim());
                    int d_gram = Integer.parseInt(w_gram.trim());

                    String decimalKilo = String.format("%03d",d_kilo);
                    String decimalGram = String.format("%03d",d_gram);

                    hospitalModels.add(new HospitalModel(code,hName,wType,decimalKilo,decimalGram,scannedQrCode));
                    hospitalAdapter.notifyDataSetChanged();
                    barcodeView.resume();
                    manualWeightAlert.dismiss();
                }else {
                    showToast("Empty field");
                }

            }
        });
        barcodeView.resume();
        manualWeightAlert.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        manualWeightAlert.setView(manualWeightView);
        manualWeightAlert.show();
    }

    public void getHospitalData(String id,String nameHos){

        StringRequest stringRequest = new StringRequest(Request.Method.POST, AppStrings.hospital_data, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {

                    JSONObject jsonObject = new JSONObject(response);

                    if (jsonObject.get("status").toString().equalsIgnoreCase("success")){

                        String name = jsonObject.getJSONArray("data").getJSONObject(0).get("name").toString();

                        if (nameHos.isEmpty()){
                            checkAttendance(id,name);
                        }else {
                            checkAttendance(id,nameHos);
                        }

                    }else {
                        showToast("Wrong QR Code 5");
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
                Map<String,String> map = new HashMap<>();
                map.put("hospital_code",id);
                return map;
            }
        };

        VolleySingleton.getInstance(getContext()).addToRequestQueue(stringRequest);

    }




    public boolean getCameraPermission(){
        Dexter.withContext(getContext()).withPermission(Manifest.permission.CAMERA).withListener(new PermissionListener() {
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

    public void checkLocationPermission(){

        Dexter.withContext(getContext()).withPermissions(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {

                if (multiplePermissionsReport.areAllPermissionsGranted()){
                    checkLocationSetting();
                }else{
                    new AlertDialog.Builder(getContext()).setMessage("Grant location permission to continue\nApp permissions > Location > Allow").setPositiveButton("Setting", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
                            intent.setData(uri);
                            startActivity(intent);
                        }
                    }).show();
                }

            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                permissionToken.continuePermissionRequest();
            }

        }).check();

    }

    public void checkLocationSetting(){
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(getContext())
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
                            fusedLocationProviderClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, new CancellationToken() {
                                @Override
                                public boolean isCancellationRequested() {
                                    return false;
                                }

                                @NonNull
                                @Override
                                public CancellationToken onCanceledRequested(@NonNull OnTokenCanceledListener onTokenCanceledListener) {
                                    return null;
                                }
                            }).addOnSuccessListener(new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location location) {
                                    if (location != null){
                                        latitude = location.getLatitude();
                                        longitude = location.getLongitude();

                                        try {

                                            List<Address> addresses = geocoder.getFromLocation(latitude,longitude,5);
                                            location_tv.setText(addresses.get(0).getAddressLine(0));

                                        }catch (Exception e){
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            });
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the user
                            // a dialog.
                            try {
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                status.startResolutionForResult(
                                        getActivity(), 1000);
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
                            fusedLocationProviderClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, new CancellationToken() {
                                @Override
                                public boolean isCancellationRequested() {
                                    return false;
                                }

                                @NonNull
                                @Override
                                public CancellationToken onCanceledRequested(@NonNull OnTokenCanceledListener onTokenCanceledListener) {
                                    return null;
                                }
                            }).addOnSuccessListener(new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location location) {
                                    if (location != null){
                                        latitude = location.getLatitude();
                                        longitude = location.getLongitude();

                                        try {

                                            List<Address> addresses = geocoder.getFromLocation(latitude,longitude,5);
                                            location_tv.setText(addresses.get(0).getAddressLine(0));

                                        }catch (Exception e){
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            });
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the user
                            // a dialog.
                            try {
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                status.startResolutionForResult(
                                        getActivity(), 1000);
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

    public void getBluetoothDevices(){
        bluetoothDevicesModel.clear();

        bluetooth.onStart();

        List<BluetoothDevice> bluetoothDevices = bluetooth.getPairedDevices();

        alertDialog = new AlertDialog.Builder(getContext()).create();

        View bl_view = LayoutInflater.from(getContext()).inflate(R.layout.blutooth_view,null,false);

        ImageView closeAlert = bl_view.findViewById(R.id.alert_dismiss);
        blRv = bl_view.findViewById(R.id.bluetooth_devices_rv);

        alertDialog.setView(bl_view);
        alertDialog.setCancelable(false);

        blRv.setLayoutManager(new LinearLayoutManager(getContext(),RecyclerView.VERTICAL,false));
        blRv.setAdapter(bluetoothDevicesAdapter);

        closeAlert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                barcodeView.resume();
                alertDialog.dismiss();
            }
        });

        alertDialog.show();

        for (int i =0;i<bluetoothDevices.size();i++){

            bluetoothDevicesModel.add(new BluetoothDevicesModel(bluetoothDevices.get(i),1));
            bluetoothDevicesAdapter.notifyDataSetChanged();

        }

        Dexter.withContext(getContext()).withPermissions(permissions).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {

                statusCheck();

            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {

            }

        }).check();
    }



    @Override
    public void getDevices(BluetoothDevice device) {
        bluetoothDevicesModel.add(new BluetoothDevicesModel(device,0));
        bluetoothDevicesAdapter.notifyDataSetChanged();
    }

    @Override
    public void getData(String data){
        Log.d("TAG", "getData: " + data);
        finalWeight = data;
        weight_tv.setText(data);
        if (isWeightAdded) {
            isWeightAdded = false;
            if (!weight_tv.getText().toString().trim().equals("0")) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run(){
                        Log.d("TAG", "Checking isFirstScanHCF before addWeightAuto: " + isFirstScanHCF);
                        if (!isFirstScanHCF){
                            addWeightAuto();
                        }else{
                            Log.d("TAG", "isFirstScanHCF is true, skipping addWeightAuto.");
                        }
                    }
                }, 1000);
            }
        }
    }

//    public void getData(String data) {
//
//        Log.d("TAG", "getData: "+data);
//        finalWeight = data;
//
//        weight_tv.setText(data);
//
//        if (isWeightAdded){
//            isWeightAdded = false;
//            if (!weight_tv.getText().toString().trim().equals("0")){
//                new Handler().postDelayed(new Runnable(){
//                    @Override
//                    public void run() {
//                        Log.d("TAG", "isFirstScanHCF: "+isFirstScanHCF);
//                        if (!isFirstScanHCF){
//                            addWeightAuto();
//                        }else {
//
//                        }
//
//
//                    }
//                }, 1000);
//            }
//        }
//
//    }

    @Override
    public void connectionStatus(BluetoothStatus bluetoothStatus) {
        if (bluetoothStatus == BluetoothStatus.CONNECTING){
            loading_ll.setVisibility(View.VISIBLE);
        }else if (bluetoothStatus == BluetoothStatus.CONNECTED){
            bluetoothConnected = true;
            loading_ll.setVisibility(View.GONE);
            MSP.getInstance(getContext()).setStringData(AppStrings.bluetoothConnection,"1");
        }else {
            bluetoothConnected = false;
        }
    }

    public void statusCheck() {
        final LocationManager manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();

        }else {

            if (bluetooth.isEnabled()){
                bluetooth.onStop();
                myBluetoothService.startScanService();
            }else {
                bluetooth.enable();
                bluetooth.onStop();
                myBluetoothService.startScanService();
            }

        }
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("Enable GPS")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void updateUI() {

        int countBags = 0;
        double countWeight = 000.000;

        if (hospitalModels.size() > 0){
            for (HospitalModel hm:hospitalModels){
                countBags++;
                String weightData = hm.getWaste_weight().trim().concat(".").concat(hm.getWaste_weight_g().trim());
                countWeight = countWeight+Double.parseDouble(weightData);
            }

            empty_tv.setVisibility(View.GONE);
        }else {
            countWeight = 0;
            countBags = 0;

            empty_tv.setVisibility(View.VISIBLE);
        }

        total_bags.setText(String.valueOf(countBags));
        total_waste_weight.setText(new DecimalFormat("000.000").format(countWeight));

    }


    public void cbwtfScanSubmitRequest(String url, Map<String,String> map){
        Log.d("TAG", "cbwtfScanSubmitRequest: "+"SCAN");


        for (Map.Entry<String, String> entry : map.entrySet()) {
            String k = entry.getKey();
            String v = entry.getValue();
            Log.d("TAG", "cbwtfScanSubmitRequest: "+"Key: " + k + ", Value: " + v);

        }

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {

                    JSONObject jsonObject = new JSONObject(response);

                    if (jsonObject.get("status").toString().equalsIgnoreCase("success")){

                        dataSize++;

                        if (dataSize < hospitalModels.size()){

                            String date = DateFormat.format("yyyy-MM-dd",new Date().getTime()).toString();

                            if (scanType == 0){

                                if (MSP.getInstance(getContext()).getStringData(AppStrings.loginAs).equals("hcf")){
                                    Map<String,String> map = new HashMap<>();
                                    map.put("weight",hospitalModels.get(dataSize).getWaste_weight()+"."+hospitalModels.get(dataSize).getWaste_weight_g());
                                    map.put("admin_id",MSP.getInstance(getContext()).getStringData(AppStrings.userCbwtfID));
                                    map.put("handhover_address",latitude+","+longitude);
                                    map.put("type",hospitalModels.get(dataSize).getWaste_color());
                                    map.put("hospital_id",hospitalModels.get(dataSize).getHcf_code().trim());
                                    map.put("seq_no",hospitalModels.get(dataSize).getQr_code());
                                    map.put("year",date);
                                    map.put("attenden_status","empty");

                                    cbwtfScanSubmitRequest(AppStrings.hcf_scan_submit_hcf,map);
                                }else{
                                    Map<String,String> map = new HashMap<>();
                                    map.put("weight",hospitalModels.get(dataSize).getWaste_weight()+"."+hospitalModels.get(dataSize).getWaste_weight_g());
                                    map.put("admin_id",MSP.getInstance(getContext()).getStringData(AppStrings.userCbwtfID));
                                    map.put("operator_name",MSP.getInstance(getContext()).getStringData(AppStrings.userID));
                                    map.put("handhover_address",latitude+","+longitude);
                                    map.put("type",hospitalModels.get(dataSize).getWaste_color());
                                    map.put("hospital_id",hospitalModels.get(dataSize).getHcf_code().trim());
                                    map.put("seq_no",hospitalModels.get(dataSize).getQr_code());
                                    map.put("year",date);
                                    map.put("attenden_status","collected");

                                    deleteQRData(hospitalModels.get(dataSize).getQr_code(),map);
                                }
                            }else {
                                Map<String,String> map = new HashMap<>();
                                map.put("weight",hospitalModels.get(dataSize).getWaste_weight()+"."+hospitalModels.get(dataSize).getWaste_weight_g());
                                map.put("receiving_date",date);
                                map.put("operator_id",MSP.getInstance(getContext()).getStringData(AppStrings.userID));
                                map.put("receiving_address",latitude+","+longitude);
                                map.put("color",hospitalModels.get(dataSize).getWaste_color());
                                map.put("hospital_id",hospitalModels.get(dataSize).getHcf_code().trim());
                                map.put("seq_no",hospitalModels.get(dataSize).getQr_code());

                                cbwtfScanSubmitRequest(AppStrings.cbwtf_scan_submit,map);
                            }

                        }

                        if (dataSize == hospitalModels.size()){
                            progressDialog.dismiss();
                            showToast("Submitted");
                            total_bags.setText("0");
                            total_waste_weight.setText("000.000");
                            dataSize = 0;
                            hospitalModels.clear();
                            hospitalAdapter.notifyDataSetChanged();
                            getQRData(AppStrings.get_all_report);
                        }

                    }

                }catch (Exception e){
                    e.printStackTrace();
                    progressDialog.dismiss();
                    Log.i("res_p","e: "+e.getMessage());
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("res_p","err: "+error.getMessage());
                progressDialog.dismiss();
            }
        }){
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return map;
            }
        };

        VolleySingleton.getInstance(getActivity()).addToRequestQueue(request);
    }

    public void getQRData(String url){
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response){
                try {
                    JSONObject jsonObject = new JSONObject(response);

                    if (jsonObject.get("status").toString().equalsIgnoreCase("success")){

                        reportsModels.clear();

                        int size = jsonObject.getJSONArray("data").length();

                        for (int i=0;i<size;i++){

                            JSONObject jsonObject1 = jsonObject.getJSONArray("data").getJSONObject(i);

                            ReportsModel model = new ReportsModel();
                            model.setQrId(jsonObject1.get("qr_id").toString());
                            model.setType(jsonObject1.get("type").toString());
                            reportsModels.add(model);
                        }

                    }else {

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

        VolleySingleton.getInstance(getContext()).addToRequestQueue(request);
    }

    public void deleteQRData(String qrId,Map<String,String> map){
        StringRequest request = new StringRequest(Request.Method.POST, AppStrings.delete_qr, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONObject jsonObject = new JSONObject(response);

                    if (jsonObject.get("status").toString().equalsIgnoreCase("success")){

                        cbwtfScanSubmitRequest(AppStrings.hcf_scan_submit,map);

                    }else {
                        cbwtfScanSubmitRequest(AppStrings.hcf_scan_submit,map);
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
                Map<String,String> params = new HashMap<>();
                params.put("qr_id",qrId);
                return params;
            }
        };

        VolleySingleton.getInstance(getContext()).addToRequestQueue(request);
    }

    public void getOtp(String otp_value,AlertDialog dialog,TextView errorText,OtpView mOtpView,String h_cOde){

        StringRequest otpRequest = new StringRequest(Request.Method.POST, AppStrings.get_otp, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {

                    JSONObject otpObject = new JSONObject(response);

                    if (otpObject.get("status").toString().equalsIgnoreCase("success")){

                        dialog.dismiss();

                        String otp = otpObject.getJSONArray("data").getJSONObject(0).get("otp").toString();

                        MSP.getInstance(getContext()).setStringData(h_cOde,otp);

                        markAttendance(h_cOde);

                    }else {

                        dialog.dismiss();
                        dialog.show();
                        errorText.setText("Wrong OTP");
                        mOtpView.setText("");

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
                Map<String,String> otp = new HashMap<>();
                otp.put("hcf_code",h_cOde);
                otp.put("otp",otp_value);
                otp.put("date",DateFormat.format("yyyy-MM-dd",new Date().getTime()).toString());
                return otp;
            }
        };

        VolleySingleton.getInstance(getContext()).addToRequestQueue(otpRequest);
    }



    public void checkAttendance(String mHospitalCode,String hos_name){
        StringRequest checkAttendanceRequest = new StringRequest(Request.Method.POST, AppStrings.check_attendance, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {

                    JSONObject otpObject = new JSONObject(response);

                    if (otpObject.get("status").toString().equalsIgnoreCase("success")){
                        submit_button.setVisibility(View.VISIBLE);
                        scanButton.setVisibility(View.VISIBLE);
                        attendanceButton.setVisibility(View.GONE);
                        attendanceCard.setVisibility(View.VISIBLE);
                        attendanceStatusTv.setText("Done");
                        attendanceStatusTv.setTextColor(getActivity().getResources().getColor(R.color.green));
                        attendanceStatusTv.setCompoundDrawablesWithIntrinsicBounds(0,0, R.drawable.ic_baseline_check_24,0);
                        setTextViewDrawableColor(attendanceStatusTv, R.color.green);
                    }else {
                        AlertDialog otpAlertDialog = new AlertDialog.Builder(getContext()).create();
                        View otpView = LayoutInflater.from(getContext()).inflate(R.layout.otp_view,null,false);
                        otpAlertDialog.setView(otpView);
                        OtpView mOtp = otpView.findViewById(R.id.otp_view);
                        MaterialButton otpButton = otpView.findViewById(R.id.otp_submit);
                        TextView otpName = otpView.findViewById(R.id.otp_name);
                        TextView otpError = otpView.findViewById(R.id.otp_error);
                        otpName.setText(hos_name);
                        otpAlertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                //progressDialog.dismiss();
                            }
                        });
                        mOtp.setOtpCompletionListener(new OnOtpCompletionListener() {
                            @Override
                            public void onOtpCompleted(String otp) {
                                otpButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        otpError.setText("");
                                        getOtp(otp,otpAlertDialog,otpError,mOtp,mHospitalCode);
                                    }
                                });
                            }
                        });
                        otpAlertDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                        otpAlertDialog.show();
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
                Map<String,String> param = new HashMap<>();
                param.put("hcf_code",mHospitalCode);
                param.put("operator_id",MSP.getInstance(getContext()).getStringData(AppStrings.userID));
                param.put("date",DateFormat.format("yyyy-MM-dd",new Date().getTime()).toString());
                return param;
            }
        };

        VolleySingleton.getInstance(getContext()).addToRequestQueue(checkAttendanceRequest);
    }

    public void markAttendance(String mHospitalCode){
        StringRequest markAttendanceRequest = new StringRequest(Request.Method.POST, AppStrings.mark_attendance, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {

                    JSONObject otpObject = new JSONObject(response);

                    if (otpObject.get("status").toString().equalsIgnoreCase("success")){

                        submit_button.setVisibility(View.VISIBLE);
                        scanButton.setVisibility(View.VISIBLE);
                        attendanceButton.setVisibility(View.GONE);
                        attendanceCard.setVisibility(View.VISIBLE);
                        attendanceStatusTv.setText("Done");
                        attendanceStatusTv.setTextColor(getActivity().getResources().getColor(R.color.green));
                        attendanceStatusTv.setCompoundDrawablesWithIntrinsicBounds(0,0, R.drawable.ic_baseline_check_24,0);
                        setTextViewDrawableColor(attendanceStatusTv, R.color.green);

                    }else {

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
                Map<String,String> param = new HashMap<>();
                param.put("hcf_code",mHospitalCode);
                param.put("operator_id",MSP.getInstance(getContext()).getStringData(AppStrings.userID));
                param.put("cbwtf_id",MSP.getInstance(getContext()).getStringData(AppStrings.userCbwtfID));
                param.put("date",DateFormat.format("yyyy-MM-dd",new Date().getTime()).toString());
                param.put("lat_long",latitude+","+longitude);
                return param;
            }
        };

        VolleySingleton.getInstance(getContext()).addToRequestQueue(markAttendanceRequest);
    }

    private void setTextViewDrawableColor(TextView textView, int color) {
        for (Drawable drawable : textView.getCompoundDrawables()) {
            if (drawable != null) {
                drawable.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(textView.getContext(), color), PorterDuff.Mode.SRC_IN));
            }
        }
    }

    private void updateHospitalLocationForOnce(String mHospitalCode) {

        StringRequest markAttendanceRequest = new StringRequest(Request.Method.POST, AppStrings.set_hospital_location, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {

                    JSONObject otpObject = new JSONObject(response);

                    if (otpObject.get("status").toString().equalsIgnoreCase("success")) {

                    } else {

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> param = new HashMap<>();
                param.put("hcf_code", mHospitalCode);
                param.put("lat_long", latitude + "," + longitude);
                return param;
            }
        };

        VolleySingleton.getInstance(getContext()).addToRequestQueue(markAttendanceRequest);
    }

    private void barcode(View view){
        barcodeView = view.findViewById(R.id.barcode_scanner);

        CameraSettings settings = new CameraSettings();
        settings.setContinuousFocusEnabled(true);
        barcodeView.setCameraSettings(settings);

        Collection<BarcodeFormat> formats = Arrays.asList(BarcodeFormat.QR_CODE, BarcodeFormat.CODE_39);
        barcodeView.getBarcodeView().setDecoderFactory(new DefaultDecoderFactory(formats));
        barcodeView.initializeFromIntent(requireActivity().getIntent());
        barcodeView.decodeContinuous(callback);

        beepManager = new BeepManager(requireActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (barcodeView != null) {
            barcodeView.resume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (barcodeView != null) {
            barcodeView.pause();
        }
    }

    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if(result.getText() == null || result.getText().equals(lastText)) {
                // Prevent duplicate scans
                return;
            }

            lastText = result.getText();

            barcodeView.pause();
            isWeightAdded = true;
            beepManager.playBeepSoundAndVibrate();

                try {
                    String[] data = lastText.split("&",2);

                    String hcfCode = data[0];

                    String[] data1 = data[1].split("/",3);

                    String hospitalName = data1[1];
                    String qrCbwtfId = data1[2];

                    String[] data2 = data1[0].split("-",2);

                    String qrCode = data2[0];
                    String qrColor = data2[1];

                    scannedQrCode = qrCode;
                    scannedHcfCode=hcfCode;

                    boolean qrAlreadyScanned = false;
                    boolean qrSwitch = false;

                    if (scanType == 0){
                        if (MSP.getInstance(getContext()).getStringData(AppStrings.loginAs).equals("hcf")){
                            if (!hcfCode.equals(MSP.getInstance(getContext()).getStringData(AppStrings.userID))){
                                qrAlreadyScanned = true;
                            }
                            for (ReportsModel rm:reportsModels){
                                if (rm.getQrId().equalsIgnoreCase(scannedQrCode)) {
                                    qrAlreadyScanned = true;
                                    break;
                                }
                            }
                        }else {
                            for (ReportsModel rm:reportsModels){
                                if (rm.getQrId().equalsIgnoreCase(scannedQrCode) && rm.getType().equals("waste")) {
                                    qrAlreadyScanned = true;
                                    break;
                                }
                            }
                        }
                    }else {
                        qrAlreadyScanned = true;

                        String customDataJson = MSP.getInstance(getContext()).getStringData("today_qr_custom_data");
                        if (customDataJson != null && !customDataJson.isEmpty()) {
                            try {
                                Gson gson = new Gson();
                                Type type = new TypeToken<List<Map<String, String>>>() {}.getType();
                                List<Map<String, String>> customDataList = gson.fromJson(customDataJson, type);

                                for (Map<String, String> dataMap : customDataList) {
                                    String qrId = dataMap.get("qr_id");
                                    String cbwtfWeight = dataMap.get("cbwtf_weight");
                                    String hcfWeight = dataMap.get("hcf_weight");

                                    if (qrId != null && qrId.equalsIgnoreCase(scannedQrCode)) {
                                        if (hcfWeight != null && (!hcfWeight.equals("0") || !hcfWeight.equals("0.0") || !hcfWeight.equals("0.00") || !hcfWeight.equals("0.000"))) {
                                            qrSwitch = true;
                                        }
                                        if (cbwtfWeight != null && (cbwtfWeight.equals("0") || cbwtfWeight.equals("0.0") || cbwtfWeight.equals("0.00") || cbwtfWeight.equals("0.000"))) {
                                            qrAlreadyScanned = false;
                                            break;
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        /*if (qrAlreadyScanned) {
                            for (ReportsModel rm : reportsModels) {
                                if (rm.getQrId().equalsIgnoreCase(scannedQrCode) && rm.getType().equals("waste")) {
                                    qrAlreadyScanned = false;
                                    break;
                                }
                            }
                        }*/
                    }

                    if (!qrCbwtfId.equals(MSP.getInstance(getContext()).getStringData(AppStrings.userCbwtfID))){
                        qrAlreadyScanned = true;
                    }

                    for (HospitalModel hmh:hospitalModels){
                        if (hmh.getQr_code().equalsIgnoreCase(scannedQrCode)) {
                            qrAlreadyScanned = true;
                            qrSwitch = true;
                            break;
                        }
                    }

                    if (!qrAlreadyScanned){
                        if (!MSP.getInstance(getContext()).containsData(AppStrings.currentHcfCode)){
                            MSP.getInstance(getContext()).setStringData(AppStrings.currentHcfCode,hcfCode);
                            MSP.getInstance(getContext()).setStringData(AppStrings.currentHcfName,hospitalName);
                            hcf_name.setText(hospitalName);
                            weight_hcf_name.setText(hospitalName);
                            codeTv.setText(hcfCode);
                            weight_type.setText(qrColor.toUpperCase());
                            switch (scanMode){
                                case 0:
                                    manualWeightInput(qrColor);
                                    break;
                                case 1:
                                    if (bluetoothConnected){
                                        weightView.setVisibility(View.VISIBLE);
                                    }else {
                                        getBluetoothDevices();
                                    }
                                    break;
                            }
                        }else if (MSP.getInstance(getContext()).containsData(AppStrings.currentHcfCode)){
                            hcf_name.setText(hospitalName);
                            weight_hcf_name.setText(hospitalName);
                            codeTv.setText(hcfCode);
                            weight_type.setText(qrColor.toUpperCase());
                            switch (scanMode){
                                case 0:
                                    manualWeightInput(qrColor);
                                    break;
                                case 1:
                                    if (bluetoothConnected){
                                        weightView.setVisibility(View.VISIBLE);
                                    }else {
                                        getBluetoothDevices();
                                    }
                                    break;
                            }
                        }else {
                            showToast("Reset HCF Code to scan other HCF", Toast.LENGTH_LONG);
                        }

                        if (qrColor.trim().equalsIgnoreCase("red")){
                            Glide.with(getContext()).load(R.drawable.red).into(weight_type_img);
                            weight_type.setTextColor(getActivity().getResources().getColor(android.R.color.holo_red_light));
                        }else if (qrColor.trim().equalsIgnoreCase("blue")){
                            Glide.with(getContext()).load(R.drawable.blue).into(weight_type_img);
                            weight_type.setTextColor(getActivity().getResources().getColor(android.R.color.holo_blue_light));
                        }else if (qrColor.trim().equalsIgnoreCase("yellow")){
                            Glide.with(getContext()).load(R.drawable.yellow).into(weight_type_img);
                            weight_type.setTextColor(getActivity().getResources().getColor(android.R.color.holo_orange_light));
                        }else if (qrColor.trim().equalsIgnoreCase("yellow c")){
                            Glide.with(getContext()).load(R.drawable.yellow).into(weight_type_img);
                            weight_type.setTextColor(getActivity().getResources().getColor(android.R.color.holo_orange_light));
                        }else if (qrColor.trim().equalsIgnoreCase("white")){
                            Glide.with(getContext()).load(R.drawable.gray).into(weight_type_img);
                            weight_type.setTextColor(getActivity().getResources().getColor(android.R.color.darker_gray));
                        }

                        isFirstScanHCF = false;

                    } else {
                        Log.d("TAG", "barcodeResult: "+scanType);
                        if (scanType == 0){
                            barcodeView.resume();
                            showToast("Already Scanned Or Wrong QR");
                        }else{
                            if(!qrSwitch && scanType != 0){
                                barcodeView.resume();
                                isFirstScanHCF = true;
                                Log.d("TAG", "barcodeResult:First scan hcf ");
                                showToast("First scan hcf");
                            }else{
                                barcodeView.resume();
                                showToast("Already Scanned Or Wrong QR OR Check For Near By Device permission");
                            }
                        }
                    }

                    updateHospitalLocationForOnce(hcfCode);

                } catch (Exception e) {
                    e.printStackTrace();
                    barcodeView.resume();
                    Log.d("TAG", "error " + e.getMessage());
                    isFirstScanHCF = true;
                    showToast("Wrong QR Code Sticker OR Check For Near By Device permission");
                }
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };

    @SuppressLint("MissingPermission")
    @Override
    public void onClickBluetoothDevice(BluetoothDevice bluetoothDevice, int status) {

        if (bluetoothDevice.getBondState() == BluetoothDevice.BOND_NONE){
            bluetoothDevice.createBond();
        }else {
            myBluetoothService.startDisconnectService();
            myBluetoothService.connectDevice(bluetoothDevice);

            weightView.setVisibility(View.VISIBLE);

            alertDialog.dismiss();

            bluetoothConnected = true;
            MSP.getInstance(getContext()).setStringData(AppStrings.bluetoothConnection,"1");
        }

    }

//    private void addWeightAuto() {
//        String code = codeTv.getText().toString();
//        String hName = hcf_name.getText().toString();
//        String wType = weight_type.getText().toString();
//        weightView.setVisibility(View.GONE);
//
//        String[] ww_ww = finalWeight.trim().split("\\.");
//
//        // Case 1: If the list is empty, add the first item directly
//        if (hospitalModels.isEmpty()) {
//            Log.d("TAG", "Adding first item to hospitalModels");
//            hospitalModels.add(new HospitalModel(code, hName, wType, ww_ww[0], ww_ww[1], scannedQrCode));
//            isFirstScanHCF = false;
//        }
//        // Case 2: If the list is not empty, check for duplicates
//        else {
//            boolean isDuplicate = false;
//
//            for (HospitalModel model : hospitalModels) {
//                if (model.qr_code.equals(scannedQrCode)) {
//                    isDuplicate = true;
//                    break;
//                }
//            }
//
//            if (!isDuplicate) {
//                Log.d("TAG", "Adding new item to hospitalModels");
//                hospitalModels.add(new HospitalModel(code, hName, wType, ww_ww[0], ww_ww[1], scannedQrCode));
//                isFirstScanHCF = false;
//            }
//            else {
//                Log.d("TAG", "Duplicate QR code, item not added");
//            }
//        }
//
//        hospitalAdapter.notifyDataSetChanged();
//        isWeightAdded = false;
//        weight_tv.setText("0");
//        barcodeView.resume();
//    }


    private void showToast(String message) {
        if (isAdded() && getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    private void showToast(String message, int duration) {
        if (isAdded() && getContext() != null) {
            Toast.makeText(getContext(), message, duration).show();
        }
    }

    private void addWeightAuto(){
        String code = codeTv.getText().toString().trim();
        String hName = hcf_name.getText().toString().trim();
        String wType = weight_type.getText().toString().trim();
        weightView.setVisibility(View.GONE);

        if (finalWeight == null || finalWeight.trim().isEmpty()) {
            Log.d("TAG", "Final weight is empty, skipping addition.");
            return;
        }

        String[] ww_ww = finalWeight.trim().split("\\.");

        // Ensure valid weight format
        if (ww_ww.length < 2) {
            Log.d("TAG", "Invalid weight format. Skipping.");
            return;
        }

        // Check if the scanned QR code already exists in reportsModels
        boolean qrExistsInReports = false;
        for (ReportsModel report : reportsModels) {
            if (report.getQrId().equalsIgnoreCase(scannedQrCode)) {
                qrExistsInReports = true;
                break;
            }
        }

        // Check if the scanned QR code already exists in hospitalModels
        boolean qrExistsInHospitals = false;
        for (HospitalModel model : hospitalModels) {
            if (model.getQr_code().equalsIgnoreCase(scannedQrCode)) {
                qrExistsInHospitals = true;
                break;
            }
        }

        if (scanType==0){
         //   if (MSP.getInstance(getContext()).containsData(AppStrings.loginAs) && MSP.getInstance(requireActivity()).getStringData(AppStrings.loginAs).equalsIgnoreCase("hcf")){
            if (MSP.getInstance(getContext()).getStringData(AppStrings.loginAs).equals("hcf"))
            {
                if (scannedHcfCode == null || MSP.getInstance(getContext()).getStringData(AppStrings.hcfCode) == null || !scannedHcfCode.equalsIgnoreCase(MSP.getInstance(getContext()).getStringData(AppStrings.hcfCode))) {
                   showToast("Invalid Hospital ");
                   barcodeView.resume();
                   //  qrAlreadyScanned = true
                   return;
               }else {
                   if (qrExistsInReports || qrExistsInHospitals) {
                       Log.d("TAG", "Duplicate QR code detected. Not adding.");
                       showToast("Duplicate QR Code! Not Added.");
                       return;
                   } else {
                       hospitalModels.add(new HospitalModel(code, hName, wType, ww_ww[0], ww_ww[1], scannedQrCode));
                       hospitalAdapter.notifyDataSetChanged();
                       isFirstScanHCF=false;
                       isWeightAdded = false;
                       weight_tv.setText("0");
                       barcodeView.resume();
                   }
               }

            }else{
                if (qrExistsInReports || qrExistsInHospitals) {
            Log.d("TAG", "Duplicate QR code detected. Not adding.");
            showToast("Duplicate QR Code! Not Added.");
            return;
        } else {
            hospitalModels.add(new HospitalModel(code, hName, wType, ww_ww[0], ww_ww[1], scannedQrCode));
            hospitalAdapter.notifyDataSetChanged();
            isFirstScanHCF=false;
            isWeightAdded = false;
            weight_tv.setText("0");
            barcodeView.resume();
        }}
        }
        else {
            if (qrExistsInHospitals){
                showToast("Duplicate QR Code! Not Added.");
                return;
            }else{
                hospitalModels.add(new HospitalModel(code, hName, wType, ww_ww[0], ww_ww[1], scannedQrCode));
                hospitalAdapter.notifyDataSetChanged();
                isFirstScanHCF=false;
                isWeightAdded = false;
                weight_tv.setText("0");
                barcodeView.resume();
            }
        }

        // If not duplicate, add the new entry
        Log.d("TAG", "Adding new QR code to hospitalModels.");

        // Notify adapter to refresh UI

    }



}