package com.appventurez.bmwms.cbwtf.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.location.Address
import android.location.Geocoder
import android.location.Location
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
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room

import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest

import com.bumptech.glide.Glide
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothStatus
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.zxing.BarcodeFormat
import com.google.zxing.ResultPoint
import com.google.zxing.client.android.BeepManager
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import com.journeyapps.barcodescanner.camera.CameraSettings
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import com.mukesh.OnOtpCompletionListener
import com.mukesh.OtpView
import com.appventurez.bmwms.R
import com.appventurez.bmwms.adapters.BluetoothDevicesAdapter
import com.appventurez.bmwms.bluetooth.MyBluetoothService
import com.appventurez.bmwms.cbwtf.adapters.HospitalAdapter
import com.appventurez.bmwms.cbwtf.models.ReportsModel
import com.appventurez.bmwms.classes.AppStrings
import com.appventurez.bmwms.classes.MSP
import com.appventurez.bmwms.classes.VolleySingleton
import com.appventurez.bmwms.database.HospitalDao
import com.appventurez.bmwms.database.HospitalModel
import com.appventurez.bmwms.database.MyDatabase
import com.appventurez.bmwms.models.BluetoothDevicesModel

import org.json.JSONObject
import java.text.DecimalFormat
import java.util.ArrayList
import java.util.Arrays
import java.util.Date
import java.util.HashMap
import java.util.Objects

