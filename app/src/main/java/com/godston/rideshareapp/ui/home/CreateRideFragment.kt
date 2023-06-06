package com.godston.rideshareapp.ui.home

import android.Manifest
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import com.godston.rideshareapp.R
import com.godston.rideshareapp.databinding.LayoutCreateRideBinding
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.io.IOException
import java.util.*

class CreateRideFragment : BottomSheetDialogFragment() {

    private lateinit var _binding: LayoutCreateRideBinding
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var price: Int = 500
    private val binding get() = _binding!!

    companion object {
        fun newInstance(): CreateRideFragment {
            return CreateRideFragment()
        }
        val PERMISSION_REQUEST_CODE: Int = 1000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = LayoutCreateRideBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
        binding.closeRide.setOnClickListener {
            dismiss()
        }
        binding.apply {
            plus.setOnClickListener {
                price += 100
                priceTv.text = price.toString()
            }
            minus.setOnClickListener {
                if (price >= 600) {
                    price -= 100
                    priceTv.text = price.toString()
                } else {
                    price = 500
                    priceTv.text = price.toString()
                }
            }
        }
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Location permission granted, proceed with location retrieval
            getLocation()
        } else {
            // Request location permission from the user
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    // Handle the location permission request response
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<String>,
//        grantResults: IntArray
//    ) {
//        if (requestCode == PERMISSION_REQUEST_CODE) {
//            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                // Location permission granted, proceed with location retrieval
//                getLocation()
//            } else {
//                // Location permission denied, handle accordingly
//            }
//        }
//    }
    private fun getLocation() {
//        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        // Request location updates
        val locationRequest = LocationRequest.create() // .apply {
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 5000 // 5 seconds (adjust according to your needs)
        locationRequest.fastestInterval = 2000

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        builder.setAlwaysShow(true)

        val result = LocationServices.getSettingsClient(
            this.requireContext()
        ).checkLocationSettings(builder.build())
        result.addOnCompleteListener { task ->
            try {
                // when GPS is on
                val response = task.getResult(ApiException::class.java)
                getUserLocation()
            } catch (error: ApiException) {
                // when GPS is off
                error.printStackTrace()
                when (error.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                        // when request to enable GPS is sent
                        val resolveApiException = error as ResolvableApiException
                        resolveApiException.startResolutionForResult(requireActivity(), 200)
                    } catch (sendIntentException: IntentSender.SendIntentException) {
                        TODO("later")
                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                        // when settings are not available
                    }
                }
            }
        }
        // }
//        fusedLocationClient.lastLocation
//            .addOnSuccessListener { location: Location? ->
//                // Handle the retrieved location
//                location?.let {
//                    val latitude = location.latitude
//                    val longitude = location.longitude
//                    val address = getAddressFromLocation(latitude, longitude)
//                    binding.fromTxt.text = Editable.Factory.getInstance().newEditable(address)
//                }
//            }
//            .addOnFailureListener { exception: Exception ->
//                // Handle the failure to retrieve location
//            }
//    }
//        val locationCallback = object : LocationCallback() {
//            override fun onLocationResult(locationResult: LocationResult?) {
//                locationResult?.lastLocation?.let { location ->
//                    val latitude = location.latitude
//                    val longitude = location.longitude
//
//                    val geocoder = Geocoder(requireContext(), Locale.getDefault())
//
//                    try {
//                        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
//                        if (addresses.isNotEmpty()) {
//                            val fetchedAddress = addresses[0]
//                            val sb = StringBuilder()
//                            for (i in 0 until fetchedAddress.maxAddressLineIndex) {
//                                sb.append(fetchedAddress.getAddressLine(i)).append(", ")
//                            }
//                            sb.append(fetchedAddress.locality)
//                            val address = sb.toString()
//                            binding.fromTxt.setText(address)
// //                            binding.fromTxt.text = Editable.Factory.getInstance().newEditable(address)
//
//                            // Stop location updates after retrieving the address
// //                            fusedLocationClient.removeLocationUpdates(this)
//                        }
//                    } catch (e: IOException) {
//                        e.printStackTrace()
//                        // Handle the exception or display a default message when the address retrieval fails
//                    }
//                }
//            }
//        }

        // Request location updates
//        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private fun getUserLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
            val location = task.getResult()
            if (location != null) {
                try {
                    val geocoder = Geocoder(requireContext(), Locale.getDefault())
                    val address = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    if (address.isNotEmpty()) {
                        val address_line = address[0].getAddressLine(0)
                        binding.fromTxt.setText(address_line)

                        val address_location = address[0].getAddressLine(0)
                        openLocation(address_location.toString())
                    }
                } catch (error: IOException) {
                }
            }
        }
    }

    private fun openLocation(location: String) {
        // here we open this location in google map
        binding.fromTxt.setOnClickListener {
            if (!binding.fromTxt.text.isEmpty()) {
                val uri = Uri.parse("geo:0, 0?q=$location")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                intent.setPackage("com.google.android.apps.maps")
                startActivity(intent)
            }
        }
    }

//    private fun getAddressFromLocation(latitude: Double, longitude: Double): String {
//        val geocoder = Geocoder(requireContext(), Locale.getDefault())
//        var address = ""
//
//        try {
//            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
//            if (addresses.isNotEmpty()) {
//                val fetchAddress = addresses[0]
//                val sb = StringBuilder()
//                for (i in 0 until fetchAddress.maxAddressLineIndex) {
//                    sb.append(fetchAddress.getAddressLine(i)).append(", ")
//                }
//                sb.append(fetchAddress.locality)
//                return sb.toString()
//            }
//        } catch (e: IOException) {
//            e.printStackTrace()
//            // Handle the exception or display a default message when the address retrieval fails
//        }
//        return address
//    }
}
