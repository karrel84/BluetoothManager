package karrel.com.btconnector.btscanner;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.karrel.mylibrary.RLog;

import java.util.List;

import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * Created by Rell on 2018. 3. 23..
 */

// LE 혹은 BLE의 블루투스 기기를 스캔한다
public class LeBluetoothSanner extends AbBluetoothScanner {

    // 블루투스 디바이스의 서브젝트
    private PublishSubject<BluetoothDevice> bluetoothDeviceMassanger;

    // 블루투스 기기 서브젝트
    private Subscription subscriptionBluetoothDevices;

    // 블루투스 LE 스캐너 5.0 이상 버전에서 사용된다
    private BluetoothLeScanner bluetoothScanner;

    // 안드로이드 펌웨어 5.0 이상에서 사용되는 스캔 콜백이다.
    private ScanCallback bluetoothScannedCallback = null;

    // 안드로이드 펌웨어 5.0 미만에서 사용되던 스캔 콜백이다.
    private BluetoothAdapter.LeScanCallback bluetoothScannedCallbackLowVersion = null;

    public LeBluetoothSanner(BluetoothScanListener listener) {
        super(listener);
        // 스캐너 초기화
        initBluetoothScanner();
    }

    // 스캐너 초기화
    private void initBluetoothScanner() {
        // 롤리팝 이상버전에서는 LE scanner 를 초기화한다
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bluetoothScanner = bluetoothAdapter.getBluetoothLeScanner();
        }
    }


    private void initPublishSubject() {
        // 블루투스가 검색되면 전달할 서브젝트를 초기화 한다
        bluetoothDeviceMassanger = PublishSubject.create();

        // 블루투스 객체를 전달하는 섭스크립션을 클리어
        clearDeviceSubscription();

        subscriptionBluetoothDevices = bluetoothDeviceMassanger
                .onBackpressureDrop() // 백프레져는 버린다
                .filter(d -> d.getName() != null) // 기기의 이름이 없으면 버린다
                .distinct() // 중복되는 기기명은 버린다
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BluetoothDevice>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(BluetoothDevice device) {
                        bluetoothScanListener.onSearchedDevice(device);
                    }
                });
    }

    // 블루투스 객체를 전달하는 섭스크립션을 클리어
    private void clearDeviceSubscription() {
        RLog.d();
        if (subscriptionBluetoothDevices != null) {
            subscriptionBluetoothDevices.unsubscribe();
            subscriptionBluetoothDevices = null;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void startScanBluetoothDevice() {
        RLog.d();
        // 콜백을 초기화한다
        initScanedDeviceCallback();
        // 섭스크립셔 초기화
        initPublishSubject();

        // 스캔을 시작한다
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bluetoothScanner.startScan(bluetoothScannedCallback);
        } else {
            bluetoothAdapter.startLeScan(bluetoothScannedCallbackLowVersion);
        }

    }

    // 콜백 초기화
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void initScanedDeviceCallback() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bluetoothScannedCallback = new BluetoothScanCallback();
        } else {
            bluetoothScannedCallbackLowVersion = new BluetoothScanCallbackForLowVersion();
        }
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void stopScanBluetoothDevice() {
        RLog.d();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (bluetoothScannedCallback != null) bluetoothScanner.stopScan(bluetoothScannedCallback);
            bluetoothScannedCallback = null;
        } else {
            if (bluetoothScannedCallbackLowVersion != null) bluetoothAdapter.stopLeScan(bluetoothScannedCallbackLowVersion);
            bluetoothScannedCallbackLowVersion = null;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    class BluetoothScanCallbackForLowVersion implements BluetoothAdapter.LeScanCallback {

        @Override
        public void onLeScan(BluetoothDevice device, int i, byte[] bytes) {
            try {
                bluetoothDeviceMassanger.onNext(device);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    class BluetoothScanCallback extends ScanCallback {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            processResult(result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult result : results) {
                processResult(result);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            RLog.e("errorCode : " + errorCode);
        }

        private void processResult(final ScanResult result) {
            try {
                BluetoothDevice device = result.getDevice();
//                RLog.d(String.format("name : %s ", device.getName()) + device.toString());
                bluetoothDeviceMassanger.onNext(device);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}
