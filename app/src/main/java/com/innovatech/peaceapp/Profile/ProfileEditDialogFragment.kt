package com.innovatech.peaceapp.Profile

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.ImageView
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.fragment.app.DialogFragment
import com.innovatech.peaceapp.R

class ProfileEditDialogFragment : DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.profile_edit, container, false)

        // Encuentra el ImageView
        val imageView: ImageView = view.findViewById(R.id.mainTxt)

        // Carga la imagen desde los recursos
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.view_background)

        // Crea un RoundedBitmapDrawable
        val roundedDrawable = RoundedBitmapDrawableFactory.create(resources, bitmap)
        roundedDrawable.isCircular = true

        // Establece el drawable redondeado en el ImageView
        imageView.setImageDrawable(roundedDrawable)

        // Encuentra el botón y establece el OnClickListener
        val btnBack: Button = view.findViewById(R.id.btnBack)
        btnBack.setOnClickListener {
            // Cierra el diálogo
            dismiss()
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog?.window?.setGravity(Gravity.TOP)
    }
}