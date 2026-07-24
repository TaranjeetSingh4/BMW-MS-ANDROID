package com.ostron.EcoGov.cbwtf.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ostron.EcoGov.R;
import com.ostron.EcoGov.activities.LoginActivity;
import com.ostron.EcoGov.classes.AppStrings;
import com.ostron.EcoGov.classes.MSP;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CbwtfProfileFragment extends Fragment {

    View view;
    ImageView appBar_img,img_big,backButton,logout_button;
    TextView name,mobile,address;
    FloatingActionButton help,enquiry;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (view == null){
            view = inflater.inflate(R.layout.fragment_cbwtf_profile, container, false);

            appBar_img = view.findViewById(R.id.cbwtf_profile_appBar_logo);
            img_big = view.findViewById(R.id.cbwtf_profile_img);
            backButton = view.findViewById(R.id.cbwtf_profile_back_button);
            logout_button = view.findViewById(R.id.cbwtf_profile_logout);
            name = view.findViewById(R.id.cbwtf_profile_name);
            mobile = view.findViewById(R.id.cbwtf_profile_mobile);
            address = view.findViewById(R.id.cbwtf_profile_address);
            help = view.findViewById(R.id.cbwtf_profile_help);
            enquiry = view.findViewById(R.id.cbwtf_profile_enquiry);

            loadUserDetails();
            loadImages();
            loadClickListeners();

        }
        return view;
    }

    public void loadImages(){

        Glide.with(getActivity()).load(R.drawable.uplogogpb).into(appBar_img);
        Glide.with(getActivity()).load(R.drawable.uplogogpb).into(img_big);

    }

    public void loadClickListeners(){

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });

        logout_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MSP.getInstance(getContext()).removeAll();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                getActivity().finish();
            }
        });

        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
                View helpView = LayoutInflater.from(getContext()).inflate(R.layout.help_view,null,false);
                alertDialog.setView(helpView);
                alertDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                alertDialog.show();
            }
        });

        enquiry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
                View enquiryView = LayoutInflater.from(getContext()).inflate(R.layout.enquiry_view,null,false);
                EditText editText = enquiryView.findViewById(R.id.enquiry_et);
                TextView textView = enquiryView.findViewById(R.id.enquiry_submit);
                alertDialog.setView(enquiryView);
                alertDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                alertDialog.show();

                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (!editText.getText().toString().trim().isEmpty()){

                            if (MSP.getInstance(getContext()).getStringData(AppStrings.loginAs).equals("cbwtf")){
                                sendQueryData("cbwtf",MSP.getInstance(getContext()).getStringData(AppStrings.userID),editText.getText().toString());
                            }else{
                                sendQueryData("hcf",MSP.getInstance(getContext()).getStringData(AppStrings.userID),editText.getText().toString());
                            }

                        }

                        editText.setText("");
                        alertDialog.dismiss();


                    }
                });
            }
        });

    }

    public void loadUserDetails(){
        name.setText(MSP.getInstance(getActivity()).getStringData(AppStrings.userName));
        mobile.setText(MSP.getInstance(getActivity()).getStringData(AppStrings.userMobile));
        address.setText(MSP.getInstance(getActivity()).getStringData(AppStrings.userAddress));
    }

    private void sendQueryData(String sender,String senderId,String message){
        StringRequest request = new StringRequest(Request.Method.POST, AppStrings.send_query, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject object = new JSONObject(response);

                    if (object.get("status").toString().equalsIgnoreCase("success")){

                        Toast.makeText(getContext(), "Query sent", Toast.LENGTH_SHORT).show();

                    }
                }catch (Exception e){

                    Toast.makeText(getContext(), "Try again later "+e.getMessage(), Toast.LENGTH_SHORT).show();

                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(getContext(), "Try again later "+error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        }){
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> map = new HashMap<>();
                map.put("sender",sender);
                map.put("sender_id",senderId);
                map.put("message",message);
                return super.getParams();
            }
        };

        Volley.newRequestQueue(getContext()).add(request);
    }

}