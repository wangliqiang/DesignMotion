package com.app.design_motion.data.remote

import com.app.design_motion.BuildConfig
import com.app.design_motion.data.service.MovieService
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitFactory {

    private lateinit var retrofit: Retrofit
    private lateinit var okHttpClient: OkHttpClient

    private lateinit var movieService: MovieService

    companion object {

        private const val READ_TIME_OUT = 5000L// 读取超过时间 单位:毫秒
        private const val WRITE_TIME_OUT = 5000L//写超过时间 单位:毫秒
        private val Instance: RetrofitFactory by lazy { RetrofitFactory() }

        fun getMovieService(): MovieService {
            return Instance.movieService
        }

    }

    init {
        initOkHttpClient()
        initRetrofit()
        // 初始化服务
        initMovieService()
    }


    private fun initOkHttpClient() {

        okHttpClient = OkHttpClient.Builder()
            .readTimeout(READ_TIME_OUT, TimeUnit.MILLISECONDS)
            .writeTimeout(WRITE_TIME_OUT, TimeUnit.MILLISECONDS)
            .addInterceptor(RequestInterceptor())
            .addInterceptor(LoggingInterceptor.loggingInterceptor)
            .build()
    }

    private fun initRetrofit() {

        val gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd hh:mm:ss")
            .create()
        retrofit = Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(BuildConfig.MOVIE_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    }

    private fun initMovieService() {
        movieService = retrofit.create(MovieService::class.java)
    }

}