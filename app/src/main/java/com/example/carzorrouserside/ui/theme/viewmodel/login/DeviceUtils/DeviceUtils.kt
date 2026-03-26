package com.example.carzorrouserside.ui.theme.viewmodel.login.DeviceUtils

import com.google.android.gms.tasks.Task

import android.provider.Settings
import android.content.Context

object DeviceUtils {
    fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )
    }
}

