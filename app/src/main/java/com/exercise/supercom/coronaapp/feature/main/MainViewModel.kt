package com.exercise.supercom.coronaapp.feature.main

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.bluetooth.le.ScanSettings.SCAN_MODE_LOW_POWER
import android.os.Handler
import androidx.lifecycle.*
import com.exercise.supercom.coronaapp.data.DatePeriodPoint
import com.exercise.supercom.coronaapp.data.model.Country
import com.exercise.supercom.coronaapp.data.model.CountryTotalCases
import com.exercise.supercom.coronaapp.data.RemoteRepository
import com.exercise.supercom.coronaapp.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import kotlin.Comparator

const val TIME_DELAY_MS: Long = 5000
const val MAC_ADDRESS_INFECTED = "64:E8:CE:FF:34:4E" // One of the mac addresses in my area for example...

@HiltViewModel
class MainViewModel @Inject constructor(
    private val remoteRepository: RemoteRepository,
    private val bluetoothManager: BluetoothManager
) : ViewModel() {

    private val _countries = MutableLiveData<List<Country>>()
    val countries: MutableLiveData<List<Country>>
        get() = _countries

    private val _countryTotalCases = MutableLiveData<CountryTotalCases>()
    val countryTotalCases: MutableLiveData<CountryTotalCases>
        get() = _countryTotalCases

    private val _isLoading = MutableLiveData(false)
    val isLoading: MutableLiveData<Boolean>
        get() = _isLoading

    private val _userCountrySpinnerPosition = MutableLiveData<Int>()
    val userCountrySpinnerPosition: MutableLiveData<Int>
        get() = _userCountrySpinnerPosition

    private lateinit var selectedCountry: Country

    private val lexicographicalComparator by lazy {
        Comparator { country1: Country, country2: Country ->
            if (country1.country > country2.country) 1 else -1
        }
    }

    private val _fromDate = MutableLiveData<String>()
    val fromDate: MutableLiveData<String>
        get() = _fromDate

    private val _toDate = MutableLiveData<String>()
    val toDate: MutableLiveData<String>
        get() = _toDate

    private val _isBleScanning = MutableLiveData(false)
    val isBleScanning: MutableLiveData<Boolean>
        get() = _isBleScanning

    private val _infectedMacAddress = MutableLiveData(MAC_ADDRESS_INFECTED)
    val infectedMacAddress: MutableLiveData<String>
        get() = _infectedMacAddress

    private val _isMacAddressFound = MutableLiveData(false)
    val isMacAddressFound: MutableLiveData<Boolean>
        get() = _isMacAddressFound

    private lateinit var selectedPeriodPoint: DatePeriodPoint

    private val bluetoothAdapter: BluetoothAdapter? by lazy { bluetoothManager.adapter }

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: MutableLiveData<String>
        get() = _errorMessage

    fun fetchCountries() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val countries = remoteRepository.getAvailableCountries()

                _countries.value = countries.sortedWith(lexicographicalComparator)
                setSelectedCountryByPosition(0)
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchCountryCovid19Statistics() {
        val countryTotalCases = CountryTotalCases(country = selectedCountry.country, confirmed = 0, deaths = 0, recovered = 0)
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val listOfDailyTotalCases = remoteRepository.getCountryAllStatusesByPeriod(selectedCountry.slug, _fromDate.value!!, _toDate.value!!)
                _countryTotalCases.value = countryTotalCases.apply {
                            confirmed = listOfDailyTotalCases[listOfDailyTotalCases.size - 1].confirmed - listOfDailyTotalCases[0].confirmed
                            deaths = listOfDailyTotalCases[listOfDailyTotalCases.size - 1].deaths - listOfDailyTotalCases[0].deaths
                            recovered = listOfDailyTotalCases[listOfDailyTotalCases.size - 1].recovered - listOfDailyTotalCases[0].recovered
                        }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setSelectedCountryByPosition(position: Int) {
         countries.value?.let {
             selectedCountry = it[position]
        }
    }

    fun setSelectedCountryByCode(countryCode: String) {
        countries.value?.let { countries ->
            val country = countries.find { country -> country.iSO2 == countryCode }
            country?.let {
                selectedCountry = it
                _userCountrySpinnerPosition.value = countries.indexOfFirst { country -> country.iSO2 == selectedCountry.iSO2 }
            }
        }
    }

    fun setDatePeriodPoint(datePeriodPoint: DatePeriodPoint) {
        selectedPeriodPoint = datePeriodPoint
    }

    fun setSelectedDate(year: Int, month: Int, dayOfMonth: Int) {
        when (selectedPeriodPoint) {
            DatePeriodPoint.START -> _fromDate.value = DateUtils.formatDateToApiFormat(date = Date(year - 1900, month, dayOfMonth))
            DatePeriodPoint.END -> _toDate.value = DateUtils.formatDateToApiFormat(date = Date(year - 1900, month, dayOfMonth))
        }
    }

    fun scanBleDevices() {
        try {
            val scanSettingsBuilder =
                ScanSettings.Builder().setScanMode(SCAN_MODE_LOW_POWER).build()
            val scanFilter =
                ScanFilter.Builder().setDeviceAddress(_infectedMacAddress.value).build()

            _isMacAddressFound.value = false
            _isBleScanning.value = true

            viewModelScope.launch {
                delay(TIME_DELAY_MS)
                bluetoothAdapter?.bluetoothLeScanner?.stopScan(bleScanCallback)
                _isBleScanning.value = false
                Timber.d("BLE: scan is stopped.")
            }

            bluetoothAdapter?.bluetoothLeScanner?.startScan(
                listOf(scanFilter),
                scanSettingsBuilder,
                bleScanCallback
            )
            Timber.d("BLE: scan is started...")
        } catch (e: Exception) {
            Timber.e("BLE: error ${e.message}")
            _isBleScanning.value = false
            errorMessage.postValue(e.message)
        }
    }

    private var bleScanCallback: ScanCallback =
        object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                super.onScanResult(callbackType, result)
                Timber.d("BLE: MAC Address ${result?.device?.address}")
                _isMacAddressFound.value = true
            }

            override fun onBatchScanResults(results: List<ScanResult?>?) {
                super.onBatchScanResults(results)
                Timber.d("BLE: ${results?.toString()}")
                _isMacAddressFound.value = true
            }

            override fun onScanFailed(errorCode: Int) {
                super.onScanFailed(errorCode)
                Timber.e("BLE: onScanFailed")
                _isMacAddressFound.value = false
            }
        }
}