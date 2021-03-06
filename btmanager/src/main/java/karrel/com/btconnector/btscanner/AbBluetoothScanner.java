package karrel.com.btconnector.btscanner;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

/**
 * Created by Rell on 2018. 3. 23..
 */

public abstract class AbBluetoothScanner implements BluetoothScannable {
    // 블루투스 스캔 리스너
    public interface BluetoothScanListener {
        // 블루투스가 사용가능하도록 설정되는것이 필요함
        void requireEnableBt();

        // 기기 찾음
        void onSearchedDevice(BluetoothDevice device);
    }

    // 블루투스 스캔 리스너
    protected BluetoothScanListener bluetoothScanListener;

    // 블루투스 아답터
    protected BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    // 생성자
    public AbBluetoothScanner(BluetoothScanListener blue) {
        this.bluetoothScanListener = blue;
    }

    protected abstract void startScanBluetoothDevice();

    @Override
    public void scanBluetoothDevice() {
        if (!isEnabledBluetooth()) {
            bluetoothScanListener.requireEnableBt();
            return;
        }

        stopScanBluetoothDevice();
        startScanBluetoothDevice();
    }

    // 블루투스 사용가능한가?
    private boolean isEnabledBluetooth() {
        return bluetoothAdapter.isEnabled();
    }
}
