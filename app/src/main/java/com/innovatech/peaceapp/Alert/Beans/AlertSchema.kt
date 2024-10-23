package com.innovatech.peaceapp.Alert.Beans

data class AlertSchema(
    var location: String,
    var type: String,
    var description: String?, // Optional field
    var idUser: Int,
    var image_url: String?, // Optional field for image URL
    var idReport: Int // Optional field for the foreign key to a report
)
