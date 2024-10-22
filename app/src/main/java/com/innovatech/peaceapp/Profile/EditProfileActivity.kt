package com.innovatech.peaceapp.Profile

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
<<<<<<< Updated upstream
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.innovatech.peaceapp.GlobalToken
import com.innovatech.peaceapp.GlobalUserEmail
import com.innovatech.peaceapp.Profile.Beans.UserProfile
import com.innovatech.peaceapp.Profile.Models.RetrofitClient
import com.innovatech.peaceapp.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
=======
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.innovatech.peaceapp.GlobalToken
import com.innovatech.peaceapp.GlobalUserEmail
import com.innovatech.peaceapp.Profile.Beans.UserEditSchema
import com.innovatech.peaceapp.Profile.Beans.UserProfile
import com.innovatech.peaceapp.Profile.Models.RetrofitClient
import com.innovatech.peaceapp.R
import com.squareup.picasso.Picasso
>>>>>>> Stashed changes

class EditProfileActivity : AppCompatActivity() {

    private lateinit var etName: EditText
<<<<<<< Updated upstream
    private lateinit var etLastName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText
    private lateinit var etPassword: EditText
=======
    private lateinit var etSurname: EditText
    private lateinit var etPhone: EditText
    private lateinit var tvEmail: TextView
    private lateinit var tvPassword: TextView
    private lateinit var ivProfileImage: ImageView

    private lateinit var btnCancel: Button
>>>>>>> Stashed changes
    private lateinit var btnSave: Button

    private lateinit var token: String
    private lateinit var email: String
<<<<<<< Updated upstream
    private var user: UserProfile? = null
=======
    private lateinit var user: UserProfile

>>>>>>> Stashed changes

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        etName = findViewById(R.id.txt_user_name)
        etLastName = findViewById(R.id.txt_user_lastname)
        etEmail = findViewById(R.id.txt_user_email)
        etPhone = findViewById(R.id.txt_user_phone)
        etPassword = findViewById(R.id.txt_user_password)
        btnSave = findViewById(R.id.btn_save_Profile)

        token = GlobalToken.token
        email = GlobalUserEmail.email

        loadUserData()

        btnSave.setOnClickListener {
            saveUserData()
        }
    }

    private fun loadUserData() {
        val service = RetrofitClient.getClient(token)

        service.getUserByEmail(email).enqueue(object : Callback<UserProfile> {
            override fun onResponse(call: Call<UserProfile>, response: Response<UserProfile>) {
                if (response.isSuccessful) {
                    val userProfile = response.body()
                    if (userProfile != null) {
                        etName.setText(userProfile.name)
                        etLastName.setText(userProfile.lastname)
                        etEmail.setText(userProfile.email)
                        etPhone.setText(userProfile.phonenumber)
                        etPassword.setText(userProfile.password)

                        user = userProfile
                    }
                } else {
                    Toast.makeText(this@EditProfileActivity, "Failed to load user data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserProfile>, t: Throwable) {
                t.printStackTrace()
                Toast.makeText(this@EditProfileActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveUserData() {
        val service = RetrofitClient.getClient(token)

        user?.let {
            it.name = etName.text.toString()
            it.lastname = etLastName.text.toString()
            it.email = etEmail.text.toString()
            it.phonenumber = etPhone.text.toString()
            it.password = etPassword.text.toString()

            service.updateUser(it.id, it).enqueue(object : Callback<UserProfile> {
                override fun onResponse(call: Call<UserProfile>, response: Response<UserProfile>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@EditProfileActivity, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@EditProfileActivity, "Failed to update profile", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<UserProfile>, t: Throwable) {
                    t.printStackTrace()
                    Toast.makeText(this@EditProfileActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } ?: run {
            Toast.makeText(this, "User data is not loaded", Toast.LENGTH_SHORT).show()
        }
        user = intent.getSerializableExtra("user") as UserProfile

        token = GlobalToken.token
        email = GlobalUserEmail.email

        initComponents()
        loadUserData()
        initListeners()
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
            updateUser()
            dialog.dismiss()
        }
        btnCancel.setOnClickListener() {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun updateUser(){
        val name = etName.text.toString()
        val surname = etSurname.text.toString()
        val phone = etPhone.text.toString()
        // val profile_image =

        val editedUser = UserEditSchema(name, surname, phone, "https://i.pinimg.com/736x/1c/d6/a6/1cd6a605ed900c762b012d56e679406e.jpg")

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
}