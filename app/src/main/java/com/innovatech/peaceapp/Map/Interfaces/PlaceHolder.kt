package com.innovatech.peaceapp.Map.Interfaces

import Beans.Location
import Beans.LocationSchema
import com.innovatech.peaceapp.Alert.Beans.Alert
import com.innovatech.peaceapp.Alert.Beans.AlertSchema
import com.innovatech.peaceapp.Map.Beans.Report
import com.innovatech.peaceapp.Map.Beans.ReportSchema
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface PlaceHolder {
    @GET("api/v1/locations/")
    fun getLocations(): Call<List<Location>>

    // POST api/v1/locations/
    // body: {"latitude": "", "longitude": "", "idReport": 1}
    @POST("api/v1/locations/")
    fun postLocation(@Body locationSchema: LocationSchema): Call<Location>

    @GET("api/v1/reports/")
    fun getAllReports(): Call<List<Report>>

    // GET api/v1/reports/user/{id}
    @GET("api/v1/reports/user/{id}")
    fun getMyReports(@Path("id") id: Int): Call<List<Report>>

    @POST("api/v1/reports/")
    fun postReport(@Body report: ReportSchema): Call<Report>

    @DELETE("api/v1/reports/{id}")
    fun deleteReport(@Path("id") id: Int): Call<Void>
    @POST("api/v1/alerts/")
    fun postAlert(@Body alertSchema: AlertSchema): Call<Alert>
    // New method to get all alerts
    @GET("api/v1/alerts/")
    fun getAllAlerts(): Call<List<Alert>>
    @DELETE("api/v1/alerts/")
    fun deleteAllAlerts(): Call<Void>

}