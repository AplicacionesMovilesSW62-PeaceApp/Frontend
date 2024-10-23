package com.innovatech.peaceapp.Map.Beans

data class ReportSchema(
    var title: String,
    var detail: String,
    var type: String,
    var user_id: Int,
    var image: String?,
    var address: String
)