package com.innovatech.peaceapp.Map

import Beans.Location
import Beans.LocationSchema
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.innovatech.peaceapp.Map.Beans.Report
import com.innovatech.peaceapp.Map.Beans.ReportSchema
import com.innovatech.peaceapp.Map.Models.RetrofitClient
import com.innovatech.peaceapp.R
import com.mapbox.geojson.Point
import org.w3c.dom.Text
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils;
import com.innovatech.peaceapp.ShareLocation.ContactsListActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NewReportActivity : AppCompatActivity() {
    lateinit var token: String
    private lateinit var txtTypeReport: TextView
    private lateinit var txtLocation: TextView
    private lateinit var edtTitle: EditText
    private lateinit var edtDetail: EditText
    private lateinit var imgMoreEvidence: ImageView
    private lateinit var imgEvidence: ImageView
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var viewOverlay: View
    private val REQUEST_CODE_PERMISSIONS = 101
    private val REQUIRED_PERMISSIONS = arrayOf(
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    private val REQUEST_CODE_IMAGE_PICKER = 102
    private lateinit var cloudinary: Cloudinary
    private lateinit var imgBitmap: Bitmap
    private var userId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_new_report)
        txtTypeReport = findViewById(R.id.txtTypeReport)
        txtLocation = findViewById(R.id.txtLocation)
        edtTitle = findViewById(R.id.edtTitle)
        edtDetail = findViewById(R.id.edtDetail)
        imgMoreEvidence = findViewById(R.id.imgMoreEvidence)
        imgEvidence = findViewById(R.id.imgEvidence)
        btnSave = findViewById(R.id.btnSave)
        btnCancel = findViewById(R.id.btnCancel)

        // recovering the current location coordinates
        val typeReport = intent.getStringExtra("type")
        val sharedPref = getSharedPreferences("GlobalPrefs", MODE_PRIVATE)
        val latitude = sharedPref.getString("latitude", "0.0")!!.toDouble()
        val longitude = sharedPref.getString("longitude", "0.0")!!.toDouble()
        val currentLocation = sharedPref.getString("currentLocation", "No location found")!!
        userId = sharedPref.getInt("userId", 0)

        txtLocation.hint = currentLocation
        token = intent.getStringExtra("token")!!

        btnCancel.setOnClickListener {
            val intent = Intent(this, ListReportsActivity::class.java)
            intent.putExtra("token", token)
            startActivity(intent)
        }

        btnSave.setOnClickListener {
            progressBar = findViewById(R.id.progressBar)
            viewOverlay = findViewById(R.id.loadingOverlay)
            progressBar.visibility = View.VISIBLE
            viewOverlay.visibility = View.VISIBLE

            val title = edtTitle.text.toString()
            val detail = edtDetail.text.toString()

            if(validateFields(title, detail)) {
                lifecycleScope.launch {
                    saveReport(title, detail, latitude, longitude, typeReport.toString(), currentLocation)
                    progressBar.visibility = View.GONE
                    viewOverlay.visibility = View.GONE
                }
            }else {
                progressBar.visibility = View.GONE
                viewOverlay.visibility = View.GONE
            }


        }

        configCloudinary()
        saveImageEvidence()
        navigationMenu()
    }

    private fun configCloudinary() {
        cloudinary = Cloudinary(
            ObjectUtils.asMap(
                "cloud_name", "dqawjz3ih",
                "api_key", getString(R.string.cloudinary_api_key),
                "api_secret", getString(R.string.cloudinary_api_secret)
            )
        )
    }

    private fun navigationMenu() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
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

        bottomNavigationView.menu.findItem(R.id.nav_report).setChecked(true)
    }

    private suspend fun saveReport(title: String, detail: String, latitude: Double, longitude: Double, typeReport: String, currentLocation: String) {
        val service = RetrofitClient.getClient(token)

        val urlImage = uploadImage()
        Log.i("URL cloudinary", urlImage.toString())

        val report = ReportSchema(title, detail, typeReport, userId, urlImage, currentLocation)
        // obtain the id of the report created

        service.postReport(report).enqueue(object : Callback<Report> {
            override fun onResponse(call: Call<Report>, response: Response<Report>) {
                if (response.isSuccessful) {
                    val report = response.body()
                    if (report != null) {
                        Log.d("Report", report.toString())
                        saveLocation(latitude.toString(), longitude.toString(), report.id)
                    }
                }
            }

            override fun onFailure(call: Call<Report>, t: Throwable) {
                Log.e("Error", t.message.toString())
            }
        })
    }

    private fun saveLocation(latitude: String, longitude: String, idReport: Int) {
        val service = RetrofitClient.getClient(token)

        val location = LocationSchema(latitude, longitude, idReport)

        service.postLocation(location).enqueue(object : Callback<Location> {
            override fun onResponse(call: Call<Location>, response: Response<Location>) {
                if (response.isSuccessful) {
                    val location = response.body()
                    if (location != null) {
                        Log.d("Location", location.toString())
                    }

                    showCorrectReportSaved()
                }
            }

            override fun onFailure(call: Call<Location>, t: Throwable) {
                Log.e("Error", t.message.toString())
            }
        })
    }

    private fun saveImageEvidence() {
        imgMoreEvidence.setOnClickListener {
            // open the options camera, gallery and file
            requestPermissions()
        }
    }

    private fun requestPermissions() {
        if (REQUIRED_PERMISSIONS.all {
                ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            }) {
            openImageOptions()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                openImageOptions()
            } else {
                Toast.makeText(this, "Permissions not granted", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openImageOptions() {
        val options = arrayOf("Camera", "Gallery")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Image From")
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> openCamera()
                1 -> openGallery()
            }
        }
        builder.show()
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQUEST_CODE_IMAGE_PICKER)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_CODE_IMAGE_PICKER)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_IMAGE_PICKER && resultCode == Activity.RESULT_OK) {
            // Caso de la galería
            val imageUri: Uri? = data?.data
            if (imageUri != null) {
                showImgPreviewFromUri(imageUri)
            } else {
                // Caso de la cámara
                val imageBitmap = data?.extras?.get("data") as? Bitmap
                if (imageBitmap != null) {
                    showImgPreviewFromBitmap(imageBitmap)
                }
            }
        }
    }

    private fun showImgPreviewFromUri(imageUri: Uri) {
        val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri))
        imgEvidence.setImageBitmap(bitmap)
        imgBitmap = bitmap
    }

    private fun showImgPreviewFromBitmap(bitmap: Bitmap) {
        imgEvidence.setImageBitmap(bitmap)
        imgBitmap = bitmap
    }

    private suspend fun uploadImage(): String? {
        var url: String? = null

        if (!this::imgBitmap.isInitialized) {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
            return url
        }

        val file = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "temp.jpg")
        val outputStream = FileOutputStream(file)
        imgBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.flush()
        outputStream.close()

        url = withContext(Dispatchers.IO) {
            val result = cloudinary.uploader().upload(file, ObjectUtils.emptyMap())
            result["url"].toString()
        }

        return url
    }

    private fun validateFields(title: String, detail: String): Boolean {
        if(title.isEmpty() || detail.isEmpty()) {
            showIncorrectSignUpDialog("Asegúrate de llenar todos los campos")
            return false
        }

        if(imgEvidence.drawable == null || !this::imgBitmap.isInitialized) {
            showIncorrectSignUpDialog("Asegúrate de subir una imagen")
            return false
        }

        return true
    }

    private fun showIncorrectSignUpDialog(texto: String){
        val dialog = Dialog(this)

        dialog.setContentView(R.layout.dialog_incorrect_signup)

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnContinue = dialog.findViewById<Button>(R.id.btnContinue)
        val tvMensaje = dialog.findViewById<TextView>(R.id.tvIncorrectSignup)

        tvMensaje.text = texto

        btnContinue.setOnClickListener {
            dialog.hide()
        }

        dialog.show()
    }

    private fun showCorrectReportSaved(){
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_correct_report)
        // to set a transparent background
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnContinue = dialog.findViewById<Button>(R.id.btnContinue)

        btnContinue.setOnClickListener {
            dialog.hide()
            val intent = Intent(this@NewReportActivity, MapActivity::class.java)
            intent.putExtra("token", token)
            startActivity(intent)
        }

        dialog.show()
    }
}