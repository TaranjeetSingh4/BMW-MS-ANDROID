package com.appventurez.bmwms.cbwtf.activities

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Vibrator
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.mutableStateOf
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.appventurez.bmwms.activities.PdfViewActivity
import com.appventurez.bmwms.cbwtf.compose.CbwtfLoginScreen
import com.appventurez.bmwms.classes.AppStrings
import com.appventurez.bmwms.classes.MSP
import com.appventurez.bmwms.classes.VolleySingleton
import org.json.JSONObject
import java.util.HashMap

class CbwtfLoginActivity : AppCompatActivity() {

    private var vibrator: Vibrator? = null
    private var loginAs = 0
    private var isLoading = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        
        loginAs = intent.getIntExtra("loginAs", 0)

        setContent {
            CbwtfLoginScreen(
                isLoading = isLoading.value,
                onLoginClick = { mobile, password ->
                    onLoginClick(mobile, password)
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
        val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:j46IAKw6rpI"))
        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=j46IAKw6rpI"))
        try {
            startActivity(appIntent)
        } catch (ex: ActivityNotFoundException) {
            startActivity(webIntent)
        }
    }

    private fun onLoginClick(mobile: String, password: String) {
        if (mobile.trim().isEmpty()) {
            vibrator?.vibrate(100)
            Toast.makeText(this, "Empty number", Toast.LENGTH_SHORT).show()
        } else if (password.trim().isEmpty()) {
            vibrator?.vibrate(100)
            Toast.makeText(this, "Empty password", Toast.LENGTH_SHORT).show()
        } else {
            isLoading.value = true
            val map = HashMap<String, String>()
            map["mobile"] = mobile
            map["password"] = password
            if (loginAs == 0) {
                networkRequest(AppStrings.cbwtf_login, map)
            } else {
                networkRequest(AppStrings.hcf_login, map)
            }
        }
    }

    fun networkRequest(url: String, map: Map<String, String>) {
        Log.d("CbwtfLoginActivity", "Network Request URL: $url")
        Log.d("CbwtfLoginActivity", "Network Request Params: $map")

        val request = object : StringRequest(Request.Method.POST, url, Response.Listener { response ->
            Log.d("CbwtfLoginActivity", "Network Response: $response")
            try {
                val jsonObject = JSONObject(response)
                if (jsonObject.get("status").toString().equals("success", ignoreCase = true)) {
                    val data = jsonObject.getJSONArray("data").getJSONObject(0)
                    if (loginAs == 0) {
                        MSP.getInstance(this).apply {
                            setStringData(AppStrings.userName, data.optString("name"))
                            setStringData(AppStrings.userMobile, data.optString("mobile"))
                            setStringData(AppStrings.userPassword, data.optString("password"))
                            setStringData(AppStrings.userAddress, data.optString("address"))
                            setStringData(AppStrings.userID, data.optString("operator_id"))
                            setStringData(AppStrings.userCbwtfID, data.optString("cbwtf_id"))
                            setStringData(AppStrings.loginAs, "cbwtf")
                        }
                    } else {
                        MSP.getInstance(this).apply {
                            setStringData(AppStrings.userName, data.optString("name"))
                            setStringData(AppStrings.userMobile, data.optString("mobile"))
                            setStringData(AppStrings.userPassword, data.optString("password"))
                            setStringData(AppStrings.userAddress, data.optString("address"))
                            setStringData(AppStrings.userID, data.optString("hospital_code"))
                            setStringData(AppStrings.userCbwtfID, data.optString("cbwtf_id"))
                            setStringData(AppStrings.loginAs, "hcf")
                        }
                    }

                    isLoading.value = false
                    val intent = Intent(this, CbwtfDashboardActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    isLoading.value = false
                    vibrator?.vibrate(100)
                    Toast.makeText(this, "Wrong credential", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                isLoading.value = false
                e.printStackTrace()
                Toast.makeText(this, "Something went wrong try again", Toast.LENGTH_SHORT).show()
            }
        }, Response.ErrorListener {
            Log.e("CbwtfLoginActivity", "Network Error: ${it.message}")
            isLoading.value = false
            Toast.makeText(this, "Something went wrong try again", Toast.LENGTH_SHORT).show()
        }) {
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String> {
                return map
            }
        }

        VolleySingleton.getInstance(this).addToRequestQueue(request)
    }
}
