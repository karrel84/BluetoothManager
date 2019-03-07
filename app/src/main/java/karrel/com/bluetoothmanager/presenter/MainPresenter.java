package karrel.com.bluetoothmanager.presenter;

import android.bluetooth.BluetoothDevice;

import org.jetbrains.annotations.Nullable;

/**
 * Created by Rell on 2018. 3. 23..
 */

public interface MainPresenter {
    void searchBluetoothDevices();

    void enabledBluetooth();

    void dissableBluetooth();

    void connectBluetooth(BluetoothDevice data);

    void disconnectBluetooth();

    interface View {

        void requireEnableBt();

        void clearLog();

        void addSearchedDevice(@Nullable BluetoothDevice device);
    }
}
