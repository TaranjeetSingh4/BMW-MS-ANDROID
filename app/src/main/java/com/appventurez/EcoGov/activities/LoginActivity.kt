package com.appventurez.EcoGov.activities

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.appventurez.EcoGov.cbwtf.activities.CbwtfLoginActivity
import com.appventurez.EcoGov.hcf.Activities.HcfLoginActivity
import com.appventurez.EcoGov.compose.LoginTypeScreen
import androidx.core.net.toUri

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            LoginTypeScreen(
                onCbwtfClick = {
                    val intent = Intent(this, CbwtfLoginActivity::class.java)
                    intent.putExtra("loginAs", 0)
                    startActivity(intent)
                },
                onHcfClick = {
                    val intent = Intent(this, HcfLoginActivity::class.java)
                    intent.putExtra("loginAs", 1)
                    startActivity(intent)
                },
                onGuidelineClick = {
                    startActivity(Intent(this, PdfViewActivity::class.java))
                },
                onYoutubeClick = {
                    openYoutube()
                }
            )
        }
    }

    private fun openYoutube() {
        val appIntent = Intent(Intent.ACTION_VIEW, "vnd.youtube:OcrMGvRdjaY".toUri())
        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=OcrMGvRdjaY"))
        try {
            startActivity(appIntent)
        } catch (ex: ActivityNotFoundException) {
            startActivity(webIntent)
        }
    }
}
