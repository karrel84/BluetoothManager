package karrel.com.btconnector.chatmanager;

import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Message;

import com.karrel.mylibrary.RLog;

import karrel.com.btconnector.BluetoothService;
import karrel.com.btconnector.Constants;
import rx.Observable;
import rx.Observer;
import rx.functions.Action0;

/**
 * Created by Rell on 2018. 1. 29..
 */

public class BluetoothChatManager {
    private static BluetoothChatManager instance;
    private BluetoothService bluetoothService;
    private BluetoothChatListener bluetoothChatListener;
    private BluetoothDevice bluetoothDevice;

    // 블루투스 상태
    private int mStatus = BluetoothService.STATE_NONE;

    private BluetoothHandler bluetoothHandler;

    public static BluetoothChatManager getInstance() {
        if (instance == null) {
            instance = new BluetoothChatManager();
        }
        return instance;
    }

    public void setListener(BluetoothChatListener listener) {
        this.bluetoothChatListener = listener;
    }


    public boolean isConnected() {
        return mStatus == BluetoothService.STATE_CONNECTED;
    }

    public void connect(final BluetoothDevice bluetoothDevice) {
        RLog.e("bluetooth name : " + bluetoothDevice.getName());
        // 접속할 기기 제거
        this.bluetoothDevice = bluetoothDevice;

        // 접속 해제
        unConnect();

        bluetoothHandler = new BluetoothHandler();
        // 접속시도
        Thread thread = new Thread(() -> {
            bluetoothService = new BluetoothService(bluetoothHandler);
            bluetoothChatListener.onStartConnect(bluetoothDevice.getName());
            bluetoothService.connect(bluetoothDevice, true);
        });
        thread.start();
    }

    public void send(byte[] out) {
        bluetoothService.write(out);
    }


    public void start() {
        if (bluetoothService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (bluetoothService.getState() == BluetoothService.STATE_NONE) {
                // Start the Bluetooth chat services
                bluetoothService.start();
            }
        }
    }


    class BluetoothHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (bluetoothChatListener == null) return;

            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    mStatus = msg.arg1;
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            RLog.d("onConnected");
                            bluetoothChatListener.onConnected(bluetoothDevice.getName());
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            RLog.d("onConnecting");
                            bluetoothChatListener.onConnecting(bluetoothDevice.getName());
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                            RLog.d("onConnectFailed");
                            bluetoothChatListener.onConnectFailed(bluetoothDevice.getName());

                            break;
                    }
                    break;
                case Constants.MESSAGE_SEND:
                    RLog.d("onMessageSend");
                    byte[] writeBuf = (byte[]) msg.obj;

                    bluetoothChatListener.onMessageSend(writeBuf);
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    bluetoothChatListener.onReadMessage(readBuf);
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    RLog.d("onDeviceName");
//                    mDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    break;
                case Constants.MESSAGE_TOAST:
                    final String toast = msg.getData().getString(Constants.TOAST);
                    RLog.d(toast);
                    break;
            }
        }
    }

    // 접속해제
    public void unConnect() {
        if (bluetoothService != null) {
            bluetoothService.stop();
        }
    }
}
