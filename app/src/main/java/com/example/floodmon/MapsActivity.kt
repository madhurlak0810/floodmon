package com.example.floodmon

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.example.floodmon.databinding.ActivityMapsBinding
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*

const val LOCATION_REQUEST_CODE=123
const val GEOFENCE_LOCATION_REQUEST_CODE=12345
const val CAMERA_ZOOM_LEVEL=14F
const val GEOFENCE_RADIUS= 1500
const val GEOFENCE_ID="Reminder GEOFENCE id"
const val GEOFENCE_EXPIRATION= 1*24*60*60*1000
const val GEOFENCE_DWELL_DELAY=10*1000//10 secs in flood prone area


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, View.OnClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private lateinit var button : Button
    private lateinit var gbref : DatabaseReference
    private lateinit var gArrayList:ArrayList<Locations>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mainMap) as SupportMapFragment
        mapFragment.getMapAsync(this)
        geofencingClient=LocationServices.getGeofencingClient(this)
        fusedLocationProviderClient=LocationServices.getFusedLocationProviderClient(this)
        button = findViewById(R.id.fireBtn)
        button.setOnClickListener(this)
        arrayListOf<Locations>().also { gArrayList = it }
    }
    override fun onClick(view: View?) {
        when(view?.id){
            R.id.fireBtn->{
                val intent= Intent(this,Fire::class.java)
                startActivity(intent)
                // do some work here
            }
        }
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        getUserData(mMap)
        if(!isLocationPermissionGranted()) {
            val permissions = mutableListOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
            ActivityCompat.requestPermissions(
                this,
                permissions.toTypedArray(),
                LOCATION_REQUEST_CODE

            )
        }
        else {

            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            this.mMap.isMyLocationEnabled= true
            //get last known location
            fusedLocationProviderClient.lastLocation.addOnSuccessListener {
                if (it != null) {
                    with(mMap) {
                        val latLng = LatLng(it.latitude, it.longitude)
                        moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, CAMERA_ZOOM_LEVEL))
                    }
                } else {
                    with(mMap) {
                        moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(12.970951072133404, 79.159527097689),
                                CAMERA_ZOOM_LEVEL
                            )

                        )
                    }
                }
            }

        }

    }



    private fun isLocationPermissionGranted():Boolean{
        return ContextCompat.checkSelfPermission(
            applicationContext, Manifest.permission.ACCESS_FINE_LOCATION
        )==PackageManager.PERMISSION_GRANTED||ContextCompat.checkSelfPermission(
            applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION
        )==PackageManager.PERMISSION_GRANTED



    }


    private fun getUserData(googleMap: GoogleMap) {

        gbref = FirebaseDatabase.getInstance().getReference("Locations")
        mMap=googleMap
        gbref.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()){

                    for (locSnapshot in snapshot.children){


                        val loc = locSnapshot.getValue(Locations::class.java)
                        val t=gArrayList.binarySearchBy(loc?.LocName) { it.LocName }
                        if(t!=-1) {
                            gArrayList.removeAt(t)
                        }
                        gArrayList.add(loc!!)

                        if(loc.AlertStatus =="Red"){
                            with(mMap){
                                val latLng=LatLng(12.951895013017841, 79.16196965271372)
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    latLng,
                                    CAMERA_ZOOM_LEVEL

                                )
                                )
                                mMap.addMarker(
                                    MarkerOptions().position(latLng)
                                        .title("Flood location")
                                )?.showInfoWindow()
                                mMap.addCircle(
                                    CircleOptions()
                                        .center(latLng)
                                        .strokeColor(Color.argb(50, 70, 70, 70))
                                        .fillColor(Color.argb(70, 186, 27, 19))
                                        .radius(GEOFENCE_RADIUS.toDouble())
                                )
                                //createGeoFence(latLng, geofencingClient)
                            }
                        }


                    }


                }

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }


        })

    }

}
