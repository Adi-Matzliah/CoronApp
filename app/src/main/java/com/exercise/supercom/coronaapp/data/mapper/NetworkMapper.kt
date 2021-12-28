package com.exercise.supercom.coronaapp.data.mapper

import com.exercise.supercom.coronaapp.data.model.Country
import com.exercise.supercom.coronaapp.data.model.CountryTotalCases
import com.exercise.supercom.coronaapp.network.response.CountryResponse
import com.exercise.supercom.coronaapp.network.response.CountryTotalCasesByDateResponse

sealed class NetworkMapper {
    object CountryMapper: IEntityMapper<CountryResponse, Country> {
        override fun mapFromEntity(entity: CountryResponse) =
            Country(
                country = entity.country,
                slug = entity.slug,
                iSO2 = entity.iSO2
            )

        override fun mapToEntity(domainModel: Country)=
            CountryResponse(
                country = domainModel.country,
                slug = domainModel.slug,
                iSO2 = ""
            )
    }

    object CountryTotalCasesMapper: IEntityMapper<CountryTotalCasesByDateResponse, CountryTotalCases> {
        override fun mapFromEntity(entity: CountryTotalCasesByDateResponse) =
            CountryTotalCases(
                country = entity.country,
                confirmed = entity.confirmed,
                deaths = entity.deaths,
                recovered = entity.recovered
            )

        override fun mapToEntity(domainModel: CountryTotalCases)=
            CountryTotalCasesByDateResponse(
                cityCode = "",
                recovered = domainModel.recovered,
                active = 0,
                country = domainModel.country,
                deaths = domainModel.deaths,
                lon = "",
                city = "",
                confirmed = domainModel.confirmed,
                countryCode = "",
                province = "",
                lat = "",
                date = ""
            )
    }
}