package karrel.com.btconnector.chatmanager;

/**
 * Created by Rell on 2018. 1. 29..
 */

public interface BtChatListener {
    void onConnected(String deviceName);

    void onConnecting(String deviceName);

    void onConnectFailed(String deviceName);

    void onMessageSend(byte[] writeBuf);

    void onReadMessage(byte[] readBuf);

    void onStartConnect(String deviceName);

    void onRetry(String deviceName, int cnt);
}
