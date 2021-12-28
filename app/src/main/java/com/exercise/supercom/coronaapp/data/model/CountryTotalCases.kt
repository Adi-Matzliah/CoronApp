package com.exercise.supercom.coronaapp.data.model

data class CountryTotalCases (
    var country: String = "",
    var confirmed: Int = 0,
    var deaths: Int = 0,
    var recovered: Int = 0
)