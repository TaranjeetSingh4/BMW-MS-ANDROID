package com.ostron.EcoGov.cbwtf.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;


import com.facebook.shimmer.ShimmerFrameLayout;
import com.ostron.EcoGov.R;
import com.ostron.EcoGov.cbwtf.adapters.GetCbwtfReportsAdapter;
import com.ostron.EcoGov.cbwtf.adapters.ReportsAdapter;
import com.ostron.EcoGov.cbwtf.models.ReportsModel;
import com.ostron.EcoGov.cbwtf.models.getCbwtfReportsModel.DataItem;
import com.ostron.EcoGov.classes.AppStrings;
import com.ostron.EcoGov.classes.MSP;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReportsFragment extends Fragment{
    View view;
    RecyclerView reportsRV;
    List<ReportsModel> reportsModels = new ArrayList<>();
    List<DataItem> reportsCbwtfModels = new ArrayList<>();
    ReportsAdapter reportsAdapter;
    GetCbwtfReportsAdapter getCbwtfReportsAdapter;
    ImageView backButton;

    TextView empty_view, txtFromDate,txtToDate;
    ShimmerFrameLayout shimmerFrameLayout;

    SearchView searchView;
    Button btGetPDF;
    String cHCode = "";
    ImageView imgFromDate,imgToDate;
    private static final int REQUEST_PERMISSIONS = 123;

    ImageView imgSend;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view == null){

            view = inflater.inflate(R.layout.fragment_reports, container, false);

            imgSend = view.findViewById(R.id.imgSend);
            reportsRV = view.findViewById(R.id.report_rv_1);
            backButton = view.findViewById(R.id.report_back_button);
            shimmerFrameLayout = view.findViewById(R.id.shimmer_report);
            empty_view = view.findViewById(R.id.report_empty_tv);
            searchView = view.findViewById(R.id.report_search_bar);
            txtFromDate=view.findViewById(R.id.txtFromDate);
            txtToDate=view.findViewById(R.id.txtToDate);
            btGetPDF=view.findViewById(R.id.btGetPDF);
            imgFromDate= view.findViewById(R.id.imgFromDate);
            imgToDate= view.findViewById(R.id.imgToDate);

            txtFromDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openDatePicker(0);
                }
            });

            imgFromDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openDatePicker(0);
                }
            });

            txtToDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openDatePicker(1);
                }
            });

            imgToDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openDatePicker(1);
                }
            });

            imgSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sendMail();
                    Toast.makeText(getContext(), "Email Sent", Toast.LENGTH_SHORT).show();
                }
            });

            btGetPDF.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                   /* checkPermissionsAndDownloadPDF();*/

                    downloadPDF();

                }
            });

            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().finish();
                }
            });

            reportsAdapter = new ReportsAdapter(getContext(),reportsModels);
            reportsRV.setLayoutManager(new LinearLayoutManager(getContext(),RecyclerView.VERTICAL,false));
            reportsRV.setAdapter(reportsAdapter);
            Map<String,String> map = new HashMap<>();

            if (MSP.getInstance(getContext()).getStringData(AppStrings.loginAs).equals("hcf")){
                searchView.setVisibility(View.GONE);

                Log.d("TAG", "onCreateView: "+1);
                map.put("hospital_id", MSP.getInstance(getContext()).getStringData(AppStrings.userID));

                getData(AppStrings.get_report_hcf,map);
            }else{
                Log.d("TAG", "onCreateView: "+2);
                map.put("operator_id", MSP.getInstance(getContext()).getStringData(AppStrings.userID));
                imgSend.setVisibility(View.GONE);
                txtFromDate.setVisibility(View.GONE);
                imgFromDate.setVisibility(View.GONE);
                imgToDate.setVisibility(View.GONE);
                txtToDate.setVisibility(View.GONE);
                btGetPDF.setVisibility(View.GONE);
                getData(AppStrings.get_report,map);
            }
            shimmerFrameLayout.stopShimmer();
            shimmerFrameLayout.setVisibility(View.GONE);

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {

                    if (!query.trim().isEmpty()){
                        searchQuery(query);
                    }else {
                        reportsAdapter.onUpdate(reportsModels);
                    }
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {

                    if (!newText.trim().isEmpty()){
                        searchQuery(newText);
                    }else {
                        reportsAdapter.onUpdate(reportsModels);
                    }

                    return true;
                }
            });

        }

        return view;
    }

    private void checkPermissionsAndDownloadPDF() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSIONS);
        } else {
            // Permission already granted, proceed with download
            downloadPDF();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with download
                downloadPDF();
            } else {
                // Permission denied, handle accordingly
                // You can show a message explaining that the app needs this permission to work
            }
        }
    }

    private void downloadPDF() {
        String fromDate = txtFromDate.getText().toString().trim();
        String toDate = txtToDate.getText().toString().trim();
        String hospitalCode = cHCode;

        String baseUrl = "https://bmwms.in/app/App_Cbwtf/get_hcf/";
        String queryParams = String.format("from_date=%s&to_date=%s&hospital_code=%s",
                fromDate, toDate, hospitalCode);
        String pdfUrl = baseUrl + hospitalCode +"/"+fromDate+"/"+toDate;

      /*  DownloadPDFTask downloadTask = new DownloadPDFTask();
        Log.d("TAG", "downloadPDF: "+pdfUrl);
        downloadTask.execute(pdfUrl);
*/

        Log.d("TAG", "downloadPDF: "+pdfUrl);

        Uri uri = Uri.parse(pdfUrl);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setPackage("com.android.chrome"); // Package name for Chrome

        try {
            startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            // Chrome is not installed or the intent failed
            // You can try to open the URL with a different browser here
        }


      /*  Map<String,String> map = new HashMap<>();
        map.put("from_date",txtFromDate.getText().toString().trim());
        map.put("to_date", txtToDate.getText().toString().trim());
        map.put("hospital_code",cHCode);
        getPDF(AppStrings.getGet_report_PDF,map);
*/
    }

    private void openDatePicker(int type) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                new DatePickerDialog.OnDateSetListener(){
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        Calendar selectedDateCalendar = Calendar.getInstance();
                        selectedDateCalendar.set(Calendar.YEAR, year);
                        selectedDateCalendar.set(Calendar.MONTH, month);
                        selectedDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        String formattedDate = dateFormat.format(selectedDateCalendar.getTime());

                      if (type == 0){
                          txtFromDate.setText(formattedDate);
                      }else {
                          txtToDate.setText(formattedDate);
                      }

                      if (!txtFromDate.getText().toString().trim().equals("from date") && !txtToDate.getText().toString().trim().equals("To date")){
                          btGetPDF.setEnabled(true);
                      }
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }


    public void getPDF(String url, Map<String,String> map){
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                String pdfUrl = "https://example.com/sample.pdf";

                DownloadPDFTask downloadTask = new DownloadPDFTask();
                downloadTask.execute(response);
                try {
                    JSONObject jsonObject = new JSONObject(response);

                    if (jsonObject.get("status").toString().equalsIgnoreCase("success")){

                    }else {

                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("TAG", "onResponse: "+error.getMessage());
            }
        }){
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return map;
            }
        };

        Volley.newRequestQueue(getContext()).add(request);
    }

    public void getData(String url, Map<String,String> map){
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response){
                try {
                    JSONObject jsonObject = new JSONObject(response);

                    Log.d("TAG", "onResponse: "+response);
                    if (jsonObject.get("status").toString().equalsIgnoreCase("success")){

                        shimmerFrameLayout.stopShimmer();
                        shimmerFrameLayout.setVisibility(View.GONE);

                        ReportsModel model = new ReportsModel();

                        int size = jsonObject.getJSONArray("data").length();


                        String cDate = "";
                        boolean firstData = false;
                        boolean sameData = false;

                        int redCount = 0;
                        int blueCount = 0;
                        int yellowCount = 0;
                        int whiteCount = 0;

                        int redCountCbwtf = 0;
                        int blueCountCbwtf = 0;
                        int yellowCountCbwtf = 0;
                        int whiteCountCbwtf = 0;

                        double hWeight = 0;
                        double cWeight = 0;

                        int packets = 0;

                        for (int i=0;i<size;i++){
                            JSONObject jsonObject1 = jsonObject.getJSONArray("data").getJSONObject(i);

                           // extractDate()
                            if (cHCode.isEmpty()){
                                cHCode = jsonObject1.get("hospital_code").toString().trim();
                                cDate = jsonObject1.get("handover_date").toString().trim();
                                firstData = true;
                                packets++;
                            }else if (cHCode.equals(jsonObject1.get("hospital_code").toString().trim()) && cDate.equals(jsonObject1.get("handover_date").toString().trim())){
                                sameData = true;
                                firstData = false;
                                packets++;
                            }else {
                                firstData = false;
                                sameData = false;
                                cHCode = jsonObject1.get("hospital_code").toString().trim();
                                cDate = jsonObject1.get("handover_date").toString().trim();
                                packets = 1;
                            }

                            if (firstData){
                                Log.d("TAG", "onResponse: "+22222);
                                model.setName(jsonObject1.get("name").toString());
                                model.setAddress(jsonObject1.get("address").toString());
                                model.setCbwtfId(jsonObject1.get("cbwtf_id").toString());
                                model.setColorTypeHcf(jsonObject1.get("color_type_hcf").toString());
                                model.setDisposeDate(jsonObject1.get("dispose_date").toString());
                                //model.setCbwtfWeight(jsonObject1.get("cbwtf_weight").toString());
                                model.setDisposeOperatorName(jsonObject1.get("dispose_operator_name").toString());
                                model.setColorTypeCbwtf(jsonObject1.get("color_type_cbwtf").toString());
                                model.setDistrict(jsonObject1.get("district").toString());
                                model.setHandoverDate(jsonObject1.get("handover_date").toString());
                                model.setHcfLatLong(jsonObject1.get("hcf_lat_long").toString());
                                model.setHcfType(jsonObject1.get("hcf_type").toString());
                                //model.setHcfWeight(jsonObject1.get("hcf_weight").toString());
                                model.setHospitalCode(jsonObject1.get("hospital_code").toString());
                                model.setHospitalType(jsonObject1.get("hospital_type").toString());
                                if (jsonObject1.get("lat_long_cbwtf").toString().trim().length() > 0){
                                    model.setLatLongCbwtf(jsonObject1.get("lat_long_cbwtf").toString());
                                }
                                model.setOperatorId(jsonObject1.get("operator_id").toString());
                                model.setOperatorIdCbwtf(jsonObject1.get("operator_id_cbwtf").toString());
                                model.setOperatorName(jsonObject1.get("operator_name").toString());
                                model.setQrDataId(jsonObject1.get("qr_data_id").toString());
                                model.setQrId(jsonObject1.get("qr_id").toString());
                                model.setRoute(jsonObject1.get("route").toString());
                                model.setType(jsonObject1.get("type").toString());
                                model.setType1(jsonObject1.get("type1").toString());

                                model.setTotalPackets(String.valueOf(packets));

                                if (jsonObject1.get("color_type_hcf").toString().toLowerCase().contains("red")){
                                    redCount++;
                                    model.setRed(String.valueOf(redCount));
                                    model.setBlue(String.valueOf(blueCount));
                                    model.setYellow(String.valueOf(yellowCount));
                                    model.setWhite(String.valueOf(whiteCount));
                                    hWeight = hWeight+Double.parseDouble(jsonObject1.get("hcf_weight").toString().trim());
                                    cWeight = cWeight+Double.parseDouble(jsonObject1.get("cbwtf_weight").toString().trim());
                                    model.setHcfWeight(String.valueOf(hWeight));
                                    model.setCbwtfWeight(String.valueOf(cWeight));

                                    if (jsonObject1.get("color_type_cbwtf").toString().toLowerCase().contains("red")){
                                        redCountCbwtf++;
                                        model.setRedCbwtf(String.valueOf(redCountCbwtf));
                                        model.setBlueCbwtf(String.valueOf(blueCountCbwtf));
                                        model.setYellowCbwtf(String.valueOf(yellowCountCbwtf));
                                        model.setWhiteCbwtf(String.valueOf(whiteCountCbwtf));
                                    }
                                }
                                if (jsonObject1.get("color_type_hcf").toString().toLowerCase().contains("blue")){
                                    blueCount++;
                                    model.setRed(String.valueOf(redCount));
                                    model.setBlue(String.valueOf(blueCount));
                                    model.setYellow(String.valueOf(yellowCount));
                                    model.setWhite(String.valueOf(whiteCount));
                                    hWeight = hWeight+Double.parseDouble(jsonObject1.get("hcf_weight").toString().trim());
                                    cWeight = cWeight+Double.parseDouble(jsonObject1.get("cbwtf_weight").toString().trim());
                                    model.setHcfWeight(String.valueOf(hWeight));
                                    model.setCbwtfWeight(String.valueOf(cWeight));

                                    if (jsonObject1.get("color_type_cbwtf").toString().toLowerCase().contains("blue")){
                                        blueCountCbwtf++;
                                        model.setRedCbwtf(String.valueOf(redCountCbwtf));
                                        model.setBlueCbwtf(String.valueOf(blueCountCbwtf));
                                        model.setYellowCbwtf(String.valueOf(yellowCountCbwtf));
                                        model.setWhiteCbwtf(String.valueOf(whiteCountCbwtf));
                                    }
                                }
                                if (jsonObject1.get("color_type_hcf").toString().toLowerCase().contains("yellow")){
                                    yellowCount++;
                                    model.setRed(String.valueOf(redCount));
                                    model.setBlue(String.valueOf(blueCount));
                                    model.setYellow(String.valueOf(yellowCount));
                                    model.setWhite(String.valueOf(whiteCount));
                                    hWeight = hWeight+Double.parseDouble(jsonObject1.get("hcf_weight").toString().trim());
                                    cWeight = cWeight+Double.parseDouble(jsonObject1.get("cbwtf_weight").toString().trim());
                                    model.setHcfWeight(String.valueOf(hWeight));
                                    model.setCbwtfWeight(String.valueOf(cWeight));

                                    if (jsonObject1.get("color_type_cbwtf").toString().toLowerCase().contains("yellow")){
                                        yellowCountCbwtf++;
                                        model.setRedCbwtf(String.valueOf(redCountCbwtf));
                                        model.setBlueCbwtf(String.valueOf(blueCountCbwtf));
                                        model.setYellowCbwtf(String.valueOf(yellowCountCbwtf));
                                        model.setWhiteCbwtf(String.valueOf(whiteCountCbwtf));
                                    }
                                }
                                if (jsonObject1.get("color_type_hcf").toString().toLowerCase().contains("white")){
                                    whiteCount++;
                                    model.setRed(String.valueOf(redCount));
                                    model.setBlue(String.valueOf(blueCount));
                                    model.setYellow(String.valueOf(yellowCount));
                                    model.setWhite(String.valueOf(whiteCount));
                                    hWeight = hWeight+Double.parseDouble(jsonObject1.get("hcf_weight").toString().trim());
                                    cWeight = cWeight+Double.parseDouble(jsonObject1.get("cbwtf_weight").toString().trim());
                                    model.setHcfWeight(String.valueOf(hWeight));
                                    model.setCbwtfWeight(String.valueOf(cWeight));

                                    if (jsonObject1.get("color_type_cbwtf").toString().toLowerCase().contains("white")){
                                        whiteCountCbwtf++;
                                        model.setRedCbwtf(String.valueOf(redCountCbwtf));
                                        model.setBlueCbwtf(String.valueOf(blueCountCbwtf));
                                        model.setYellowCbwtf(String.valueOf(yellowCountCbwtf));
                                        model.setWhiteCbwtf(String.valueOf(whiteCountCbwtf));
                                    }
                                }

                            }else if (sameData){

                                model.setName(jsonObject1.get("name").toString());
                                model.setAddress(jsonObject1.get("address").toString());
                                model.setCbwtfId(jsonObject1.get("cbwtf_id").toString());
                                model.setColorTypeHcf(jsonObject1.get("color_type_hcf").toString());
                                model.setDisposeDate(jsonObject1.get("dispose_date").toString());
                                //model.setCbwtfWeight(jsonObject1.get("cbwtf_weight").toString());
                                model.setDisposeOperatorName(jsonObject1.get("dispose_operator_name").toString());
                                model.setColorTypeCbwtf(jsonObject1.get("color_type_cbwtf").toString());
                                model.setDistrict(jsonObject1.get("district").toString());
                                model.setHandoverDate(jsonObject1.get("handover_date").toString());
                                model.setHcfLatLong(jsonObject1.get("hcf_lat_long").toString());
                                model.setHcfType(jsonObject1.get("hcf_type").toString());
                                //model.setHcfWeight(jsonObject1.get("hcf_weight").toString());
                                model.setHospitalCode(jsonObject1.get("hospital_code").toString());
                                model.setHospitalType(jsonObject1.get("hospital_type").toString());
                                if (jsonObject1.get("lat_long_cbwtf").toString().trim().length() > 0){
                                    model.setLatLongCbwtf(jsonObject1.get("lat_long_cbwtf").toString());
                                }
                                model.setOperatorId(jsonObject1.get("operator_id").toString());
                                model.setOperatorIdCbwtf(jsonObject1.get("operator_id_cbwtf").toString());
                                model.setOperatorName(jsonObject1.get("operator_name").toString());
                                model.setQrDataId(jsonObject1.get("qr_data_id").toString());
                                model.setQrId(jsonObject1.get("qr_id").toString());
                                model.setRoute(jsonObject1.get("route").toString());
                                model.setType(jsonObject1.get("type").toString());
                                model.setType1(jsonObject1.get("type1").toString());

                                model.setTotalPackets(String.valueOf(packets));

                                if (jsonObject1.get("color_type_hcf").toString().toLowerCase().contains("red")){
                                    redCount++;
                                    model.setRed(String.valueOf(redCount));
                                    model.setBlue(String.valueOf(blueCount));
                                    model.setYellow(String.valueOf(yellowCount));
                                    model.setWhite(String.valueOf(whiteCount));
                                    hWeight = hWeight+Double.parseDouble(jsonObject1.get("hcf_weight").toString().trim());
                                    cWeight = cWeight+Double.parseDouble(jsonObject1.get("cbwtf_weight").toString().trim());
                                    model.setHcfWeight(String.valueOf(hWeight));
                                    model.setCbwtfWeight(String.valueOf(cWeight));

                                    if (jsonObject1.get("color_type_cbwtf").toString().toLowerCase().contains("red")){
                                        redCountCbwtf++;
                                        model.setRedCbwtf(String.valueOf(redCountCbwtf));
                                        model.setBlueCbwtf(String.valueOf(blueCountCbwtf));
                                        model.setYellowCbwtf(String.valueOf(yellowCountCbwtf));
                                        model.setWhiteCbwtf(String.valueOf(whiteCountCbwtf));
                                    }
                                }
                                if (jsonObject1.get("color_type_hcf").toString().toLowerCase().contains("blue")){
                                    blueCount++;
                                    model.setRed(String.valueOf(redCount));
                                    model.setBlue(String.valueOf(blueCount));
                                    model.setYellow(String.valueOf(yellowCount));
                                    model.setWhite(String.valueOf(whiteCount));
                                    hWeight = hWeight+Double.parseDouble(jsonObject1.get("hcf_weight").toString().trim());
                                    cWeight = cWeight+Double.parseDouble(jsonObject1.get("cbwtf_weight").toString().trim());
                                    model.setHcfWeight(String.valueOf(hWeight));
                                    model.setCbwtfWeight(String.valueOf(cWeight));

                                    if (jsonObject1.get("color_type_cbwtf").toString().toLowerCase().contains("blue")){
                                        blueCountCbwtf++;
                                        model.setRedCbwtf(String.valueOf(redCountCbwtf));
                                        model.setBlueCbwtf(String.valueOf(blueCountCbwtf));
                                        model.setYellowCbwtf(String.valueOf(yellowCountCbwtf));
                                        model.setWhiteCbwtf(String.valueOf(whiteCountCbwtf));
                                    }
                                }
                                if (jsonObject1.get("color_type_hcf").toString().toLowerCase().contains("yellow")){
                                    yellowCount++;
                                    model.setRed(String.valueOf(redCount));
                                    model.setBlue(String.valueOf(blueCount));
                                    model.setYellow(String.valueOf(yellowCount));
                                    model.setWhite(String.valueOf(whiteCount));
                                    hWeight = hWeight+Double.parseDouble(jsonObject1.get("hcf_weight").toString().trim());
                                    cWeight = cWeight+Double.parseDouble(jsonObject1.get("cbwtf_weight").toString().trim());
                                    model.setHcfWeight(String.valueOf(hWeight));
                                    model.setCbwtfWeight(String.valueOf(cWeight));

                                    if (jsonObject1.get("color_type_cbwtf").toString().toLowerCase().contains("yellow")){
                                        yellowCountCbwtf++;
                                        model.setRedCbwtf(String.valueOf(redCountCbwtf));
                                        model.setBlueCbwtf(String.valueOf(blueCountCbwtf));
                                        model.setYellowCbwtf(String.valueOf(yellowCountCbwtf));
                                        model.setWhiteCbwtf(String.valueOf(whiteCountCbwtf));
                                    }
                                }
                                if (jsonObject1.get("color_type_hcf").toString().toLowerCase().contains("white")){
                                    whiteCount++;
                                    model.setRed(String.valueOf(redCount));
                                    model.setBlue(String.valueOf(blueCount));
                                    model.setYellow(String.valueOf(yellowCount));
                                    model.setWhite(String.valueOf(whiteCount));
                                    hWeight = hWeight+Double.parseDouble(jsonObject1.get("hcf_weight").toString().trim());
                                    cWeight = cWeight+Double.parseDouble(jsonObject1.get("cbwtf_weight").toString().trim());
                                    model.setHcfWeight(String.valueOf(hWeight));
                                    model.setCbwtfWeight(String.valueOf(cWeight));

                                    if (jsonObject1.get("color_type_cbwtf").toString().toLowerCase().contains("white")){
                                        whiteCountCbwtf++;
                                        model.setRedCbwtf(String.valueOf(redCountCbwtf));
                                        model.setBlueCbwtf(String.valueOf(blueCountCbwtf));
                                        model.setYellowCbwtf(String.valueOf(yellowCountCbwtf));
                                        model.setWhiteCbwtf(String.valueOf(whiteCountCbwtf));
                                    }
                                }

                            } else if (!sameData) {

                                redCount = 0;
                                blueCount = 0;
                                yellowCount = 0;
                                whiteCount = 0;

                                redCountCbwtf = 0;
                                blueCountCbwtf = 0;
                                yellowCountCbwtf = 0;
                                whiteCountCbwtf = 0;

                                hWeight = 0;
                                cWeight = 0;

                                reportsModels.add(model);
                                reportsAdapter.notifyItemInserted(i);

                                model = new ReportsModel();

                                model.setName(jsonObject1.get("name").toString());
                                model.setAddress(jsonObject1.get("address").toString());
                                model.setCbwtfId(jsonObject1.get("cbwtf_id").toString());
                                model.setColorTypeHcf(jsonObject1.get("color_type_hcf").toString());
                                model.setDisposeDate(jsonObject1.get("dispose_date").toString());
                                //model.setCbwtfWeight(jsonObject1.get("cbwtf_weight").toString());
                                model.setDisposeOperatorName(jsonObject1.get("dispose_operator_name").toString());
                                model.setColorTypeCbwtf(jsonObject1.get("color_type_cbwtf").toString());
                                model.setDistrict(jsonObject1.get("district").toString());
                                model.setHandoverDate(jsonObject1.get("handover_date").toString());
                                model.setHcfLatLong(jsonObject1.get("hcf_lat_long").toString());
                                model.setHcfType(jsonObject1.get("hcf_type").toString());
                                //model.setHcfWeight(jsonObject1.get("hcf_weight").toString());
                                model.setHospitalCode(jsonObject1.get("hospital_code").toString());
                                model.setHospitalType(jsonObject1.get("hospital_type").toString());
                                if (jsonObject1.get("lat_long_cbwtf").toString().trim().length() > 0){
                                    model.setLatLongCbwtf(jsonObject1.get("lat_long_cbwtf").toString());
                                }
                                model.setOperatorId(jsonObject1.get("operator_id").toString());
                                model.setOperatorIdCbwtf(jsonObject1.get("operator_id_cbwtf").toString());
                                model.setOperatorName(jsonObject1.get("operator_name").toString());
                                model.setQrDataId(jsonObject1.get("qr_data_id").toString());
                                model.setQrId(jsonObject1.get("qr_id").toString());
                                model.setRoute(jsonObject1.get("route").toString());
                                model.setType(jsonObject1.get("type").toString());
                                model.setType1(jsonObject1.get("type1").toString());

                                model.setTotalPackets(String.valueOf(packets));

                                if (jsonObject1.get("color_type_hcf").toString().toLowerCase().contains("red")){
                                    redCount++;
                                    model.setRed(String.valueOf(redCount));
                                    model.setBlue(String.valueOf(blueCount));
                                    model.setYellow(String.valueOf(yellowCount));
                                    model.setWhite(String.valueOf(whiteCount));
                                    hWeight = hWeight+Double.parseDouble(jsonObject1.get("hcf_weight").toString().trim());
                                    cWeight = cWeight+Double.parseDouble(jsonObject1.get("cbwtf_weight").toString().trim());
                                    model.setHcfWeight(String.valueOf(hWeight));
                                    model.setCbwtfWeight(String.valueOf(cWeight));

                                    if (jsonObject1.get("color_type_cbwtf").toString().toLowerCase().contains("red")){
                                        redCountCbwtf++;
                                        model.setRedCbwtf(String.valueOf(redCountCbwtf));
                                        model.setBlueCbwtf(String.valueOf(blueCountCbwtf));
                                        model.setYellowCbwtf(String.valueOf(yellowCountCbwtf));
                                        model.setWhiteCbwtf(String.valueOf(whiteCountCbwtf));
                                    }
                                }
                                if (jsonObject1.get("color_type_hcf").toString().toLowerCase().contains("blue")){
                                    blueCount++;
                                    model.setRed(String.valueOf(redCount));
                                    model.setBlue(String.valueOf(blueCount));
                                    model.setYellow(String.valueOf(yellowCount));
                                    model.setWhite(String.valueOf(whiteCount));
                                    hWeight = hWeight+Double.parseDouble(jsonObject1.get("hcf_weight").toString().trim());
                                    cWeight = cWeight+Double.parseDouble(jsonObject1.get("cbwtf_weight").toString().trim());
                                    model.setHcfWeight(String.valueOf(hWeight));
                                    model.setCbwtfWeight(String.valueOf(cWeight));

                                    if (jsonObject1.get("color_type_cbwtf").toString().toLowerCase().contains("blue")){
                                        blueCountCbwtf++;
                                        model.setRedCbwtf(String.valueOf(redCountCbwtf));
                                        model.setBlueCbwtf(String.valueOf(blueCountCbwtf));
                                        model.setYellowCbwtf(String.valueOf(yellowCountCbwtf));
                                        model.setWhiteCbwtf(String.valueOf(whiteCountCbwtf));
                                    }
                                }
                                if (jsonObject1.get("color_type_hcf").toString().toLowerCase().contains("yellow")){
                                    yellowCount++;
                                    model.setRed(String.valueOf(redCount));
                                    model.setBlue(String.valueOf(blueCount));
                                    model.setYellow(String.valueOf(yellowCount));
                                    model.setWhite(String.valueOf(whiteCount));
                                    hWeight = hWeight+Double.parseDouble(jsonObject1.get("hcf_weight").toString().trim());
                                    cWeight = cWeight+Double.parseDouble(jsonObject1.get("cbwtf_weight").toString().trim());
                                    model.setHcfWeight(String.valueOf(hWeight));
                                    model.setCbwtfWeight(String.valueOf(cWeight));

                                    if (jsonObject1.get("color_type_cbwtf").toString().toLowerCase().contains("yellow")){
                                        yellowCountCbwtf++;
                                        model.setRedCbwtf(String.valueOf(redCountCbwtf));
                                        model.setBlueCbwtf(String.valueOf(blueCountCbwtf));
                                        model.setYellowCbwtf(String.valueOf(yellowCountCbwtf));
                                        model.setWhiteCbwtf(String.valueOf(whiteCountCbwtf));
                                    }
                                }
                                if (jsonObject1.get("color_type_hcf").toString().toLowerCase().contains("white")){
                                    whiteCount++;
                                    model.setRed(String.valueOf(redCount));
                                    model.setBlue(String.valueOf(blueCount));
                                    model.setYellow(String.valueOf(yellowCount));
                                    model.setWhite(String.valueOf(whiteCount));
                                    hWeight = hWeight+Double.parseDouble(jsonObject1.get("hcf_weight").toString().trim());
                                    cWeight = cWeight+Double.parseDouble(jsonObject1.get("cbwtf_weight").toString().trim());
                                    model.setHcfWeight(String.valueOf(hWeight));
                                    model.setCbwtfWeight(String.valueOf(cWeight));

                                    if (jsonObject1.get("color_type_cbwtf").toString().toLowerCase().contains("white")){
                                        whiteCountCbwtf++;
                                        model.setRedCbwtf(String.valueOf(redCountCbwtf));
                                        model.setBlueCbwtf(String.valueOf(blueCountCbwtf));
                                        model.setYellowCbwtf(String.valueOf(yellowCountCbwtf));
                                        model.setWhiteCbwtf(String.valueOf(whiteCountCbwtf));
                                    }
                                }
                            }

                        }


                        reportsModels.add(model);
                        Log.d("TAG", "onResponse: "+reportsModels.size());
                        Iterator<ReportsModel> iterator = reportsModels.iterator();
                        while (iterator.hasNext()) {
                            ReportsModel modell = iterator.next();
                            if (!modell.getCreatedOn().equalsIgnoreCase(getCurrentDateTime().toString())) {
                                iterator.remove();
                                Log.d("TAG", "12345:3 " + modell.getCreatedOn());
                            } else {
                                Log.d("TAG", "12345:2 " + reportsModels.size());
                            }
                        }


                        reportsAdapter.notifyDataSetChanged();

                        if (size < 1){
                            empty_view.setVisibility(View.VISIBLE);
                        }

                    }else {
                        shimmerFrameLayout.stopShimmer();
                        shimmerFrameLayout.setVisibility(View.GONE);
                        empty_view.setVisibility(View.VISIBLE);
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

        Volley.newRequestQueue(getContext()).add(request);
    }

    public void searchQuery(String text){
        List<ReportsModel> list = new ArrayList<>();
        for (ReportsModel reportsModel:reportsModels){
            if (reportsModel.getName().trim().toLowerCase().contains(text.trim().toLowerCase())){
                list.add(reportsModel);
            }
        }
        reportsAdapter.onUpdate(list);
    }

    private class DownloadPDFTask extends AsyncTask<String, Void, File> {

        @Override
        protected File doInBackground(String... urls) {
            String pdfUrl = urls[0];
            File pdfFile = null;

            try {
                URL url = new URL(pdfUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = connection.getInputStream();
                    File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    long currentTimeMillis = System.currentTimeMillis();
                    String pdfFileName = "downloaded_file_" + currentTimeMillis + ".pdf";
                    pdfFile = new File(storageDir, pdfFileName);

                    FileOutputStream outputStream = new FileOutputStream(pdfFile);
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, len);
                    }
                    outputStream.close();
                    inputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return pdfFile;
        }

        @Override
        protected void onPostExecute(File pdfFile) {
            if (pdfFile != null) {
                openPDFWithReader(pdfFile);
            }
        }
    }

    private void openPDFWithReader(File pdfFile) {
        Uri pdfUri = FileProvider.getUriForFile(requireContext(), "com.ostron.gov.fileprovider", pdfFile);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(pdfUri, "application/pdf");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            // Handle exception if no PDF reader app is installed
        }
    }

    private static String extractDate(String timestamp) {
        try {
            // Parse the input timestamp
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date parsedDate = inputFormat.parse(timestamp);

            // Format the date to get only the date part
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
            return outputFormat.format(parsedDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    @SuppressLint("SimpleDateFormat")
    private String getCurrentDateTime() {
        // Create a SimpleDateFormat object with the desired format
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // Get the current date and time and format it
        Date currentDate = new Date();
        return dateFormat.format(currentDate);
    }

    public void sendMail()
    {

        StringRequest generateOtpRequest = new StringRequest(Request.Method.POST, AppStrings.send_mail, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {


                try {



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
                otp.put("hcf_code",cHCode);
                return otp;
            }
        };

        Volley.newRequestQueue(getContext()).add(generateOtpRequest);
    }

}