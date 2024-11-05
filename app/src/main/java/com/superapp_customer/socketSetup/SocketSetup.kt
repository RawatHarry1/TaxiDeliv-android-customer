package com.superapp_customer.socketSetup

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.superapp_customer.BuildConfig
import com.superapp_customer.model.dataClass.MessageData
import com.superapp_customer.model.dataClass.base.ClientConfig
import com.superapp_customer.model.dataClass.userData.UserDataDC
import com.superapp_customer.util.SharedPreferencesManager
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import io.socket.engineio.client.transports.WebSocket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * Socket SetUp
 * */
object SocketSetup {

    private var socket: Socket? = null
    private var socketInterface: SocketInterface? = null


    /**
     * Initialize Socket Interface
     * */
    fun initializeInterface(socketInterface: SocketInterface) {
        SocketSetup.socketInterface = socketInterface
    }


    /**
     * For Connect Socket
     * */
    fun connectSocket() {
        try {
            if (socket == null) {
                val ioOptions = IO.Options().apply {
                    forceNew = true
                    reconnectionAttempts = Integer.MAX_VALUE
                    timeout = 10000
                    transports = arrayOf(WebSocket.NAME)
                }
                socket = IO.socket(BuildConfig.SOCKET_BASE_URL, ioOptions)
                listenerOn(Socket.EVENT_CONNECT, SOCKET_CONNECT)
                listenerOn(Socket.EVENT_DISCONNECT, SOCKET_DISCONNECT)
                listenerOn(Socket.EVENT_CONNECT_ERROR, SOCKET_ERROR)
                listenerOn(SocketKeys.DRIVER_LOCATION_LISTENER, driverLocationListener)

                if (socket?.connected() == false)
                    socket?.connect()
            } else {
                if (socket?.connected() == false)
                    socket?.connect()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun listenToMessage(engagementId: String, customerId: String) {
        val listener = "msg_receiver_listener_${engagementId}_${customerId}"
        val allMsgListener = "list_of_message"
        Log.e("SocketPrint", listener)
        Log.e("SocketPrint", allMsgListener)
        listenerOn(listener, driverMessageListener)
        listenerOn(allMsgListener, allMessageListener)
    }

    fun listenerOffOnMessage(engagementId: String, customerId: String) {
        val listener = "msg_receiver_listener_${engagementId}_${customerId}"
        val allMsgListener = "list_of_message"
        listenerOff(listener, driverMessageListener)
        listenerOff(allMsgListener, allMessageListener)
    }

    /**
     * Driver Location Listener
     * */
    private val driverLocationListener = Emitter.Listener {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val json = JSONObject(it[0].toString())
                Log.i("SOCKET", "ON DRIVER LOCATION:: ${Gson().toJson(json)}")
                socketInterface?.driverLocation(
                    latLng = LatLng(
                        json.optString("latitude").toDoubleOrNull() ?: 0.0,
                        json.optString("longitude").toDoubleOrNull() ?: 0.0
                    ),
                    bearing = json.optString("direction").toFloatOrNull() ?: 0.0F,
                    eta = json.optString("eta").toIntOrNull() ?: 0
                )
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Driver Message Listener
     * */
    private val driverMessageListener = Emitter.Listener {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val json = JSONObject(it.firstOrNull().toString())
                val msg = Gson().fromJson(json.toString(), MessageData::class.java)
                Log.e("SocketPrint", "Socket listen on message ${Gson().toJson(json)}")
                msg?.let {
                    socketInterface?.driverMessage(it)
                }
            } catch (e: Exception) {
                Log.e("SocketPrint", "Socket listen on message error ${e.message}")
                e.printStackTrace()
            }
        }
    }


    /**
     * All Message Listener
     * */
    private val allMessageListener = Emitter.Listener {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val json = JSONObject(it.firstOrNull().toString()).getJSONArray("thread")
                val itemType = object : TypeToken<List<MessageData>>() {}.type
                val msgList = Gson().fromJson<List<MessageData>>(json.toString(), itemType)
                Log.e("SocketPrint", "Socket listen on all message ${Gson().toJson(msgList)}")
                msgList?.let {
                    socketInterface?.allMessages(it as ArrayList<MessageData>)
                }
            } catch (e: Exception) {
                Log.e("SocketPrint", "Socket listen on message error ${e.message}")
                e.printStackTrace()
            }
        }
    }


    /**
     * For Disconnect Socket
     * */
    fun disconnectSocket() {
        if (socket?.connected() == true) {
            listenerOff(Socket.EVENT_CONNECT, SOCKET_CONNECT)
            listenerOff(Socket.EVENT_DISCONNECT, SOCKET_DISCONNECT)
            listenerOff(Socket.EVENT_CONNECT_ERROR, SOCKET_ERROR)
            listenerOff(SocketKeys.DRIVER_LOCATION_LISTENER, driverLocationListener)
            socket?.disconnect()
            socket = null
        }
    }


    private val SOCKET_CONNECT = Emitter.Listener {
        Log.e("SocketPrint", "Socket Connected")
    }


    private val SOCKET_DISCONNECT = Emitter.Listener {
        Log.e("SocketPrint", "Socket Disconnected")
    }


    private val SOCKET_ERROR = Emitter.Listener {
        Log.e("SocketPrint", "Socket Error   ${it[0]}")
    }


    /**
     * Listener On
     * */
    private fun listenerOn(key: String, listener: Emitter.Listener) {
        try {
            if (socket?.hasListeners(key) == false)
                socket?.on(key, listener)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    /**
     * Listener Off
     * */
    private fun listenerOff(key: String, listener: Emitter.Listener) {
        try {
            if (socket?.hasListeners(key) == true)
                socket?.off(key, listener)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun startRideEmit(tripId: String) {
        val clientConfig =
            SharedPreferencesManager.getModel<ClientConfig>(SharedPreferencesManager.Keys.CLIENT_CONFIG)
        val userData =
            SharedPreferencesManager.getModel<UserDataDC>(SharedPreferencesManager.Keys.USER_DATA)
        emit(SocketKeys.CUSTOMER_TRACKING, JSONObject().apply {
            put("operatorToken", clientConfig?.operatorToken.orEmpty())
            put("accessToken", userData?.accessToken.orEmpty())
            put("engagementId", tripId)
        })
    }

    fun startMsgEmit(
        msg: String = "",
        senderId: String = "",
        receiverId: String = "",
        type: String = "",
        engagementId: String = "",
        attachment: String = "",
        thumbnail: String = ""

    ) {
        emit(SocketKeys.SEND_MESSAGE, JSONObject().apply {
            put("sender_id", senderId)
            put("receiver_id", receiverId)
            put("message", msg)
            put("engagement_id", engagementId)
            put("attachment", attachment)
            put("attachment_type", type)
            put("thumbnail", thumbnail)
            put("device_type", "0")
            put("login_type", "0")

        })
    }


    fun getAllMsg(
        senderId: String = "",
        engagementId: String = ""
    ) {
        emit(SocketKeys.GET_ALL_MESSAGE, JSONObject().apply {
            put("user_id", senderId)
            put("ride_id", engagementId)
        })
    }


    /**
     * Emit Socket
     * */
    private fun <T> emit(key: String, data: T) {
        try {
            if (socket?.connected() == false)
                connectSocket()
            socket?.emit(key, data)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}