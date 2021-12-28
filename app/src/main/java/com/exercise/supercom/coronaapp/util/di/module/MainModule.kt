package com.exercise.supercom.coronaapp.util.di.module

import com.exercise.supercom.coronaapp.network.Covid19Api
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import retrofit2.Retrofit

@InstallIn(ActivityRetainedComponent::class)
@Module
object MainModule {
    @Provides
    @ActivityRetainedScoped
    internal fun provideMainApi(retrofit: Retrofit): Covid19Api = retrofit.create(Covid19Api::class.java)
}