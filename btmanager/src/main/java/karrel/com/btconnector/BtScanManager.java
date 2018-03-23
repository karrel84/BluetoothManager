package karrel.com.btconnector;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;

import com.karrel.mylibrary.RLog;

import java.util.List;
import java.util.Set;

import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * Created by Rell on 2018. 1. 29..
 * <p>
 * 블루투스 리스트를 가져오는 메니저이다.
 */

public class BtScanManager {

    private BluetoothAdapter mBtAdapter;

    private BluetoothLeScanner mBleScanner;
    private ScanCallback mScanCallback;
    private BluetoothAdapter.LeScanCallback mLeScanCallback;

    private boolean mScanning;

    private static BtScanManager intance;
    private PublishSubject<BluetoothDevice> mPublishSubject;

    private BtScanListener mListener;
    private Subscription mSubscription;

    /**
     * BT 전체 검색
     */
    private boolean isScanDiscovery = true;
    private Subscription mSubscriptionList;

    public BtScanManager(BtScanListener listener) throws Exception {
        init();
        mListener = listener;
    }

    public static BtScanManager getInstance(BtScanListener listener) throws Exception {
        RLog.d();
        if (intance == null) {
            intance = new BtScanManager(listener);
        }
        return intance;
    }

    private void init() throws Exception {
        RLog.d();

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) throw new Exception("블루투스 기능을 이용할 수 없습니다.");

        // BLE 스캐너를 만든다
        boolean useableBle = setupBleScanner();
        // BLE 를 쓸 수 없다면 LE 스캐너를 구동한다.
        if (useableBle == false) setupLeScanner();
    }

    private void initPublishSubject() {
        mPublishSubject = PublishSubject.create();

        if (mSubscriptionList != null) {
            mSubscriptionList.unsubscribe();
            mSubscriptionList = null;
        }

        mSubscriptionList = mPublishSubject
                .onBackpressureDrop()
                .filter(d -> d.getName() != null)
                .distinct()
                .subscribeOn(Schedulers.newThread())
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
                        mListener.onSearchedDevice(device);
                    }
                });
    }


    @SuppressLint("NewApi")
    public void startScanLe() {
        RLog.d("startScanLe");
        if (mScanning) return;
        mScanning = true;

        initPublishSubject();

        if (mBleScanner != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mBleScanner.startScan(mScanCallback);
            }
        } else {
            mBtAdapter.startLeScan(mLeScanCallback);
        }
    }

    @SuppressLint("NewApi")
    public void stopBtScan() {
        RLog.d("stopBtScan");
        if (!mScanning) return;
        RLog.d("stopBtScan");

        mScanning = false;
        if (mBleScanner != null) {
            mBleScanner.stopScan(mScanCallback);
        } else {
            mBtAdapter.stopLeScan(mLeScanCallback);
        }
    }


    /**
     * discovery 를 통해서 장치를 scan 한다.
     *
     * @param context
     */
    public void startScanDiscovery(Context context) {
        RLog.d();
        if (mBtAdapter.isDiscovering()) {
            return;
        }

        initPublishSubject();

        getPairedDevice();

        context.registerReceiver(mDiscoveryReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        mBtAdapter.startDiscovery();
    }

    private void getPairedDevice() {
        // 블루투스 adapter가 있으면, 블루투스 adater에서 페어링된 장치 목록을 불러올 수 있다.
        Set<BluetoothDevice> pairDevices = mBtAdapter.getBondedDevices();

        //페어링된 장치가 있으면
        if (pairDevices.size() > 0) {
            for (BluetoothDevice device : pairDevices) {
                //페어링된 장치 이름과, MAC주소를 가져올 수 있다.
                mListener.onSearchedDevice(device);
            }
        }
    }

    public void stopScanDiscovery(Context context) {
        RLog.d();
        try {
            context.unregisterReceiver(mDiscoveryReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mBtAdapter.cancelDiscovery();
    }


    private boolean setupBleScanner() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (createBleScanner()) {
                setupBleScancallback();
                return true;
            }
        }
        return false;
    }

    /**
     * LE scanner
     */
    private void setupLeScanner() {
        mLeScanCallback = (device, rssi, scanRecord) -> {
            try {
                RLog.d(String.format("name : %s rssi : %s", device.getName(), rssi));
                mPublishSubject.onNext(device);
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

    }

    /**
     * BLE 스캐너를 만든다
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private boolean createBleScanner() {
        RLog.d("mBtAdapter : " + mBtAdapter.toString());
        mBleScanner = mBtAdapter.getBluetoothLeScanner();
        return mBleScanner != null;
    }

    /**
     * BLE 스캔 콜백을 만든다
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setupBleScancallback() {
        mScanCallback = new ScanCallback() {
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
                RLog.d(".onScanFailed");
            }

            private void processResult(final ScanResult result) {
                try {
                    BluetoothDevice device = result.getDevice();

                    RLog.d(String.format("name : %s ", device.getName()) + device.toString());

                    mPublishSubject.onNext(device);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        };
    }


    private BroadcastReceiver mDiscoveryReceiver = new BroadcastReceiver() {

        @Override

        public void onReceive(Context context, Intent intent) {

            if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                RLog.d(String.format("name : %s ", device.getName()) + device.toString());
                mPublishSubject.onNext(device);
            }
        }
    };
}
