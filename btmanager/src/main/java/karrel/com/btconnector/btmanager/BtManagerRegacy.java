package karrel.com.btconnector.btmanager;

import android.Manifest;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import com.gun0912.tedpermission.PermissionListener;
import com.karrel.mylibrary.RLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import karrel.com.btconnector.BtChatListener;
import karrel.com.btconnector.BtChatManager;
import karrel.com.btconnector.BtLifecyclable;
import karrel.com.btconnector.BtScanListener;
import karrel.com.btconnector.btscanner.BtScanner;
import karrel.com.btconnector.btscanner.BtScannerable;
import karrel.com.btconnector.btscanner.LeBtSanner;
import karrel.com.btconnector.permission.PermissionCheckable;
import karrel.com.btconnector.permission.PermissionChecker;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by Rell on 2018. 1. 29..
 * <p>
 * 블루투스 리스트 조회, 접속, 데이터 송/수신
 */

public class BtManagerRegacy implements BtChatListener, BtListener, BtLifecyclable, BtManagerable {

    private static BtManagerRegacy mBtManager;
    private final Context context;
    private BtChatManager mBtChatManager;
    private BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();

    private boolean isScanDiscovery = true;
    private List<BtListener> mBtListenerList = new ArrayList<>();
    private BluetoothA2dp mBluetoothA2dp;

    private boolean isConnecting;
    private BluetoothDevice mDevice;

    // 퍼미션 체커
    private PermissionCheckable permissionChecker;

    // 블루투스 스캐너
    private BtScannerable btScanner;

    // 체크해야할 권한
    private String[] permissions = {Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_COARSE_LOCATION};

    public static BtManagerRegacy getInstance(Context context, BtListener listener) {
        if (mBtManager == null) {
            mBtManager = new BtManagerRegacy(context, listener);
        }
        return mBtManager;
    }


    public BtManagerRegacy(Context context, BtListener listener) {
        this.context = context;
        permissionChecker = new PermissionChecker(context);
        btScanner = new LeBtSanner(btScannerListener);
        init();
        addListener(listener);
    }

    private void init() {
        RLog.d();
        // init chat manager
        mBtChatManager = BtChatManager.getInstance();
        mBtChatManager.setListener(this);

    }

    public boolean isConnected() {
        return mBtChatManager.isConnected();
    }

    public void addListener(BtListener listener) {
        mBtListenerList.add(listener);
    }

    public void removeListener(BtListener listener) {
        mBtListenerList.remove(listener);
    }

    public BtManagerRegacy setScanDiscovery(boolean isScanDiscovery) {
        this.isScanDiscovery = isScanDiscovery;
        return this;
    }

    public synchronized void connect(BluetoothDevice device) {
        if (isConnecting) return;
        mDevice = device;
        isConnecting = true;
        checkPermission(new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                // 검색 중단
//                stopScan();
                // connnect to BT
                mBtChatManager.connect(device);
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                deniedPermission();
                isConnecting = false;
            }
        });
    }


    /**
     * 이미 접속된 기기가 있는지 체크
     * 시스템에서 연결된 기기가 있으면 가져온다.
     */
    public void checkConnectedDevice() {
        RLog.e();
        mAdapter.getProfileProxy(context, mProfileListener, BluetoothProfile.A2DP);
    }
