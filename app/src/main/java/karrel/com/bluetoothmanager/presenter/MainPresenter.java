package karrel.com.bluetoothmanager.presenter;

/**
 * Created by Rell on 2018. 3. 23..
 */

public interface MainPresenter {
    void searchBluetoothDevices();

    void enabledBluetooth();

    void dissableBluetooth();

    void connectBluetooth();

    void deconnectBluetooth();

    interface View {

        void requireEnableBt();
    }
}
