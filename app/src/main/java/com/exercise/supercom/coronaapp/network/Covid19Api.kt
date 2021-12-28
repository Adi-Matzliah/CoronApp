package com.exercise.supercom.coronaapp.network
import com.exercise.supercom.coronaapp.network.response.CountryResponse
import com.exercise.supercom.coronaapp.network.response.CountryTotalCasesByDateResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface Covid19Api {

    @GET("countries")
    suspend fun getCountries(): List<CountryResponse>

    @GET("total/country/{country}")
    suspend fun getCountryAllStatusesByPeriod(@Path("country") country: String, @Query("from", encoded=true) fromDate: String, @Query("to", encoded=true) toDate:String): List<CountryTotalCasesByDateResponse>
}

