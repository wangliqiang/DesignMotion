package com.app.design_motion.data.remote

import com.app.design_motion.BuildConfig
import okhttp3.logging.HttpLoggingInterceptor
import timber.log.Timber

object LoggingInterceptor {

    val loggingInterceptor = HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
        override fun log(message: String) {
            Timber.e(message)
        }
    }).apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.BASIC
        }
    }
}