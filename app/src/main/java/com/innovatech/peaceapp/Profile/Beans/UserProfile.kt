package com.innovatech.peaceapp.Profile.Beans

import java.io.Serializable


class UserProfile : Serializable {
    var id: Int
    var name: String
    var lastname: String
    var phonenumber: String
    var email: String
    var password: String
    var user_id: String
    var profile_image: String

    constructor(id: Int, name: String, lastname: String, phone: String, email: String, password:
    String, user_id: String, profile_image: String) {
        this.id = id
        this.name = name
        this.lastname = lastname
        this.phonenumber = phone
        this.email = email
        this.password = password
        this.user_id = user_id
        this.profile_image = profile_image
    }
    constructor(name: String, lastname: String, phone: String, email: String, password: String,
                user_id: String, profile_image: String) {
        this.id = 0
        this.name = name
        this.lastname = lastname
        this.phonenumber = phone
        this.email = email
        this.password = password
        this.user_id = user_id
        this.profile_image = profile_image
    }
}