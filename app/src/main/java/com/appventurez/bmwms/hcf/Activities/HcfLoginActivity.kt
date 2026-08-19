package com.appventurez.bmwms.hcf.Activities

import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.os.Vibrator
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.appventurez.bmwms.activities.PdfViewActivity
import com.appventurez.bmwms.cbwtf.activities.CbwtfDashboardActivity
import com.appventurez.bmwms.classes.AppStrings
import com.appventurez.bmwms.classes.MSP
import com.appventurez.bmwms.classes.VolleySingleton
import com.appventurez.bmwms.hcf.compose.HcfLoginScreen
import org.json.JSONObject
import java.util.HashMap
import androidx.core.net.toUri

class HcfLoginActivity : AppCompatActivity() {

    private var vibrator: Vibrator? = null
    private var loginAs = 1
    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        progressDialog = ProgressDialog(this)
        progressDialog?.setMessage("Logging in...")
        loginAs = intent.getIntExtra("loginAs", 1)

        setContent {
            HcfLoginScreen(
                onLoginClick = { email, password ->
                    onLoginClick(email, password)
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
        val appIntent = Intent(Intent.ACTION_VIEW, "vnd.youtube:j46IAKw6rpI".toUri())
        val webIntent = Intent(Intent.ACTION_VIEW,
            "http://www.youtube.com/watch?v=j46IAKw6rpI".toUri())
        try {
            startActivity(appIntent)
        } catch (ex: ActivityNotFoundException) {
            startActivity(webIntent)
        }
    }

    private fun onLoginClick(email: String, password: String) {
        if (email.trim().isEmpty()) {
            vibrator?.vibrate(100)
            Toast.makeText(this, "Empty email", Toast.LENGTH_SHORT).show()
        } else if (password.trim().isEmpty()) {
            vibrator?.vibrate(100)
            Toast.makeText(this, "Empty password", Toast.LENGTH_SHORT).show()
        } else {
            progressDialog?.show()
            val map = HashMap<String, String>()
            map["email"] = email
            map["password"] = password
            if (loginAs == 0) {
                networkRequest(AppStrings.cbwtf_login, map)
            } else {
                networkRequest(AppStrings.hcf_login, map)
            }
        }
    }

    fun networkRequest(url: String, map: Map<String, String>) {
        Log.d("HcfLoginActivity", "Network Request URL: $url")
        Log.d("HcfLoginActivity", "Network Request Params: $map")

        val request = object : StringRequest(Request.Method.POST, url, Response.Listener { response ->
            Log.d("HcfLoginActivity", "Network Response: $response")
            try {
                val jsonObject = JSONObject(response)
                if (jsonObject.get("status").toString().equals("success", ignoreCase = true)) {
                    val data = jsonObject.getJSONArray("data").getJSONObject(0)
                    if (loginAs == 0) {
                        MSP.getInstance(this).apply {
                            setStringData(AppStrings.userName, data.optString("name"))
                            setStringData(AppStrings.userMobile, data.optString("email"))
                            setStringData(AppStrings.userPassword, data.optString("password"))
                            setStringData(AppStrings.userAddress, data.optString("address"))
                            setStringData(AppStrings.userID, data.optString("operator_id"))
                            setStringData(AppStrings.userCbwtfID, data.optString("cbwtf_id"))
                            setStringData(AppStrings.loginAs, "cbwtf")
                        }
                    } else {
                        MSP.getInstance(this).apply {
                            setStringData(AppStrings.userName, data.optString("name"))
                            setStringData(AppStrings.userMobile, data.optString("email"))
                            setStringData(AppStrings.userPassword, data.optString("password"))
                            setStringData(AppStrings.userAddress, data.optString("address"))
                            setStringData(AppStrings.userID, data.optString("hospital_code"))
                            setStringData(AppStrings.userCbwtfID, data.optString("cbwtf_id"))
                            setStringData(AppStrings.loginAs, "hcf")
                        }
                    }

                    progressDialog?.dismiss()
                    val intent = Intent(this, CbwtfDashboardActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    progressDialog?.dismiss()
                    vibrator?.vibrate(100)
                    Toast.makeText(this, "Wrong credential", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                progressDialog?.dismiss()
                e.printStackTrace()
                Toast.makeText(this, "Something went wrong try again", Toast.LENGTH_SHORT).show()
            }
        }, Response.ErrorListener {
            Log.e("HcfLoginActivity", "Network Error: ${it.message}")
            progressDialog?.dismiss()
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
