package com.appventurez.bmwms.cbwtf.fragments

import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appventurez.bmwms.R
import com.appventurez.bmwms.cbwtf.adapters.ReportsAdapter
import com.appventurez.bmwms.cbwtf.models.ReportsModel
import com.appventurez.bmwms.classes.AppStrings
import com.appventurez.bmwms.classes.MSP
import com.appventurez.bmwms.network.RetrofitClient
import com.appventurez.bmwms.repository.ReportRepository
import com.appventurez.bmwms.viewmodel.ReportViewModel
import com.facebook.shimmer.ShimmerFrameLayout
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ReportsFragment : Fragment() {

    private val viewModel: ReportViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ReportViewModel(ReportRepository(RetrofitClient.apiService)) as T
            }
        }
    }

    private lateinit var reportsRV: RecyclerView
    private var reportsModels = ArrayList<ReportsModel>()
    private var originalAggregatedModels = ArrayList<ReportsModel>()
    private lateinit var reportsAdapter: ReportsAdapter
    private lateinit var backButton: ImageView
    private lateinit var emptyView: TextView
    private lateinit var txtFromDate: TextView
    private lateinit var txtToDate: TextView
    private lateinit var btGetPDF: Button
    private lateinit var shimmerFrameLayout: ShimmerFrameLayout
    private lateinit var searchView: SearchView
    private lateinit var imgFromDate: ImageView
    private lateinit var imgToDate: ImageView
    private lateinit var imgSend: ImageView

    private var cHCode = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_reports, container, false)

        initViews(view)
        setupRecyclerView()
        setupListeners()
        observeViewModel()
        loadData()

        return view
    }

    private fun initViews(view: View) {
        imgSend = view.findViewById(R.id.imgSend)
        reportsRV = view.findViewById(R.id.report_rv_1)
        backButton = view.findViewById(R.id.report_back_button)
        shimmerFrameLayout = view.findViewById(R.id.shimmer_report)
        emptyView = view.findViewById(R.id.report_empty_tv)
        searchView = view.findViewById(R.id.report_search_bar)
        txtFromDate = view.findViewById(R.id.txtFromDate)
        txtToDate = view.findViewById(R.id.txtToDate)
        btGetPDF = view.findViewById(R.id.btGetPDF)
        imgFromDate = view.findViewById(R.id.imgFromDate)
        imgToDate = view.findViewById(R.id.imgToDate)
    }

    private fun setupRecyclerView() {
        reportsAdapter = ReportsAdapter(requireContext(), reportsModels)
        reportsRV.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        reportsRV.adapter = reportsAdapter
    }

    private fun setupListeners() {
        txtFromDate.setOnClickListener { openDatePicker(0) }
        imgFromDate.setOnClickListener { openDatePicker(0) }
        txtToDate.setOnClickListener { openDatePicker(1) }
        imgToDate.setOnClickListener { openDatePicker(1) }
        imgSend.setOnClickListener { sendMail() }
        btGetPDF.setOnClickListener { downloadPDF() }
        backButton.setOnClickListener { requireActivity().finish() }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterReports(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterReports(newText)
                return true
            }
        })
    }

    private fun observeViewModel() {
        viewModel.reportResponse.observe(viewLifecycleOwner) { response ->
            if (response.isSuccessful) {
                val rawList = response.body()?.data
                if (!rawList.isNullOrEmpty()) {
                    aggregateData(rawList)
                } else {
                    shimmerFrameLayout.stopShimmer()
                    shimmerFrameLayout.visibility = View.GONE
                    emptyView.visibility = View.VISIBLE
                }
            } else {
                shimmerFrameLayout.stopShimmer()
                shimmerFrameLayout.visibility = View.GONE
                emptyView.visibility = View.VISIBLE
            }
        }

        viewModel.sendMailResponse.observe(viewLifecycleOwner) { response ->
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.status?.equals("success", ignoreCase = true) == true) {
                    showToast("Email Sent Successfully")
                } else {
                    showToast("Failed to send email: ${body?.message}")
                }
            } else {
                showToast("Error sending email")
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                shimmerFrameLayout.startShimmer()
                shimmerFrameLayout.visibility = View.VISIBLE
            } else {
                shimmerFrameLayout.stopShimmer()
                shimmerFrameLayout.visibility = View.GONE
            }
        }
    }

    private fun loadData() {
        val map = HashMap<String, String>()
        val loginAs = MSP.getInstance(requireContext()).getStringData(AppStrings.loginAs)
        val userID = MSP.getInstance(requireContext()).getStringData(AppStrings.userID)

        if (loginAs == "hcf") {
            searchView.visibility = View.GONE
            map["hospital_id"] = userID ?: ""
            viewModel.getReports(AppStrings.get_report_hcf, map)
        } else {
            imgSend.visibility = View.GONE
            txtFromDate.visibility = View.GONE
            imgFromDate.visibility = View.GONE
            imgToDate.visibility = View.GONE
            txtToDate.visibility = View.GONE
            btGetPDF.visibility = View.GONE
            map["operator_id"] = userID ?: ""
            viewModel.getReports(AppStrings.get_report, map)
        }
    }

    private fun aggregateData(rawList: List<ReportsModel>) {
        reportsModels.clear()
        originalAggregatedModels.clear()
        
        var currentHCode = ""
        var currentDate = ""
        var packets = 0
        
        var redCount = 0
        var blueCount = 0
        var yellowCount = 0
        var whiteCount = 0

        var redCountCbwtf = 0
        var blueCountCbwtf = 0
        var yellowCountCbwtf = 0
        var whiteCountCbwtf = 0

        var hWeight = 0.0
        var cWeight = 0.0
        
        var currentModel: ReportsModel? = null

        for (item in rawList) {
            val hospitalCode = item.hospitalCode?.trim() ?: ""
            val handoverDate = item.handoverDate?.trim() ?: ""

            if (currentHCode.isEmpty()) {
                currentHCode = hospitalCode
                currentDate = handoverDate
                cHCode = hospitalCode // Store first one found for other uses
                packets = 1
                currentModel = createNewAggregatedModel(item)
            } else if (currentHCode == hospitalCode && currentDate == handoverDate) {
                packets++
            } else {
                // Finalize previous model
                currentModel?.let {
                    finalizeAggregatedModel(it, packets, redCount, blueCount, yellowCount, whiteCount, 
                        redCountCbwtf, blueCountCbwtf, yellowCountCbwtf, whiteCountCbwtf, hWeight, cWeight)
                    originalAggregatedModels.add(it)
                }

                // Reset for new group
                currentHCode = hospitalCode
                currentDate = handoverDate
                packets = 1
                redCount = 0
                blueCount = 0
                yellowCount = 0
                whiteCount = 0
                redCountCbwtf = 0
                blueCountCbwtf = 0
                yellowCountCbwtf = 0
                whiteCountCbwtf = 0
                hWeight = 0.0
                cWeight = 0.0
                currentModel = createNewAggregatedModel(item)
            }

            // Aggregation logic
            val colorHcf = item.colorTypeHcf?.lowercase() ?: ""
            val colorCbwtf = item.colorTypeCbwtf?.lowercase() ?: ""
            
            hWeight += item.hcfWeight?.toDoubleOrNull() ?: 0.0
            cWeight += item.cbwtfWeight?.toDoubleOrNull() ?: 0.0

            if (colorHcf.contains("red")) redCount++
            if (colorHcf.contains("blue")) blueCount++
            if (colorHcf.contains("yellow")) yellowCount++
            if (colorHcf.contains("white")) whiteCount++

            if (colorCbwtf.contains("red")) redCountCbwtf++
            if (colorCbwtf.contains("blue")) blueCountCbwtf++
            if (colorCbwtf.contains("yellow")) yellowCountCbwtf++
            if (colorCbwtf.contains("white")) whiteCountCbwtf++
        }

        // Add last model
        currentModel?.let {
            finalizeAggregatedModel(it, packets, redCount, blueCount, yellowCount, whiteCount, 
                redCountCbwtf, blueCountCbwtf, yellowCountCbwtf, whiteCountCbwtf, hWeight, cWeight)
            originalAggregatedModels.add(it)
        }

        reportsModels.addAll(originalAggregatedModels)
        reportsAdapter.notifyDataSetChanged()
        
        if (reportsModels.isEmpty()) {
            emptyView.visibility = View.VISIBLE
        } else {
            emptyView.visibility = View.GONE
        }
    }

    private fun createNewAggregatedModel(item: ReportsModel): ReportsModel {
        val model = ReportsModel()
        model.name = item.name
        model.address = item.address
        model.cbwtfId = item.cbwtfId
        model.colorTypeHcf = item.colorTypeHcf
        model.disposeDate = item.disposeDate
        model.disposeOperatorName = item.disposeOperatorName
        model.colorTypeCbwtf = item.colorTypeCbwtf
        model.district = item.district
        model.handoverDate = item.handoverDate
        model.hcfLatLong = item.hcfLatLong
        model.hcfType = item.hcfType
        model.hospitalCode = item.hospitalCode
        model.hospitalType = item.hospitalType
        model.latLongCbwtf = item.latLongCbwtf
        model.operatorId = item.operatorId
        model.operatorIdCbwtf = item.operatorIdCbwtf
        model.operatorName = item.operatorName
        model.qrDataId = item.qrDataId
        model.qrId = item.qrId
        model.route = item.route
        model.type = item.type
        model.type1 = item.type1
        model.createdOn = item.createdOn
        return model
    }

    private fun finalizeAggregatedModel(
        model: ReportsModel, packets: Int, red: Int, blue: Int, yellow: Int, white: Int,
        redC: Int, blueC: Int, yellowC: Int, whiteC: Int, hW: Double, cW: Double
    ) {
        model.totalPackets = packets.toString()
        model.red = red.toString()
        model.blue = blue.toString()
        model.yellow = yellow.toString()
        model.white = white.toString()
        model.redCbwtf = redC.toString()
        model.blueCbwtf = blueC.toString()
        model.yellowCbwtf = yellowC.toString()
        model.whiteCbwtf = whiteC.toString()
        model.hcfWeight = hW.toString()
        model.cbwtfWeight = cW.toString()
    }

    private fun filterReports(text: String?) {
        val query = text?.trim()?.lowercase() ?: ""
        val filteredList = if (query.isEmpty()) {
            originalAggregatedModels
        } else {
            originalAggregatedModels.filter {
                it.name?.trim()?.lowercase()?.contains(query) == true
            }
        }
        reportsAdapter.onUpdate(filteredList)
    }

    private fun openDatePicker(type: Int) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            R.style.DatePickerTheme,
            { _, year, month, dayOfMonth ->
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val selectedDateCalendar = Calendar.getInstance()
                selectedDateCalendar.set(Calendar.YEAR, year)
                selectedDateCalendar.set(Calendar.MONTH, month)
                selectedDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                val formattedDate = dateFormat.format(selectedDateCalendar.time)

                if (type == 0) {
                    txtFromDate.text = formattedDate
                } else {
                    txtToDate.text = formattedDate
                }

                if (txtFromDate.text.toString().trim() != "from date" && 
                    txtToDate.text.toString().trim() != "To date") {
                    btGetPDF.isEnabled = true
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun downloadPDF() {
        val fromDate = txtFromDate.text.toString().trim()
        val toDate = txtToDate.text.toString().trim()
        val hospitalCode = cHCode

        val baseUrl = AppStrings.getGet_report_PDF + "/"
        val pdfUrl = "$baseUrl$hospitalCode/$fromDate/$toDate"

        Log.d("TAG", "downloadPDF: $pdfUrl")

        val uri = Uri.parse(pdfUrl)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.android.chrome")

        try {
            startActivity(intent)
        } catch (ex: ActivityNotFoundException) {
            val browserIntent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(browserIntent)
        }
    }

    private fun sendMail() {
        val map = HashMap<String, String>()
        map["hcf_code"] = cHCode
        map["from_date"] = txtFromDate.text.toString().trim()
        map["to_date"] = txtToDate.text.toString().trim()
        viewModel.sendMail(AppStrings.send_mail, map)
    }

    private fun showToast(message: String) {
        if (isAdded && context != null) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }
}
