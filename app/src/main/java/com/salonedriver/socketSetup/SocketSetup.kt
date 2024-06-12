package com.salonedriver.socketSetup

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.salonedriver.BuildConfig
import com.salonedriver.R
import com.salonedriver.SaloneDriver
import com.salonedriver.model.dataclassses.clientConfig.ClientConfigDC
import com.salonedriver.model.dataclassses.userData.UserDataDC
import com.salonedriver.util.SharedPreferencesManager
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import io.socket.engineio.client.transports.WebSocket
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
    fun initializeInterface(socketInterface: SocketInterface){
        SocketSetup.socketInterface = socketInterface
    }


    /**
     * For Connect Socket
     * */
    fun connectSocket() {
        try {
            if (socket == null){
                val ioOptions = IO.Options().apply {
                    forceNew = true
                    reconnectionAttempts = Integer.MAX_VALUE
                    timeout = 10000
                    transports = arrayOf(WebSocket.NAME)
//                    query = "token=$authToken"
                }
                socket = IO.socket("https://dev-rides-api.venustaxi.in", ioOptions)
                listenerOn(Socket.EVENT_CONNECT, SOCKET_CONNECT)
                listenerOn(Socket.EVENT_DISCONNECT, SOCKET_DISCONNECT)
                listenerOn(Socket.EVENT_CONNECT_ERROR, SOCKET_ERROR)
                if (socket?.connected() == false)
                    socket?.connect()
            }else{
                if (socket?.connected() == false)
                    socket?.connect()
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
    }


    /**
     * For Disconnect Socket
     * */
    fun disconnectSocket() {
        if (socket?.connected() == true){
            listenerOff(Socket.EVENT_CONNECT, SOCKET_CONNECT)
            listenerOff(Socket.EVENT_DISCONNECT, SOCKET_DISCONNECT)
            listenerOff(Socket.EVENT_CONNECT_ERROR, SOCKET_ERROR)
            socket?.disconnect()
            socket = null
        }
    }


    private val SOCKET_CONNECT = Emitter.Listener {
        Log.e("SocketPrint","Socket Connected")
    }


    private val SOCKET_DISCONNECT = Emitter.Listener {
        Log.e("SocketPrint","Socket Disconnected")
    }


    private val SOCKET_ERROR = Emitter.Listener {
        Log.e("SocketPrint","Socket Error   ${it[0]}")
    }


    /**
     * Listener On
     * */
    private fun listenerOn(key: String, listener: Emitter.Listener) {
        try {
            if (socket?.hasListeners(key) == false)
                socket?.on(key, listener)
        }catch (e:Exception){
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
        }catch (e:Exception){
            e.printStackTrace()
        }
    }


    /**
     * Emit Location
     * */
    fun emitLocation(latLng: LatLng, bearing: String , engagementId: String){
        val clientConfig = SharedPreferencesManager.getModel<ClientConfigDC>(SharedPreferencesManager.Keys.CLIENT_CONFIG)
        val userData = SharedPreferencesManager.getModel<UserDataDC>(SharedPreferencesManager.Keys.USER_DATA)
        Log.e("sfdsfsdfds","sdfsdfsdfsdf  ${latLng.latitude}")
        emit(SocketKeys.DRIVER_TRACKING, JSONObject().apply {
            put("operatorToken", clientConfig?.operatorToken.orEmpty())
            put("accessToken", userData?.accessToken.orEmpty())
            put("latitude", latLng.latitude)
            put("longitude", latLng.longitude)
            put("engagementId", engagementId)
            put("direction", bearing)
        })
    }


    /**
     * Emit Socket
     * */
    private fun <T> emit(key: String, data: T){
        try {
            if (socket?.connected() == false)
                connectSocket()
            socket?.emit(key, data)
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

}