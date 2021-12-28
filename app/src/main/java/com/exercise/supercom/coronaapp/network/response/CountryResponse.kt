package com.exercise.supercom.coronaapp.network.response

import com.google.gson.annotations.SerializedName

data class CountryResponse(

	@SerializedName("Country")
	val country: String,

	@SerializedName("Slug")
	val slug: String,

	@SerializedName("ISO2")
	val iSO2: String
)
