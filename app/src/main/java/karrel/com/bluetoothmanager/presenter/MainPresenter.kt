package karrel.com.bluetoothmanager.presenter

import android.bluetooth.BluetoothDevice

/**
 * Created by Rell on 2018. 3. 23..
 */

interface MainPresenter {
    fun searchBluetoothDevices()

    fun enabledBluetooth()

    fun dissableBluetooth()

    fun connectBluetooth(data: BluetoothDevice)

    fun disconnectBluetooth()

    fun onResume()
    fun onStop()
    fun onDestroy()

    interface View {

        fun requireEnableBt()

        fun clearLog()

        fun addSearchedDevice(device: BluetoothDevice?)
    }
}
