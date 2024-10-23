package com.innovatech.peaceapp.Map

import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.innovatech.peaceapp.Alert.AlertActivity
import com.innovatech.peaceapp.Alert.Beans.Alert
import com.innovatech.peaceapp.Alert.Beans.AlertSchema
import com.innovatech.peaceapp.GlobalToken
import com.innovatech.peaceapp.GlobalUserEmail
import com.innovatech.peaceapp.Map.Beans.PropertiesPlace
import com.innovatech.peaceapp.Map.Beans.Report
import com.innovatech.peaceapp.Map.Models.RetrofitClient
import com.innovatech.peaceapp.Map.Models.RetrofitClientMapbox
import com.innovatech.peaceapp.Profile.Beans.UserProfile
import com.innovatech.peaceapp.Profile.MainProfileActivity
import com.innovatech.peaceapp.R
import com.innovatech.peaceapp.ShareLocation.ContactsListActivity
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.attribution.attribution
import com.mapbox.maps.plugin.compass.compass
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.logo.logo
import com.mapbox.maps.plugin.scalebar.scalebar
import com.mapbox.search.autofill.AddressAutofill
import com.mapbox.search.autofill.AddressAutofillResult
import com.mapbox.search.autofill.AddressAutofillSuggestion
import com.mapbox.search.autofill.Query
import com.mapbox.search.ui.adapter.autofill.AddressAutofillUiAdapter
import com.mapbox.search.ui.view.CommonSearchViewConfiguration
import com.mapbox.search.ui.view.DistanceUnitType
import com.mapbox.search.ui.view.SearchResultsView
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


class MapActivity : AppCompatActivity() {
    private lateinit var mapView: MapView
    private lateinit var token: String
    private lateinit var txtCurrentLocation: TextView
    private lateinit var addressAutofill: AddressAutofill
    private lateinit var searchLocation: EditText
    private lateinit var searchResultsView: SearchResultsView
    private lateinit var addressAutofillUiAdapter: AddressAutofillUiAdapter
    private var ignoreNextQueryTextUpdate: Boolean = false
    private lateinit var mapPin: View
    private var coordinatesCurrentLocation: Point = Point.fromLngLat(0.0, 0.0)
    private var currentLocation: String = ""
    private var ignoreNextMapIdleEvent: Boolean = false
    private var isUserInteracting: Boolean = false
    private lateinit var userPhoto: ImageView
    private var c: Int = 0
    private var expandArrow: ImageView? = null
    private var compressedArrow: ImageView? = null
    private var isKeyboardVisible = false
    private var isExpanded = false
    private lateinit var searchBox: CardView
    private val processedReports = mutableSetOf<Int>() // This will store the ID of the reports that have already triggered an alert.
    private val popupTriggeredReports = mutableSetOf<Int>() // Tracks reports that have triggered a popup
    private var userId: Int = 0
    private lateinit var email: String

