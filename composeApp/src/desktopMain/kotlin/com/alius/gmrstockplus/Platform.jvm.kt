package com.alius.gmrstockplus

class DesktopPlatform : Platform {
    override val name: String = "Desktop (${System.getProperty("os.name")}, Java ${System.getProperty("java.version")})"
    override val isAuthSupported: Boolean = false
    override val isMobile: Boolean = false
}

actual fun getPlatform(): Platform = DesktopPlatform()

actual fun getPlatformContext(): Any? = null