package com.altran.datamonitoringsystem

import org.eclipse.paho.client.mqttv3.MqttMessage

interface MqttStatusListener {
    fun onConnectComplete(reconnect: Boolean, serverURI: String)
    fun onConnectFailure(exception: Throwable)
    fun onConnectionLost(exception: Throwable)
    fun onTopicSubscriptionSuccess()
    fun onTopicSubscriptionError(exception: Throwable)
    fun onMessageArrived(topic: String, message: MqttMessage)
}