package com.ostron.EcoGov.cbwtf.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
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
import com.google.android.material.textfield.TextInputEditText;
import com.ostron.EcoGov.R;
import com.ostron.EcoGov.activities.PdfViewActivity;
import com.ostron.EcoGov.classes.AppStrings;
import com.ostron.EcoGov.classes.MSP;
import com.ostron.EcoGov.classes.VolleySingleton;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CbwtfLoginActivity extends AppCompatActivity {

    ImageView headerImg,footerImg;
    TextView guideline_tv,youtube_tv;
    TextInputEditText mobile_et,password_et;

    Vibrator vibrator;
    int loginAs = 0;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cbwtf_login);

        vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);

        progressDialog = new ProgressDialog(this);
        headerImg = findViewById(R.id.cbwtf_login_header_img);
        footerImg = findViewById(R.id.cbwtf_login_bottom_img);
        guideline_tv = findViewById(R.id.cbwtf_guideline_link_tv);
        youtube_tv = findViewById(R.id.cbwtf_youtube_link_tv);
        mobile_et = findViewById(R.id.cbwtf_login_mobile_et);
        password_et = findViewById(R.id.cbwtf_login_password_et);

        Glide.with(this).load(R.drawable.uplogogpb).into(headerImg);
     //   Glide.with(this).load(R.drawable.img_011).into(footerImg);

        loginAs = getIntent().getIntExtra("loginAs",0);

        progressDialog.setCancelable(false);
        progressDialog.setMessage("Please wait...");
        progressDialog.create();

        guideline_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CbwtfLoginActivity.this, PdfViewActivity.class));
            }
        });

        youtube_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:j46IAKw6rpI"));
                Intent webIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://www.youtube.com/watch?v=j46IAKw6rpI"));
                try {
                    startActivity(appIntent);
                } catch (ActivityNotFoundException ex) {
                    startActivity(webIntent);
                }
                //startActivity(new Intent(CbwtfLoginActivity.this, WebViewActivity.class));
            }
        });

    }

    public void onLogin(View view){

        if (mobile_et.getText().toString().trim().isEmpty()){
            vibrator.vibrate(100);
            Snackbar.make(mobile_et,"Empty number",1000).show();
        }else if (password_et.getText().toString().trim().isEmpty()){
            vibrator.vibrate(100);
            Snackbar.make(password_et,"Empty password",1000).show();
        }else {

            progressDialog.show();

            Map<String,String> map = new HashMap<>();
            map.put("mobile",mobile_et.getText().toString());
            map.put("password",password_et.getText().toString());
            if (loginAs == 0){
                networkRequest(AppStrings.cbwtf_login,map);
            }else {
                networkRequest(AppStrings.hcf_login,map);
            }
        }
    }

    public void networkRequest(String url,Map<String,String> map){
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

                            MSP.getInstance(CbwtfLoginActivity.this).setStringData(AppStrings.userName,operatorName);
                            MSP.getInstance(CbwtfLoginActivity.this).setStringData(AppStrings.userMobile,operatorMobile);
                            MSP.getInstance(CbwtfLoginActivity.this).setStringData(AppStrings.userPassword,userPassword);
                            MSP.getInstance(CbwtfLoginActivity.this).setStringData(AppStrings.userAddress,operatorAddress);
                            MSP.getInstance(CbwtfLoginActivity.this).setStringData(AppStrings.userID,operatorID);
                            MSP.getInstance(CbwtfLoginActivity.this).setStringData(AppStrings.userCbwtfID,operatorCbwtfID);
                            MSP.getInstance(CbwtfLoginActivity.this).setStringData(AppStrings.loginAs,"cbwtf");
                        }else {
                            String operatorID = jsonObject.getJSONArray("data").getJSONObject(0).get("hospital_code").toString();
                            String operatorName = jsonObject.getJSONArray("data").getJSONObject(0).get("name").toString();
                            String operatorAddress = jsonObject.getJSONArray("data").getJSONObject(0).get("address").toString();
                            String operatorMobile = jsonObject.getJSONArray("data").getJSONObject(0).get("mobile").toString();
                            String operatorCbwtfID = jsonObject.getJSONArray("data").getJSONObject(0).get("cbwtf_id").toString();
                            String userPassword = jsonObject.getJSONArray("data").getJSONObject(0).get("password").toString();

                            MSP.getInstance(CbwtfLoginActivity.this).setStringData(AppStrings.userName,operatorName);
                            MSP.getInstance(CbwtfLoginActivity.this).setStringData(AppStrings.userMobile,operatorMobile);
                            MSP.getInstance(CbwtfLoginActivity.this).setStringData(AppStrings.userPassword,userPassword);
                            MSP.getInstance(CbwtfLoginActivity.this).setStringData(AppStrings.userAddress,operatorAddress);
                            MSP.getInstance(CbwtfLoginActivity.this).setStringData(AppStrings.userID,operatorID);
                            MSP.getInstance(CbwtfLoginActivity.this).setStringData(AppStrings.userCbwtfID,operatorCbwtfID);
                            MSP.getInstance(CbwtfLoginActivity.this).setStringData(AppStrings.loginAs,"hcf");
                        }

                        progressDialog.dismiss();

                        Intent intent = new Intent(CbwtfLoginActivity.this, CbwtfDashboardActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();

                    }else {
                        progressDialog.dismiss();
                        vibrator.vibrate(100);
                        Snackbar.make(password_et,"Wrong credential",1000).show();
                    }

                }catch (Exception e){
                    progressDialog.dismiss();
                    e.printStackTrace();
                    Toast.makeText(CbwtfLoginActivity.this, "Something went wrong try again", Toast.LENGTH_SHORT).show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Toast.makeText(CbwtfLoginActivity.this, "Something went wrong try again", Toast.LENGTH_SHORT).show();
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
}