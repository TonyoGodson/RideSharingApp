package com.godston.rideshareapp.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.res.Resources
import android.location.Geocoder
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.godston.rideshareapp.R
import com.godston.rideshareapp.databinding.FragmentHomeBinding
import com.godston.rideshareapp.utils.Common
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import java.io.IOException
import java.util.*

class HomeFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var mapFragment: SupportMapFragment
    private var _binding: FragmentHomeBinding? = null

//    location
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

//    online system
    private lateinit var onLineRef: DatabaseReference
    private lateinit var currentUserRef: DatabaseReference
    private lateinit var driverLocationReference: DatabaseReference
    private lateinit var geoFire: GeoFire

    private val onlineValueEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.exists()) {
                currentUserRef.onDisconnect().removeValue()
            }
        }

        override fun onCancelled(error: DatabaseError) {
            Snackbar.make(mapFragment.requireView(), error.message, Snackbar.LENGTH_LONG).show()
        }
    }
    private val binding get() = _binding!!

    override fun onDestroy() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        geoFire.removeLocation(FirebaseAuth.getInstance().currentUser!!.uid)
        onLineRef.removeEventListener(onlineValueEventListener)
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        registerOnlineSystem()
    }

    private fun registerOnlineSystem() {
        onLineRef.addValueEventListener(onlineValueEventListener)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        init()
        mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.createRideBtn.setOnClickListener {
            val createRideFragment = CreateRideFragment()
            createRideFragment.show(parentFragmentManager, "bottomSheet")
        }
    }

    @SuppressLint("MissingPermission")
    private fun init() {
        onLineRef = FirebaseDatabase.getInstance().getReference().child(".info/connected")
        driverLocationReference = FirebaseDatabase.getInstance().getReference(Common.DRIVERS_LOCATION_REFERENCES)
        currentUserRef = FirebaseDatabase.getInstance().getReference(Common.DRIVERS_LOCATION_REFERENCES).child(
            FirebaseAuth.getInstance().currentUser!!.uid
        )

        geoFire = GeoFire(driverLocationReference)
        registerOnlineSystem()

        locationRequest = LocationRequest()
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        locationRequest.setFastestInterval(3000)
        locationRequest.interval = 5000
        locationRequest.setSmallestDisplacement(10f)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)

                val newPos = LatLng(locationResult!!.lastLocation.latitude, locationResult!!.lastLocation.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newPos, 18f))

                geoFire.setLocation(
                    FirebaseAuth.getInstance().currentUser!!.uid,
                    GeoLocation(locationResult.lastLocation.latitude, locationResult.lastLocation.longitude)
                ) { key: String?, error: DatabaseError? ->
                    if (error != null) {
                        Snackbar.make(mapFragment.requireView(), error.message, Snackbar.LENGTH_LONG).show()
                    } else {
                        Snackbar.make(mapFragment.requireView(), "You are online", Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
    }
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap!!

        Dexter.withContext(requireContext())
            .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(object : PermissionListener {
                @SuppressLint("MissingPermission")
                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                    mMap.isMyLocationEnabled = true
                    mMap.uiSettings.isMyLocationButtonEnabled = true
                    mMap.setOnMyLocationClickListener { location ->
                        val geocoder = Geocoder(requireContext(), Locale.getDefault())
                        try {
                            val addresses = geocoder.getFromLocation(
                                location.latitude,
                                location.longitude,
                                1
                            )
                            if (addresses.isNotEmpty()) {
                                val address = addresses[0]
                                val userAddress = address.getAddressLine(0)
                                Toast.makeText(context!!, userAddress, Toast.LENGTH_LONG).show()
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
//                        fusedLocationProviderClient.lastLocation
//                            .addOnFailureListener { e ->
//                                Toast.makeText(context!!, e.message, Toast.LENGTH_LONG).show()
//                            }.addOnSuccessListener { location ->
//                                val userLatLng = LatLng(location.latitude, location.longitude)
//                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, googleMap.maxZoomLevel))
//                            }
//                        true
//                    }

                    val locationButton = (
                        mapFragment.requireView()!!
//                    val view = mapFragment.view!!
                            .findViewById<View>("1".toInt())!!
                            .parent!! as View
                        )
                        .findViewById<View>("2".toInt())
                    val params = locationButton.layoutParams as RelativeLayout.LayoutParams
                    params.addRule(RelativeLayout.ALIGN_TOP, 0)
                    params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
                    params.bottomMargin = 50
                }

                override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                    Toast.makeText(context!!, "Permission ${p0!!.permissionName} was denied.", Toast.LENGTH_LONG).show()
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: PermissionRequest?,
                    p1: PermissionToken?
                ) {
                    TODO("Not yet implemented")
                }
            }).check()
//        mMap.uiSettings.isZoomControlsEnabled = true
        try {
            val success = googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.uber_maps_style
                )
            )
            if (!success) {
                Log.e("Map Error", "Style parsing error")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e("Maps Error", e.message.toString())
        }
        // Add a marker in Sydney and move the camera
//        val sydney = LatLng(-34.0, 151.0)
//        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }
}
