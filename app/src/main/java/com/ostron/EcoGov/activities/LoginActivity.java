package com.ostron.EcoGov.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.ostron.EcoGov.R;
import com.ostron.EcoGov.cbwtf.activities.CbwtfLoginActivity;

public class LoginActivity extends AppCompatActivity {

    ImageView headerImg,footerImg;
    TextView guidelines,youtube;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        headerImg = findViewById(R.id.login_header_img);
       // footerImg = findViewById(R.id.login_bottom_img);
        guidelines = findViewById(R.id.guideline_link_tv);
        youtube = findViewById(R.id.youtube_link_tv);

        Glide.with(this).load(R.drawable.uplogogpb).into(headerImg);
     //   Glide.with(this).load(R.drawable.img_011).centerCrop().into(footerImg);

        guidelines.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this,PdfViewActivity.class));
            }
        });

        youtube.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:OcrMGvRdjaY"));
                Intent webIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://www.youtube.com/watch?v=OcrMGvRdjaY"));
                try {
                    startActivity(appIntent);
                } catch (ActivityNotFoundException ex) {
                    startActivity(webIntent);
                }
                //startActivity(new Intent(CbwtfLoginActivity.this, WebViewActivity.class));
            }
        });

    }

    public void onCbwtf(View view){
        Intent intent = new Intent(LoginActivity.this, CbwtfLoginActivity.class);
        intent.putExtra("loginAs",0);
        startActivity(intent);
        //finish();
    }
    public void onHcf(View view){
        Intent intent = new Intent(LoginActivity.this, CbwtfLoginActivity.class);
        intent.putExtra("loginAs",1);
        startActivity(intent);
        //finish();
    }
}