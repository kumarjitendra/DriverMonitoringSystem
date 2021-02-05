package com.altran.datamonitoringsystem

interface MqttManager {
    fun init()
    fun connect()
    fun sendMessage(topic: String, message: String)
}