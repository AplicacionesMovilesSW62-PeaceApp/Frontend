package com.innovatech.peaceapp.Map

import android.app.Dialog
import android.content.Intent
import android.media.Image
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.innovatech.peaceapp.GlobalToken
import com.innovatech.peaceapp.Map.Beans.Report
import com.innovatech.peaceapp.Map.Models.RetrofitClient
import com.innovatech.peaceapp.R
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ReportDetailActivity : AppCompatActivity() {
    lateinit var token: String
    private lateinit var report: Report
    private lateinit var txtTipoReporte: TextView
    private lateinit var imgReporte: ImageView
    private lateinit var txtUbicacionReporte: TextView
    private lateinit var txtFechaReporte: TextView
    private lateinit var txtDescripcionReporte: TextView
    private lateinit var btnSalirReporteDetallado: Button
    private lateinit var txtTituloReporte: TextView
    private lateinit var btnDeleteReport: ImageView
    private var userId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_report_detail)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        token = GlobalToken.token
        report = intent.getSerializableExtra("report") as Report
        txtTipoReporte = findViewById(R.id.txtTipoReporte)
        imgReporte = findViewById(R.id.imgReporte)
        txtUbicacionReporte = findViewById(R.id.txtUbicacionReporte)
        txtFechaReporte = findViewById(R.id.txtFechaReporte)
        txtDescripcionReporte = findViewById(R.id.txtDescripcionReporte)
        btnSalirReporteDetallado = findViewById(R.id.btnSalirReporteDetallado)
        txtTituloReporte = findViewById(R.id.txtTituloReporte)
        btnDeleteReport = findViewById(R.id.btnDeleteReport)

        val sharedPref = getSharedPreferences("GlobalPrefs", MODE_PRIVATE)
        userId = sharedPref.getInt("userId", 0)

        if (userId == report.idUser) btnDeleteReport.visibility = ImageView.VISIBLE


        btnSalirReporteDetallado.setOnClickListener {
            finish()
        }

        btnDeleteReport.setOnClickListener {
            showDeleteReportDialog()
        }

        setReportData()
        navigationMenu()
    }

    private fun showDeleteReportDialog(){
        val dialog = Dialog(this)

        dialog.setContentView(R.layout.dialog_delete_report)

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)
        val btnEliminar = dialog.findViewById<Button>(R.id.btnEliminar)
        val tvMensaje = dialog.findViewById<TextView>(R.id.tvDeleteReport)

        tvMensaje.text = "Estás a punto de eliminar el reporte " + "\"" + report.title + "\"" + ". ¿Estás seguro?"

        btnCancel.setOnClickListener {
            dialog.hide()
        }
        btnEliminar.setOnClickListener {
            dialog.hide()

            val service = RetrofitClient.getClient(token)
            service.deleteReport(report.id).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        finish()
                        val intent = Intent(this@ReportDetailActivity, ListReportsActivity::class.java)
                        intent.putExtra("token", token)
                        startActivity(intent)
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.e("Error", t.message.toString())
                }
            })
        }

        dialog.show()
    }

    private fun setReportData() {
        txtTituloReporte.text = report.title
        txtTipoReporte.text = report.type
        txtUbicacionReporte.text = report.address
        txtFechaReporte.text = report.createdAt.substring(0,10)
        txtDescripcionReporte.text = report.detail
        Picasso.get().load(report.image)
            .resize(300, 300)
            .centerCrop().into(imgReporte)
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
                    true
                }
                else -> false
            }

        }

        bottomNavigationView.menu.findItem(R.id.nav_report).setChecked(true)
    }
}