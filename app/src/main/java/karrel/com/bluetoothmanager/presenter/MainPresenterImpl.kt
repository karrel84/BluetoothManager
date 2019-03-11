package karrel.com.bluetoothmanager.presenter

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.support.annotation.RequiresPermission
import android.support.v4.content.ContextCompat
import karrel.com.bluetoothmanager.util.ByteConverter
import karrel.com.btconnector.btmanager.BluetoothListener
import karrel.com.btconnector.btmanager.BluetoothManager

/**
 * Created by Rell on 2018. 3. 23..
 */

class MainPresenterImpl(private val view: MainPresenter.View, context: Context) : MainPresenter {

    private val bluetoothManager: BluetoothManager = BluetoothManager.getInstance(context)
    // 블루투스 기기

    private val bluetoothListener = object : BluetoothListener {
        override fun onFailedConnect(deviceName: String?) {
            println("onFailedConnect($deviceName)")
        }

        override fun onLostedConnect(deviceName: String?) {
            println("onLostedConnect($deviceName)")
        }

        override fun onConnected(deviceName: String?) {
            println("onConnected($deviceName)")
        }

        override fun onConnecting(deviceName: String?) {
            println("onConnecting($deviceName)")
        }

        override fun onListening(deviceName: String?) {
            println("onListening($deviceName)")
        }

        override fun onSendMessage(writeBuf: ByteArray?) {
            println("onSendMessage()")
        }

        override fun onReadMessage(readBuf: ByteArray?) {
            println("onReadMessage(${ByteConverter.byteToHex(readBuf)})")
        }

        override fun onStartConnect(deviceName: String?) {
            println("onStartConnect($deviceName)")
        }

        override fun onDoingNothing(deviceName: String?) {
            println("onDoingNothing($deviceName)")
        }

        override fun deniedPermission() {
            println("deniedPermission()")
        }

        override fun requireEnableBt() {
            println("requireEnableBt()")
            view.requireEnableBt()
        }

        override fun onSearchedDevice(device: BluetoothDevice?) {

            val text = "device : ${device?.name}, address : ${device?.address}, bondState : ${device?.bondState}"
            println("onSearchedDevice : $text")

            view.addSearchedDevice(device)

        }
    }

    init {

        bluetoothManager.setScanner(BluetoothManager.Scanner.DISCOVERY)
        bluetoothManager.addBluetoothCallback(bluetoothListener)
    }


    override fun onResume() {
    }

    override fun onStop() {
        bluetoothManager.stopBluetoothDeviceScan()
    }

    override fun onDestroy() {
    }

    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun searchBluetoothDevices() {
        bluetoothManager.startBluetoothDeviceScan()
        view.clearLog()
    }

    // BT 설정 성공
    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun enabledBluetooth() {
        // 블루투스 탐색 시작
        searchBluetoothDevices()
    }

    override fun dissableBluetooth() {
        // BT 설정 실패
    }

    override fun connectBluetooth(data: BluetoothDevice) {
        bluetoothManager.connect(data)
    }

    override fun disconnectBluetooth() {
        bluetoothManager.disConnect()
    }
}
