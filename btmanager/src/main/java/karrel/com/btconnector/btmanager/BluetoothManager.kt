package karrel.com.btconnector.btmanager

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.support.annotation.RequiresPermission
import karrel.com.btconnector.btscanner.AbBluetoothScanner
import karrel.com.btconnector.btscanner.BluetoothScannable
import karrel.com.btconnector.btscanner.DiscoveryScanner
import karrel.com.btconnector.btscanner.LeBluetoothSanner
import karrel.com.btconnector.chatmanager.BluetoothChatManager
import java.util.*

/**
 * Created by Rell on 2018. 3. 23..
 *
 *
 * 블루투스 리스트 조회, 접속, 데이터 송/수신
 */

class BluetoothManager(private val context: Context) : BluetoothListener, BluetoothManagerable {


    // 블루투스 스캐너
    private var bluetoothScanner: BluetoothScannable? = null

    // 블루투스 콜백
    private val bluetoothListeners = ArrayList<BluetoothListener>()

    // 채팅 매니저
    private val bluetoothChatManager = BluetoothChatManager.getInstance()

    // 블루투스 스캐너 리스너
    private val btScannerListener = object : AbBluetoothScanner.BluetoothScanListener {
        override fun requireEnableBt() {
            // 블루투스 이용가능 설정이 필요합니다.
            this@BluetoothManager.requireEnableBt()
        }

        override fun onSearchedDevice(device: BluetoothDevice) {
            this@BluetoothManager.onSearchedDevice(device)
        }
    }

    enum class Scanner {
        DISCOVERY, BLE
    }

    init {

        // 디바이스 스캐너
        bluetoothScanner = LeBluetoothSanner(btScannerListener)
        // 채팅 매니저 콜백
        bluetoothChatManager.setListener(this)
    }

    fun setScanner(discovery: Scanner) {
        bluetoothScanner = when (discovery) {
            BluetoothManager.Scanner.DISCOVERY -> DiscoveryScanner(btScannerListener, context)
            BluetoothManager.Scanner.BLE -> LeBluetoothSanner(btScannerListener)
        }
    }

    fun addBluetoothCallback(bluetoothListener: BluetoothListener) {
        bluetoothListeners.add(bluetoothListener)
    }

    fun removeBluetoothCallback(bluetoothListener: BluetoothListener) {
        bluetoothListeners.remove(bluetoothListener)
    }

    // 권한이 거부됨
    override fun deniedPermission() {
        for (listener in bluetoothListeners) listener.deniedPermission()
    }

    // 블루투스 활성화 요청
    override fun requireEnableBt() {
        for (listener in bluetoothListeners) listener.requireEnableBt()

    }

    override fun onSearchedDevice(device: BluetoothDevice?) {
        if (device == null) return
        if (device.name == null) return

        for (listener in bluetoothListeners) listener.onSearchedDevice(device)
    }


    override fun onConnected(deviceName: String?) {
        for (listener in bluetoothListeners)
            listener.onConnected(deviceName)
    }

    override fun onConnecting(deviceName: String?) {
        for (listener in bluetoothListeners) listener.onConnecting(deviceName)
    }

    override fun onListening(deviceName: String?) {
        for (listener in bluetoothListeners) listener.onListening(deviceName)
    }

    override fun onSendMessage(writeBuf: ByteArray?) {
        for (listener in bluetoothListeners) listener.onSendMessage(writeBuf)
    }

    override fun onReadMessage(readBuf: ByteArray?) {
        for (listener in bluetoothListeners) listener.onReadMessage(readBuf)
    }

    override fun onStartConnect(deviceName: String?) {
        for (listener in bluetoothListeners) listener.onStartConnect(deviceName)
    }

    override fun onDoingNothing(deviceName: String?) {
        for (listener in bluetoothListeners) listener.onDoingNothing(deviceName)
    }

    override fun onFailedConnect(deviceName: String?) {
        for (listener in bluetoothListeners) listener.onFailedConnect(deviceName)
    }

    override fun onLostedConnect(deviceName: String?) {
        for (listener in bluetoothListeners) listener.onLostedConnect(deviceName)
    }


//    // 체크해야할 권한
//    private val bluetoothPermissions = arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_COARSE_LOCATION)


    // 블루투스 스캔
    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun startBluetoothDeviceScan() {
        // 퍼미션이 체크 완료되었는가?
        scanBluetoothDevice()
    }

    override fun stopBluetoothDeviceScan() {
        // 스캔 중지
        bluetoothScanner!!.stopScanBluetoothDevice()
    }

    override fun connect(device: BluetoothDevice) {
        // 스캔 중지
        stopBluetoothDeviceScan()
        // 블루투스 접속
        bluetoothChatManager.connect(device)
    }

    override fun disConnect() {
        if (bluetoothChatManager.isConnected) bluetoothChatManager.disConnect()
    }

    // 블루투스 스캔 시작
    private fun scanBluetoothDevice() {
        // 블루투스 스캔 시작
        bluetoothScanner!!.scanBluetoothDevice()
    }

    override fun isConnected(): Boolean {
        // 연결되었나?
        return bluetoothChatManager.isConnected
    }

    override fun sendData(bytes: ByteArray) {
        // 데이터 송신
        bluetoothChatManager.send(bytes)
    }

    companion object {

        // 싱글턴
        private var bluetoothManager: BluetoothManager? = null

        // 싱글턴
        fun getInstance(context: Context): BluetoothManager {
            if (bluetoothManager == null) {
                bluetoothManager = BluetoothManager(context)
            }
            return bluetoothManager as BluetoothManager
        }
    }
}
