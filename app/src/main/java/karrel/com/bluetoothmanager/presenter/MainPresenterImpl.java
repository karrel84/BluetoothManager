package karrel.com.bluetoothmanager.presenter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;

import com.karrel.mylibrary.RLog;

import karrel.com.btconnector.btmanager.BluetoothListener;
import karrel.com.btconnector.btmanager.BtManager;
import karrel.com.btconnector.btmanager.BluetoothManagerable;

/**
 * Created by Rell on 2018. 3. 23..
 */

public class MainPresenterImpl implements MainPresenter {
    private MainPresenter.View view;
    private BluetoothManagerable bluetoothManager;

    public MainPresenterImpl(MainPresenter.View view, Context context) {
        this.view = view;

        bluetoothManager = BtManager.getInstance(context, btListener);
    }

    @Override
    public void searchBt() {
        RLog.e();
        bluetoothManager.scanBluetooth();
    }

    // BT 설정 성공
    @Override
    public void enabledBluetooth() {
        // 블루투스 탐색 시작
        searchBt();
    }

    @Override
    public void dissableBluetooth() {
        // BT 설정 실패
        RLog.e();
    }

    private final BluetoothListener btListener = new BluetoothListener() {
        @Override
        public void deniedPermission() {
            RLog.e();
        }

        @Override
        public void requireEnableBt() {
            RLog.e();
            view.requireEnableBt();
        }

        @Override
        public void onSearchedDevice(BluetoothDevice device) {
            RLog.d("device : " + device.getName());

            String name = device.getName();
            if (name.equals("MASSAGE")) {
                bluetoothManager.connect(device);
            }
        }
    };
}
