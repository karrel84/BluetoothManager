package karrel.com.btconnector.chatmanager

/**
 * Created by Rell on 2018. 1. 29..
 */

interface BluetoothChatListener {
    fun onConnected(deviceName: String?)

    fun onConnecting(deviceName: String?)

    fun onListening(deviceName: String?)

    fun onSendMessage(writeBuf: ByteArray?)

    fun onReadMessage(readBuf: ByteArray?)

    fun onStartConnect(deviceName: String?)

    fun onDoingNothing(deviceName: String?)

    fun onFailedConnect(deviceName: String?)

    fun onLostedConnect(deviceName: String?)
}
