package com.innovatech.peaceapp.Profile

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.innovatech.peaceapp.GlobalToken
import com.innovatech.peaceapp.GlobalUserEmail
import com.innovatech.peaceapp.Profile.Beans.UserProfile
import com.innovatech.peaceapp.Profile.Models.RetrofitClient
import com.innovatech.peaceapp.R
import com.innovatech.peaceapp.StartingPoint.InitialActivity
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainProfileActivity : AppCompatActivity() {

    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvPhone: TextView
    private lateinit var tvPassword: TextView
    private lateinit var ivProfileImage: ImageView

    private lateinit var btnEditProfile: MaterialButton
    private lateinit var btnDeleteAccount: TextView
    private lateinit var llLogout: LinearLayout
    private lateinit var ivEye: ImageView

    private lateinit var token: String
    private lateinit var email: String

    private lateinit var user: UserProfile

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        token = GlobalToken.token
        email = GlobalUserEmail.email

        initComponents()
        loadUserData()
        initListeners()
    }

    private fun initComponents() {

        tvName = findViewById(R.id.txt_user_name)
        tvEmail = findViewById(R.id.txt_user_email)
        tvPhone = findViewById(R.id.txt_user_phone)
        tvPassword = findViewById(R.id.txt_user_password)
        ivProfileImage = findViewById(R.id.ivProfileImage)

        btnEditProfile = findViewById(R.id.btnEditar)
        btnDeleteAccount = findViewById(R.id.tvEliminar)
        llLogout = findViewById(R.id.ll_logout)
        ivEye = findViewById(R.id.iv_eye)
    }

    private fun loadUserData() {
        val service = RetrofitClient.getClient(token)

        service.getUserByEmail(email)
            .enqueue(
                object: Callback<UserProfile> {
                    override fun onResponse(call: Call<UserProfile>, response:
                    Response<UserProfile>) {
                        val userProfile = response.body()
                        if (userProfile != null) {

                            Log.i("andriush", userProfile.name+" "+userProfile.email+" " +
                                    ""+userProfile.phonenumber+" "+userProfile.password)

                            tvName.text = userProfile.name + " " + userProfile.lastname
                            tvEmail.text = userProfile.email
                            tvPhone.text = userProfile.phonenumber
                            tvPassword.text = userProfile.password

                            Picasso.get().load(userProfile.profile_image).into(ivProfileImage)

                            user = UserProfile(
                                userProfile.id,
                                userProfile.name,
                                userProfile.lastname,
                                userProfile.phonenumber,
                                userProfile.email,
                                userProfile.password,
                                userProfile.user_id,
                                userProfile.profile_image
                            )
                        }
                    }

                    override fun onFailure(p0: Call<UserProfile>, p1: Throwable) {
                        p1.printStackTrace()
                    }
                })


    }

    private fun initListeners() {
        btnEditProfile.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            intent.putExtra("user", user)
            startActivity(intent)
        }

        llLogout.setOnClickListener {
            showDeleteUserDialog()
        }

//        btnDeleteAccount.setOnClickListener {
//            Log.i("MUYBUENAS", "delete account clicked")
//            showDeleteUserDialog()
//        }

        ivEye.setOnClickListener {
            changePasswordVisibility()
        }
    }

    private fun showDeleteUserDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_delete_profile)

        // set transparent background
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnDelete = dialog.findViewById<Button>(R.id.btnEliminar)
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)

        dialog.show()

        btnDelete.setOnClickListener {
            // DE MOMENTO HAREMOS UN LOG OUT
            logout()
            //deleteUser()
        }
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun logout() {
        val intent = Intent(this, InitialActivity::class.java)
        startActivity(intent)
    }

    private fun deleteUser() {
        val service = RetrofitClient.getClient(token)

        Log.i("MUYBUENAS", user.user_id)
        service.deleteUser(user.user_id.toLong())

        navigateToInitialActivity()


    }

    private fun navigateToInitialActivity() {
        val intent = Intent(this, InitialActivity::class.java)
        startActivity(intent)
    }

    private fun changePasswordVisibility(){
        if(tvPassword.inputType == 129){
            tvPassword.inputType = 1
            // change the icon
            ivEye.setImageResource(R.drawable.ic_closed_eye)
        }else{
            tvPassword.inputType = 129
            ivEye.setImageResource(R.drawable.ic_open_eye)
        }
    }
}