package karrel.com.btconnector;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Message;

import com.karrel.mylibrary.RLog;

/**
 * Created by Rell on 2018. 1. 29..
 */

public class BtChatManager {
    private static BtChatManager instance;
    private BtService mChatService;
    private boolean isConnecting;
    private BtChatListener mListener;
    private String mDeviceName;

    private int mStatus = BtService.STATE_NONE;

    private BluetoothDevice mDevice;

    public static BtChatManager getInstance() {
        if (instance == null) {
            instance = new BtChatManager();
        }
        return instance;
    }

    public void setListener(BtChatListener listener) {
        this.mListener = listener;
    }


    public boolean isConnected() {
        return mStatus == BtService.STATE_CONNECTED;
    }

    public void connect(final BluetoothDevice device) {
        if (isConnecting) return;
        isConnecting = true;
        mDevice = device;
        mDeviceName = device.getName();

        RLog.e(String.format("connect %s", device.getName()));
        if (mChatService != null) {
            mChatService.stop();
        }
        Thread thread = new Thread(() -> {
            try {
                RLog.d("sleep");
                Thread.sleep(mChatService != null ? 3000 : 0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                RLog.d("finally");
                mChatService = new BtService(mHandler);
                mListener.onStartConnect(device.getName());
                mChatService.connect(device, true);
                isConnecting = false;
            }
        });
        thread.start();
    }

    public void send(byte[] out) {
        mChatService.write(out);
    }


    public void start() {
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BtService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }
    }


    /**
     * The Handler that gets information byte_box from the BtService
     */
    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (mListener == null) return;

            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    mStatus = msg.arg1;
                    switch (msg.arg1) {
                        case BtService.STATE_CONNECTED:
                            RLog.d("onConnected");
                            mListener.onConnected(mDeviceName);
                            break;
                        case BtService.STATE_CONNECTING:
                            RLog.d("onConnecting");
                            mListener.onConnecting(mDeviceName);
                            break;
                        case BtService.STATE_LISTEN:
                        case BtService.STATE_NONE:
                            RLog.d("onConnectFailed");
                            mListener.onConnectFailed(mDeviceName);

                            break;
                    }
                    break;
                case Constants.MESSAGE_SEND:
                    RLog.d("onMessageSend");
                    byte[] writeBuf = (byte[]) msg.obj;
                    if (RLog.isEnabled()) {
//                        RLog.e(ByteConverter.byteToHexLog(writeBuf));
                    }

                    mListener.onMessageSend(writeBuf);
                    break;
                case Constants.MESSAGE_READ:
//                    RLog.d("onReadMessage");
                    byte[] readBuf = (byte[]) msg.obj;

                    if (RLog.isEnabled()) {
//                        RLog.e(ByteConverter.byteToHexLog(readBuf));
                    }

                    mListener.onReadMessage(readBuf);
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    RLog.d("onDeviceName");
                    mDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    break;
                case Constants.MESSAGE_TOAST:
                    final String toast = msg.getData().getString(Constants.TOAST);
                    RLog.d(toast);
                    break;
            }
        }
    };

    public void unConnect() {
        if (mChatService != null) {
            mChatService.stop();
        }
    }
}
