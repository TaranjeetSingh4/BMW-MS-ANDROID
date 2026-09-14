package com.appventurez.bmwms.cbwtf.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.appventurez.bmwms.R
import com.appventurez.bmwms.activities.LoginActivity
import com.appventurez.bmwms.classes.AppStrings
import com.appventurez.bmwms.classes.MSP
import com.appventurez.bmwms.network.RetrofitClient
import com.appventurez.bmwms.repository.DashboardRepository
import com.appventurez.bmwms.viewmodel.DashboardViewModel
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton

class CbwtfProfileFragment : Fragment() {

    private var _view: View? = null
    private lateinit var appBarImg: ImageView
    private lateinit var imgBig: ImageView
    private lateinit var backButton: ImageView
    private lateinit var logoutButton: ImageView
    private lateinit var name: TextView
    private lateinit var mobile: TextView
    private lateinit var address: TextView
    private lateinit var help: FloatingActionButton
    private lateinit var enquiry: FloatingActionButton

    private val viewModel: DashboardViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DashboardViewModel(DashboardRepository(RetrofitClient.apiService)) as T
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (_view == null) {
            _view = inflater.inflate(R.layout.fragment_cbwtf_profile, container, false)

            _view?.let {
                appBarImg = it.findViewById(R.id.cbwtf_profile_appBar_logo)
                imgBig = it.findViewById(R.id.cbwtf_profile_img)
                backButton = it.findViewById(R.id.cbwtf_profile_back_button)
                logoutButton = it.findViewById(R.id.cbwtf_profile_logout)
                name = it.findViewById(R.id.cbwtf_profile_name)
                mobile = it.findViewById(R.id.cbwtf_profile_mobile)
                address = it.findViewById(R.id.cbwtf_profile_address)
                help = it.findViewById(R.id.cbwtf_profile_help)
                enquiry = it.findViewById(R.id.cbwtf_profile_enquiry)

                loadUserDetails()
                loadImages()
                loadClickListeners()
                setupObservers()
            }
        }
        return _view
    }

    private fun loadImages() {
        context?.let {
            Glide.with(it).load(R.drawable.uplogogpb).into(appBarImg)
            Glide.with(it).load(R.drawable.uplogogpb).into(imgBig)
        }
    }

    private fun loadClickListeners() {
        backButton.setOnClickListener { activity?.finish() }

        logoutButton.setOnClickListener {
            context?.let {
                MSP.getInstance(it).removeAll()
                val intent = Intent(activity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                activity?.finish()
            }
        }

        help.setOnClickListener {
            val alertDialog = AlertDialog.Builder(context).create()
            val helpView = LayoutInflater.from(context).inflate(R.layout.help_view, null, false)
            alertDialog.setView(helpView)
            alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            alertDialog.show()
        }

        enquiry.setOnClickListener {
            val alertDialog = AlertDialog.Builder(context).create()
            val enquiryView = LayoutInflater.from(context).inflate(R.layout.enquiry_view, null, false)
            val editText = enquiryView.findViewById<EditText>(R.id.enquiry_et)
            val submitBtn = enquiryView.findViewById<TextView>(R.id.enquiry_submit)
            alertDialog.setView(enquiryView)
            alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            alertDialog.show()

            submitBtn.setOnClickListener {
                val msg = editText.text.toString().trim()
                if (msg.isNotEmpty()) {
                    val msp = MSP.getInstance(context)
                    val loginAs = msp.getStringData(AppStrings.loginAs)
                    val sender = if (loginAs == "cbwtf") "cbwtf" else "hcf"
                    val senderId = msp.getStringData(AppStrings.userID)
                    
                    val map = mapOf(
                        "sender" to sender,
                        "sender_id" to senderId,
                        "message" to msg
                    )
                    viewModel.sendQuery(AppStrings.send_query, map)
                }
                editText.setText("")
                alertDialog.dismiss()
            }
        }
    }

    private fun setupObservers() {
        viewModel.sendQueryResponse.observe(viewLifecycleOwner) { response ->
            if (response.isSuccessful) {
                if (response.body()?.status.equals("success", ignoreCase = true)) {
                    showToast("Query sent")
                }
            } else {
                showToast("Try again later")
            }
        }

        viewModel.error.observe(viewLifecycleOwner) {
            showToast("Try again later $it")
        }
    }

    private fun loadUserDetails() {
        activity?.let {
            val msp = MSP.getInstance(it)
            name.text = msp.getStringData(AppStrings.userName)
            mobile.text = msp.getStringData(AppStrings.userMobile)
            address.text = msp.getStringData(AppStrings.userAddress)
        }
    }

    private fun showToast(message: String) {
        if (isAdded) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}