import me.aflak.bluetooth.Bluetooth

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

    //Bluetooth
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

            getQRData(AppStrings.get_all_report)

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
                    val scanOptions = ScanOptions()
                    scanOptions.setOrientationLocked(true)
                    scanOptions.setBarcodeImageEnabled(true)
                    scanOptions.setPrompt("Scan Waste Barcode")
                    // launcher.launch(scanOptions);
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
                Log.d("TAG", "onClick: HCF Scan ")
                if (dataSize < hospitalModels.size) {
                    if (loading_ll != null) loading_ll!!.visibility = View.VISIBLE

                    val date = DateFormat.format("yyyy-MM-dd", Date().time).toString()

                    if (scanType == 0) {
                        if (MSP.getInstance(requireContext()).getStringData(AppStrings.loginAs) == "hcf") {
                            val map: MutableMap<String, String> = HashMap()
                            map["weight"] = hospitalModels[dataSize].waste_weight + "." + hospitalModels[dataSize].waste_weight_g
                            map["admin_id"] = MSP.getInstance(requireContext()).getStringData(AppStrings.userCbwtfID)
                            map["handhover_address"] = "$latitude,$longitude"
                            map["type"] = hospitalModels[dataSize].waste_color
                            map["hospital_id"] = hospitalModels[dataSize].hcf_code
                            map["seq_no"] = hospitalModels[dataSize].qr_code
                            map["year"] = date
                            map["attenden_status"] = "collected"
                            cbwtfScanSubmitRequest(AppStrings.hcf_scan_submit_hcf, map)
                        } else {
                            val map: MutableMap<String, String> = HashMap()
                            map["weight"] = hospitalModels[dataSize].waste_weight + "." + hospitalModels[dataSize].waste_weight_g
                            map["admin_id"] = MSP.getInstance(requireContext()).getStringData(AppStrings.userCbwtfID)
                            map["operator_name"] = MSP.getInstance(requireContext()).getStringData(AppStrings.userID)
                            map["handhover_address"] = "$latitude,$longitude"
                            map["type"] = hospitalModels[dataSize].waste_color
                            map["hospital_id"] = hospitalModels[dataSize].hcf_code
                            map["seq_no"] = hospitalModels[dataSize].qr_code
                            map["year"] = date
                            map["attenden_status"] = "collected"

                            deleteQRData(hospitalModels[dataSize].qr_code, map)
                        }
                    } else {
                        val map: MutableMap<String, String> = HashMap()
                        map["weight"] = hospitalModels[dataSize].waste_weight + "." + hospitalModels[dataSize].waste_weight_g
                        map["receiving_date"] = date
                        map["operator_id"] = MSP.getInstance(requireContext()).getStringData(AppStrings.userID)
                        map["receiving_address"] = "$latitude,$longitude"
                        map["color"] = hospitalModels[dataSize].waste_color
                        map["hospital_id"] = hospitalModels[dataSize].hcf_code
                        map["seq_no"] = hospitalModels[dataSize].qr_code

                        cbwtfScanSubmitRequest(AppStrings.cbwtf_scan_submit, map)
                    }
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

                if (scanType == 0) {
                    if (MSP.getInstance(requireContext()).getStringData(AppStrings.loginAs) == "cbwtf") {
                        if (MSP.getInstance(requireContext()).getStringData(AppStrings.attendance_compulsory) == "1") {
                            submit_button!!.visibility = View.GONE
                            scanButton!!.visibility = View.GONE
                            attendanceButton!!.visibility = View.VISIBLE
                            attendanceCard!!.visibility = View.VISIBLE
                            attendanceStatusTv!!.text = "Pending"
                            attendanceStatusTv!!.setTextColor(requireActivity().resources.getColor(R.color.yellow))
                            attendanceStatusTv!!.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_pending_actions_24, 0)
                            setTextViewDrawableColor(attendanceStatusTv!!, R.color.yellow)
                        }
                    }
                }
            }

            attendanceButton!!.setOnClickListener {
                if (getCameraPermission()) {
                    val scanOptions = ScanOptions()
                    scanOptions.setOrientationLocked(true)
                    scanOptions.setBarcodeImageEnabled(true)
                    scanOptions.setPrompt("Scan Attendance Barcode")
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

            if (scanType == 0) {
                if (MSP.getInstance(requireContext()).getStringData(AppStrings.loginAs) == "cbwtf") {
                    if (MSP.getInstance(requireContext()).getStringData(AppStrings.attendance_compulsory) == "1") {
                        submit_button!!.visibility = View.GONE
                        scanButton!!.visibility = View.GONE
                        attendanceButton!!.visibility = View.VISIBLE
                        attendanceCard!!.visibility = View.VISIBLE
                        attendanceStatusTv!!.text = "Pending"
                        attendanceStatusTv!!.setTextColor(requireActivity().resources.getColor(R.color.yellow))
                        attendanceStatusTv!!.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_pending_actions_24, 0)
                        setTextViewDrawableColor(attendanceStatusTv!!, R.color.yellow)
                    }
                }
            }
        }
        return rootView
    }

    var attendanceScanner: ActivityResultLauncher<ScanOptions> = registerForActivityResult(ScanContract(), ActivityResultCallback { result ->
        if (result.contents != null) {
            try {
                val data = result.contents.split("&".toRegex(), 2).toTypedArray()
                val hcfCode = data[0]
                val data1 = data[1].split("/".toRegex(), 3).toTypedArray()
                val hospitalName = data1[1]
                val qrCbwtfId = data1[2]
                val data2 = data1[0].split("-".toRegex(), 2).toTypedArray()
                val qrCode = data2[0]
                val qrColor = data2[1]

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
                Log.e(TAG, "Exception: ", e)
                showToast("Wrong QR Code 7")
            }
        }
    })

    fun manualWeightInput(w_color: String) {
        val manualWeightAlert = AlertDialog.Builder(requireContext()).create()
        val manualWeightView = LayoutInflater.from(requireContext()).inflate(R.layout.manual_weight_view, null, false)

        val manualWeightColor = manualWeightView.findViewById<TextView>(R.id.manual_weight_color)
        val manualWeightImage = manualWeightView.findViewById<ImageView>(R.id.manual_weight_image)
        val manualWeightKilo = manualWeightView.findViewById<EditText>(R.id.manual_weight_kilo)
        val manualWeightGram = manualWeightView.findViewById<EditText>(R.id.manual_weight_gram)
        val manualWeightAdd = manualWeightView.findViewById<MaterialButton>(R.id.manual_weight_add)

        manualWeightColor.text = w_color.uppercase()

        if (w_color.trim().equals("red", ignoreCase = true)) {
            Glide.with(requireContext()).load(R.drawable.red).into(manualWeightImage)
            manualWeightColor.setTextColor(requireActivity().resources.getColor(android.R.color.holo_red_light))
        } else if (w_color.trim().equals("blue", ignoreCase = true)) {
            Glide.with(requireContext()).load(R.drawable.blue).into(manualWeightImage)
            manualWeightColor.setTextColor(requireActivity().resources.getColor(android.R.color.holo_blue_light))
        } else if (w_color.trim().equals("yellow", ignoreCase = true)) {
            Glide.with(requireContext()).load(R.drawable.yellow).into(manualWeightImage)
            manualWeightColor.setTextColor(requireActivity().resources.getColor(android.R.color.holo_orange_light))
        } else if (w_color.trim().equals("yellow c", ignoreCase = true)) {
            Glide.with(requireContext()).load(R.drawable.yellow).into(manualWeightImage)
            manualWeightColor.setTextColor(requireActivity().resources.getColor(android.R.color.holo_orange_light))
        } else if (w_color.trim().equals("white", ignoreCase = true)) {
            Glide.with(requireContext()).load(R.drawable.gray).into(manualWeightImage)
            manualWeightColor.setTextColor(requireActivity().resources.getColor(android.R.color.darker_gray))
        }

        manualWeightAdd.setOnClickListener {
            val code = codeTv!!.text.toString()
            val hName = hcf_name!!.text.toString()
            val wType = manualWeightColor.text.toString()

            val w_kilo = manualWeightKilo.text.toString().trim()
            val w_gram = manualWeightGram.text.toString().trim()

            if (w_kilo.isNotEmpty() && w_gram.isNotEmpty()) {
                val d_kilo = w_kilo.toInt()
                val d_gram = w_gram.toInt()

                val decimalKilo = String.format("%03d", d_kilo)
                val decimalGram = String.format("%03d", d_gram)

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

    fun getHospitalData(id: String, nameHos: String) {
        val stringRequest = object : StringRequest(Method.POST, AppStrings.hospital_data, Response.Listener { response ->
            try {
                val jsonObject = JSONObject(response)
                if (jsonObject.get("status").toString().equals("success", ignoreCase = true)) {
                    val name = jsonObject.getJSONArray("data").getJSONObject(0).get("name").toString()
                    if (nameHos.isEmpty()) {
                        checkAttendance(id, name)
                    } else {
                        checkAttendance(id, nameHos)
                    }
                } else {
                    showToast("Wrong QR Code 5")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception: ", e)
            }
        }, Response.ErrorListener { }) {
            override fun getParams(): Map<String, String> {
                val map: MutableMap<String, String> = HashMap()
                map["hospital_code"] = id
                return map
            }
        }
        VolleySingleton.getInstance(rootView!!.context).addToRequestQueue(stringRequest)
    }

    fun getCameraPermission(): Boolean {
        Dexter.withContext(requireContext()).withPermission(Manifest.permission.CAMERA).withListener(object : PermissionListener {
            override fun onPermissionGranted(permissionGrantedResponse: PermissionGrantedResponse) {
                isCameraPermissionGranted = true
            }

            override fun onPermissionDenied(permissionDeniedResponse: PermissionDeniedResponse) {
                isCameraPermissionGranted = false
            }

            override fun onPermissionRationaleShouldBeShown(permissionRequest: PermissionRequest?, permissionToken: PermissionToken?) {
                permissionToken?.continuePermissionRequest()
            }
        }).check()

        return isCameraPermissionGranted
    }

    fun checkLocationPermission() {
        Dexter.withContext(requireContext()).withPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(multiplePermissionsReport: MultiplePermissionsReport) {
                if (multiplePermissionsReport.areAllPermissionsGranted()) {
                    checkLocationSetting()
                } else {
                    AlertDialog.Builder(requireContext())
                        .setMessage("Grant location permission to continue\nApp permissions > Location > Allow")
                        .setPositiveButton("Setting") { _, _ ->
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            val uri = Uri.fromParts("package", requireActivity().packageName, null)
                            intent.data = uri
                            startActivity(intent)
                        }.show()
                }
            }

            override fun onPermissionRationaleShouldBeShown(list: MutableList<PermissionRequest>?, permissionToken: PermissionToken?) {
                permissionToken?.continuePermissionRequest()
            }
        }).check()
    }

    @SuppressLint("MissingPermission")
    fun checkLocationSetting() {
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = (30 * 1000).toLong()
        locationRequest.fastestInterval = (5 * 1000).toLong()

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)

        val task = LocationServices.getSettingsClient(requireContext()).checkLocationSettings(builder.build())

        task.addOnCompleteListener { task1 ->
            try {
                task1.getResult(ApiException::class.java)
                isLocationGranted = true
                fusedLocationProviderClient!!.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, object : CancellationToken() {
                    override fun isCancellationRequested(): Boolean {
                        return false
                    }

                    override fun onCanceledRequested(onTokenCanceledListener: OnTokenCanceledListener): CancellationToken {
                        return this
                    }
                }).addOnSuccessListener { location ->
                    if (location != null) {
                        latitude = location.latitude
                        longitude = location.longitude
                        Log.d("LocationCheck", "Fetched location: Lat=$latitude, Long=$longitude")
                        try {
                            val addresses = geocoder!!.getFromLocation(latitude, longitude, 5)
                            if (addresses != null && addresses.isNotEmpty()) {
                                location_tv!!.text = addresses[0].getAddressLine(0)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Exception: ", e)
                        }
                    }
                }
            } catch (exception: ApiException) {
                when (exception.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                        val resolvable = exception as ResolvableApiException
                        resolvable.startResolutionForResult(requireActivity(), 1000)
                    } catch (e: Exception) {
                        Log.e(TAG, "ClassCastException: ", e)
                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> { }
                }
            }
        }
    }

    fun getBluetoothDevices() {
        bluetoothDevicesModel.clear()
        bluetooth!!.onStart()

        val bluetoothDevices = bluetooth!!.pairedDevices
        alertDialog = AlertDialog.Builder(requireContext()).create()

        val bl_view = LayoutInflater.from(requireContext()).inflate(R.layout.blutooth_view, null, false)
        val closeAlert = bl_view.findViewById<ImageView>(R.id.alert_dismiss)
        blRv = bl_view.findViewById(R.id.bluetooth_devices_rv)

        alertDialog!!.setView(bl_view)
        alertDialog!!.setCancelable(false)

        blRv!!.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        blRv!!.adapter = bluetoothDevicesAdapter

        closeAlert.setOnClickListener {
            barcodeView!!.resume()
            alertDialog!!.dismiss()
        }

        alertDialog!!.show()

        for (i in bluetoothDevices.indices) {
            bluetoothDevicesModel.add(BluetoothDevicesModel(bluetoothDevices[i], 1))
            bluetoothDevicesAdapter!!.notifyDataSetChanged()
        }

        Dexter.withContext(requireContext()).withPermissions(*permissions).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(multiplePermissionsReport: MultiplePermissionsReport) {
                statusCheck()
            }

            override fun onPermissionRationaleShouldBeShown(list: MutableList<PermissionRequest>?, permissionToken: PermissionToken?) { }
        }).check()
    }

    override fun getDevices(device: BluetoothDevice) {
        bluetoothDevicesModel.add(BluetoothDevicesModel(device, 0))
        bluetoothDevicesAdapter!!.notifyDataSetChanged()
    }

    override fun getData(data: String) {
        Log.d("TAG", "getData: $data")
        finalWeight = data
        weight_tv!!.text = data
        if (isWeightAdded == true) {
            isWeightAdded = false
            if (weight_tv!!.text.toString().trim() != "0") {
                Handler(Looper.getMainLooper()).postDelayed({
                    Log.d("TAG", "Checking isFirstScanHCF before addWeightAuto: $isFirstScanHCF")
                    if (isFirstScanHCF == false) {
                        addWeightAuto()
                    } else {
                        Log.d("TAG", "isFirstScanHCF is true, skipping addWeightAuto.")
                    }
                }, 1000)
            }
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
            buildAlertMessageNoGps()
        } else {
            if (bluetooth!!.isEnabled) {
                bluetooth!!.onStop()
                myBluetoothService!!.startScanService()
            } else {
                bluetooth!!.enable()
                bluetooth!!.onStop()
                myBluetoothService!!.startScanService()
            }
        }
    }

    private fun buildAlertMessageNoGps() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage("Enable GPS")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ -> startActivity(newIntentForLocationSource()) }
        val alert = builder.create()
        alert.show()
    }

    private fun newIntentForLocationSource(): Intent {
        return Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
    }

    override fun updateUI() {
        var countBags = 0
        var countWeight = 000.000

        if (hospitalModels.isNotEmpty()) {
            for (hm in hospitalModels) {
                countBags++
                val weightData = hm.waste_weight.trim() + "." + hm.waste_weight_g.trim()
                countWeight += weightData.toDouble()
            }
            empty_tv!!.visibility = View.GONE
        } else {
            countWeight = 0.0
            countBags = 0
            empty_tv!!.visibility = View.VISIBLE
        }

        total_bags!!.text = countBags.toString()
        total_waste_weight!!.text = DecimalFormat("000.000").format(countWeight)
    }

    fun cbwtfScanSubmitRequest(url: String, map: Map<String, String>) {
        Log.d("TAG", "cbwtfScanSubmitRequest: SCAN")
        val request = object : StringRequest(Method.POST, url, Response.Listener { response ->
            try {
                val jsonObject = JSONObject(response)
                if (jsonObject.get("status").toString().equals("success", ignoreCase = true)) {
                    dataSize++
                    if (dataSize < hospitalModels.size) {
                        val date = DateFormat.format("yyyy-MM-dd", Date().time).toString()
                        if (scanType == 0) {
                            if (MSP.getInstance(requireContext()).getStringData(AppStrings.loginAs) == "hcf") {
                                val nextMap: MutableMap<String, String> = HashMap()
                                nextMap["weight"] = hospitalModels[dataSize].waste_weight + "." + hospitalModels[dataSize].waste_weight_g
                                nextMap["admin_id"] = MSP.getInstance(requireContext()).getStringData(AppStrings.userCbwtfID)
                                nextMap["handhover_address"] = "$latitude,$longitude"
                                nextMap["type"] = hospitalModels[dataSize].waste_color
                                nextMap["hospital_id"] = hospitalModels[dataSize].hcf_code.trim()
                                nextMap["seq_no"] = hospitalModels[dataSize].qr_code
                                nextMap["year"] = date
                                nextMap["attenden_status"] = "empty"
                                cbwtfScanSubmitRequest(AppStrings.hcf_scan_submit_hcf, nextMap)
                            } else {
                                val nextMap: MutableMap<String, String> = HashMap()
                                nextMap["weight"] = hospitalModels[dataSize].waste_weight + "." + hospitalModels[dataSize].waste_weight_g
                                nextMap["admin_id"] = MSP.getInstance(requireContext()).getStringData(AppStrings.userCbwtfID)
                                nextMap["operator_name"] = MSP.getInstance(requireContext()).getStringData(AppStrings.userID)
                                nextMap["handhover_address"] = "$latitude,$longitude"
                                nextMap["type"] = hospitalModels[dataSize].waste_color
                                nextMap["hospital_id"] = hospitalModels[dataSize].hcf_code.trim()
                                nextMap["seq_no"] = hospitalModels[dataSize].qr_code
                                nextMap["year"] = date
                                nextMap["attenden_status"] = "collected"
                                deleteQRData(hospitalModels[dataSize].qr_code, nextMap)
                            }
                        } else {
                            val nextMap: MutableMap<String, String> = HashMap()
                            nextMap["weight"] = hospitalModels[dataSize].waste_weight + "." + hospitalModels[dataSize].waste_weight_g
                            nextMap["receiving_date"] = date
                            nextMap["operator_id"] = MSP.getInstance(requireContext()).getStringData(AppStrings.userID)
                            nextMap["receiving_address"] = "$latitude,$longitude"
                            nextMap["color"] = hospitalModels[dataSize].waste_color
                            nextMap["hospital_id"] = hospitalModels[dataSize].hcf_code.trim()
                            nextMap["seq_no"] = hospitalModels[dataSize].qr_code
                            cbwtfScanSubmitRequest(AppStrings.cbwtf_scan_submit, nextMap)
                        }
                    }

                    if (dataSize == hospitalModels.size) {
                        if (loading_ll != null) loading_ll!!.visibility = View.GONE
                        showToast("Submitted")
                        total_bags!!.text = "0"
                        total_waste_weight!!.text = "000.000"
                        dataSize = 0
                        hospitalModels.clear()
                        hospitalAdapter!!.notifyDataSetChanged()
                        getQRData(AppStrings.get_all_report)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception: ", e)
                if (loading_ll != null) loading_ll!!.visibility = View.GONE
            }
        }, Response.ErrorListener { error ->
            VolleySingleton.logVolleyError("CbwtfHcfScanFragment", error)
            if (loading_ll != null) loading_ll!!.visibility = View.GONE
        }) {
            override fun getParams(): Map<String, String> {
                return map
            }
        }
        VolleySingleton.getInstance(requireActivity()).addToRequestQueue(request)
    }

    fun getQRData(url: String) {
        val request = StringRequest(Request.Method.POST, url, Response.Listener { response ->
            try {
                val jsonObject = JSONObject(response)
                if (jsonObject.get("status").toString().equals("success", ignoreCase = true)) {
                    reportsModels.clear()
                    val size = jsonObject.getJSONArray("data").length()
                    for (i in 0 until size) {
                        val jsonObject1 = jsonObject.getJSONArray("data").getJSONObject(i)
                        val model = ReportsModel()
                        model.qrId = jsonObject1.get("qr_id").toString()
                        model.type = jsonObject1.get("type").toString()
                        reportsModels.add(model)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception: ", e)
            }
        }, Response.ErrorListener { error -> VolleySingleton.logVolleyError("CbwtfHcfScanFragment", error) })

        VolleySingleton.getInstance(requireContext()).addToRequestQueue(request)
    }

    fun deleteQRData(qrId: String, map: Map<String, String>) {
        val request = object : StringRequest(Method.POST, AppStrings.delete_qr, Response.Listener { response ->
            try {
                val jsonObject = JSONObject(response)
                if (jsonObject.get("status").toString().equals("success", ignoreCase = true)) {
                    cbwtfScanSubmitRequest(AppStrings.hcf_scan_submit, map)
                } else {
                    cbwtfScanSubmitRequest(AppStrings.hcf_scan_submit, map)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception: ", e)
            }
        }, Response.ErrorListener { error -> VolleySingleton.logVolleyError("CbwtfHcfScanFragment", error) }) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["qr_id"] = qrId
                return params
            }
        }
        VolleySingleton.getInstance(requireContext()).addToRequestQueue(request)
    }

    fun getOtp(otp_value: String, dialog: AlertDialog, errorText: TextView, mOtpView: OtpView, h_cOde: String) {
        val otpRequest = object : StringRequest(Method.POST, AppStrings.get_otp, Response.Listener { response ->
            try {
                val otpObject = JSONObject(response)
                if (otpObject.get("status").toString().equals("success", ignoreCase = true)) {
                    dialog.dismiss()
                    val otp = otpObject.getJSONArray("data").getJSONObject(0).get("otp").toString()
                    MSP.getInstance(requireContext()).setStringData(h_cOde, otp)
                    markAttendance(h_cOde)
                } else {
                    dialog.dismiss()
                    dialog.show()
                    errorText.text = "Wrong OTP"
                    mOtpView.setText("")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception: ", e)
            }
        }, Response.ErrorListener { }) {
            override fun getParams(): Map<String, String> {
                val otp: MutableMap<String, String> = HashMap()
                otp["hcf_code"] = h_cOde
                otp["otp"] = otp_value
                otp["date"] = DateFormat.format("yyyy-MM-dd", Date().time).toString()
                return otp
            }
        }
        VolleySingleton.getInstance(requireContext()).addToRequestQueue(otpRequest)
    }

    fun checkAttendance(mHospitalCode: String, hos_name: String) {
        val checkAttendanceRequest = object : StringRequest(Method.POST, AppStrings.check_attendance, Response.Listener { response ->
            try {
                val otpObject = JSONObject(response)
                if (otpObject.get("status").toString().equals("success", ignoreCase = true)) {
                    submit_button!!.visibility = View.VISIBLE
                    scanButton!!.visibility = View.VISIBLE
                    attendanceButton!!.visibility = View.GONE
                    attendanceCard!!.visibility = View.VISIBLE
                    attendanceStatusTv!!.text = "Done"
                    attendanceStatusTv!!.setTextColor(ContextCompat.getColor(requireContext(), R.color.green))
                    attendanceStatusTv!!.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_check_24, 0)
                    setTextViewDrawableColor(attendanceStatusTv!!, R.color.green)
                } else {
                    val otpAlertDialog = AlertDialog.Builder(requireContext()).create()
                    val otpView = LayoutInflater.from(requireContext()).inflate(R.layout.otp_view, null, false)
                    otpAlertDialog.setView(otpView)
                    val mOtp = otpView.findViewById<OtpView>(R.id.otp_view)
                    val otpButton = otpView.findViewById<MaterialButton>(R.id.otp_submit)
                    val otpName = otpView.findViewById<TextView>(R.id.otp_name)
                    val otpError = otpView.findViewById<TextView>(R.id.otp_error)
                    otpName.text = hos_name
                    otpAlertDialog.setOnDismissListener {
                        if (loading_ll != null) loading_ll!!.visibility = View.GONE
                    }
                    mOtp.setOtpCompletionListener { otp ->
                        otpButton.setOnClickListener {
                            otpError.text = ""
                            getOtp(otp, otpAlertDialog, otpError, mOtp, mHospitalCode)
                        }
                    }
                    otpAlertDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
                    otpAlertDialog.show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception: ", e)
            }
        }, Response.ErrorListener { }) {
            override fun getParams(): Map<String, String> {
                val param: MutableMap<String, String> = HashMap()
                param["hcf_code"] = mHospitalCode
                param["operator_id"] = MSP.getInstance(requireContext()).getStringData(AppStrings.userID)
                param["date"] = DateFormat.format("yyyy-MM-dd", Date().time).toString()
                return param
            }
        }
        VolleySingleton.getInstance(requireContext()).addToRequestQueue(checkAttendanceRequest)
    }

    fun markAttendance(mHospitalCode: String) {
        val markAttendanceRequest = object : StringRequest(Method.POST, AppStrings.mark_attendance, Response.Listener { response ->
            try {
                val otpObject = JSONObject(response)
                if (otpObject.get("status").toString().equals("success", ignoreCase = true)) {
                    submit_button!!.visibility = View.VISIBLE
                    scanButton!!.visibility = View.VISIBLE
                    attendanceButton!!.visibility = View.GONE
                    attendanceCard!!.visibility = View.VISIBLE
                    attendanceStatusTv!!.text = "Done"
                    attendanceStatusTv!!.setTextColor(ContextCompat.getColor(requireContext(), R.color.green))
                    attendanceStatusTv!!.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_check_24, 0)
                    setTextViewDrawableColor(attendanceStatusTv!!, R.color.green)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception: ", e)
            }
        }, Response.ErrorListener { }) {
            override fun getParams(): Map<String, String> {
                val param: MutableMap<String, String> = HashMap()
                param["hcf_code"] = mHospitalCode
                param["operator_id"] = MSP.getInstance(requireContext()).getStringData(AppStrings.userID)
                param["cbwtf_id"] = MSP.getInstance(requireContext()).getStringData(AppStrings.userCbwtfID)
                param["date"] = DateFormat.format("yyyy-MM-dd", Date().time).toString()
                param["lat_long"] = "$latitude,$longitude"
                return param
            }
        }
        VolleySingleton.getInstance(requireContext()).addToRequestQueue(markAttendanceRequest)
    }

    private fun setTextViewDrawableColor(textView: TextView, color: Int) {
        for (drawable in textView.compoundDrawables) {
            if (drawable != null) {
                drawable.colorFilter = PorterDuffColorFilter(ContextCompat.getColor(textView.context, color), PorterDuff.Mode.SRC_IN)
            }
        }
    }

    private fun updateHospitalLocationForOnce(mHospitalCode: String) {
        val markAttendanceRequest = object : StringRequest(Method.POST, AppStrings.set_hospital_location, Response.Listener { response ->
            try {
                val otpObject = JSONObject(response)
                if (otpObject.get("status").toString().equals("success", ignoreCase = true)) {
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception: ", e)
            }
        }, Response.ErrorListener { }) {
            override fun getParams(): Map<String, String> {
                val param: MutableMap<String, String> = HashMap()
                param["hcf_code"] = mHospitalCode
                param["lat_long"] = "$latitude,$longitude"
                return param
            }
        }
        VolleySingleton.getInstance(requireContext()).addToRequestQueue(markAttendanceRequest)
    }

    private fun barcode(view: View) {
        barcodeView = view.findViewById(R.id.barcode_scanner)

        val settings = CameraSettings()
        settings.isContinuousFocusEnabled = true
        barcodeView!!.cameraSettings = settings

        val formats: Collection<BarcodeFormat> = Arrays.asList(BarcodeFormat.QR_CODE, BarcodeFormat.CODE_39)
        barcodeView!!.barcodeView.decoderFactory = DefaultDecoderFactory(formats)
        barcodeView!!.initializeFromIntent(requireActivity().intent)
        barcodeView!!.decodeContinuous(callback)

        beepManager = BeepManager(requireActivity())
    }

    override fun onResume() {
        super.onResume()
        if (barcodeView != null) {
            barcodeView!!.resume()
        }
    }

    override fun onPause() {
        super.onPause()
        if (barcodeView != null) {
            barcodeView!!.pause()
        }
    }

    interface RescanCallback {
        fun onResult(qrAlreadyScanned: Boolean, qrSwitch: Boolean, data: JSONObject)
    }

    private val callback: BarcodeCallback = object : BarcodeCallback {
        override fun barcodeResult(result: BarcodeResult) {
            if (result.text == null || result.text == lastText) {
                return
            }

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
                        getRescanObjectData(scannedQrCode, object : RescanCallback {
                            override fun onResult(qrAlreadyScanned: Boolean, qrSwitch: Boolean, data: JSONObject) {
                                val hCode = data.optString("hospital_code", hcfCode)
                                val hName = data.optString("name", hospitalName)
                                val qCol = data.optString("color_type_hcf", qrColor)
                                finalizeScanLogic(qrAlreadyScanned, qrSwitch, hCode, hName, qCol, qrCbwtfId)
                            }
                        })
                    } else {
                        finalizeScanLogic(qrAlreadyScanned, qrSwitch, hcfCode, hospitalName, qrColor, qrCbwtfId)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                barcodeView!!.resume()
                Log.d(TAG, "error " + e.message)
                isFirstScanHCF = true
                showToast("Wrong QR Code Sticker OR Check For Near By Device permission")
            }
        }

        private fun finalizeScanLogic(qrAlreadyScanned: Boolean, qrSwitch: Boolean, hcfCode: String, hospitalName: String, qrColor: String, qrCbwtfId: String) {
            var finalQrAlreadyScanned = qrAlreadyScanned
            var finalQrSwitch = qrSwitch

            if (qrCbwtfId != MSP.getInstance(requireContext()).getStringData(AppStrings.userCbwtfID)) {
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
                    MSP.getInstance(requireContext()).setStringData(AppStrings.currentHcfName, hospitalName)
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
                    showToast("Reset HCF Code to scan other HCF", Toast.LENGTH_LONG)
                }

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

            updateHospitalLocationForOnce(hcfCode)
        }

        override fun possibleResultPoints(resultPoints: List<ResultPoint>) {}
    }

    @SuppressLint("MissingPermission")
    override fun onClickBluetoothDevice(bluetoothDevice: BluetoothDevice, status: Int) {
        if (bluetoothDevice.bondState == BluetoothDevice.BOND_NONE) {
            bluetoothDevice.createBond()
        } else {
            myBluetoothService!!.startDisconnectService()
            myBluetoothService!!.connectDevice(bluetoothDevice)

            weightView!!.visibility = View.VISIBLE
            alertDialog!!.dismiss()

            bluetoothConnected = true
            MSP.getInstance(requireContext()).setStringData(AppStrings.bluetoothConnection, "1")
        }
    }

    private fun showToast(message: String) {
        if (isAdded && context != null) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showToast(message: String, duration: Int) {
        if (isAdded && context != null) {
            Toast.makeText(context, message, duration).show()
        }
    }

    private fun addWeightAuto() {
        val code = codeTv!!.text.toString().trim()
        val hName = hcf_name!!.text.toString().trim()
        val wType = weight_type!!.text.toString().trim()
        weightView!!.visibility = View.GONE

        if (finalWeight == null || finalWeight.trim().isEmpty()) {
            Log.d("TAG", "Final weight is empty, skipping addition.")
            return
        }

        val ww_ww = finalWeight.trim().split("\\.".toRegex()).toTypedArray()

        if (ww_ww.size < 2) {
            Log.d("TAG", "Invalid weight format. Skipping.")
            return
        }

        var qrExistsInReports = false
        for (report in reportsModels) {
            if (report.qrId.equals(scannedQrCode, ignoreCase = true)) {
                qrExistsInReports = true
                break
            }
        }

        var qrExistsInHospitals = false
        for (model in hospitalModels) {
            if (model.qr_code.equals(scannedQrCode, ignoreCase = true)) {
                qrExistsInHospitals = true
                break
            }
        }

        if (scanType == 0) {
            if (MSP.getInstance(requireContext()).getStringData(AppStrings.loginAs) == "hcf") {
                if (scannedHcfCode == null || MSP.getInstance(requireContext()).getStringData(AppStrings.hcfCode) == null || !scannedHcfCode.equals(MSP.getInstance(requireContext()).getStringData(AppStrings.hcfCode), ignoreCase = true)) {
                    showToast("Invalid Hospital ")
                    barcodeView!!.resume()
                    return
                } else {
                    if (qrExistsInReports || qrExistsInHospitals) {
                        Log.d("TAG", "Duplicate QR code detected. Not adding.")
                        showToast("Duplicate QR Code! Not Added.")
                        return
                    } else {
                        hospitalModels.add(HospitalModel(code, hName, wType, ww_ww[0], ww_ww[1], scannedQrCode))
                        hospitalAdapter!!.notifyDataSetChanged()
                        isFirstScanHCF = false
                        isWeightAdded = false
                        weight_tv!!.text = "0"
                        barcodeView!!.resume()
                    }
                }
            } else {
                if (qrExistsInReports || qrExistsInHospitals) {
                    Log.d("TAG", "Duplicate QR code detected. Not adding.")
                    showToast("Duplicate QR Code! Not Added.")
                    return
                } else {
                    hospitalModels.add(HospitalModel(code, hName, wType, ww_ww[0], ww_ww[1], scannedQrCode))
                    hospitalAdapter!!.notifyDataSetChanged()
                    isFirstScanHCF = false
                    isWeightAdded = false
                    weight_tv!!.text = "0"
                    barcodeView!!.resume()
                }
            }
        } else {
            if (qrExistsInHospitals) {
                showToast("Duplicate QR Code! Not Added.")
                return
            } else {
                hospitalModels.add(HospitalModel(code, hName, wType, ww_ww[0], ww_ww[1], scannedQrCode))
                hospitalAdapter!!.notifyDataSetChanged()
                isFirstScanHCF = false
                isWeightAdded = false
                weight_tv!!.text = "0"
                barcodeView!!.resume()
            }
        }
        Log.d("TAG", "Adding new QR code to hospitalModels.")
    }

    fun getRescanObjectData(qrcode: String, callback: RescanCallback) {
        val request = object : StringRequest(Method.POST, AppStrings.get_qr_data, Response.Listener { response ->
            try {
                val jsonObject = JSONObject(response)
                if (jsonObject.get("status").toString().equals("success", ignoreCase = true)) {
                    val data = jsonObject.getJSONObject("data")
                    val cbwtfWeight = data.optString("cbwtf_weight")
                    val hcfWeight = data.optString("hcf_weight")

                    var qrSwitch = false
                    var qrAlreadyScanned = true

                    if (hcfWeight != null && hcfWeight != "0" && hcfWeight != "0.0" && hcfWeight != "0.00" && hcfWeight != "0.000") {
                        qrSwitch = true
                    }
                    if (cbwtfWeight != null && (cbwtfWeight == "0" || cbwtfWeight == "0.0" || cbwtfWeight == "0.00" || cbwtfWeight == "0.000")) {
                        qrAlreadyScanned = false
                    }

                    callback.onResult(qrAlreadyScanned, qrSwitch, data)
                } else {
                    showToast("QR Data Not Found")
                    barcodeView!!.resume()
                }
            } catch (e: Exception) {
                Log.d(TAG, "Exception " + e.message)
                barcodeView!!.resume()
            }
        }, Response.ErrorListener { barcodeView!!.resume() }) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["qr_code"] = qrcode
                return params
            }
        }
        VolleySingleton.getInstance(requireContext()).addToRequestQueue(request)
    }

    companion object {
        private const val TAG = "CbwtfHcfScanFragment"
    }
}
