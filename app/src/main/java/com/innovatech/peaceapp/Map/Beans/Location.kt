package Beans

class Location {
    var id: Int
    var createdAt: String
    var updatedAt: String
    var idReport: Int
    var alongitude: Double
    var alatitude: Double

    constructor(id: Int, createdAt: String, updatedAt: String, idReport: Int, alongitude: String, alatitude: String) {
        this.id = id
        this.createdAt = createdAt
        this.updatedAt = updatedAt
        this.idReport = idReport
        this.alongitude = alongitude.toDouble()
        this.alatitude = alatitude.toDouble()
    }

    constructor(alatitude: String,  alongitude: String, idReport: String) {
        this.alatitude = alatitude.toDouble()
        this.alongitude = alongitude.toDouble()
        this.idReport = idReport.toInt()
        this.id = 0
        this.createdAt = ""
        this.updatedAt = ""
    }
}