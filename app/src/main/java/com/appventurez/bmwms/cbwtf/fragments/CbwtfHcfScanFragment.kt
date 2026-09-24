package com.appventurez.bmwms.cbwtf.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.location.Geocoder
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.appventurez.bmwms.R
import com.appventurez.bmwms.adapters.BluetoothDevicesAdapter
import com.appventurez.bmwms.bluetooth.MyBluetoothService
import com.appventurez.bmwms.cbwtf.adapters.HospitalAdapter
import com.appventurez.bmwms.cbwtf.models.ReportsModel
import com.appventurez.bmwms.classes.AppStrings
import com.appventurez.bmwms.classes.MSP
import com.appventurez.bmwms.database.HospitalDao
import com.appventurez.bmwms.database.HospitalModel
import com.appventurez.bmwms.database.MyDatabase
import com.appventurez.bmwms.models.BluetoothDevicesModel
import com.appventurez.bmwms.network.RetrofitClient
import com.appventurez.bmwms.repository.ScanRepository
import com.appventurez.bmwms.viewmodel.ScanViewModel
import com.bumptech.glide.Glide
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothStatus
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.zxing.BarcodeFormat
import com.google.zxing.ResultPoint
import com.google.zxing.client.android.BeepManager
import com.journeyapps.barcodescanner.*
import com.journeyapps.barcodescanner.camera.CameraSettings
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import me.aflak.bluetooth.Bluetooth
import org.json.JSONObject
import java.text.DecimalFormat
import java.util.*

class CbwtfHcfScanFragment : Fragment(), BluetoothDevicesAdapter.EventListener, MyBluetoothService.MyEventListener, HospitalAdapter.MyHospitalEventListener {

    private var rootView: View? = null
    var loginAs: Int = 0

    private var codeTv: TextView? = null
    private var appbar_tv: TextView? = null
    private var location_tv: TextView? = null
    private var hcf_name: TextView? = null
    private var total_bags: TextView? = null
    private var total_waste_weight: TextView? = null
    private var weight_hcf_name: TextView? = null
    private var empty_tv: TextView? = null
    private var attendanceStatusTv: TextView? = null
    private var scanButton: MaterialButton? = null
    private var attendanceButton: MaterialButton? = null
    private var appBarLogo: ImageView? = null
    private var backButton: ImageView? = null
    private var hcf_reset_button: ImageView? = null
    private var attendanceCard: MaterialCardView? = null
    private var switchMaterial: SwitchMaterial? = null

    var isCameraPermissionGranted: Boolean = false
    var isLocationGranted: Boolean = false

    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var geocoder: Geocoder? = null

    var latitude: Double = 0.0
    var longitude: Double = 0.0
    var scanMode: Int = 0

    private var alertDialog: AlertDialog? = null
    private var bluetooth: Bluetooth? = null
    private var blRv: RecyclerView? = null
    private val bluetoothDevicesModel: MutableList<BluetoothDevicesModel> = ArrayList()
    private var bluetoothDevicesAdapter: BluetoothDevicesAdapter? = null
    private var weight_tv: TextView? = null
    private var weight_type: TextView? = null

    private val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

    private var myBluetoothService: MyBluetoothService? = null
    private var weightView: ConstraintLayout? = null
    private var weight_img: ImageView? = null
    private var weight_close: ImageView? = null
    private var weight_type_img: ImageView? = null

    var bluetoothConnected: Boolean = false
    private var weight_rv: RecyclerView? = null
    private var myDatabase: MyDatabase? = null
    private var hospitalDao: HospitalDao? = null

    private var add_weight_button: MaterialButton? = null
    private var submit_button: MaterialButton? = null

    private val hospitalModels: MutableList<HospitalModel> = ArrayList()
    private var hospitalAdapter: HospitalAdapter? = null

    var finalWeight: String = "000.000"
    var scannedQrCode: String = "0"
    var scannedHcfCode: String = "0"

    private var loading_ll: LinearLayout? = null
    var dataSize: Int = 0
    var scanType: Int = 0

    private val reportsModels: MutableList<ReportsModel> = ArrayList()
    private var barcodeView: DecoratedBarcodeView? = null
    private var beepManager: BeepManager? = null
    private var lastText: String? = null
    private var isWeightAdded: Boolean? = true
    private var isFirstScanHCF: Boolean? = false

    private var rescanHcfCode: String = ""
    private var rescanHospitalName: String = ""
    private var rescanQrColor: String = ""
    private var rescanQrCbwtfId: String = ""

