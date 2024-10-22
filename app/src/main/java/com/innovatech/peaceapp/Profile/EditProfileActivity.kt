package com.innovatech.peaceapp.Profile

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
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
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.innovatech.peaceapp.GlobalToken
import com.innovatech.peaceapp.GlobalUserEmail
import com.innovatech.peaceapp.Profile.Beans.UserEditSchema
import com.innovatech.peaceapp.Profile.Beans.UserProfile
import com.innovatech.peaceapp.Profile.Models.RetrofitClient
import com.innovatech.peaceapp.R
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class EditProfileActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etSurname: EditText
    private lateinit var etPhone: EditText
    private lateinit var tvEmail: TextView
    private lateinit var tvPassword: TextView
    private lateinit var ivProfileImage: ImageView

    private lateinit var btnCancel: Button
    private lateinit var btnSave: Button

    private lateinit var token: String
    private lateinit var email: String
    private lateinit var user: UserProfile

    private val REQUIRED_PERMISSIONS = arrayOf(
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    private val REQUEST_CODE_IMAGE_PICKER = 102
    private val REQUEST_CODE_PERMISSIONS = 101
    private lateinit var cloudinary: Cloudinary
    private lateinit var imgBitmap: Bitmap


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        user = intent.getSerializableExtra("user") as UserProfile

        token = GlobalToken.token
        email = GlobalUserEmail.email

        initComponents()
        loadUserData()

        configCloudinary()
        initListeners()
    }

    private fun configCloudinary() {
        cloudinary = Cloudinary(
            ObjectUtils.asMap(
                "cloud_name", "dqawjz3ih",
                "api_key", getString(R.string.cloudinary_api_secret),
                "api_secret", getString(R.string.cloudinary_api_secret)
            )
        )
    }

    private fun initComponents() {
        etName = findViewById(R.id.etName)
        etSurname = findViewById(R.id.etSurname)
        etPhone = findViewById(R.id.etPhone)
        tvEmail = findViewById(R.id.txt_user_email)
        tvPassword = findViewById(R.id.txt_user_password)
        ivProfileImage = findViewById(R.id.ivProfile)

        btnCancel = findViewById(R.id.btnCancel)
        btnSave = findViewById(R.id.btnSave)
    }

    private fun loadUserData() {
        etName.setText(user.name)
        etSurname.setText(user.lastname)
        etPhone.setText(user.phonenumber)
        tvEmail.text = user.email
        tvPassword.text = user.password
        Picasso.get().load(user.profile_image).into(ivProfileImage)
    }

    private fun initListeners() {
        btnCancel.setOnClickListener() {
            showCancelEditDialog()
        }
        btnSave.setOnClickListener() {
            if(validateFields()){
                showConfirmationDialog()
            }
        }
        ivProfileImage.setOnClickListener() {
            requestPermissions()
        }
    }
    private fun showConfirmationDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_confirm_edit)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)

        val btnContinue = dialog.findViewById<Button>(R.id.btnContinue)
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)

        btnContinue.setOnClickListener() {
            lifecycleScope.launch {
                updateUser()
            }
            dialog.dismiss()
        }
        btnCancel.setOnClickListener() {
            dialog.dismiss()
        }
        dialog.show()
    }

    private suspend fun updateUser(){
        val name = etName.text.toString()
        val surname = etSurname.text.toString()
        val phone = etPhone.text.toString()
        val urlImage = uploadImage()
        Log.i("URL", urlImage.toString())

        val editedUser = UserEditSchema(name, surname, phone, urlImage.toString())

        val service = RetrofitClient.getClient(token)
        service.updateUser(user.id, editedUser)
            .enqueue(object : retrofit2.Callback<UserProfile> {
                override fun onResponse(call: retrofit2.Call<UserProfile>, response: retrofit2.Response<UserProfile>) {
                    if (response.isSuccessful) {
                        val userProfile = response.body()
                        if (userProfile?.name != null) {
                            val intent = Intent(this@EditProfileActivity, MainProfileActivity::class.java)
                            startActivity(intent)
                        }
                    }
                }

                override fun onFailure(call: retrofit2.Call<UserProfile>, t: Throwable) {
                    t.printStackTrace()
                }
            })

    }

    private fun validateFields(): Boolean {
        if (etName.text.isEmpty() || etSurname.text.isEmpty() || etPhone.text.isEmpty()) {
            showIncorrectEditDialog("Asegúrate de llenar todos los campos")
            return false
        }
        if (!android.util.Patterns.PHONE.matcher(etPhone.text).matches()) {
            showIncorrectEditDialog("Teléfono no válido")
            return false
        }
        if(etPhone.text.length != 9) {
            showIncorrectEditDialog("El teléfono debe tener 9 dígitos")
            return false
        }

        else {
            return true
        }
    }

    private fun showIncorrectEditDialog(texto: String){
        val dialog = Dialog(this)

        dialog.setContentView(R.layout.dialog_incorrect_signup)

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)


        val btnContinue = dialog.findViewById<Button>(R.id.btnContinue)
        val tvMensaje = dialog.findViewById<TextView>(R.id.tvIncorrectSignup)

        tvMensaje.text = texto

        btnContinue.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showCancelEditDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_cancel_edit)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)

        val btnDescartar = dialog.findViewById<Button>(R.id.btnDescartar)
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)

        btnDescartar.setOnClickListener() {
            dialog.dismiss()
            finish()
        }
        btnCancel.setOnClickListener() {
            dialog.dismiss()
        }
        dialog.show()
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
        ivProfileImage.setImageBitmap(bitmap)
        imgBitmap = bitmap
    }

    private fun showImgPreviewFromBitmap(bitmap: Bitmap) {
        ivProfileImage.setImageBitmap(bitmap)
        imgBitmap = bitmap
    }

    private suspend fun uploadImage(): String? {
        var url: String? = null

        if (!this::imgBitmap.isInitialized) {
            url = "https://static.vecteezy.com/system/resources/thumbnails/009/292/244/small/default-avatar-icon-of-social-media-user-vector.jpg"
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



    private fun saveBitmap(bitmap: Bitmap) {
        val file = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "evidence.jpg")
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.flush()
        outputStream.close()
        Toast.makeText(this, "Image saved", Toast.LENGTH_SHORT).show()
    }

}