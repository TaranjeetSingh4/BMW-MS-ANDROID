package com.appventurez.bmwms.cbwtf.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.text.format.DateFormat
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.appventurez.bmwms.R
import com.appventurez.bmwms.activities.LoginActivity
import com.appventurez.bmwms.activities.PdfViewActivity
import com.appventurez.bmwms.classes.AppStrings
import com.appventurez.bmwms.classes.MSP
import com.appventurez.bmwms.network.RetrofitClient
import com.appventurez.bmwms.repository.DashboardRepository
import com.appventurez.bmwms.viewmodel.DashboardViewModel
import com.bumptech.glide.Glide
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.card.MaterialCardView
import com.google.gson.Gson
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import java.text.DecimalFormat
import java.util.*

class CbwtfDashboardActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "CbwtfDashboardActivity"
        private const val CAMERA_PERMISSION_REQUEST_CODE = 100
    }

    private lateinit var guidelineTv: TextView
    private lateinit var youtubeTv: TextView
    private lateinit var headerTv: TextView
    private lateinit var todayAttempted: TextView
    private lateinit var todayCollected: TextView
    private lateinit var otpTv: TextView
    private lateinit var bottomImg: ImageView
    private lateinit var hcfImg: ImageView
    private lateinit var reportImg: ImageView
    private lateinit var profileImg: ImageView
    private lateinit var logoutImg: ImageView
    private lateinit var headerLogoImg: ImageView
    private lateinit var scannerImg: ImageView
    private lateinit var rescanImg: ImageView
    private lateinit var hcfCard: MaterialCardView
    private lateinit var reportCard: MaterialCardView
    private lateinit var profileCard: MaterialCardView
    private lateinit var logoutCard: MaterialCardView
    private lateinit var rescanCard: MaterialCardView
    private lateinit var llToday: LinearLayout
    private lateinit var otpLl: LinearLayout
    private lateinit var view0: View

    private var isLocationGranted = false
    private var isCameraPermissionGranted = false
    private var isBluetoothGranted = false
    private var vibrator: Vibrator? = null
    private var fId = 0
    private var isFromScan = false

    private val viewModel: DashboardViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DashboardViewModel(DashboardRepository(RetrofitClient.apiService)) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        initVibrator()
        viewBinding()
        loadImages()
        clickListeners()
        setupObservers()

        viewModel.getNotice(AppStrings.get_app_notice)

        if (!checkCameraPermission()) {
            requestCameraPermission()
        }
    }

    private fun initVibrator() {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
    }

    private fun viewBinding() {
        guidelineTv = findViewById(R.id.cbwtf_dashboard_guideline_link_tv)
        youtubeTv = findViewById(R.id.cbwtf_dashboard_youtube_link_tv)
        headerTv = findViewById(R.id.cbwtf_dashboard_header_tv)
        todayAttempted = findViewById(R.id.cbwtf_dashboard_attempted_tv)
        todayCollected = findViewById(R.id.cbwtf_dashboard_waste_collected_tv)
        scannerImg = findViewById(R.id.cbwtf_dashboard_header_scan_img)
        bottomImg = findViewById(R.id.cbwtf_dashboard_bottom_img)
        hcfImg = findViewById(R.id.cbwtf_dashboard_hcf_scan_img)
        reportImg = findViewById(R.id.cbwtf_dashboard_reports_img)
        profileImg = findViewById(R.id.cbwtf_dashboard_profile_img)
        logoutImg = findViewById(R.id.cbwtf_dashboard_logout_img)
        headerLogoImg = findViewById(R.id.cbwtf_dashboard_header_logo_img)
        rescanImg = findViewById(R.id.cbwtf_dashboard_rescan_img)
        hcfCard = findViewById(R.id.cbwtf_dashboard_hcf_scan_card)
        reportCard = findViewById(R.id.cbwtf_dashboard_reports_card)
        profileCard = findViewById(R.id.cbwtf_dashboard_profile_card)
        logoutCard = findViewById(R.id.cbwtf_dashboard_logout_card)
        rescanCard = findViewById(R.id.cbwtf_dashboard_rescan_card)
        llToday = findViewById(R.id.today_collected_ll)
        view0 = findViewById(R.id.view_0)
        otpLl = findViewById(R.id.otp_ll)
        otpTv = findViewById(R.id.otp_tv)

        if (MSP.getInstance(this).getStringData(AppStrings.loginAs) == "hcf") {
            rescanCard.visibility = View.GONE
            logoutCard.visibility = View.VISIBLE
            llToday.visibility = View.GONE
            view0.visibility = View.GONE
        }
    }

    private fun loadImages() {
        Glide.with(this).load(R.drawable.img_011).into(bottomImg)
        Glide.with(this).load(R.drawable.qr_code).into(hcfImg)
        Glide.with(this).load(R.drawable.user_profile).into(profileImg)
        Glide.with(this).load(R.drawable.user_report).into(reportImg)
        Glide.with(this).load(R.drawable.exit_img).into(logoutImg)
        Glide.with(this).load(R.drawable.uplogogpb).into(headerLogoImg)
        Glide.with(this).load(R.drawable.qr_scan).into(rescanImg)
    }

    private fun clickListeners() {
        profileCard.setOnClickListener { startContainerActivity(1) }
        reportCard.setOnClickListener { startContainerActivity(10) }
        scannerImg.setOnClickListener {
            if (getCameraPermission()) {
                startScanner()
            } else {
                cameraPermissionAlertDialog()
            }
        }
        hcfCard.setOnClickListener {
            if (!getBluetoothPermission()) {
                Toast.makeText(this, "Bluetooth permission needed", Toast.LENGTH_SHORT).show()
            }
            if (isLocationGranted) {
                startContainerActivity(0)
            } else {
                checkLocationPermission()
            }
        }
        rescanCard.setOnClickListener {
            if (isLocationGranted) {
                startContainerActivity(9)
            } else {
                checkLocationPermission()
            }
        }
        logoutCard.setOnClickListener { onLogout() }
        guidelineTv.setOnClickListener { openGuidelines() }
        youtubeTv.setOnClickListener { openYoutubeLink() }
    }

    private fun setupObservers() {
        viewModel.noticeResponse.observe(this) { response ->
            if (response.isSuccessful) {
                response.body()?.data?.getOrNull(0)?.let { notice ->
                    if (notice.noticeStatus == "1") {
                        showNoticeDialog(notice.noticeUrl ?: "", false)
                    } else if (notice.noticeStatus == "2") {
                        showNoticeDialog(notice.noticeUrl ?: "", true)
                    }
                }
            }
        }

        viewModel.cbwtfDataResponse.observe(this) { response ->
            if (response.isSuccessful) {
                response.body()?.data?.getOrNull(0)?.let { data ->
                    MSP.getInstance(this).setStringData(AppStrings.attendance_compulsory, data.attendanceCompulsory)
                    headerTv.text = data.name
                }
            }
        }

        viewModel.todayDataResponse.observe(this) { response ->
            if (response.isSuccessful) {
                val dataList = response.body()?.data ?: emptyList()
                val uniqueHospitals = dataList.mapNotNull { it.hospitalCode }.toSet()
                var totalWeight = 0.0
                dataList.forEach {
                    totalWeight += it.hcfWeight?.toDoubleOrNull() ?: 0.0
                }

                val jsonCustomData = Gson().toJson(dataList)
                MSP.getInstance(this).setStringData("today_qr_custom_data", jsonCustomData)

                todayAttempted.text = uniqueHospitals.size.toString()
                todayCollected.text = DecimalFormat("000.000").format(totalWeight)
            }
        }

        viewModel.hospitalDataResponse.observe(this) { response ->
            if (response.isSuccessful) {
                response.body()?.data?.getOrNull(0)?.let { data ->
                    AlertDialog.Builder(this)
                        .setMessage("Hospital name: ${data.name}\n\nHospital address: ${data.address}")
                        .show()
                }
            }
        }

        viewModel.otpResponse.observe(this) { response ->
            if (response.isSuccessful) {
                response.body()?.data?.getOrNull(0)?.let { data ->
                    otpTv.text = data.otp
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun showNoticeDialog(url: String, cancelable: Boolean) {
        val webView = WebView(this)
        webView.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        webView.loadUrl(url)
        
        val dialog = AlertDialog.Builder(this)
            .setCancelable(true) // Force cancelable to true to allow clicking outside
            .setView(webView)
            .create()

        // Dismiss when clicking inside the WebView
        webView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                dialog.dismiss()
            }
            false
        }

        dialog.show()
    }

    override fun onResume() {
        super.onResume()
        loadDashboardData()
    }

    private fun loadDashboardData() {
        val msp = MSP.getInstance(this)
        val loginAs = msp.getStringData(AppStrings.loginAs)
        val date = DateFormat.format("yyyy-MM-dd", Date().time).toString()

        if (loginAs == "hcf") {
            headerTv.text = msp.getStringData(AppStrings.userName)
            val map = mapOf(
                "hospital_id" to msp.getStringData(AppStrings.userID),
                "date" to date
            )
            viewModel.getTodayData(AppStrings.get_today_data_hcf, map)
            generateOtp()
            scanOtpStatus(true)
        } else {
            val cbwtfMap = mapOf("cbwtf_id" to msp.getStringData(AppStrings.userCbwtfID))
            viewModel.getCbwtfData(AppStrings.cbwtf_data, cbwtfMap)

            val todayMap = mapOf(
                "operator_id" to msp.getStringData(AppStrings.userID),
                "date" to date
            )
            viewModel.getTodayData(AppStrings.get_today_data, todayMap)
            scanOtpStatus(false)
        }
    }

    private fun generateOtp() {
        val msp = MSP.getInstance(this)
        val otpValue = String.format("%04d", Random().nextInt(10000))
        val date = DateFormat.format("yyyy-MM-dd", Date().time).toString()
        val map = mapOf(
            "hcf_code" to msp.getStringData(AppStrings.userID),
            "hcf_name" to msp.getStringData(AppStrings.userName),
            "hcf_contact" to msp.getStringData(AppStrings.userMobile),
            "otp" to otpValue,
            "date" to date
        )
        viewModel.generateOtp(AppStrings.generate_otp, map)
    }

    private fun scanOtpStatus(isHCF: Boolean) {
        if (isHCF) {
            hcfCard.visibility = View.GONE
            otpLl.visibility = View.GONE
            scannerImg.visibility = View.INVISIBLE
        } else {
            hcfCard.visibility = View.VISIBLE
            otpLl.visibility = View.GONE
            scannerImg.visibility = View.VISIBLE
        }
    }

    private fun startContainerActivity(value: Int) {
        fId = value
        isFromScan = true
        if (checkCameraPermission()) {
            val intent = Intent(this, CbwtfFragmentContainerActivity::class.java)
            intent.putExtra("f_id", value)
            startActivity(intent)
        } else {
            requestCameraPermission()
        }
    }

    private fun onLogout() {
        MSP.getInstance(this).removeAll()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun openGuidelines() {
        startActivity(Intent(this, PdfViewActivity::class.java))
    }

    private fun openYoutubeLink() {
        val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:OcrMGvRdjaY"))
        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=OcrMGvRdjaY"))
        try {
            startActivity(appIntent)
        } catch (ex: ActivityNotFoundException) {
            startActivity(webIntent)
        }
    }

    private fun getCameraPermission(): Boolean {
        Dexter.withContext(this).withPermission(Manifest.permission.CAMERA).withListener(object : PermissionListener {
            override fun onPermissionGranted(response: PermissionGrantedResponse) { isCameraPermissionGranted = true }
            override fun onPermissionDenied(response: PermissionDeniedResponse) { isCameraPermissionGranted = false }
            override fun onPermissionRationaleShouldBeShown(request: PermissionRequest, token: PermissionToken) { token.continuePermissionRequest() }
        }).check()
        return isCameraPermissionGranted
    }

    private fun getBluetoothPermission(): Boolean {
        Dexter.withContext(this).withPermission(Manifest.permission.BLUETOOTH).withListener(object : PermissionListener {
            override fun onPermissionGranted(response: PermissionGrantedResponse) { isBluetoothGranted = true }
            override fun onPermissionDenied(response: PermissionDeniedResponse) { isBluetoothGranted = false }
            override fun onPermissionRationaleShouldBeShown(request: PermissionRequest, token: PermissionToken) { token.continuePermissionRequest() }
        }).check()
        return isBluetoothGranted
    }

    private fun checkLocationPermission() {
        Dexter.withContext(this).withPermission(Manifest.permission.ACCESS_FINE_LOCATION).withListener(object : PermissionListener {
            override fun onPermissionGranted(response: PermissionGrantedResponse) { checkLocationSetting() }
            override fun onPermissionDenied(response: PermissionDeniedResponse) {
                AlertDialog.Builder(this@CbwtfDashboardActivity)
                    .setMessage("Grant location permission to continue\nApp permissions > Location > Allow")
                    .setPositiveButton("Setting") { _, _ ->
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        intent.data = Uri.fromParts("package", packageName, null)
                        startActivity(intent)
                    }.show()
            }
            override fun onPermissionRationaleShouldBeShown(request: PermissionRequest, token: PermissionToken) { token.continuePermissionRequest() }
        }).check()
    }

    private fun checkLocationSetting() {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 30000
            fastestInterval = 5000
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest).setAlwaysShow(true)
        val task = LocationServices.getSettingsClient(this).checkLocationSettings(builder.build())

        task.addOnCompleteListener { taskResult ->
            try {
                taskResult.getResult(ApiException::class.java)
                isLocationGranted = true
            } catch (exception: ApiException) {
                if (exception.statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                    try {
                        (exception as ResolvableApiException).startResolutionForResult(this, 1000)
                    } catch (e: Exception) { }
                }
            }
        }
    }

    private val launcher = registerForActivityResult(ScanContract()) { result ->
        result.contents?.let { contents ->
            try {
                val data = contents.split("&", limit = 2)
                val hcfCode = data[0]
                viewModel.getHospitalData(AppStrings.hospital_data, mapOf("hospital_code" to hcfCode))
            } catch (e: Exception) {
                Toast.makeText(this, "Wrong QR Code", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startScanner() {
        val scanOptions = ScanOptions().apply {
            setOrientationLocked(true)
            setBarcodeImageEnabled(true)
            setPrompt("Scan Waste Barcode")
        }
        launcher.launch(scanOptions)
    }

    private fun cameraPermissionAlertDialog() {
        AlertDialog.Builder(this)
            .setMessage("Grant camera permission to continue\nApp permissions > Camera > Allow")
            .setPositiveButton("Setting") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.fromParts("package", packageName, null)
                startActivity(intent)
            }.show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (isFromScan) {
                    startContainerActivity(fId)
                }
            }
        }
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
    }
}
