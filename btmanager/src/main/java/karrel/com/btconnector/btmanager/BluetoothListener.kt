package karrel.com.btconnector.btmanager

import android.bluetooth.BluetoothDevice

import karrel.com.btconnector.chatmanager.BluetoothChatListener

/**
 * Created by Rell on 2018. 1. 29..
 */

interface BluetoothListener : BluetoothChatListener {
    // 권한이 거부됨
    fun deniedPermission()

    // 블루투스 사용가능 요청
    fun requireEnableBt()

    // 기기 찾음
    fun onSearchedDevice(device: BluetoothDevice?)
}
