package com.alius.gmrstockplus

interface Platform {
    val name: String
    val isAuthSupported: Boolean
    val isMobile: Boolean
}

expect fun getPlatform(): Platform
expect fun getPlatformContext(): Any?