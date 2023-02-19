package com.example.floodmon

data class Locations(
    var Temperature : String ?= null,
    var Humidity : String ?= null,
    var Distance : String ?= null,
    var AlertStatus : String ?= null,
    var LocName : String ?= null,
    var WaterFlow:String?=null)
