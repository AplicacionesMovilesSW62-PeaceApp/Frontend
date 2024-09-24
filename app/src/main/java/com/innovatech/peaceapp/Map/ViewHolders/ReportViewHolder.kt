package com.innovatech.peaceapp.Map.ViewHolders

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.innovatech.peaceapp.Map.Beans.Report
import com.innovatech.peaceapp.R
import com.squareup.picasso.Picasso

class ReportViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val reportImg = view.findViewById<ImageView>(R.id.imgReport)
    val reportTitle = view.findViewById<TextView>(R.id.txtTitle)
    val reportDate = view.findViewById<TextView>(R.id.txtDate)
    val reportLocation = view.findViewById<TextView>(R.id.txtLocation)

    fun render(reportModel: Report) {
        reportTitle.text = reportModel.title
        reportDate.text = reportModel.detail
        reportLocation.text = reportModel.type
        Picasso.get().load(reportModel.image)
            .resize(300, 300)
            .centerCrop().into(reportImg)
    }


}