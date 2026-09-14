package com.appventurez.bmwms.cbwtf.activities

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.appventurez.bmwms.activities.PdfViewActivity
import com.appventurez.bmwms.cbwtf.compose.CbwtfLoginScreen
import com.appventurez.bmwms.classes.AppStrings
import com.appventurez.bmwms.classes.MSP
import com.appventurez.bmwms.network.RetrofitClient
import com.appventurez.bmwms.repository.LoginRepository
import com.appventurez.bmwms.viewmodel.LoginViewModel
import java.util.HashMap

class CbwtfLoginActivity : AppCompatActivity() {
    private var TAG = "CbwtfLoginActivity"
    private var vibrator: Vibrator? = null
    private var loginAs = 0

    private val viewModel: LoginViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return LoginViewModel(LoginRepository(RetrofitClient.apiService)) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
        
        loginAs = intent.getIntExtra("loginAs", 0)

        setContent {
            val loginResponse = viewModel.loginResponse.value

            LaunchedEffect(loginResponse) {
                loginResponse?.let { response ->
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body?.status.equals("success", ignoreCase = true) && !body?.data.isNullOrEmpty()) {
                            val data = body!!.data!![0]
                            val msp = MSP.getInstance(this@CbwtfLoginActivity)
                            
                            if (loginAs == 0) {
                                msp.setStringData(AppStrings.userName, data.name)
                                msp.setStringData(AppStrings.userMobile, data.mobile)
                                msp.setStringData(AppStrings.userPassword, data.password)
                                msp.setStringData(AppStrings.userAddress, data.address)
                                msp.setStringData(AppStrings.userID, data.operatorId)
                                msp.setStringData(AppStrings.userCbwtfID, data.cbwtfId)
                                msp.setStringData(AppStrings.loginAs, "cbwtf")
                            } else {
                                msp.setStringData(AppStrings.userName, data.name)
                                msp.setStringData(AppStrings.userMobile, data.mobile)
                                msp.setStringData(AppStrings.userPassword, data.password)
                                msp.setStringData(AppStrings.userAddress, data.address)
                                msp.setStringData(AppStrings.userID, data.hospitalCode)
                                msp.setStringData(AppStrings.userCbwtfID, data.cbwtfId)
                                msp.setStringData(AppStrings.loginAs, "hcf")
                            }

                            val intent = Intent(this@CbwtfLoginActivity, CbwtfDashboardActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        } else {
                            vibrate(100)
                            Toast.makeText(this@CbwtfLoginActivity, "Wrong credential", Toast.LENGTH_SHORT).show()
                            viewModel.resetLoginResponse()
                        }
                    } else {
                        Toast.makeText(this@CbwtfLoginActivity, "Something went wrong try again", Toast.LENGTH_SHORT).show()
                        viewModel.resetLoginResponse()
                    }
                }
            }

            CbwtfLoginScreen(
                isLoading = viewModel.isLoading.value,
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
            Log.d(TAG, "ActivityNotFoundException "+ ex.message)
            startActivity(webIntent)
        }
    }

    private fun vibrate(duration: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(duration)
        }
    }

    private fun onLoginClick(mobile: String, password: String) {
        if (mobile.trim().isEmpty()) {
            vibrate(100)
            Toast.makeText(this, "Empty number", Toast.LENGTH_SHORT).show()
        } else if (password.trim().isEmpty()) {
            vibrate(100)
            Toast.makeText(this, "Empty password", Toast.LENGTH_SHORT).show()
        } else {
            val map = HashMap<String, String>()
            map["mobile"] = mobile
            map["password"] = password
            val url = if (loginAs == 0) AppStrings.cbwtf_login else AppStrings.hcf_login
            viewModel.login(url, map)
        }
    }
}