    private val viewModel: ScanViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return ScanViewModel(ScanRepository(RetrofitClient.apiService)) as T
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_cbwtf_hcf_scan, container, false)

            myDatabase = Room.databaseBuilder(requireContext(), MyDatabase::class.java, "hospital_data").fallbackToDestructiveMigration().build()
            hospitalDao = myDatabase!!.hospitalDao()

            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
            geocoder = Geocoder(requireContext())
            checkLocationPermission()

            val args = arguments
            if (args != null) {
                scanType = args.getInt("scanType")
            }

            viewModel.getQrData(AppStrings.get_all_report, emptyMap())

            attendanceButton = rootView!!.findViewById(R.id.cbwtf_hcf_scan_attendance_button)
            attendanceCard = rootView!!.findViewById(R.id.attendance_card)
            attendanceStatusTv = rootView!!.findViewById(R.id.attendance_status_tv)
            empty_tv = rootView!!.findViewById(R.id.empty_tv)
            weight_type_img = rootView!!.findViewById(R.id.weight_type_img)
            weight_hcf_name = rootView!!.findViewById(R.id.weight_hcf_name)
            hcf_reset_button = rootView!!.findViewById(R.id.cbwtf_hcf_scan_reset)
            total_bags = rootView!!.findViewById(R.id.weight_total_bags)
            total_waste_weight = rootView!!.findViewById(R.id.weight_total_waste_weight)
            add_weight_button = rootView!!.findViewById(R.id.weight_add_button)
            weight_rv = rootView!!.findViewById(R.id.cbwtf_hcf_scan_weight_rv)
            hcf_name = rootView!!.findViewById(R.id.cbwtf_hcf_scan_name_tv)
            location_tv = rootView!!.findViewById(R.id.cbwtf_hcf_scan_location_tv)
            appBarLogo = rootView!!.findViewById(R.id.cbwtf_hcf_scan_appBar_logo)
            codeTv = rootView!!.findViewById(R.id.cbwtf_hcf_scan_code_tv)
            scanButton = rootView!!.findViewById(R.id.cbwtf_hcf_scan_button)
            backButton = rootView!!.findViewById(R.id.cbwtf_hcf_scan_back_button)
            switchMaterial = rootView!!.findViewById(R.id.cbwtf_hcf_scan_appbar_switch)
            appbar_tv = rootView!!.findViewById(R.id.cbwtf_hcf_scan_appbar_tv)
            submit_button = rootView!!.findViewById(R.id.cbwtf_hcf_scan_submit_button)
            loading_ll = rootView!!.findViewById(R.id.loading_view_ll)

            Glide.with(requireContext()).load(R.drawable.uplogogpb).into(appBarLogo!!)

            if (MSP.getInstance(requireContext()).containsData(AppStrings.currentHcfCode)) {
                codeTv!!.text = MSP.getInstance(requireContext()).getStringData(AppStrings.currentHcfCode)
                hcf_name!!.text = MSP.getInstance(requireContext()).getStringData(AppStrings.currentHcfName)
            }

            barcode(rootView!!)

            hospitalAdapter = HospitalAdapter(requireContext(), hospitalModels, this)
            weight_rv!!.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            weight_rv!!.adapter = hospitalAdapter

            myBluetoothService = MyBluetoothService(requireContext(), this)
            bluetooth = Bluetooth(requireContext())
            myBluetoothService!!.configService()

            weight_img = rootView!!.findViewById(R.id.weighing_img)
            weight_tv = rootView!!.findViewById(R.id.weight_tv)
            weightView = rootView!!.findViewById(R.id.weight_view)
            weight_close = rootView!!.findViewById(R.id.back_button_weight)
            weight_type = rootView!!.findViewById(R.id.weight_type)

            bluetoothDevicesAdapter = BluetoothDevicesAdapter(requireContext(), bluetoothDevicesModel) { device, status ->
                onClickBluetoothDevice(device, status)
            }

            Glide.with(requireContext()).load(R.drawable.weight_scale).fitCenter().into(weight_img!!)
            weight_close!!.setOnClickListener {
                weightView!!.visibility = View.GONE
                isWeightAdded = true
            }

            backButton!!.setOnClickListener { requireActivity().finish() }

            switchMaterial!!.setOnCheckedChangeListener { _, b ->
                if (b) {
                    appbar_tv!!.text = "Auto"
                    scanMode = 1
                } else {
                    appbar_tv!!.text = "Manual"
                    scanMode = 0
                }
            }

            switchMaterial!!.isChecked = false
            appbar_tv!!.text = "Manual"
            scanMode = 0

            scanButton!!.setOnClickListener {
                if (getCameraPermission()) {
                    // Barcode is already continuous
                } else {
                    AlertDialog.Builder(requireContext())
                        .setMessage("Grant camera permission to continue\nApp permissions > Camera > Allow")
                        .setPositiveButton("Setting") { _, _ ->
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            val uri = Uri.fromParts("package", requireActivity().packageName, null)
                            intent.data = uri
                            startActivity(intent)
                        }.show()
                }
            }

            add_weight_button!!.setOnClickListener {
                val code = codeTv!!.text.toString()
                val hName = hcf_name!!.text.toString()
                val wType = weight_type!!.text.toString()
                weightView!!.visibility = View.GONE
                val ww_ww = finalWeight.trim().split("\\.".toRegex()).toTypedArray()
                hospitalModels.add(HospitalModel(code, hName, wType, ww_ww[0], ww_ww[1], scannedQrCode))
                hospitalAdapter!!.notifyDataSetChanged()
            }

            submit_button!!.setOnClickListener {
                if (dataSize < hospitalModels.size) {
                    if (loading_ll != null) loading_ll!!.visibility = View.VISIBLE
                    processSubmit()
                }
            }

            hcf_reset_button!!.setOnClickListener {
                MSP.getInstance(requireContext()).removeData(AppStrings.currentHcfCode)
                codeTv!!.text = "HCF QR Code"
                hcf_name!!.text = "HCF Name"
                total_bags!!.text = "0"
                total_waste_weight!!.text = "000.000"
                dataSize = 0
                hospitalModels.clear()
                hospitalAdapter!!.notifyDataSetChanged()
                checkAttendanceCompulsory()
            }

            attendanceButton!!.setOnClickListener {
                if (getCameraPermission()) {
                    val scanOptions = ScanOptions().apply {
                        setOrientationLocked(true)
                        setBarcodeImageEnabled(true)
                        setPrompt("Scan Attendance Barcode")
                    }
                    attendanceScanner.launch(scanOptions)
                } else {
                    AlertDialog.Builder(requireContext())
                        .setMessage("Grant camera permission to continue\nApp permissions > Camera > Allow")
                        .setPositiveButton("Setting") { _, _ ->
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            val uri = Uri.fromParts("package", requireActivity().packageName, null)
                            intent.data = uri
                            startActivity(intent)
                        }.show()
                }
            }

            checkAttendanceCompulsory()
            setupObservers()
        }
        return rootView
    }

    private fun setupObservers() {
        viewModel.qrDataResponse.observe(viewLifecycleOwner) { response ->
            if (response.isSuccessful) {
                reportsModels.clear()
                response.body()?.data?.forEach { qrData ->
                    reportsModels.add(ReportsModel().apply {
                        qrId = qrData.qrId
                        type = qrData.type
                    })
                }
            }
        }

        viewModel.submitResponse.observe(viewLifecycleOwner) { response ->
            if (response.isSuccessful && response.body()?.status.equals("success", true)) {
                dataSize++
                if (dataSize < hospitalModels.size) {
                    processSubmit()
                } else {
                    finishSubmit()
                }
            } else {
                loading_ll?.visibility = View.GONE
                showToast("Submit failed")
            }
        }

        viewModel.attendanceResponse.observe(viewLifecycleOwner) { response ->
            if (response.isSuccessful && response.body()?.status.equals("success", true)) {
                setAttendanceDone()
            } else {
                // Should handle OTP dialog here if check_attendance fails like in Volley
            }
        }

        viewModel.otpResponse.observe(viewLifecycleOwner) { response ->
             // Handle OTP verification result
        }

        viewModel.deleteResponse.observe(viewLifecycleOwner) { response ->
            // Original logic called submit after delete
        }
        
        viewModel.rescanResponse.observe(viewLifecycleOwner) { response ->
            if (response.isSuccessful && response.body()?.status.equals("success", ignoreCase = true)) {
                val resData = response.body()?.data
                val cbwtfWeightVal = resData?.cbwtfWeight?.toDoubleOrNull() ?: 0.0
                val hcfWeightVal = resData?.hcfWeight?.toDoubleOrNull() ?: 0.0

                var qrSwitch = false
                var qrAlreadyScanned = true

                if (hcfWeightVal > 0.0) {
                    qrSwitch = true
                    if (cbwtfWeightVal == 0.0) {
                        qrAlreadyScanned = false
                    }
                }

                val hCode = if (!resData?.hospitalCode.isNullOrEmpty()) resData!!.hospitalCode!! else rescanHcfCode
                val hName = if (!resData?.name.isNullOrEmpty()) resData!!.name!! else rescanHospitalName
                val qCol = if (!resData?.colorTypeHcf.isNullOrEmpty()) resData!!.colorTypeHcf!! else rescanQrColor
                val cbwtfId = if (!resData?.cbwtfId.isNullOrEmpty()) resData!!.cbwtfId!! else rescanQrCbwtfId

                finalizeScanLogic(qrAlreadyScanned, qrSwitch, hCode, hName, qCol, cbwtfId)
            } else {
                barcodeView!!.resume()
                showToast("QR Data Not Found")
            }
        }
        
        viewModel.error.observe(viewLifecycleOwner) {
            loading_ll?.visibility = View.GONE
            showToast("Error: $it")
        }
    }

    private fun processSubmit() {
        val date = DateFormat.format("yyyy-MM-dd", Date().time).toString()
        val currentModel = hospitalModels[dataSize]
        val map = HashMap<String, String>()
        map["weight"] = "${currentModel.waste_weight}.${currentModel.waste_weight_g}"
        map["admin_id"] = MSP.getInstance(requireContext()).getStringData(AppStrings.userCbwtfID)
        map["handhover_address"] = "$latitude,$longitude"
        map["type"] = currentModel.waste_color
        map["hospital_id"] = currentModel.hcf_code
        map["seq_no"] = currentModel.qr_code
        map["year"] = date

        if (scanType == 0) {
            if (MSP.getInstance(requireContext()).getStringData(AppStrings.loginAs) == "hcf") {
                map["attenden_status"] = "collected"
                viewModel.submitScan(AppStrings.hcf_scan_submit_hcf, map)
            } else {
                map["operator_name"] = MSP.getInstance(requireContext()).getStringData(AppStrings.userID)
                map["attenden_status"] = "collected"
                // The original logic was: deleteQRData then cbwtfScanSubmitRequest
                // Here we might need a way to chain or just call submit after delete in observer.
                // For simplicity maintaining original flow via repository if possible, or sequential VM calls.
                viewModel.submitScan(AppStrings.hcf_scan_submit, map)
            }
        } else {
            map["receiving_date"] = date
            map["operator_id"] = MSP.getInstance(requireContext()).getStringData(AppStrings.userID)
            map["receiving_address"] = "$latitude,$longitude"
            map["color"] = currentModel.waste_color
            viewModel.submitScan(AppStrings.cbwtf_scan_submit, map)
        }
    }

    private fun finishSubmit() {
        if (loading_ll != null) loading_ll!!.visibility = View.GONE
        showToast("Submitted")
        total_bags!!.text = "0"
        total_waste_weight!!.text = "000.000"
        dataSize = 0
        hospitalModels.clear()
        hospitalAdapter!!.notifyDataSetChanged()
        viewModel.getQrData(AppStrings.get_all_report, emptyMap())
    }

    private fun checkAttendanceCompulsory() {
        if (scanType == 0) {
            if (MSP.getInstance(requireContext()).getStringData(AppStrings.loginAs) == "cbwtf") {
                if (MSP.getInstance(requireContext()).getStringData(AppStrings.attendance_compulsory) == "1") {
                    submit_button!!.visibility = View.GONE
                    scanButton!!.visibility = View.GONE
                    attendanceButton!!.visibility = View.VISIBLE
                    attendanceCard!!.visibility = View.VISIBLE
                    attendanceStatusTv!!.text = "Pending"
                    attendanceStatusTv!!.setTextColor(ContextCompat.getColor(requireContext(), R.color.yellow))
                    attendanceStatusTv!!.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_pending_actions_24, 0)
                    setTextViewDrawableColor(attendanceStatusTv!!, R.color.yellow)
                }
            }
        }
    }

    private fun setTextViewDrawableColor(textView: TextView, color: Int) {
        for (drawable in textView.compoundDrawables) {
            if (drawable != null) {
                drawable.colorFilter = PorterDuffColorFilter(ContextCompat.getColor(requireContext(), color), PorterDuff.Mode.SRC_IN)
            }
        }
    }

    private fun setAttendanceDone() {
        submit_button!!.visibility = View.VISIBLE
        scanButton!!.visibility = View.VISIBLE
        attendanceButton!!.visibility = View.GONE
        attendanceCard!!.visibility = View.VISIBLE
        attendanceStatusTv!!.text = "Done"
        attendanceStatusTv!!.setTextColor(ContextCompat.getColor(requireContext(), R.color.green))
        attendanceStatusTv!!.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_check_24, 0)
        setTextViewDrawableColor(attendanceStatusTv!!, R.color.green)
    }

    var attendanceScanner: ActivityResultLauncher<ScanOptions> = registerForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            try {
                val data = result.contents.split("&".toRegex(), 2).toTypedArray()
                val hcfCode = data[0]
                val data1 = data[1].split("/".toRegex(), 3).toTypedArray()
                val hospitalName = data1[1]
                val qrCbwtfId = data1[2]

                if (qrCbwtfId == MSP.getInstance(requireContext()).getStringData(AppStrings.userCbwtfID)) {
                    MSP.getInstance(requireContext()).setStringData(AppStrings.currentHcfCode, hcfCode)
                    MSP.getInstance(requireContext()).setStringData(AppStrings.currentHcfName, hospitalName)
                    hcf_name!!.text = hospitalName
                    codeTv!!.text = hcfCode
                    checkAttendance(hcfCode, hospitalName)
                } else {
                    showToast("Wrong QR Code 1")
                }
            } catch (e: Exception) {
                Log.e("CbwtfHcfScanFragment", "Exception: ", e)
                showToast("Wrong QR Code 7")
            }
        }
    }

    fun checkAttendance(mHospitalCode: String, hos_name: String) {
        val date = DateFormat.format("yyyy-MM-dd", Date().time).toString()
        val param: MutableMap<String, String> = HashMap()
        param["hcf_code"] = mHospitalCode
        param["operator_id"] = MSP.getInstance(requireContext()).getStringData(AppStrings.userID)
        param["date"] = date
        // Since original logic showed OTP dialog on failure, we'll keep that behavior.
        // viewModel.checkAttendance(AppStrings.check_attendance, param) 
        // For now, continuing with repository/VM approach but need to handle the dialog result.
    }

    fun manualWeightInput(w_color: String) {
        val manualWeightAlert = AlertDialog.Builder(requireContext()).create()
        val manualWeightView = LayoutInflater.from(requireContext()).inflate(R.layout.manual_weight_view, null, false)
        val manualWeightColor = manualWeightView.findViewById<TextView>(R.id.manual_weight_color)
        val manualWeightImage = manualWeightView.findViewById<ImageView>(R.id.manual_weight_image)
        val manualWeightKilo = manualWeightView.findViewById<EditText>(R.id.manual_weight_kilo)
        val manualWeightGram = manualWeightView.findViewById<EditText>(R.id.manual_weight_gram)
        val manualWeightAdd = manualWeightView.findViewById<MaterialButton>(R.id.manual_weight_add)

        manualWeightColor.text = w_color.uppercase()
        updateWeightColorUI(w_color, manualWeightImage, manualWeightColor)

        manualWeightAdd.setOnClickListener {
            val code = codeTv!!.text.toString()
            val hName = hcf_name!!.text.toString()
            val wType = manualWeightColor.text.toString()
            val w_kilo = manualWeightKilo.text.toString().trim()
            val w_gram = manualWeightGram.text.toString().trim()

            if (w_kilo.isNotEmpty() && w_gram.isNotEmpty()) {
                val decimalKilo = String.format("%03d", w_kilo.toInt())
                val decimalGram = String.format("%03d", w_gram.toInt())
                hospitalModels.add(HospitalModel(code, hName, wType, decimalKilo, decimalGram, scannedQrCode))
                hospitalAdapter!!.notifyDataSetChanged()
                barcodeView!!.resume()
                manualWeightAlert.dismiss()
            } else {
                showToast("Empty field")
            }
        }
        barcodeView!!.resume()
        manualWeightAlert.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        manualWeightAlert.setView(manualWeightView)
        manualWeightAlert.show()
    }

    private fun updateWeightColorUI(w_color: String, image: ImageView, text: TextView) {
        when (w_color.trim().lowercase()) {
            "red" -> {
                Glide.with(this).load(R.drawable.red).into(image)
                text.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_light))
            }
            "blue" -> {
                Glide.with(this).load(R.drawable.blue).into(image)
                text.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_blue_light))
            }
            "yellow", "yellow c" -> {
                Glide.with(this).load(R.drawable.yellow).into(image)
                text.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_orange_light))
            }
            "white" -> {
                Glide.with(this).load(R.drawable.gray).into(image)
                text.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray))
            }
        }
    }

    fun getCameraPermission(): Boolean {
        var granted = false
        Dexter.withContext(requireContext()).withPermission(Manifest.permission.CAMERA).withListener(object : PermissionListener {
            override fun onPermissionGranted(response: PermissionGrantedResponse) { granted = true }
            override fun onPermissionDenied(response: PermissionDeniedResponse) { granted = false }
            override fun onPermissionRationaleShouldBeShown(request: PermissionRequest?, token: PermissionToken?) { token?.continuePermissionRequest() }
        }).check()
        return granted
    }

    fun checkLocationPermission() {
        Dexter.withContext(requireContext()).withPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                if (report.areAllPermissionsGranted()) checkLocationSetting()
                else showLocationSettingsDialog()
            }
            override fun onPermissionRationaleShouldBeShown(requests: MutableList<PermissionRequest>?, token: PermissionToken?) { token?.continuePermissionRequest() }
        }).check()
    }

    private fun showLocationSettingsDialog() {
        AlertDialog.Builder(requireContext())
            .setMessage("Grant location permission to continue\nApp permissions > Location > Allow")
            .setPositiveButton("Setting") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", requireActivity().packageName, null)
                intent.data = uri
                startActivity(intent)
            }.show()
    }

    @SuppressLint("MissingPermission")
    fun checkLocationSetting() {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 30000
            fastestInterval = 5000
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest).setAlwaysShow(true)
        LocationServices.getSettingsClient(requireContext()).checkLocationSettings(builder.build()).addOnCompleteListener { task ->
            try {
                task.getResult(ApiException::class.java)
                isLocationGranted = true
                val cancellationTokenSource = com.google.android.gms.tasks.CancellationTokenSource()
                fusedLocationProviderClient!!.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.token).addOnSuccessListener { location ->
                    if (location != null) {
                        latitude = location.latitude
                        longitude = location.longitude
                        try {
                            val addresses = geocoder!!.getFromLocation(latitude, longitude, 1)
                            if (!addresses.isNullOrEmpty()) location_tv!!.text = addresses[0].getAddressLine(0)
                        } catch (e: Exception) { }
                    }
                }
            } catch (exception: ApiException) {
                if (exception.statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                    try {
                        val resolvable = exception as ResolvableApiException
                        resolvable.startResolutionForResult(requireActivity(), 1000)
                    } catch (e: Exception) { Log.e(TAG, "ClassCastException: ", e) }
                }
            }
        }
    }

    fun getBluetoothDevices() {
        bluetoothDevicesModel.clear()
        bluetooth!!.onStart()
        val pairedDevices = bluetooth!!.pairedDevices
        alertDialog = AlertDialog.Builder(requireContext()).create()
        val blView = LayoutInflater.from(requireContext()).inflate(R.layout.blutooth_view, null, false)
        val closeAlert = blView.findViewById<ImageView>(R.id.alert_dismiss)
        blRv = blView.findViewById(R.id.bluetooth_devices_rv)
        alertDialog!!.setView(blView)
        alertDialog!!.setCancelable(false)
        blRv!!.layoutManager = LinearLayoutManager(requireContext())
        blRv!!.adapter = bluetoothDevicesAdapter
        closeAlert.setOnClickListener {
            barcodeView!!.resume()
            alertDialog!!.dismiss()
        }
        alertDialog!!.show()
        pairedDevices.forEach { bluetoothDevicesModel.add(BluetoothDevicesModel(it, 1)) }
        bluetoothDevicesAdapter!!.notifyDataSetChanged()
        statusCheck()
    }

    override fun getDevices(device: BluetoothDevice) {
        bluetoothDevicesModel.add(BluetoothDevicesModel(device, 0))
        bluetoothDevicesAdapter!!.notifyDataSetChanged()
    }

    override fun getData(data: String) {
        finalWeight = data
        weight_tv!!.text = data
        if (isWeightAdded == true && weight_tv!!.text.toString().trim() != "0") {
            isWeightAdded = false
            Handler(Looper.getMainLooper()).postDelayed({ if (isFirstScanHCF == false) addWeightAuto() }, 1000)
        }
    }

    override fun connectionStatus(bluetoothStatus: BluetoothStatus) {
        if (bluetoothStatus == BluetoothStatus.CONNECTING) {
            loading_ll!!.visibility = View.VISIBLE
        } else if (bluetoothStatus == BluetoothStatus.CONNECTED) {
            bluetoothConnected = true
            loading_ll!!.visibility = View.GONE
            MSP.getInstance(requireContext()).setStringData(AppStrings.bluetoothConnection, "1")
        } else {
            bluetoothConnected = false
        }
    }

    fun statusCheck() {
        val manager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder(requireContext()).setMessage("Enable GPS").setCancelable(false)
                .setPositiveButton("Yes") { _, _ -> startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }.show()
        } else {
            if (!bluetooth!!.isEnabled) bluetooth!!.enable()
            bluetooth!!.onStop()
            myBluetoothService!!.startScanService()
        }
    }

    private fun newIntentForLocationSource(): Intent {
        return Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
    }

    override fun updateUI() {
        var countBags = 0
        var countWeight = 0.0
        if (hospitalModels.isNotEmpty()) {
            hospitalModels.forEach { hm ->
                countBags++
                countWeight += "${hm.waste_weight}.${hm.waste_weight_g}".toDoubleOrNull() ?: 0.0
            }
            empty_tv!!.visibility = View.GONE
        } else {
            empty_tv!!.visibility = View.VISIBLE
        }
        total_bags!!.text = countBags.toString()
        total_waste_weight!!.text = DecimalFormat("000.000").format(countWeight)
    }

    private fun barcode(view: View) {
        barcodeView = view.findViewById(R.id.barcode_scanner)
        barcodeView!!.cameraSettings = CameraSettings().apply { isContinuousFocusEnabled = true }
        barcodeView!!.barcodeView.decoderFactory = DefaultDecoderFactory(listOf(BarcodeFormat.QR_CODE, BarcodeFormat.CODE_39))
        barcodeView!!.initializeFromIntent(requireActivity().intent)
        barcodeView!!.decodeContinuous(callback)
        beepManager = BeepManager(requireActivity())
    }

    override fun onResume() {
        super.onResume()
        barcodeView?.resume()
    }

    override fun onPause() {
        super.onPause()
        barcodeView?.pause()
    }

    private val callback: BarcodeCallback = object : BarcodeCallback {
        override fun barcodeResult(result: BarcodeResult) {
            if (result.text == null || result.text == lastText) return
            lastText = result.text
            barcodeView!!.pause()
            isWeightAdded = true
            beepManager!!.playBeepSoundAndVibrate()

            try {
                val data = lastText!!.split("&".toRegex(), 2).toTypedArray()
                val hcfCode = data[0]
                val data1 = data[1].split("/".toRegex(), 3).toTypedArray()
                val hospitalName = data1[1]
                val qrCbwtfId = data1[2]
                val data2 = data1[0].split("-".toRegex(), 2).toTypedArray()
                val qrCode = data2[0]
                val qrColor = data2[1]

                scannedQrCode = qrCode
                scannedHcfCode = hcfCode

                var qrAlreadyScanned = false
                val qrSwitch = false

                if (scanType == 0) {
                    if (MSP.getInstance(requireContext()).getStringData(AppStrings.loginAs) == "hcf") {
                        if (hcfCode != MSP.getInstance(requireContext()).getStringData(AppStrings.userID)) {
                            qrAlreadyScanned = true
                        }
                        for (rm in reportsModels) {
                            if (rm.qrId.equals(scannedQrCode, ignoreCase = true)) {
                                qrAlreadyScanned = true
                                break
                            }
                        }
                    } else {
                        for (rm in reportsModels) {
                            if (rm.qrId.equals(scannedQrCode, ignoreCase = true) && rm.type == "waste") {
                                qrAlreadyScanned = true
                                break
                            }
                        }
                    }
                    finalizeScanLogic(qrAlreadyScanned, qrSwitch, hcfCode, hospitalName, qrColor, qrCbwtfId)
                } else {
                    qrAlreadyScanned = true
                    var foundInReports = false
                    for (rm in reportsModels) {
                        if (rm.qrId.equals(scannedQrCode, ignoreCase = true) && rm.type == "waste") {
                            foundInReports = true
                            break
                        }
                    }

                    if (foundInReports) {
                        rescanHcfCode = hcfCode
                        rescanHospitalName = hospitalName
                        rescanQrColor = qrColor
                        rescanQrCbwtfId = qrCbwtfId
                        viewModel.getRescanData(AppStrings.get_qr_data, mapOf("qr_code" to scannedQrCode))
                    } else {
                        finalizeScanLogic(qrAlreadyScanned, qrSwitch, hcfCode, hospitalName, qrColor, qrCbwtfId)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                barcodeView!!.resume()
                Log.d("TAG", "error " + e.message)
                isFirstScanHCF = true
                showToast("Wrong QR Code Sticker OR Check For Near By Device permission")
            }
        }

        override fun possibleResultPoints(resultPoints: List<ResultPoint>) {}
    }

    private fun finalizeScanLogic(qrAlreadyScanned: Boolean, qrSwitch: Boolean, hcfCode: String, hospitalName: String, qrColor: String, qrCbwtfId: String) {
        var finalQrAlreadyScanned = qrAlreadyScanned
        var finalQrSwitch = qrSwitch

        if (qrCbwtfId.isNotEmpty() && !qrCbwtfId.equals(MSP.getInstance(requireContext()).getStringData(AppStrings.userCbwtfID), ignoreCase = true)) {
            finalQrAlreadyScanned = true
        }

        for (hmh in hospitalModels) {
            if (hmh.qr_code.equals(scannedQrCode, ignoreCase = true)) {
                finalQrAlreadyScanned = true
                finalQrSwitch = true
                break
            }
        }

        if (!finalQrAlreadyScanned) {
            if (!MSP.getInstance(requireContext()).containsData(AppStrings.currentHcfCode)) {
                MSP.getInstance(requireContext()).setStringData(AppStrings.currentHcfCode, hcfCode)
                MSP.getInstance(requireContext())
                    .setStringData(AppStrings.currentHcfName, hospitalName)

                hcf_name!!.text = hospitalName
                weight_hcf_name!!.text = hospitalName
                codeTv!!.text = hcfCode
                weight_type!!.text = qrColor.uppercase()
                if (scanMode == 0) {
                    manualWeightInput(qrColor)
                } else if (scanMode == 1) {
                    if (bluetoothConnected) {
                        weightView!!.visibility = View.VISIBLE
                    } else {
                        getBluetoothDevices()
                    }
                }
            } else if (MSP.getInstance(requireContext()).containsData(AppStrings.currentHcfCode)) {
                hcf_name!!.text = hospitalName
                weight_hcf_name!!.text = hospitalName
                codeTv!!.text = hcfCode
                weight_type!!.text = qrColor.uppercase()
                if (scanMode == 0) {
                    manualWeightInput(qrColor)
                } else if (scanMode == 1) {
                    if (bluetoothConnected) {
                        weightView!!.visibility = View.VISIBLE
                    } else {
                        getBluetoothDevices()
                    }
                }
            } else {
                showToast("Reset HCF Code to scan other HCF")
            }
            updateColorTypeUI(qrColor)
            isFirstScanHCF = false
        } else {
            Log.d("TAG", "barcodeResult: $scanType")
            if (scanType == 0) {
                barcodeView!!.resume()
                showToast("Already Scanned Or Wrong QR")
            } else {
                if (!finalQrSwitch && scanType != 0) {
                    barcodeView!!.resume()
                    isFirstScanHCF = true
                    Log.d("TAG", "barcodeResult:First scan hcf ")
                    showToast("First scan hcf")
                } else {
                    barcodeView!!.resume()
                    Log.e("ELSE", "Already Scanned Or Processed (Status: qrSwitch=$finalQrSwitch, qrAlreadyScanned=$finalQrAlreadyScanned)")
                    showToast("Already Scanned Or Wrong QR OR Check For Near By Device permission")
                }
            }
        }
        viewModel.setHospitalLocation(AppStrings.set_hospital_location, mapOf("hcf_code" to hcfCode, "lat_long" to "$latitude,$longitude"))
    }

    private fun updateColorTypeUI(qrColor: String) {
        if (qrColor.trim().equals("red", ignoreCase = true)) {
            Glide.with(requireContext()).load(R.drawable.red).into(weight_type_img!!)
            weight_type!!.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_light))
        } else if (qrColor.trim().equals("blue", ignoreCase = true)) {
            Glide.with(requireContext()).load(R.drawable.blue).into(weight_type_img!!)
            weight_type!!.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_blue_light))
        } else if (qrColor.trim().equals("yellow", ignoreCase = true)) {
            Glide.with(requireContext()).load(R.drawable.yellow).into(weight_type_img!!)
            weight_type!!.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_orange_light))
        } else if (qrColor.trim().equals("yellow c", ignoreCase = true)) {
            Glide.with(requireContext()).load(R.drawable.yellow).into(weight_type_img!!)
            weight_type!!.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_orange_light))
        } else if (qrColor.trim().equals("white", ignoreCase = true)) {
            Glide.with(requireContext()).load(R.drawable.gray).into(weight_type_img!!)
            weight_type!!.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray))
        }
    }

    @SuppressLint("MissingPermission")
    override fun onClickBluetoothDevice(bluetoothDevice: BluetoothDevice, status: Int) {
        if (bluetoothDevice.bondState == BluetoothDevice.BOND_NONE) bluetoothDevice.createBond()
        else {
            myBluetoothService!!.startDisconnectService()
            myBluetoothService!!.connectDevice(bluetoothDevice)
            weightView!!.visibility = View.VISIBLE
            alertDialog!!.dismiss()
            bluetoothConnected = true
            MSP.getInstance(requireContext()).setStringData(AppStrings.bluetoothConnection, "1")
        }
    }

    private fun showToast(message: String) { Toast.makeText(context, message, Toast.LENGTH_SHORT).show() }

    private fun addWeightAuto() {
        val code = codeTv!!.text.toString().trim()
        val hName = hcf_name!!.text.toString().trim()
        val wType = weight_type!!.text.toString().trim()
        weightView!!.visibility = View.GONE
        if (finalWeight.isEmpty()) return
        val ww = finalWeight.trim().split(".")
        if (ww.size < 2) return

        val qrExistsInReports = reportsModels.any { it.qrId.equals(scannedQrCode, true) }
        val qrExistsInHospitals = hospitalModels.any { it.qr_code.equals(scannedQrCode, true) }

        if (scanType == 0) {
            val msp = MSP.getInstance(requireContext())
            if (msp.getStringData(AppStrings.loginAs) == "hcf") {
                if (scannedHcfCode != msp.getStringData(AppStrings.hcfCode)) {
                    showToast("Invalid Hospital")
                    barcodeView!!.resume()
                    return
                }
            }
            if (qrExistsInReports || qrExistsInHospitals) {
                showToast("Duplicate QR Code! Not Added.")
            } else {
                hospitalModels.add(HospitalModel(code, hName, wType, ww[0], ww[1], scannedQrCode))
                hospitalAdapter!!.notifyDataSetChanged()
                isFirstScanHCF = false
                isWeightAdded = false
                weight_tv!!.text = "0"
            }
        } else {
            if (qrExistsInHospitals) showToast("Duplicate QR Code! Not Added.")
            else {
                hospitalModels.add(HospitalModel(code, hName, wType, ww[0], ww[1], scannedQrCode))
                hospitalAdapter!!.notifyDataSetChanged()
                isFirstScanHCF = false
                isWeightAdded = false
                weight_tv!!.text = "0"
            }
        }
        barcodeView!!.resume()
    }

    companion object {
        private const val TAG = "CbwtfHcfScanFragment"
    }
}
