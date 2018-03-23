package karrel.com.btconnector;

import android.bluetooth.BluetoothDevice;

/**
 * Created by Rell on 2018. 1. 29..
 */

public interface BtScanListener {
    void onSearchedDevice(BluetoothDevice device);
}
