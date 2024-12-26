package com.vyba_dri.socketSetup

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.vyba_dri.BuildConfig
import com.vyba_dri.model.dataclassses.MessageData
import com.vyba_dri.model.dataclassses.clientConfig.ClientConfigDC
import com.vyba_dri.model.dataclassses.userData.UserDataDC
import com.vyba_dri.util.SharedPreferencesManager
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
//                    query = "token=$authToken"
                }
                socket = IO.socket(BuildConfig.SOCKET_BASE_URL, ioOptions)
                listenerOn(Socket.EVENT_CONNECT, SOCKET_CONNECT)
                listenerOn(Socket.EVENT_DISCONNECT, SOCKET_DISCONNECT)
                listenerOn(Socket.EVENT_CONNECT_ERROR, SOCKET_ERROR)
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


    /**
     * For Disconnect Socket
     * */
    fun disconnectSocket() {
        if (socket?.connected() == true) {
            listenerOff(Socket.EVENT_CONNECT, SOCKET_CONNECT)
            listenerOff(Socket.EVENT_DISCONNECT, SOCKET_DISCONNECT)
            listenerOff(Socket.EVENT_CONNECT_ERROR, SOCKET_ERROR)
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


    fun listenToMessage(engagementId: String, driverId: String) {
        val listener = "msg_receiver_listener_${engagementId}_${driverId}"
        val allMsgListener = "list_of_message"
        Log.e("SocketPrint", listener)
        Log.e("SocketPrint", allMsgListener)
        listenerOn(listener, customerMessageListener)
        listenerOn(allMsgListener, allMessageListener)
    }

    fun listenerOffOnMessage(engagementId: String, customerId: String) {
        val listener = "msg_receiver_listener_${engagementId}_${customerId}"
        listenerOff(listener, customerMessageListener)
        listenerOff("list_of_message", allMessageListener)
    }


    /**
     * Customer Message Listener
     * */
    private val customerMessageListener = Emitter.Listener {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val json = JSONObject(it.firstOrNull().toString())
                val msg = Gson().fromJson(json.toString(), MessageData::class.java)

                Log.e("SocketPrint", "Socket listen on message ${Gson().toJson(json)}")
                msg?.let {
                    socketInterface?.onCustomerMsg(it)
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
                    socketInterface?.allMsg(it as ArrayList<MessageData>)
                }
            } catch (e: Exception) {
                Log.e("SocketPrint", "Socket listen on message error ${e.message}")
                e.printStackTrace()
            }
        }
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


    /**
     * Emit Location
     * */
    fun emitLocation(latLng: LatLng, bearing: String, engagementId: String) {
        val clientConfig =
            SharedPreferencesManager.getModel<ClientConfigDC>(SharedPreferencesManager.Keys.CLIENT_CONFIG)
        val userData =
            SharedPreferencesManager.getModel<UserDataDC>(SharedPreferencesManager.Keys.USER_DATA)
        Log.e(
            "LocationUpdate",
            "onEmit   ${latLng.latitude}"
        )
        emit(SocketKeys.DRIVER_TRACKING, JSONObject().apply {
            put("operatorToken", clientConfig?.operatorToken.orEmpty())
            put("accessToken", userData?.accessToken.orEmpty())
            put("latitude", latLng.latitude)
            put("longitude", latLng.longitude)
            put("engagementId", engagementId)
            put("direction", bearing)
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
            put("login_type", "1")
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