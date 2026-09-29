package com.appventurez.bmwms.utils

import android.content.Context
import com.appventurez.bmwms.BuildConfig
import com.appventurez.bmwms.classes.AppStrings
import com.appventurez.bmwms.classes.MSP
import com.google.firebase.crashlytics.FirebaseCrashlytics

object CrashlyticsHelper {

    private val crashlytics: FirebaseCrashlytics by lazy {
        FirebaseCrashlytics.getInstance()
    }

    fun log(message: String) {
        try {
            crashlytics.log(message)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setUserId(userId: String) {
        try {
            crashlytics.setUserId(userId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setKey(key: String, value: String) {
        try {
            crashlytics.setCustomKey(key, value)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setKey(key: String, value: Int) {
        try {
            crashlytics.setCustomKey(key, value)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setKey(key: String, value: Boolean) {
        try {
            crashlytics.setCustomKey(key, value)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setKey(key: String, value: Double) {
        try {
            crashlytics.setCustomKey(key, value)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun recordException(exception: Throwable) {
        try {
            crashlytics.recordException(exception)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateCrashlyticsUserContext(context: Context) {
        try {
            val msp = MSP.getInstance(context)

            val loginAs = msp.getStringData(AppStrings.loginAs) ?: "unknown"
            val userId = msp.getStringData(AppStrings.userID) ?: ""
            val cbwtfId = msp.getStringData(AppStrings.userCbwtfID) ?: ""
            val hcfCode = msp.getStringData(AppStrings.hcfCode) ?: ""

            setKey("environment", BuildConfig.FLAVOR)
            setKey("app_version", BuildConfig.VERSION_NAME)
            setKey("base_url", BuildConfig.BASE_URL)

            setKey("login_as", loginAs)
            setKey("user_id", userId)
            setKey("cbwtf_id", cbwtfId)
            setKey("hcf_code", hcfCode)

            if (userId.isNotEmpty()) {
                setUserId(userId)
            } else {
                setUserId("")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
