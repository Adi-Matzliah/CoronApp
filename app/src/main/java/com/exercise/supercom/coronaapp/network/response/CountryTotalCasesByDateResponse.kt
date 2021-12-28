package com.exercise.supercom.coronaapp.network.response

import com.google.gson.annotations.SerializedName

data class CountryTotalCasesByDateResponse(

	@SerializedName("CityCode")
	val cityCode: String,

	@SerializedName("Recovered")
	val recovered: Int,

	@SerializedName("Active")
	val active: Int,

	@SerializedName("Country")
	val country: String,

	@SerializedName("Deaths")
	val deaths: Int,

	@SerializedName("Lon")
	val lon: String,

	@SerializedName("City")
	val city: String,

	@SerializedName("Confirmed")
	val confirmed: Int,

	@SerializedName("CountryCode")
	val countryCode: String,

	@SerializedName("Province")
	val province: String,

	@SerializedName("Lat")
	val lat: String,

	@SerializedName("Date")
	val date: String
)