//
//
//    private void startScan() {
//        if (isScanDiscovery) {
//            mBtScanManager.startScanDiscovery(context);
//        } else {
//            mBtScanManager.startScanLe();
//        }
//
//
//        onBondedDevices(mAdapter.getBondedDevices());
//
//    }
//
//    private void stopScan() {
//        if (mBtScanManager == null) {
//            return;
//        }
//
//        if (isScanDiscovery) {
//            mBtScanManager.stopScanDiscovery(context);
//        } else {
//            mBtScanManager.stopBtScan();
//        }
//    }


    /**
     * 블루투스가 사용 가능하면
     */
    private boolean isEnabledBluetooth() {
        return mAdapter.isEnabled();
    }


    public void sendMessage(byte[] msg) throws Exception {
        if (!mBtChatManager.isConnected()) {
            throw new Exception("기기가 연결되지 않았습니다.");
        }

        mBtChatManager.send(msg);
    }

    private BtScanListener mScanListener = device -> scanedDevice(device);

    //    @Override
    public void onBondedDevices(Set<BluetoothDevice> bondedDevices) {
        Observable.just(bondedDevices)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(d -> {
                    for (BtListener listener : mBtListenerList) {
//                        listener.onBondedDevices(d);
                    }
                }, e -> e.printStackTrace());
    }

    @Override
    public void onConnected(String deviceName) {
        isConnecting = false;
        RLog.e(deviceName);
        Observable.just(deviceName)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                    for (BtListener listener : mBtListenerList) {
//                        listener.onConnected(deviceName);
                    }
                }, e -> e.printStackTrace());
    }

    @Override
    public void onConnecting(String deviceName) {
        RLog.e(deviceName);
        Observable.just(deviceName)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                    for (BtListener listener : mBtListenerList) {
//                        listener.onConnecting(deviceName);
                    }
                }, e -> e.printStackTrace());

    }

    @Override
    public void onConnectFailed(String deviceName) {
        isConnecting = false;
        RLog.e(deviceName);
        Observable.just(deviceName)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                    for (BtListener listener : mBtListenerList) {
//                        listener.onConnectFailed(deviceName);
                    }
                }, e -> e.printStackTrace());
    }

    @Override
    public void onMessageSend(byte[] writeBuf) {
//        RLog.d();
        Observable.just(writeBuf)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                    for (BtListener listener : mBtListenerList) {
//                        listener.onMessageSend(writeBuf);
                    }
                }, e -> e.printStackTrace());
    }

    @Override
    public void onReadMessage(byte[] readBuf) {
        Observable.just(readBuf)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                    for (BtListener listener : mBtListenerList) {
//                        listener.onReadMessage(readBuf);
                    }
                }, e -> e.printStackTrace());
    }

    @Override
    public void onStartConnect(String deviceName) {
        RLog.d();
        Observable.just(deviceName)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                    for (BtListener listener : mBtListenerList) {
//                        listener.onStartConnect(deviceName);
                    }
                }, e -> e.printStackTrace());

    }

    @Override
    public void onRetry(String name, int cnt) {
        Observable.just(name)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                    for (BtListener listener : mBtListenerList) {
//                        listener.onRetry(name, cnt);
                    }
                }, e -> e.printStackTrace());
    }

    @Override
    public void deniedPermission() {
        RLog.d();

        Observable.just("")
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                    for (BtListener listener : mBtListenerList) {
                        listener.deniedPermission();
                    }
                }, e -> e.printStackTrace());
    }

    @Override
    public void requireEnableBt() {
        RLog.d();
        Observable.just("")
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                    for (BtListener listener : mBtListenerList) {
                        listener.requireEnableBt();
                    }
                }, e -> e.printStackTrace());
    }

    @Override
    public void onSearchedDevice(BluetoothDevice device) {

    }

    //    @Override
    public void onScanFail(String message) {
        RLog.d();
        Observable.just(message)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                    for (BtListener listener : mBtListenerList) {
//                        listener.onScanFail(message);
                    }
                }, e -> e.printStackTrace());
    }

    //    @Override
    public void scanedDevice(BluetoothDevice bluetoothDevice) {
//        RLog.d();
        Observable.just(bluetoothDevice)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                    for (BtListener listener : mBtListenerList) {
//                        listener.scanedDevice(bluetoothDevice);
                    }
                }, e -> e.printStackTrace());
    }

    //    @Override
    public void onHaveConnectedDevice(BluetoothDevice bluetoothDevice) {
        Observable.just(bluetoothDevice)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                    for (BtListener listener : mBtListenerList) {
//                        listener.onHaveConnectedDevice(bluetoothDevice);
                    }
                }, e -> e.printStackTrace());
    }

    private BluetoothProfile.ServiceListener mProfileListener = new BluetoothProfile.ServiceListener() {
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            RLog.d("profile : " + profile);

            if (profile == BluetoothProfile.A2DP) {
                mBluetoothA2dp = (BluetoothA2dp) proxy;

                if (mBluetoothA2dp.getConnectedDevices().size() > 0) { //A2DP 프로파일로 연결된 장치가 있으면.
                    // 연결된 장치와 socket 연결을 한다.
                    RLog.d("HaveConnectedDevice");
                    BluetoothDevice device = mBluetoothA2dp.getConnectedDevices().get(0);
                    onHaveConnectedDevice(device);

                } else {
                    RLog.d("Not HaveConnectedDevice");
                }
            }

            RLog.d("closeProfileProxy");
            mAdapter.closeProfileProxy(BluetoothProfile.A2DP, proxy);
        }

        @Override
        public void onServiceDisconnected(int profile) {

        }
    };

    // 접속 가능상태인지?
    public boolean isAutoConnectable() {
        RLog.d();
        // 블루투스 사용 불가 상태면 리턴 펄스
        if (!isEnabledBluetooth()) return false;
        RLog.d();
        // 퍼미션 체크 안되어있으면 리턴 펄스
        if (!isPermissionGranted()) return false;
        RLog.d();

        return true;
    }

    public boolean isPermissionGranted() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        for (String p : permissions) {
            boolean isGranted = context.checkSelfPermission(p) == PackageManager.PERMISSION_GRANTED;
            RLog.d(String.format("p : %s, is : %s", p, isGranted));
            if (!isGranted) {
                return false;
            }
        }
        return true;
    }


    @Override
    public void onStart() {
        // 기존에 접속했던 BT가 있으면 자동 접속을 해주는게 맞으려나 ...
        if (mDevice != null) {
            mBtChatManager.connect(mDevice);
        }
    }

    @Override
    public void onStop() {
        // 접속을 종료한다
        mBtChatManager.unConnect();
    }

    @Override
    public void scanBluetooth() {
        // 퍼미션이 체크 완료되었는가?
        checkPermission(new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                // 스캔 시작
                startBtScan();
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                deniedPermission();
            }
        });
    }

    // 퍼미션 체크
    private void checkPermission(PermissionListener listener) {
        permissionChecker.checkPermission(permissions, listener);
    }

    // 블루투스 스캔 시작
    private void startBtScan() {
        // 블루투스 스캔 시작
        btScanner.startBtScan();
    }

    // 블루투스 스캐너 리스너
    private final BtScanner.BluetoothScanListener btScannerListener = new BtScanner.BluetoothScanListener() {
        @Override
        public void requireEnableBt() {
            // 블루투스 이용가능 설정이 필요합니다.
            BtManagerRegacy.this.requireEnableBt();
        }

        @Override
        public void onSearchedDevice(BluetoothDevice device) {
            RLog.d(String.format("검색된 기기는 %s", device.getName()));
        }
    };
}
