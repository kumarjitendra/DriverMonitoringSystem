package com.altran.datamonitoringsystem

import android.content.Context
import android.widget.Toast
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import kotlin.jvm.Throws

class MqttManagerImpl(private val applicationContext: Context,
                      private val serverUri: String,
                      private val clientId: String,
                      private val topics: Array<String>,
                      private val topicQos: IntArray)
    : MqttManager, MqttStatusListener {
    lateinit var mqttAndroidClient: MqttAndroidClient
    lateinit var mqttStatusListener: MqttStatusListener

    override fun init() {
        mqttAndroidClient = MqttAndroidClient(applicationContext, serverUri, clientId)
        mqttAndroidClient.setCallback(object : MqttCallbackExtended {
            override fun connectComplete(reconnect: Boolean, serverURI: String) {

                mqttStatusListener.onConnectComplete(reconnect, serverURI)
                if (reconnect) {
                    // Because Clean Session is true, we need to re-subscribe
                    subscribeToTopic()
                }
            }

            override fun connectionLost(cause: Throwable) {
                mqttStatusListener.onConnectionLost(cause)
            }

            @Throws(Exception::class)
            override fun messageArrived(topic: String, message: MqttMessage) {
                mqttStatusListener.onMessageArrived(topic, message)
            }

            override fun deliveryComplete(token: IMqttDeliveryToken) {}
        })

    }

    override fun connect() {
        try {
            mqttAndroidClient.connect(createConnectOptions(), null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    mqttAndroidClient.setBufferOpts(createDisconnectedBufferOptions())
                    subscribeToTopic()
                }

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    mqttStatusListener.onConnectFailure(exception)
                }
            })
        } catch (ex: MqttException) {
            ex.printStackTrace()
        }
    }

    override fun sendMessage(topic: String, message: String) {
        mqttAndroidClient.let {
            try {
                val mqttMessage = MqttMessage().apply {
                    payload = message.toByteArray()
                }
                it.publish(topic, mqttMessage)

                Toast.makeText(
                        applicationContext,
                        " sending Driver state via MQTT  : " + message+ "\n"+
                                " topic : "+topic,
                        Toast.LENGTH_SHORT
                ).show()
            } catch (e: MqttException) {
                e.printStackTrace()
            }
        }
    }

    private fun createDisconnectedBufferOptions(): DisconnectedBufferOptions? {
        return DisconnectedBufferOptions().apply {
            isBufferEnabled = true
            bufferSize = 100
            isPersistBuffer = false
            isDeleteOldestMessages = false
        }

    }

    private fun createConnectOptions(): MqttConnectOptions? {
        return MqttConnectOptions().apply {
            isAutomaticReconnect = true
            isCleanSession = false
        }

    }


    private fun subscribeToTopic() {
        try {
            mqttAndroidClient.subscribe(topics, topicQos, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    mqttStatusListener.onTopicSubscriptionSuccess()
                }

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    mqttStatusListener.onTopicSubscriptionError(exception)
                }
            })
        } catch (ex: MqttException) {
            ex.printStackTrace()
        }
    }


    override fun onConnectComplete(reconnect: Boolean, serverURI: String) {
        TODO("Not yet implemented")
    }

    override fun onConnectFailure(exception: Throwable) {
        TODO("Not yet implemented")
    }

    override fun onConnectionLost(exception: Throwable) {
        TODO("Not yet implemented")
    }

    override fun onTopicSubscriptionSuccess() {
        TODO("Not yet implemented")
    }

    override fun onTopicSubscriptionError(exception: Throwable) {
        TODO("Not yet implemented")
    }

    override fun onMessageArrived(topic: String, message: MqttMessage) {
        TODO("Not yet implemented")
    }

}