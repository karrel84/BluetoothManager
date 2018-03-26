package karrel.com.btconnector.btmanager;

import android.bluetooth.BluetoothDevice;

/**
 * Created by Rell on 2018. 3. 23..
 */

public interface BluetoothManagerable {
    // 블루투스 스캔
    void startBluetoothDeviceScan();

    // 블루투스 스캔 중지
    void stopBluetoothDeviceScan();

    // 블루투스 연결
    void connect(BluetoothDevice device);

    void disConnect();
}
