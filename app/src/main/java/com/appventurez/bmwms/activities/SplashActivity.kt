package com.appventurez.bmwms.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.appventurez.bmwms.BuildConfig
import com.appventurez.bmwms.R
import com.appventurez.bmwms.cbwtf.activities.CbwtfDashboardActivity
import com.appventurez.bmwms.classes.AppStrings
import com.appventurez.bmwms.classes.MSP
import com.appventurez.bmwms.model.UserData
import com.appventurez.bmwms.network.RetrofitClient
import com.appventurez.bmwms.repository.SplashRepository
import com.appventurez.bmwms.viewmodel.SplashViewModel
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "SplashActivity"
    }

    private lateinit var splashImage: ImageView
    private var loginAs = 0
    private var isNextActivityStarted = false
    private lateinit var downloadAlert: AlertDialog
    private lateinit var progressTv: TextView

    private val viewModel: SplashViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SplashViewModel(SplashRepository(RetrofitClient.apiService)) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        initViews()
        setupObservers()

        startTimer()
        // viewModel.getAppVersion(AppStrings.get_app_version)
    }

    private fun initViews() {
        downloadAlert = AlertDialog.Builder(this).create()
        val downloadView = LayoutInflater.from(this).inflate(R.layout.download_alert_view, null, false)
        progressTv = downloadView.findViewById(R.id.download_progress_tv)
        downloadAlert.setView(downloadView)
        downloadAlert.window?.setBackgroundDrawableResource(android.R.color.transparent)
        downloadAlert.setCancelable(false)

        splashImage = findViewById(R.id.splash_image)
        Glide.with(this).load(R.drawable.uplogogpb).into(splashImage)
    }

    private fun setupObservers() {
        viewModel.loginResponse.observe(this) { response ->
            if (isNextActivityStarted) return@observe

            if (response.isSuccessful) {
                val loginResponse = response.body()
                val data = loginResponse?.data
                if (loginResponse?.status.equals("success", ignoreCase = true) && !data.isNullOrEmpty()) {
                    val userData = data[0]
                    saveUserData(userData)
                    navigateToNext(Intent(this, CbwtfDashboardActivity::class.java))
                } else {
                    Snackbar.make(splashImage, "Your account is banned or removed", 1000).show()
                    navigateToNext(Intent(this, LoginActivity::class.java))
                }
            } else {
                showErrorAndLogin()
            }
        }

        viewModel.versionResponse.observe(this) { response ->
            if (response.isSuccessful) {
                val versionResponse = response.body()
                val data = versionResponse?.data
                if (versionResponse?.status.equals("success", ignoreCase = true) && !data.isNullOrEmpty()) {
                    val version = data[0].version
                    version?.trim()?.toIntOrNull()?.let { checkAppVersion(it) }
                }
            }
        }

        viewModel.error.observe(this) {
            showErrorAndLogin()
        }
    }

    private fun saveUserData(userData: UserData) {
        val msp = MSP.getInstance(this)
        val operatorID = if (loginAs == 0) userData.operatorId else userData.hospitalCode

        msp.setStringData(AppStrings.userPassword, userData.password)
        msp.setStringData(AppStrings.userName, userData.name)
        msp.setStringData(AppStrings.userMobile, userData.mobile)
        msp.setStringData(AppStrings.userAddress, userData.address)
        msp.setStringData(AppStrings.userID, operatorID)
        msp.setStringData(AppStrings.userCbwtfID, userData.cbwtfId)

        if (loginAs == 0) {
            msp.setStringData(AppStrings.loginAs, "cbwtf")
        } else {
            msp.setStringData(AppStrings.loginAs, "hcf")
            msp.setStringData(AppStrings.hcfCode, userData.hospitalCode)
        }
    }

    private fun showErrorAndLogin() {
        Toast.makeText(this, "Something went wrong try again", Toast.LENGTH_SHORT).show()
        navigateToNext(Intent(this, LoginActivity::class.java))
    }

    private fun startTimer() {
        lifecycleScope.launch {
            delay(2000)
            if (isNextActivityStarted) return@launch
            Log.d(TAG, "Timer fired, current flag isNextActivityStarted: $isNextActivityStarted")

            val msp = MSP.getInstance(this@SplashActivity)
            if (msp.containsData(AppStrings.loginAs)) {
                val loginType = msp.getStringData(AppStrings.loginAs)
                if (loginType.equals("cbwtf", ignoreCase = true)) {
                    loginAs = 0
                    viewModel.login(AppStrings.cbwtf_login, getLoginMap(msp))
                } else if (loginType.equals("hcf", ignoreCase = true)) {
                    loginAs = 1
                    viewModel.login(AppStrings.hcf_login, getLoginMap(msp))
                } else {
                    navigateToNext(Intent(this@SplashActivity, LoginActivity::class.java))
                }
            } else {
                navigateToNext(Intent(this@SplashActivity, LoginActivity::class.java))
            }
        }
    }

    private fun getLoginMap(msp: MSP): Map<String, String> {
        val map = HashMap<String, String>()
        map["mobile"] = msp.getStringData(AppStrings.userMobile)
        map["password"] = msp.getStringData(AppStrings.userPassword)
        return map
    }

    @Synchronized
    private fun navigateToNext(intent: Intent) {
        if (!isNextActivityStarted) {
            isNextActivityStarted = true
            val activityName = intent.component?.className ?: "Unknown"
            Log.d(TAG, "Navigating to next Activity: $activityName")
            startActivity(intent)
            finish()
        } else {
            val activityName = intent.component?.className ?: "Unknown"
            Log.d(TAG, "Redundant navigation attempt blocked to: $activityName")
        }
    }

    private fun checkAppVersion(value: Int) {
        if (BuildConfig.VERSION_CODE == value) {
            startTimer()
        } else {
            AlertDialog.Builder(this)
                .setTitle("Update App")
                .setMessage("This app version is outdated please update to latest version")
                .setCancelable(false)
                .setPositiveButton("Update") { _, _ ->
                    Dexter.withContext(this@SplashActivity)
                        .withPermissions(
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                        .withListener(object : MultiplePermissionsListener {
                            override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                                // checkApkFile();
                            }

                            override fun onPermissionRationaleShouldBeShown(
                                permissions: List<PermissionRequest>,
                                token: PermissionToken
                            ) {
                                token.continuePermissionRequest()
                            }
                        }).check()
                }
                .show()
        }
    }
}
