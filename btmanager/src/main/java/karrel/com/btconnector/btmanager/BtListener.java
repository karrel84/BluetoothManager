package karrel.com.btconnector.btmanager;

import android.bluetooth.BluetoothDevice;

/**
 * Created by Rell on 2018. 1. 29..
 */

public interface BtListener {
    // 권한이 거부됨
    void deniedPermission();

    // 블루투스 사용가능 요청
    void requireEnableBt();

    // 기기 찾음
    void onSearchedDevice(BluetoothDevice device);
}
