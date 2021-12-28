package com.exercise.supercom.coronaapp.util.di.module

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.telephony.TelephonyManager
import androidx.core.content.getSystemService
import com.exercise.supercom.coronaapp.R
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module/*(includes = [ContextModule::class])*/
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    internal fun provideHttpCache(@ApplicationContext context: Context): Cache {
        val cacheSize: Long = 10 * 1024 * 1024
        return Cache(context.cacheDir, cacheSize)
    }

    @Provides
    @Singleton
    internal fun provideHttpLoginInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

    @Provides
    @Singleton
    internal fun provideOkHttpClient(cache: Cache, loggingInterceptor: HttpLoggingInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            /*.authenticator(authenticator)*/
            .build()


    @Provides
    @Singleton
    internal fun provideRetrofit(@ApplicationContext context: Context, gson: Gson, okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .baseUrl(context.getString(R.string.base_url))
            .client(okHttpClient)
            .build()

    @Provides
    @Singleton
    internal fun provideGsonBuilder(): GsonBuilder = GsonBuilder()


    @Provides
    @Singleton
    internal fun provideGson(gsonBuilder: GsonBuilder): Gson =
        gsonBuilder
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create()

    @Provides
    @Singleton
    internal fun provideTelephonyManager(@ApplicationContext context: Context) = context.getSystemService<TelephonyManager>()

    @Provides
    @Singleton
    internal fun provideNetworkInfo(@ApplicationContext context: Context): NetworkInfo? {
        val connectivityManager = context.getSystemService<ConnectivityManager>()
        return connectivityManager?.activeNetworkInfo
    }
}