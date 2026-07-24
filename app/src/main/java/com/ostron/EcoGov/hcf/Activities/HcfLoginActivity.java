package com.ostron.EcoGov.hcf.Activities;

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
import com.ostron.EcoGov.activities.PdfViewActivity;

public class HcfLoginActivity extends AppCompatActivity {

    ImageView headerImg,footerImg;
    TextView guideline_tv,youtube_tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hcf_login);

        headerImg = findViewById(R.id.hcf_login_header_img);
       footerImg = findViewById(R.id.hcf_login_bottom_img);
        guideline_tv = findViewById(R.id.hcf_guideline_link_tv);
        youtube_tv = findViewById(R.id.hcf_youtube_link_tv);

        Glide.with(this).load(R.drawable.eco).into(headerImg);
      //  Glide.with(this).load(R.drawable.img_011).into(footerImg);

        guideline_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HcfLoginActivity.this, PdfViewActivity.class));
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
}