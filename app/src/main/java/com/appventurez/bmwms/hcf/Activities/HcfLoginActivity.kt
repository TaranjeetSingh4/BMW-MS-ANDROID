package com.appventurez.bmwms.hcf.Activities

import android.content.ActivityNotFoundException
import android.content.Intent
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
import com.appventurez.bmwms.cbwtf.activities.CbwtfDashboardActivity
import com.appventurez.bmwms.classes.AppStrings
import com.appventurez.bmwms.classes.MSP
import com.appventurez.bmwms.hcf.compose.HcfLoginScreen
import com.appventurez.bmwms.network.RetrofitClient
import com.appventurez.bmwms.repository.LoginRepository
import com.appventurez.bmwms.viewmodel.LoginViewModel
import java.util.HashMap
import androidx.core.net.toUri

class HcfLoginActivity : AppCompatActivity() {
    private var TAG = "HcfLoginActivity"

    private var vibrator: Vibrator? = null
    private var loginAs = 1
    
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
        loginAs = intent.getIntExtra("loginAs", 1)

        setContent {
            val isLoading = viewModel.isLoading.value
            val loginResponse = viewModel.loginResponse.value

            LaunchedEffect(loginResponse) {
                loginResponse?.let { response ->
                    if (response.isSuccessful && response.body()?.status.equals("success", ignoreCase = true)) {
                        val userData = response.body()?.data?.getOrNull(0)
                        userData?.let { data ->
                            if (loginAs == 0) {
                                MSP.getInstance(this@HcfLoginActivity).apply {
                                    setStringData(AppStrings.userName, data.name)
                                    setStringData(AppStrings.userMobile, data.email)
                                    setStringData(AppStrings.userPassword, data.password)
                                    setStringData(AppStrings.userAddress, data.address)
                                    setStringData(AppStrings.userID, data.operatorId)
                                    setStringData(AppStrings.userCbwtfID, data.cbwtfId)
                                    setStringData(AppStrings.loginAs, "cbwtf")
                                }
                            } else {
                                MSP.getInstance(this@HcfLoginActivity).apply {
                                    setStringData(AppStrings.userName, data.name)
                                    setStringData(AppStrings.userMobile, data.email)
                                    setStringData(AppStrings.userPassword, data.password)
                                    setStringData(AppStrings.userAddress, data.address)
                                    setStringData(AppStrings.userID, data.hospitalCode)
                                    setStringData(AppStrings.userCbwtfID, data.cbwtfId)
                                    setStringData(AppStrings.loginAs, "hcf")
                                }
                            }
                            val intent = Intent(this@HcfLoginActivity, CbwtfDashboardActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        vibrate(100)
                        Toast.makeText(this@HcfLoginActivity, "Wrong credential", Toast.LENGTH_SHORT).show()
                    }
                    viewModel.resetLoginResponse()
                }
            }

            HcfLoginScreen(
                isLoading = isLoading,
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

    private fun onLoginClick(email: String, password: String) {
        if (email.trim().isEmpty()) {
            vibrate(100)
            Toast.makeText(this, "Empty email", Toast.LENGTH_SHORT).show()
        } else if (password.trim().isEmpty()) {
            vibrate(100)
            Toast.makeText(this, "Empty password", Toast.LENGTH_SHORT).show()
        } else {
            val map = HashMap<String, String>()
            map["email"] = email
            map["password"] = password
            val url = if (loginAs == 0) AppStrings.cbwtf_login else AppStrings.hcf_login
            viewModel.login(url, map)
        }
    }
}
