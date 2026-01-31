package com.alius.gmrstockplus

import android.os.Build
import com.alius.gmrstockplus.core.AppContextProvider

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
    override val isAuthSupported: Boolean = true
    override val isMobile: Boolean = true
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun getPlatformContext(): Any? = AppContextProvider.appContext