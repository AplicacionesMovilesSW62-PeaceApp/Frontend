package com.innovatech.peaceapp.ShareLocation

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.telephony.SmsManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.innovatech.peaceapp.GlobalToken
import com.innovatech.peaceapp.Map.ListReportsActivity
import com.innovatech.peaceapp.Map.MapActivity
import com.innovatech.peaceapp.R
import com.innovatech.peaceapp.ShareLocation.Beans.Contact
import kotlinx.coroutines.launch

class ContactsListActivity : AppCompatActivity() {

        private lateinit var etSearch: EditText
        private lateinit var btnSendLocation: Button
        private lateinit var rvContacts: RecyclerView
        private lateinit var contactAdapter: Adapter
        private val selectedContacts = mutableListOf<Contact>()
        private var latitude = ""
        private var longitude = ""
        private lateinit var token: String

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_contacts_list)
            val sharedPref = getSharedPreferences("GlobalPrefs", MODE_PRIVATE)
            latitude = sharedPref.getString("latitude", "0.0")!!.toString()
            longitude = sharedPref.getString("longitude", "0.0")!!.toString()
            token = GlobalToken.token


            lifecycleScope.launch {
                askForPermissions()
            }
            initComponents()

            val contactList = loadContacts()
            contactAdapter = Adapter(contactList) { contact, isSelected ->
                if (isSelected) {
                    selectedContacts.add(contact)
                } else {
                    selectedContacts.remove(contact)
                }
            }
            rvContacts.layoutManager = LinearLayoutManager(applicationContext)
            rvContacts.adapter = contactAdapter

            btnSendLocation.setOnClickListener {
                if(selectedContacts.isEmpty()) {
                    Toast.makeText(this, "Selecciona al menos un contacto para continuar", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                sendLocationToSelectedContacts()
            }

            editTextListeners(contactList)
            navigationMenu()
        }

        //async function to wait for permissions
        private suspend fun askForPermissions() {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS),
                    1001)
            }

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), 1002)
            }
        }

        private fun initComponents() {
            etSearch = findViewById(R.id.etSearch)
            btnSendLocation = findViewById(R.id.btnSendLocation)
            rvContacts = findViewById(R.id.rv_contacts)
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

        bottomNavigationView.menu.findItem(R.id.nav_shared_location).setChecked(true)
    }

    private fun sendLocationToSelectedContacts() {
        val locationMessage = "¡Hola! Esta es mi ubicación en PeaceApp: http://maps.google.com/?q=a,b"
        val smsManager = SmsManager.getDefault()
        selectedContacts.forEach { contact ->
            smsManager.sendTextMessage(contact.phone, null, locationMessage, null, null)
        }
        Toast.makeText(this, "Ubicación enviada a los contactos seleccionados", Toast.LENGTH_SHORT).show()

    }
    private fun initListeners(){
//        btnSearch.setOnClickListener(){
//            showGeneratedUrlDialog(generateRandomString())
//        }
    }

    @SuppressLint("Range")
    private fun loadContacts(): List<Contact> {
        val contactList = mutableListOf<Contact>()
        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            null
        )

        cursor?.use {
            while (it.moveToNext()) {
                var name = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone
                    .DISPLAY_NAME))
                var phoneNumber = it.getString(it.getColumnIndex(ContactsContract
                    .CommonDataKinds.Phone.NUMBER))
                Log.i("Contact", "Name: $name - Phone: $phoneNumber - ${it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI))}")

                var image = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone
                    .PHOTO_URI))
                if(image == null) { // asignar imagen por defecto
                    image = "https://static.vecteezy.com/system/resources/thumbnails/009/292/244/small/default-avatar-icon-of-social-media-user-vector.jpg"
                }
                var position = contactList.size

                var contact = Contact(name, phoneNumber, image)
                contactList.add(contact)
            }
        }

        return contactList
    }


    private fun editTextListeners(contactList: List<Contact>) {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {  }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterContacts(contactList, s.toString())
            }

            override fun afterTextChanged(s: Editable?) { }

        })
    }

    private fun filterContacts(contactList: List<Contact>, search: String) {
        val filteredList = contactList.filter {
            it.name.contains(search, ignoreCase = true) || it.phone.contains(search)
        }

        contactAdapter.updateContacts(filteredList)
    }




    private fun generateRandomString() : String {
        val length = 6
        val charset = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        return (1..length)
            .map { charset.random() }
            .joinToString("")
    }

    private fun showGeneratedUrlDialog(url:String) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_generated_url)

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnCopy = dialog.findViewById<CardView>(R.id.cvCopyContainer)
        val btnContinue = dialog.findViewById<Button>(R.id.btnContinue)
        val tvUrlGenerada = dialog.findViewById<TextView>(R.id.tvGeneratedUrl)

        tvUrlGenerada.text = "http://www.peaceapp.com/$url"

        btnContinue.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }


}