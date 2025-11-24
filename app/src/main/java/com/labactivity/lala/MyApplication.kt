package com.labactivity.lala

import android.app.Application
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.labactivity.lala.UNIFIEDCOMPILER.CompilerFactory

/**
 * Application class for initializing global services
 * CRITICAL: Chaquopy Python MUST be initialized here
 */
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // STEP 1: Initialize Python (Chaquopy) - MUST be done before any Python code runs
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }

        // STEP 2: Initialize CompilerFactory with application context
        CompilerFactory.initialize(this)
    }
}
