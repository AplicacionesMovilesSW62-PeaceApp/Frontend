package com.innovatech.peaceapp.StartingPoint

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Email
import android.util.Log
import android.widget.Button
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
import com.innovatech.peaceapp.R
import com.innovatech.peaceapp.StartingPoint.Beans.User
import com.innovatech.peaceapp.StartingPoint.Beans.UserAuth
import com.innovatech.peaceapp.StartingPoint.Beans.UserAuthenticated
import com.innovatech.peaceapp.StartingPoint.Models.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignInActivity : AppCompatActivity() {

    private lateinit var btnSignIn: Button
    private lateinit var btnSignUp: TextView

    private lateinit var edtEmail: TextView
    private lateinit var edtPassword: TextView

    private lateinit var ivEye: ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_in)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initComponents()
        initListeners()
    }

    private fun initComponents() {
        btnSignIn = findViewById<Button>(R.id.btn_signin)
        btnSignUp = findViewById<TextView>(R.id.tv_create_account)
        edtEmail = findViewById<TextView>(R.id.et_email)
        edtPassword = findViewById<TextView>(R.id.et_password)
        ivEye = findViewById<ImageView>(R.id.iv_eye)
    }

    private fun initListeners() {
        btnSignIn.setOnClickListener {

            if(validateSignUpFields())
                signIn(edtEmail.text.toString(), edtPassword.text.toString())
        }

        btnSignUp.setOnClickListener{
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        ivEye.setOnClickListener {
            changePasswordVisibility()
        }
    }
    private fun signIn(email:String, password:String){
        val service = RetrofitClient.placeHolder

        val user = UserAuth(email, password)
        service.signIn(user).enqueue(object : Callback<UserAuthenticated> {
            override fun onResponse(p0: Call<UserAuthenticated>, response: Response<UserAuthenticated>) {
                if(response.isSuccessful){
                    val user = response.body()
                    if(user?.username != null){
                        val intent = Intent(this@SignInActivity, MapActivity::class.java)
                        val token = user.token

                        val sharedPref = getSharedPreferences("GlobalPrefs", MODE_PRIVATE)
                        with(sharedPref.edit()) {
                            putInt("userId", user.id)
                            apply()
                        }

                        GlobalToken.setToken(token)
                        GlobalUserEmail.setEmail(email)

                        intent.putExtra("token", token)
                        startActivity(intent)
                    }else {
                        Log.e("Mensaje", user!!.message)
                        // mostrar el dialog de error
                        when(user.message){
                            "User not found" -> showIncorrectSignInDialog("No existe un usuario " +
                                    "con este correo")
                            "Invalid password" -> showIncorrectSignInDialog("La contraseña es " +
                                    "incorrecta")
                            else -> showIncorrectSignInDialog("Error desconocido")
                        }

                    }
                }
            }
            override fun onFailure(p0: Call<UserAuthenticated>, p1: Throwable) {
                Toast.makeText(this@SignInActivity, "Error: ${p1.message}", Toast.LENGTH_LONG).show()
                Log.e("ERROR", p1.message.toString())
            }
        })
    }
    private fun showIncorrectSignInDialog(mensaje: String){
        val dialog = Dialog(this)

        dialog.setContentView(R.layout.dialog_incorrect_signup)

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnContinue = dialog.findViewById<Button>(R.id.btnContinue)
        val tvMensaje = dialog.findViewById<TextView>(R.id.tvIncorrectSignup)

        tvMensaje.text = mensaje

        btnContinue.setOnClickListener {
            dialog.hide()
        }

        dialog.show()
    }

    private fun validateSignUpFields():Boolean{
        if(edtEmail.text.isEmpty() || edtPassword.text.isEmpty()){
            showIncorrectSignInDialog("Asegúrate de llenar todos los campos")
            return false
        }
        return true
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