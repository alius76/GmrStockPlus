package com.alius.gmrstockplus

import platform.UIKit.UIDevice

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
    override val isAuthSupported: Boolean = true
    override val isMobile: Boolean = true
}

actual fun getPlatform(): Platform = IOSPlatform()

actual fun getPlatformContext(): Any? = null