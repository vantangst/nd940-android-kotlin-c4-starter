package com.udacity.project4.locationreminders.savereminder.selectreminderlocation

import android.Manifest
import android.app.AlertDialog
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Criteria
import android.location.Geocoder
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.getAddress
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*


class SelectLocationFragment : BaseFragment() {

    // Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map: GoogleMap
    private var currentPoi: PointOfInterest? = null
    private var permissionDialog: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val layoutId = R.layout.fragment_select_location
        binding = DataBindingUtil.inflate(inflater, layoutId, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        binding.btnSave.setOnClickListener {
            onLocationSelected()
        }

        // add the map setup implementation
        val supportMapFragment =
            childFragmentManager.findFragmentById(R.id.google_map) as SupportMapFragment?
        supportMapFragment?.getMapAsync { googleMap ->
            map = googleMap
            //add style to the map
            setMapStyle(map)
            // put a marker to location that the user selected
            setPoiClick(map)
            setMarkerOnLongClick(map)
            enableMyLocation()
        }
        return binding.root
    }

    private fun enableMyLocation(map: GoogleMap) {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            map.isMyLocationEnabled = true
            //zoom to the user location after taking his permission
            val locationManager =
                requireActivity().getSystemService(LOCATION_SERVICE) as LocationManager
            val provider = locationManager.getBestProvider(Criteria(), true)
            provider?.let {
                val location = locationManager.getLastKnownLocation(it)
                if (location != null) {
                    val coordinate = LatLng(location.latitude, location.longitude)
                    val cameraUpdateFactory = CameraUpdateFactory.newLatLngZoom(coordinate, 18f)
                    map.animateCamera(cameraUpdateFactory)
                }
            }
        }
    }

    private fun onLocationSelected() {
        currentPoi?.let {
            _viewModel.selectedPOI.value = it
            _viewModel.latitude.value = it.latLng.latitude
            _viewModel.longitude.value = it.latLng.longitude
            _viewModel.reminderSelectedLocationStr.value = it.name
            _viewModel.navigationCommand.value = NavigationCommand.Back
        } ?: run {
            _viewModel.showErrorMessage.value = getString(R.string.select_poi)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    // Places a marker on the map and displays an info window that contains POI name.
    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            currentPoi = poi
            map.clear()
            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            poiMarker?.showInfoWindow()
        }
    }

    private fun setMarkerOnLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { selectedLocation ->
            map.clear()
            val snippet = String.format(
                getString(R.string.lat_long_snippet),
                selectedLocation.latitude,
                selectedLocation.longitude
            )
            Geocoder(requireContext(), Locale.getDefault()).getAddress(
                selectedLocation.latitude,
                selectedLocation.longitude
            ) {
                val locationName = it?.featureName ?: it?.thoroughfare ?: it?.locality ?: "Unknown name"
                currentPoi = PointOfInterest(
                    selectedLocation,
                    snippet,
                    locationName
                )
                map.addMarker(
                    MarkerOptions()
                        .position(selectedLocation)
                        .title(locationName)
                        .snippet(snippet)
                )?.showInfoWindow()
            }
        }
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.map_style
                )
            )

            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }
    }

    private fun enableMyLocation() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // You can use the API that requires the permission.
                // enable my location
                enableMyLocation(map)
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                showPermissionDialog()
            }
            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                requestPermissionLauncher.launch(
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            }
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your app.
                // enable my location
                enableMyLocation(map)
            } else {
                showPermissionDialog()
            }
        }

    private fun openPermissionSetting() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri: Uri = Uri.fromParts("package", requireActivity().packageName, null)
        intent.data = uri
        requireActivity().startActivity(intent)
    }

    private fun showPermissionDialog() {
        if (permissionDialog == null) {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle(getString(R.string.location_required_error))
            builder.setMessage(getString(R.string.permission_denied_explanation))

            builder.setPositiveButton(R.string.settings) { dialog, _ ->
                openPermissionSetting()
                dialog.dismiss()
            }

            builder.setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            permissionDialog = builder.show()
        } else {
            permissionDialog?.show()
        }
    }

    companion object {
        private const val TAG = "SelectLocationFragment"
    }
}