package karrel.com.btconnector.chatmanager;

/**
 * Created by Rell on 2018. 1. 29..
 */

public interface BluetoothChatListener {
    void onConnectedSuccess(String deviceName);

    void onConnecting(String deviceName);

    void onInitalized(String deviceName);

    void onMessageSend(byte[] writeBuf);

    void onReadMessage(byte[] readBuf);

    void onStartConnect(String deviceName);

    void onConnectedFail(String name);
}