    private lateinit var handler: Handler
    private val proximityCheckRunnable = object : Runnable {
        override fun run() {
            val currentLat = coordinatesCurrentLocation.latitude()
            val currentLon = coordinatesCurrentLocation.longitude()

            // Llama a checkProximityToReports con las coordenadas actuales
            checkProximityToReports(currentLat, currentLon)

            // Repite el chequeo después de un intervalo de tiempo (por ejemplo, 5000 ms = 5 segundos)
            handler.postDelayed(this, 1000)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        handler = Handler()
        handler.post(proximityCheckRunnable)
        // Example function that updates the alert counter
        val sharedPref = getSharedPreferences("GlobalPrefs", MODE_PRIVATE)
        userId = sharedPref.getInt("userId", 0)
        enableEdgeToEdge()
        setContentView(R.layout.activity_map)
        var tk = GlobalToken
        Log.i("Token", tk.token.toString())
// In MapActivity - pass the current location to AlertActivity when the alert button is pressed
        val warningButton = findViewById<ImageView>(R.id.iconImage)
        warningButton.setOnClickListener {

            // Start the AlertActivity as before
            val intent = Intent(this, AlertActivity::class.java)
            intent.putExtra("currentLocation", currentLocation) // Pass the current location
            startActivity(intent)
        }


        addressAutofill = AddressAutofill.create(locationProvider = null)
        searchLocation = findViewById(R.id.searchLocation)
        searchResultsView = findViewById(R.id.search_results_view)
        mapPin = findViewById(R.id.map_pin)
        txtCurrentLocation = findViewById(R.id.currentLocation)
        token = intent.getStringExtra("token")!!
        mapView = findViewById(R.id.mapView)
        userPhoto = findViewById(R.id.userPhoto)
        searchBox = findViewById(R.id.container_search);
        expandArrow = findViewById(R.id.expand_arrow);
        compressedArrow = findViewById(R.id.compressed_arrow);
        email = GlobalUserEmail.email
        userPhoto.setOnClickListener {
            val intent = Intent(this, MainProfileActivity::class.java)
            startActivity(intent)

        }


        searchResultsView.initialize(
            SearchResultsView.Configuration(
                commonConfiguration = CommonSearchViewConfiguration(DistanceUnitType.IMPERIAL)
            )
        )

        addressAutofillUiAdapter = AddressAutofillUiAdapter(view = searchResultsView, addressAutofill = addressAutofill)
        addressAutofillUiAdapter.addSearchListener(object :
            AddressAutofillUiAdapter.SearchListener {
            override fun onSuggestionSelected(suggestion: AddressAutofillSuggestion) {
                /* when is selected a suggestion, the address is shown in the search bar */

                // ALERTA: Buscador con el autocompletado
                // no descomentar, costo adicional
                selectSuggestion(suggestion)
            }
            override fun onSuggestionsShown(suggestions: List<AddressAutofillSuggestion>) {}
            override fun onError(e: Exception) {}
        })

        /* when is received a click on the search bar, the search results view is shown */
        searchLocation.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
                if (ignoreNextQueryTextUpdate) {
                    ignoreNextQueryTextUpdate = false
                    return
                }

                val query = Query.create(text.toString())
                if (query != null) {
                    lifecycleScope.launchWhenStarted {
                        // ALERTA: AddressAutoFill
                        //Log.i("AddressAutofill SEARCH QUERY", "Searching for: $query")
                        addressAutofillUiAdapter.search(query) // this function is used to search the address
                    }
                }
                searchResultsView.isVisible = query != null
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // Nothing to do
            }

            override fun afterTextChanged(s: Editable) {
                // Nothing to do
            }
        })

        // event: when the map is moved, flag for user interaction is set to true
        mapView.mapboxMap.addOnCameraChangeListener {
            isUserInteracting = true
        }
        // event: when the map is idle, the center of the map is obtained
        mapView.mapboxMap.addOnMapIdleListener {
            if (!isUserInteracting) { // if the map is being moved by the user, the center is not obtained
                return@addOnMapIdleListener
            }

            // the map is stopped and obtain the center
            val center = mapView.mapboxMap.cameraState.center
            Log.i("Center", center.toString())

            // ALERTA: solo comentar para pruebas seguras y especificas, ya que consume mucho
            // cada vez que se mueve el mapa, se obtiene la dirección del centro
            // es un costo adicional

            obtainNamePlace(center.longitude(), center.latitude())

            isUserInteracting = false
        }

        loadUserPhoto()
        listenKeyboard()
        locateCurrentPosition()
        obtainAllLocations()
        setupMap()
        navigationMenu()
        deleteAllAlerts()
    }
    private fun loadUserPhoto() {
        val service = com.innovatech.peaceapp.Profile.Models.RetrofitClient.getClient(token)

        service.getUserByEmail(email)
            .enqueue(
                object: Callback<UserProfile> {
                    override fun onResponse(call: Call<UserProfile>, response:
                    Response<UserProfile>) {
                        val userProfile = response.body()
                        if (userProfile != null) {
                            Picasso.get().load(userProfile.profile_image).into(userPhoto)
                        }
                    }

                    override fun onFailure(p0: Call<UserProfile>, p1: Throwable) {
                        p1.printStackTrace()
                    }
                })


    }
    private fun sharedGlobalCoordinates() {
        val sharedPref = getSharedPreferences("GlobalPrefs", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("currentLocation", txtCurrentLocation.text.toString())
            putString("latitude", coordinatesCurrentLocation.latitude().toString())
            putString("longitude", coordinatesCurrentLocation.longitude().toString())
            apply()
        }
    }

    // function to select the suggestion
    private fun selectSuggestion(suggestion: AddressAutofillSuggestion) {
        lifecycleScope.launchWhenStarted {
            // ALERTA AddressAutoFill
            val response = addressAutofill.select(suggestion)
            response.onValue { result ->
                Log.i("AddressAutofill SELECTSUGGESTION", "Selected suggestion: $result")
                // obtaining the point of the selected location
                coordinatesCurrentLocation = result.suggestion.coordinate!!

                sharedGlobalCoordinates()
                // showing the result.address
                showAddressAutofillResult(result)
            }.onError {
                Log.e("AddressAutofill", "Error selecting suggestion: $it")
            }
        }
    }

    // function to show the address in the map
    private fun showAddressAutofillResult(result: AddressAutofillResult) {
        val address = result.address

        Log.i("AddressAutofill showAddressAutofillResult", "Address: $address")

        // move the camera to the selected location
        moveCamera(coordinatesCurrentLocation.longitude(), coordinatesCurrentLocation.latitude())
        ignoreNextMapIdleEvent = true
        ignoreNextQueryTextUpdate = true

        // set the name of the current location in the text view
        txtCurrentLocation.text = listOfNotNull(
            address.houseNumber,
            address.street
        ).joinToString()
        searchLocation.clearFocus()

        // clear the edit text
        searchLocation.text.clear()
        searchResultsView.isVisible = false
        collapseSearchBox();
    }

    private fun navigationMenu() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->

            // check the icon for default with the actual activity

            bottomNavigationView.menu.findItem(R.id.nav_map).setIcon(R.drawable.location_icon)
            bottomNavigationView.menu.findItem(R.id.nav_report).setIcon(R.drawable.reports_icon)
            bottomNavigationView.menu.findItem(R.id.nav_shared_location).setIcon(R.drawable.share_location_icon)

            if(item.isChecked) {
                return@setOnNavigationItemSelectedListener false
            }
            when (item.itemId) {
                R.id.nav_map -> {
                    val intent = Intent(this, MapActivity::class.java)
                    intent.putExtra("token", token)
                    startActivity(intent)
                    true
                }
                R.id.nav_report -> {
                    val intent = Intent(this, ListReportsActivity::class.java)
                    intent.putExtra("token", token)
                    startActivity(intent)
                    true
                }
                R.id.nav_shared_location -> {
                    val intent = Intent(this, ContactsListActivity::class.java)
                    intent.putExtra("token", token)
                    startActivity(intent)
                    true
                }
                else -> false
            }

        }

        bottomNavigationView.menu.findItem(R.id.nav_map).setChecked(true)
    }

    private fun obtainAllLocations() {
        val service = RetrofitClient.getClient(token)
        val reportsLocations = HashMap<Int, String>()
        service.getAllReports().enqueue(object: Callback<List<Report>> {
            override fun onResponse(call: Call<List<Report>>, response: Response<List<Report>>) {
                val reports = response.body()
                if (reports != null) {
                    for(report in reports) {
                        reportsLocations[report.id] = report.type
                    }
                }
            }

            override fun onFailure(call: Call<List<Report>>, t: Throwable) {
                Log.e("Error MAP", t.message.toString())
            }
        })

        service.getLocations().enqueue(object: Callback<List<Beans.Location>> {
            override fun onResponse(call: Call<List<Beans.Location>>, response: Response<List<Beans.Location>>) {
                val locations = response.body()
                Log.d("MapActivity", "Locations: $locations") // Log the locations
                if (locations != null) {
                    for (location in locations) {
                        if(location.alatitude == 0.0 && location.alongitude == 0.0) continue

                        var typeReport = R.drawable.alert_marker
                        when (reportsLocations[location.idReport]) {
                            "Robo" -> typeReport = R.drawable.alert_marker
                            "Accidente" -> typeReport = R.drawable.accident_marker
                            "Falta de iluminación" -> typeReport = R.drawable.illumination_marker
                            "Acoso" -> typeReport = R.drawable.acoso_marker
                            "Otro" -> typeReport = R.drawable.other_marker
                        }

                        addMarker(location.alatitude, location.alongitude, typeReport)
                    }
                }
            }

            override fun onFailure(call: Call<List<Beans.Location>>, t: Throwable) {
                Log.e("Error MAP", t.message.toString())
            }
        })

    }

    private fun addMarker(latitude: Double, longitude: Double, svgResId: Int) {
        val drawable = AppCompatResources.getDrawable(this, svgResId)
        val bitmap = drawableToBitmap(drawable!!)

        val point = Point.fromLngLat(longitude, latitude)
        val annotationApi = mapView.annotations
        val pointAnnotationManager = annotationApi.createPointAnnotationManager()
        val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
            .withPoint(point)
            .withIconImage(bitmap)
            .withIconSize(0.5)
        pointAnnotationManager.create(pointAnnotationOptions)
    }
    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun locateCurrentPosition() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
            return
        }

        val locationComponentPlugin: LocationComponentPlugin = mapView.location
        locationComponentPlugin.updateSettings {
            enabled = true
            locationPuck = LocationPuck2D(
                topImage = null,
                bearingImage = null,
                shadowImage = null
            )
        }

        locationComponentPlugin.addOnIndicatorPositionChangedListener { point ->
            val latitude = point.latitude()
            val longitude = point.longitude()

            if (c == 0) {
                obtainNamePlace(longitude, latitude)
                moveCamera(longitude, latitude)
                sharedGlobalCoordinates()
                c++
            }
        }
    }


    private fun obtainNamePlace(longitude: Double, latitude: Double) {
        // obtaining the name of the current location
        val service = RetrofitClientMapbox.getClient()
        Log.i("Geocoding API obtainNamePlace", "Longitude: $longitude, Latitude: $latitude")
        service.getPlace(longitude, latitude, getString(R.string.mapbox_access_token)).enqueue(object: Callback<PropertiesPlace> {
            override fun onResponse(call: Call<PropertiesPlace>, response: Response<PropertiesPlace>) {
                val place = response.body()
                Log.i("Place", place.toString())

                // Set the name of the current location
                currentLocation = place?.features?.get(0)?.properties?.name_preferred.toString()
                coordinatesCurrentLocation = Point.fromLngLat(longitude, latitude)
                txtCurrentLocation.text = currentLocation

                sharedGlobalCoordinates() // Store coordinates if needed
            }

            override fun onFailure(call: Call<PropertiesPlace>, t: Throwable) {
                Log.e("Error MAP", t.message.toString())
            }
        })
    }


    private fun moveCamera(longitude: Double, latitude: Double) {
        mapView.camera.easeTo(
            CameraOptions.Builder()
                .center(Point.fromLngLat(longitude, latitude))
                .zoom(15.0)
                .build()
        )
    }

    private fun setupMap() {
        mapView.logo.updateSettings {
            enabled = false
        }

        mapView.compass.updateSettings {
            enabled = false
        }

        // disable scale bar of mapbox on the top left corner of the screen
        mapView.scalebar.updateSettings {
            enabled = false
        }

        // disable the information icon next of mapbox logo
        mapView.attribution.updateSettings {
            enabled = false
        }
    }

    private fun listenKeyboard() {
        val mainLayout = findViewById<ConstraintLayout>(R.id.main)
        mainLayout.viewTreeObserver.addOnGlobalLayoutListener {
            val r = Rect()
            mainLayout.getWindowVisibleDisplayFrame(r)
            val screenHeight = mainLayout.rootView.height
            val keypadHeight = screenHeight - r.bottom
            if (keypadHeight > screenHeight * 0.15) {
                // Teclado visible, expandir caja de búsqueda
                if (!isKeyboardVisible) {
                    expandSearchBox()
                    isKeyboardVisible = true
                }
            } else {
                if (isKeyboardVisible) {
                    collapseSearchBox()
                    isKeyboardVisible = false
                }
            }
        }

        expandArrowManually()
    }

    private fun expandArrowManually() {
        expandArrow!!.setOnClickListener { v: View? ->
            if (isExpanded) {
                collapseSearchBox()
            } else {
                expandSearchBox()
            }
            toggleArrow()
            isExpanded = !isExpanded
        }

        compressedArrow!!.setOnClickListener { v: View? ->
            if (isExpanded) {
                collapseSearchBox()
            } else {
                expandSearchBox()
            }
            toggleArrow()
            isExpanded = !isExpanded
        }
    }

    private fun expandSearchBox() {
        expandArrow!!.visibility = View.GONE
        compressedArrow!!.visibility = View.VISIBLE
        val animator = ValueAnimator.ofInt(searchBox.height, 800) // Expande a 400dp
        animator.addUpdateListener { valueAnimator: ValueAnimator ->
            val `val` = valueAnimator.animatedValue as Int
            searchBox.layoutParams.height = `val`
            searchBox.requestLayout()
        }
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.setDuration(300)
        animator.start()
    }

    private fun collapseSearchBox() {
        expandArrow!!.visibility = View.VISIBLE
        compressedArrow!!.visibility = View.GONE
        val animator = ValueAnimator.ofInt(searchBox.height, 120) // Contrae a 120dp
        animator.addUpdateListener { valueAnimator: ValueAnimator ->
            val `val` = valueAnimator.animatedValue as Int
            searchBox.layoutParams.height = `val`
            searchBox.requestLayout()
        }
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.setDuration(300)
        animator.start()
    }
    private fun toggleArrow() {
        if (isExpanded) {
            expandArrow!!.visibility = View.VISIBLE
            compressedArrow!!.visibility = View.GONE
        } else {
            expandArrow!!.visibility = View.GONE
            compressedArrow!!.visibility = View.VISIBLE
        }
    }
    // Function to calculate the distance between two coordinates
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371.0 // Radius of the Earth in kilometers
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }
    // Function to check proximity to reports and create alerts if needed
    private fun checkProximityToReports(userLat: Double, userLon: Double) {
        val radius = 0.5 // Radius in kilometers (0.5km = 500 meters)
        val service = RetrofitClient.getClient(token)

        service.getLocations().enqueue(object : Callback<List<Beans.Location>> {
            override fun onResponse(call: Call<List<Beans.Location>>, response: Response<List<Beans.Location>>) {
                val locations = response.body()
                if (locations != null) {
                    var nearbyAlertCount = 0 // To count how many alerts are within the radius

                    for (location in locations) {
                        if (location.alatitude == 0.0 && location.alongitude == 0.0) continue

                        // Check if the report was already processed
                        if (processedReports.contains(location.idReport)) {
                            continue // Skip if alert was already created for this report
                        }

                        // Calculate the distance to the user
                        val distance = calculateDistance(userLat, userLon, location.alatitude, location.alongitude)
                        if (distance <= radius) {
                            // Increment the count of alerts in proximity
                            nearbyAlertCount++

                            // Create a new alert for each location within the radius
                            createNewAlert(location)

                            // Mark this report as processed
                            processedReports.add(location.idReport)
                        }
                    }

                    // Only show a popup if more than one alert was detected
                    if (nearbyAlertCount > 1) {
                        showAlertPopupForMultipleAlerts()
                    }
                }
            }

            override fun onFailure(call: Call<List<Beans.Location>>, t: Throwable) {
                Log.e("Error", "Failed to fetch locations: ${t.message}")
            }
        })
    }
    private fun createNewAlert(location: Beans.Location) {
        // Retrieve userId from Shared Preferences
        val sharedPref = getSharedPreferences("GlobalPrefs", MODE_PRIVATE)
        val userId = sharedPref.getInt("userId", 0) // Default to 0 if not found

// Log the retrieved userId to check if it's correct
        Log.i("Alert", "Retrieved User ID: $userId")

        if (userId == 0) {
            Log.e("Alert", "User ID not found in shared preferences.")
            return // Abort if userId is not found
        }

        // Fetch the report details based on idReport
        val service = RetrofitClient.getClient(token)
        service.getAllReports().enqueue(object : Callback<List<Report>> {
            override fun onResponse(call: Call<List<Report>>, response: Response<List<Report>>) {
                val reports = response.body()
                if (reports != null) {
                    val report = reports.find { it.id == location.idReport }
                    if (report != null) {
                        // Create the AlertSchema object with userId
                        val alertSchema = AlertSchema(
                            location = report.address,
                            type = report.type,
                            description = report.detail, // Optional description
                            idUser = userId,  // Use the retrieved userId here
                            image_url = report.image, // Pass the image URL if available, otherwise set to null
                            idReport = report.id
                        )

                        // Check if an identical alert exists before posting
                        checkForDuplicateAlert(alertSchema) { exists ->
                            if (!exists) {
                                // Post the alert only if it doesn't exist
                                postAlert(alertSchema)
                            } else {
                                Log.i("Alert", "Duplicate alert found, skipping creation.")
                            }
                        }
                    }
                }
            }

            override fun onFailure(call: Call<List<Report>>, t: Throwable) {
                Log.e("Error", "Failed to fetch reports: ${t.message}")
            }
        })
    }


    // Function to check for duplicate alerts based on the id or alert schema
    private fun checkForDuplicateAlert(alertSchema: AlertSchema, callback: (exists: Boolean) -> Unit) {
        val service = RetrofitClient.getClient(token)

        // Query to get all alerts for the user
        service.getAllAlerts().enqueue(object : Callback<List<Alert>> {
            override fun onResponse(call: Call<List<Alert>>, response: Response<List<Alert>>) {
                val alerts = response.body()
                if (alerts != null) {
                    // Check if any alert matches the current alert schema or has the same id
                    val duplicateAlert = alerts.find {
                                (it.location == alertSchema.location &&
                                        it.type == alertSchema.type &&
                                        it.description == alertSchema.description)
                    }
                    callback(duplicateAlert != null) // Return true if duplicate is found, else false
                } else {
                    callback(false) // No alerts, so no duplicates
                }
            }

            override fun onFailure(call: Call<List<Alert>>, t: Throwable) {
                Log.e("Error", "Failed to fetch alerts: ${t.message}")
                callback(false) // On failure, assume no duplicates
            }
        })
    }

    // Function to show a popup when multiple alerts are detected
    private fun showAlertPopupForMultipleAlerts() {
        val alertDialog = android.app.AlertDialog.Builder(this)
        alertDialog.setTitle("Alerta Detectada")
        alertDialog.setMessage("Se ha detectado una alerta en la zona.")
        alertDialog.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
        alertDialog.show()
    }
    // Function to post an alert
    private fun postAlert(alertSchema: AlertSchema) {
        val service = RetrofitClient.getClient(token)

        // Debugging: Log the alertSchema before posting it
        Log.d("AlertSchema", "Posting alert: $alertSchema")

        service.postAlert(alertSchema).enqueue(object : Callback<Alert> {
            override fun onResponse(call: Call<Alert>, response: Response<Alert>) {
                if (response.isSuccessful) {
                    Log.i("Alert", "New alert posted successfully.")
                } else {
                    Log.e("Alert Error", "Failed to post alert: ${response.errorBody()?.string()}")
                }
            }


            override fun onFailure(call: Call<Alert>, t: Throwable) {
                Log.e("Alert Error", "Error posting alert: ${t.message}")
            }
        })

    }
    private fun deleteAllAlerts() {
        val service = RetrofitClient.getClient(token)

        service.deleteAllAlerts().enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.i("MapActivity", "All alerts deleted successfully.")
                } else {
                    Log.e("MapActivity", "Failed to delete alerts: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("MapActivity", "Error deleting alerts: ${t.message}")
            }
        })
    }

}