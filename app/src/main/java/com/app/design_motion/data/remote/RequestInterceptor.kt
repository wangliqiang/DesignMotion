package com.app.design_motion.data.remote

import com.app.design_motion.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response

class RequestInterceptor : Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response {
    val originalRequest = chain.request()
    val originalUrl = originalRequest.url

    // add api automatically every requests.
    val url = originalUrl.newBuilder()
      .addQueryParameter("apikey", BuildConfig.MOVIE_API_KEY)
      .build()

    val requestBuilder = originalRequest.newBuilder().url(url)
    val request = requestBuilder.build()
    return chain.proceed(request)
  }
}
