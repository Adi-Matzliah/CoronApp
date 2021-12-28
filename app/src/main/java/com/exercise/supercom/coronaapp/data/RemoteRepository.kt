package com.exercise.supercom.coronaapp.data
import com.exercise.supercom.coronaapp.data.mapper.NetworkMapper
import com.exercise.supercom.coronaapp.network.Covid19Api
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.coroutineContext

class RemoteRepository @Inject constructor(private val api: Covid19Api) {

    suspend fun getAvailableCountries() =
        withContext(coroutineContext + Dispatchers.IO) {
            api.getCountries()
                .map { NetworkMapper.CountryMapper.mapFromEntity(it) }
        }

    suspend fun getCountryAllStatusesByPeriod(country: String, fromDate: String, toDate: String) =
        withContext(coroutineContext + Dispatchers.IO) {
            api.getCountryAllStatusesByPeriod(country, fromDate, toDate)
                .map { NetworkMapper.CountryTotalCasesMapper.mapFromEntity(it) }
        }
}


