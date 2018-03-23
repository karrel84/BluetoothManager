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
public class LeBtSanner extends BtScanner {

    // 블루투스 디바이스의 서브젝트
    private PublishSubject<BluetoothDevice> bdSubject;

    // 블루투스 기기 서브젝트
    private Subscription subscriptionDevices;

    // 블루투스 LE 스캐너 5.0 이상 버전에서 사용된다
    private BluetoothLeScanner leScanner;

    public LeBtSanner(BluetoothScanListener listener) {
        super(listener);
        // 콜백 리스너 초기화
        initPublishSubject();

        // 스캐너 초기화
        initScanner();
    }

    private void initScanner() {
        // 롤리팝 이상버전에서는 LE scanner 를 초기화한다
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            leScanner = btAdapter.getBluetoothLeScanner();
        }
    }


    private void initPublishSubject() {
        // 블루투스가 검색되면 전달할 서브젝트를 초기화 한다
        bdSubject = PublishSubject.create();

        // 블루투스 객체를 전달하는 섭스크립션을 클리어
        clearDeviceSubscription();

        subscriptionDevices = bdSubject
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
                        listener.onSearchedDevice(device);
                    }
                });
    }

    // 블루투스 객체를 전달하는 섭스크립션을 클리어
    private void clearDeviceSubscription() {
        if (subscriptionDevices != null) {
            subscriptionDevices.unsubscribe();
            subscriptionDevices = null;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void startScan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            leScanner.startScan(scanCallback);
        } else {
            btAdapter.startLeScan(leScanCallback);
        }

    }


    // 안드로이드 펌웨어 5.0 이상에서 사용되는 스캔 콜백이다.
    private ScanCallback scanCallback = new ScanCallback() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            processResult(result);
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
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

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        private void processResult(final ScanResult result) {
            try {
                BluetoothDevice device = result.getDevice();
//                RLog.d(String.format("name : %s ", device.getName()) + device.toString());
                bdSubject.onNext(device);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };

    // 안드로이드 펌웨어 5.0 미만에서 사용되던 스캔 콜백이다.
    private BluetoothAdapter.LeScanCallback leScanCallback = (device, rssi, scanRecord) -> {
        try {
            bdSubject.onNext(device);
        } catch (Exception e) {
            e.printStackTrace();
        }
    };

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void stopScan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            leScanner.stopScan(scanCallback);
        } else {
            btAdapter.stopLeScan(leScanCallback);
        }

        clearDeviceSubscription();
    }
}
