package com.innovatech.peaceapp.StartingPoint

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.innovatech.peaceapp.GlobalToken
import com.innovatech.peaceapp.GlobalUserEmail
import com.innovatech.peaceapp.Map.MapActivity
import com.innovatech.peaceapp.Profile.Beans.UserProfile
import com.innovatech.peaceapp.Profile.Beans.UserProfileSchema
import com.innovatech.peaceapp.R
import com.innovatech.peaceapp.StartingPoint.Beans.User
import com.innovatech.peaceapp.StartingPoint.Beans.UserAuth
import com.innovatech.peaceapp.StartingPoint.Beans.UserAuthenticated
import com.innovatech.peaceapp.StartingPoint.Beans.UserSchema
import com.innovatech.peaceapp.StartingPoint.Models.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignUpActivity : AppCompatActivity() {

    private lateinit var etName:EditText
    private lateinit var etLastName:EditText
    private lateinit var etPhone:EditText
    private lateinit var btnSignUp:Button
    private lateinit var btnSignIn:TextView
    private lateinit var edtEmail:TextView
    private lateinit var edtPassword:TextView

    private lateinit var ivEye:ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initComponents()


        btnSignUp.setOnClickListener {
            if(validateSignUpFields())
                saveUserCredentials()
        }

        btnSignIn.setOnClickListener{
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }

        ivEye.setOnClickListener{
            changePasswordVisibility()
        }

    }

    private fun initComponents() {
        etName = findViewById(R.id.et_name)
        etLastName = findViewById(R.id.et_surname)
        etPhone = findViewById(R.id.et_phone)
        btnSignUp = findViewById(R.id.btn_signup)
        btnSignIn = findViewById(R.id.tv_log_in)
        edtEmail = findViewById(R.id.et_email)
        edtPassword = findViewById(R.id.et_password)
        ivEye = findViewById(R.id.iv_eye)
    }

    private fun saveUserCredentials(){
        val service = RetrofitClient.placeHolder

        var roles = listOf("ROLE_USER")

        val user = UserSchema(
            edtEmail.text.toString(),
            edtPassword.text.toString(),
            roles)

        service.signUp(user).enqueue(object : Callback<User> {
            override fun onResponse(p0: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    val user = response.body()
                    if (user?.username != null) {
                        authenticateUser()
                    }
                }
            }

            override fun onFailure(p0: Call<User>, p1: Throwable) {
                Toast.makeText(this@SignUpActivity, "Error: ${p1.message}", Toast.LENGTH_LONG).show()
                Log.i("SIGNUPERROR1", p1.message.toString())
            }
        })
    }

    private fun authenticateUser(){
        val service = RetrofitClient.placeHolder

        service.signIn(UserAuth(edtEmail.text.toString(), edtPassword.text.toString())).enqueue(object : Callback<UserAuthenticated> {
            override fun onResponse(p0: Call<UserAuthenticated>, response: Response<UserAuthenticated>) {
                if (response.isSuccessful) {
                    val user = response.body()
                    if (user?.username != null) {
                        val sharedPref = getSharedPreferences("GlobalPrefs", MODE_PRIVATE)
                        with(sharedPref.edit()) {
                            putString("userId", user.id.toString())
                            apply()
                        }


                        GlobalToken.setToken(user.token)

                        Log.i("Token", user.token.toString())

                        GlobalUserEmail.setEmail(user.username)

                        saveUser(user.id, user.token)
                    }
                }
            }

            override fun onFailure(p0: Call<UserAuthenticated>, p1: Throwable) {
                Toast.makeText(this@SignUpActivity, "Error: ${p1.message}", Toast.LENGTH_LONG).show()
                Log.i("SIGNUPERROR3", p1.message.toString())
            }
        })
    }

    private fun saveUser(user_id: Int, token: String){
        val serviceUserProfile = com.innovatech.peaceapp.Profile.Models.RetrofitClient.getClient(token)

        val userProfile = UserProfileSchema(
            etName.text.toString(),
            etLastName.text.toString(),
            etPhone.text.toString(),
            edtEmail.text.toString(),
            edtPassword.text.toString(),
            user_id.toString(),
            "https://static.vecteezy.com/system/resources/previews/009/292/244/non_2x/default" +
                    "-avatar-icon-of-social-media-user-vector.jpg", // default user image
            )

        serviceUserProfile.createUser(userProfile).enqueue(object : Callback<UserProfile> {
            override fun onResponse(p0: Call<UserProfile>, response: Response<UserProfile>) {
                if (response.isSuccessful) {
                    val userProfile = response.body()
                    if (userProfile?.name != null) {


                        Log.i("UserInSignUp", userProfile.id.toString()+ " "+userProfile.name + " "
                                + userProfile.lastname + " " + userProfile.email + " " + userProfile.phonenumber + " " +
                                userProfile.password + " " + userProfile.user_id + " " + userProfile.profile_image)


                        showCorrectSignUpDialog(token)
                    }
                }
            }

            override fun onFailure(p0: Call<UserProfile>, p1: Throwable) {
                Toast.makeText(this@SignUpActivity, "Error: ${p1.message}", Toast.LENGTH_LONG).show()
                Log.i("ErrorSIGNUPPPPPPP2", p1.message.toString())
            }
        })
    }

    private fun navigateToMapActivity(token: String){
        val intent = Intent(this, MapActivity::class.java)
        intent.putExtra("token", token)
        startActivity(intent)
    }

    private fun showCorrectSignUpDialog(token: String){
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_correct_signup)
        // to set a transparent background
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnContinue = dialog.findViewById<Button>(R.id.btnContinue)

        btnContinue.setOnClickListener {
            dialog.hide()
            navigateToMapActivity(token)
        }

        dialog.show()
    }

    private fun validateSignUpFields(): Boolean{
        if(etName.text.isEmpty() || etLastName.text.isEmpty() || etPhone.text.isEmpty() || edtEmail.text.isEmpty() || edtPassword.text.isEmpty()){
            showIncorrectSignUpDialog("Asegúrate de llenar todos los campos")
            return false
        }
        // validate email
        if(!android.util.Patterns.EMAIL_ADDRESS.matcher(edtEmail.text).matches()){
            // show toast message
            showIncorrectSignUpDialog("Email no válido")
            return false
        }
        // validate only numbers in phone
        if(!android.util.Patterns.PHONE.matcher(etPhone.text).matches()){
            // show toast message
            showIncorrectSignUpDialog("Teléfono no válido")
            return false
        }
        // validate password
        if(edtPassword.text.length < 6){
            // show toast message
            showIncorrectSignUpDialog("La contraseña debe tener al menos 6 caracteres")
            return false
        }
        // validate phone
        if(etPhone.text.length != 9){
            // show toast message
            showIncorrectSignUpDialog("El teléfono debe tener 9 dígitos")
            return false
        }
        return true
    }

    private fun showIncorrectSignUpDialog(texto: String){
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
    private fun changePasswordVisibility(){
        if(edtPassword.inputType == 129){
            edtPassword.inputType = 1
            // change the icon
            ivEye.setImageResource(R.drawable.ic_closed_eye)
        }else{
            edtPassword.inputType = 129
            ivEye.setImageResource(R.drawable.ic_open_eye)
        }
    }

}