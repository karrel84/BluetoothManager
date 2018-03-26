package karrel.com.bluetoothmanager.presenter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;

import com.karrel.mylibrary.RLog;

import karrel.com.btconnector.btmanager.BluetoothListener;
import karrel.com.btconnector.btmanager.BluetoothManager;
import karrel.com.btconnector.btmanager.BluetoothManagerable;

/**
 * Created by Rell on 2018. 3. 23..
 */

public class MainPresenterImpl implements MainPresenter {
    private MainPresenter.View view;
    private BluetoothManagerable bluetoothManager;
    // 블루투스 기기
    private BluetoothDevice bluetoothDevice;

    public MainPresenterImpl(MainPresenter.View view, Context context) {
        this.view = view;

        bluetoothManager = BluetoothManager.getInstance(context, bluetoothListener);
    }

    @Override
    public void searchBluetoothDevices() {
        RLog.e();
        bluetoothManager.startBluetoothDeviceScan();
    }

    // BT 설정 성공
    @Override
    public void enabledBluetooth() {
        // 블루투스 탐색 시작
        searchBluetoothDevices();
    }

    @Override
    public void dissableBluetooth() {
        // BT 설정 실패
        RLog.e();
    }

    @Override
    public void connectBluetooth() {
        RLog.e();
        if (bluetoothDevice == null) return;
        bluetoothManager.connect(bluetoothDevice);
    }

    @Override
    public void disconnectBluetooth() {
        bluetoothManager.disConnect();
    }

    private final BluetoothListener bluetoothListener = new BluetoothListener() {
        @Override
        public void onConnectedSuccess(String deviceName) {
            RLog.e("deviceName : " + deviceName);
        }

        @Override
        public void onConnecting(String deviceName) {
            RLog.e("deviceName : " + deviceName);
        }

        @Override
        public void onInitalized(String deviceName) {
            RLog.e("deviceName : " + deviceName);
        }

        @Override
        public void onMessageSend(byte[] writeBuf) {
            RLog.e("writeBuf : " + writeBuf);
        }

        @Override
        public void onReadMessage(byte[] readBuf) {
            RLog.e("readBuf : " + readBuf);
        }

        @Override
        public void onStartConnect(String deviceName) {
            RLog.e("deviceName : " + deviceName);
        }

        @Override
        public void onConnectedFail(String name) {
            RLog.e("deviceName : " + name);
        }

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
                bluetoothDevice = device;
            }
        }
    };
}
