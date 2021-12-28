package com.exercise.supercom.coronaapp.feature.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.DatePicker
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.exercise.supercom.coronaapp.R
import com.exercise.supercom.coronaapp.data.DatePeriodPoint
import com.exercise.supercom.coronaapp.databinding.MainActivityBinding
import com.exercise.supercom.coronaapp.util.checkSelfPermissionCompat
import com.exercise.supercom.coronaapp.util.requestPermissionsCompat
import com.exercise.supercom.coronaapp.util.shouldShowRequestPermissionRationaleCompat
import com.exercise.supercom.coronaapp.util.showSnackbar
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

const val PERMISSION_REQUEST_FOR_LOCATION = 0
const val PERMISSION_REQUEST_FOR_BLUETOOTH = 1
const val REQUEST_ENABLE_BT = 2

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener,
    AdapterView.OnItemSelectedListener,
    ActivityCompat.OnRequestPermissionsResultCallback {

    private lateinit var layout: View

    private val mainViewModel: MainViewModel by viewModels()

    private lateinit var binding: MainActivityBinding

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val geoCoder: Geocoder by lazy { Geocoder(this) }

    @Inject lateinit var bluetoothManager: BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? by lazy { bluetoothManager.adapter }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initDataBinding()
        initLocationService()
        setListeners()
        subscribeObservers()
        mainViewModel.fetchCountries()
    }

    private fun setListeners() {
        binding.tvSelectStartDate.setOnClickListener {
            mainViewModel.setDatePeriodPoint(DatePeriodPoint.START)
            showDatePicker()
        }
        binding.tvSelectEndDate.setOnClickListener {
            mainViewModel.setDatePeriodPoint(DatePeriodPoint.END)
            showDatePicker()
        }
        binding.spCountries.onItemSelectedListener = this
    }

    private fun initLocationService() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun initDataBinding() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        with(binding) {
            viewModel = mainViewModel
            lifecycleOwner = this@MainActivity
            layout = root
        }
    }

    private fun subscribeObservers() {
        mainViewModel.countries.observe(this) { countries ->
            val adapter = ArrayAdapter(
                this,
                R.layout.custom_spinner_dropdown_item,
                countries.map { it.country })
            binding.spCountries.adapter = adapter
        }

        mainViewModel.userCountrySpinnerPosition.observe(this) { position ->
            binding.spCountries.setSelection(position)
        }

        mainViewModel.errorMessage.observe(this) { error ->
            Snackbar.make(binding.root, error, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun showDatePicker() {
        val dialog = DatePickerDialog(
            this@MainActivity,
            android.R.style.Theme_Material_Dialog_MinWidth,
            this@MainActivity,
            Calendar.getInstance().get(Calendar.YEAR),
            Calendar.getInstance().get(Calendar.MONTH),
            Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        )

        dialog.show()
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        mainViewModel.setSelectedDate(year, month, dayOfMonth)
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        mainViewModel.setSelectedCountryByPosition(position)
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        mainViewModel.setSelectedCountryByPosition(0)
    }

    fun onFindLocationBtnPressed(view: View) {
        startLocationDetection()
    }

    @SuppressLint("MissingPermission")
    fun startLocationDetection() {
        if (checkSelfPermissionCompat(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    location?.let {
                        val countryCode =
                            geoCoder.getFromLocation(location.latitude, location.longitude, 1)
                                .first().countryCode
                        mainViewModel.setSelectedCountryByCode(countryCode)
                    }
                }
        } else {
            requestLocationPermission(PERMISSION_REQUEST_FOR_LOCATION)
        }
    }

    fun onBluetoothScanBtnPressed(view: View) {
        if (bluetoothAdapter == null) {
            Snackbar.make(layout, R.string.bluetooth_is_not_available, Snackbar.LENGTH_SHORT).show()
            return
        }
        if (checkSelfPermissionCompat(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission(PERMISSION_REQUEST_FOR_BLUETOOTH)
            return
        }
        if (bluetoothAdapter?.isEnabled == false) {
            onBluetoothEnableRequest()
            return
        }
        mainViewModel.scanBleDevices()
    }

    private fun onBluetoothEnableRequest() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
    }


    private fun requestLocationPermission(requestCode: Int) {
        // Permission has not been granted and must be requested.
        if (shouldShowRequestPermissionRationaleCompat(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // Display a SnackBar with a button to request the missing permission.
            layout.showSnackbar(
                R.string.location_access_required,
                Snackbar.LENGTH_INDEFINITE, R.string.ok
            ) {
                requestPermissionsCompat(
                    arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                    requestCode
                )
            }

        } else {
            Snackbar.make(layout, R.string.location_permission_not_available, Snackbar.LENGTH_SHORT).show()
            // Request the permission. The result will be received in onRequestPermissionResult().
            requestPermissionsCompat(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 1)
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_FOR_LOCATION,
            PERMISSION_REQUEST_FOR_BLUETOOTH -> {
                // Request for location permission.
                if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission has been granted. Start location detection.
                    Snackbar.make(layout, R.string.location_permission_granted, Snackbar.LENGTH_SHORT).show()

                    if (requestCode == PERMISSION_REQUEST_FOR_LOCATION) {
                        startLocationDetection()
                    } else if (requestCode == PERMISSION_REQUEST_FOR_BLUETOOTH) {
                        if (bluetoothAdapter?.isEnabled == false) {
                            onBluetoothEnableRequest()
                        }
                    }
                } else {
                    // Permission request was denied.
                    Snackbar.make(layout, R.string.location_permission_denied, Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }
}