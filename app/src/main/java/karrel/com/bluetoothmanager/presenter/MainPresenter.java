package karrel.com.bluetoothmanager.presenter;

/**
 * Created by Rell on 2018. 3. 23..
 */

public interface MainPresenter {
    void searchBt();

    void enabledBluetooth();

    void dissableBluetooth();

    interface View {

        void requireEnableBt();
    }
}
