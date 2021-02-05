package com.altran.datamonitoringsystem

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ExpandableListAdapter
import android.widget.ExpandableListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.slider.Slider
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.util.*

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class MainActivity : AppCompatActivity() {

    var expandableListView: ExpandableListView? = null
    var expandableListAdapter: ExpandableListAdapter? = null
    var expandableListTitle: List<String>? = null
    var expandableListDetail: HashMap<String, List<String>>? = null
    private var statePosition = 0
    private var clientId = "MyAndroidClientId" + System.currentTimeMillis()
    lateinit var mqttManager: MqttManagerImpl
    private var messageToSend: String? = null
    lateinit var discreteSlider: Slider
    private var discreteSliderValue: Float = 0.0F

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mqttManager = MqttManagerImpl(
                applicationContext,
                serverUri,
                clientId,
                arrayOf(subscriptionTopic),
                IntArray(1) { 0 })
        mqttManager.init()
        mqttManager.connect()
        initMqttStatusListener()
        expandableListView = findViewById<View>(R.id.expandableListView) as ExpandableListView
        expandableListDetail = ExpandableListDataPump.getData()
        expandableListTitle = ArrayList((expandableListDetail as HashMap<String, MutableList<String>>?)?.keys)
        expandableListAdapter = CustomExpandableListAdapter(this, expandableListTitle, expandableListDetail)
        expandableListView!!.setAdapter(expandableListAdapter)
        expandableListView!!.setOnGroupExpandListener { groupPosition ->
            statePosition = groupPosition
            Toast.makeText(applicationContext,
                    """ The driver is : ${(expandableListTitle as ArrayList<String>).get(groupPosition)}
                        | select level on slider and click on collapse  to send driver state and level via mqtt
                    """.trimMargin(),
                    Toast.LENGTH_LONG).show()

            discreteSlider = findViewById(R.id.slider) as Slider
            discreteSlider.visibility = View.VISIBLE
            discreteSlider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
                override fun onStartTrackingTouch(slider: Slider) {
                    Log.i(TAG, "onStartTrackingTouch: " + slider.value)
                }

                override fun onStopTrackingTouch(slider: Slider) {
                    Log.i(TAG, "onStopTrackingTouch: " + slider.value)
                }
            })

            discreteSlider.addOnChangeListener { slider, value, fromUser ->
                Log.i(TAG, """onValueChange: ${slider.value}value : $value fromUser :$fromUser"""
                )
                discreteSliderValue = value
            }

        }

        expandableListView!!.setOnGroupCollapseListener { groupPosition ->

            val listTitle :String =(expandableListTitle as ArrayList<String>).get(groupPosition)
            val sliderValue :String = "-> level : "+discreteSliderValue.toString()

            messageToSend = listTitle.plus(sliderValue)

            Log.i(TAG, "onCreate: group collapsed : " + messageToSend)

            mqttManager.sendMessage(publishTopic, messageToSend.toString())
            discreteSlider.value = 0.0f
            discreteSlider.visibility = View.INVISIBLE
        }

        expandableListView!!.setOnChildClickListener { parent, v, groupPosition, childPosition, id ->
        /*    Log.i(TAG, "onCreate: OnChildClickListener : ${(expandableListDetail
                    as HashMap<String, MutableList<String>>?)?.get((expandableListTitle as ArrayList<String>).
            get(groupPosition))!![childPosition]}")*/

            false
        }
    }
    private fun initMqttStatusListener() {
        mqttManager.mqttStatusListener = object : MqttStatusListener {
            override fun onConnectComplete(reconnect: Boolean, serverURI: String) {
                if (reconnect) {
                    displayInDebugLog("Reconnected to : $serverURI")
                } else {
                    displayInDebugLog("Connected to: $serverURI")
                }
            }

            override fun onConnectFailure(exception: Throwable) {
                displayInDebugLog("Failed to connect")
            }

            override fun onConnectionLost(exception: Throwable) {
                displayInDebugLog("The Connection was lost.")
            }

            override fun onMessageArrived(topic: String, message: MqttMessage) {
                Toast.makeText(
                        applicationContext,
                        "  Received Driver state via mqtt : " + message + "\n" +
                                " topic : " + topic,
                        Toast.LENGTH_LONG
                ).show()
            }

            override fun onTopicSubscriptionSuccess() {
                displayInDebugLog("Subscribed!")
            }

            override fun onTopicSubscriptionError(exception: Throwable) {
                displayInDebugLog("Failed to subscribe")
            }
        }
    }

    private fun displayInDebugLog(message: String) {
        Log.i(TAG, message)
    }
    companion object {
        const val TAG = "MainActivity"
        const val serverUri = "tcp://broker.hivemq.com:1883"
        /*const val serverUri = "mqtt://test.mosquitto.org:1883"*/
        const val publishTopic = "DriverState"
        const val subscriptionTopic = "DriverState"
    }
}