package karrel.com.btconnector.chatmanager

import android.bluetooth.BluetoothDevice
import android.os.Handler
import android.os.Message

import karrel.com.btconnector.BluetoothService
import karrel.com.btconnector.Constants

/**
 * Created by Rell on 2018. 1. 29..
 */

class BluetoothChatManager {
    private var bluetoothService: BluetoothService? = null
    private var bluetoothChatListener: BluetoothChatListener? = null

    // 블루투스 상태
    private var mStatus = BluetoothService.STATE_NONE

    private var bluetoothHandler: BluetoothHandler? = null
    private var connectDeviceName: String? = null


    val isConnected: Boolean
        get() = mStatus == BluetoothService.STATE_CONNECTED

    fun setListener(listener: BluetoothChatListener) {
        this.bluetoothChatListener = listener
    }

    fun connect(bluetoothDevice: BluetoothDevice) {

        connectDeviceName = bluetoothDevice.name

        // 접속 해제
        disConnect()

        bluetoothHandler = BluetoothHandler()
        // 접속시도
        val thread = Thread {
            bluetoothService = BluetoothService(bluetoothHandler)
            bluetoothChatListener!!.onStartConnect(bluetoothDevice.name)
            bluetoothService!!.connect(bluetoothDevice, true)
        }
        thread.start()
    }

    fun send(out: ByteArray) {
        bluetoothService!!.write(out)
    }


    fun start() {
        if (bluetoothService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (bluetoothService!!.state == BluetoothService.STATE_NONE) {
                // Start the Bluetooth chat services
                bluetoothService!!.start()
            }
        }
    }


    internal inner class BluetoothHandler : Handler() {
        override fun handleMessage(msg: Message) {
            if (bluetoothChatListener == null) return

            when (msg.what) {
                Constants.MESSAGE_STATE_CHANGE -> {
                    mStatus = msg.arg1

                    when (msg.arg1) {
                        BluetoothService.STATE_CONNECTED -> bluetoothChatListener!!.onConnected(connectDeviceName)
                        BluetoothService.STATE_CONNECTING -> bluetoothChatListener!!.onConnecting(connectDeviceName)
                        BluetoothService.STATE_LISTEN -> bluetoothChatListener!!.onListening(connectDeviceName)
                        BluetoothService.STATE_NONE -> bluetoothChatListener!!.onDoingNothing(connectDeviceName)
                    }
                }
                Constants.MESSAGE_SEND -> {
                    val writeBuf = msg.obj as ByteArray
                    bluetoothChatListener!!.onSendMessage(writeBuf)
                }
                Constants.MESSAGE_READ -> {
                    val readBuf = msg.obj as ByteArray
                    bluetoothChatListener!!.onReadMessage(readBuf)
                }
                Constants.MESSAGE_CONNECTION_FAILED -> {
                    bluetoothChatListener!!.onFailedConnect(connectDeviceName)
                }
                Constants.MESSAGE_CONNECTION_LOSTED -> {
                    bluetoothChatListener!!.onLostedConnect(connectDeviceName)
                }

            }
        }
    }

    // 접속해제
    fun disConnect() {
        if (bluetoothService != null) {
            bluetoothService!!.stop()
        }
    }

    companion object {
        private var instance: BluetoothChatManager? = null

        fun getInstance(): BluetoothChatManager {
            if (instance == null) {
                instance = BluetoothChatManager()
            }
            return instance as BluetoothChatManager
        }
    }
}
