package com.appventurez.bmwms.classes

import android.app.Application
import android.content.Context
import com.appventurez.bmwms.utils.CrashlyticsHelper
import com.google.firebase.FirebaseApp

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        ctx = this
        FirebaseApp.initializeApp(this)
        CrashlyticsHelper.log("App onCreate initialized")
        CrashlyticsHelper.updateCrashlyticsUserContext(this)
    }

    companion object {
        @JvmField
        var ctx: Context? = null

        @JvmStatic
        fun getCtx(): Context? = ctx

        @JvmStatic
        fun updateCrashlyticsUserContext(context: Context) {
            CrashlyticsHelper.updateCrashlyticsUserContext(context)
        }
    }
}
