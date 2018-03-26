package karrel.com.btconnector.btmanager;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.Context;

import com.gun0912.tedpermission.PermissionListener;
import com.karrel.mylibrary.RLog;

import java.util.ArrayList;

import karrel.com.btconnector.btscanner.BluetoothScannable;
import karrel.com.btconnector.btscanner.BluetoothScanner;
import karrel.com.btconnector.btscanner.LeBluetoothSanner;
import karrel.com.btconnector.chatmanager.BluetoothChatManager;
import karrel.com.btconnector.permission.PermissionCheckable;
import karrel.com.btconnector.permission.PermissionChecker;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by Rell on 2018. 3. 23..
 * <p>
 * 블루투스 리스트 조회, 접속, 데이터 송/수신
 */

public class BluetoothManager implements BluetoothListener, BluetoothManagerable {

    // 싱글턴
    private static BluetoothManager bluetoothManager;

    // 퍼미션 체커
    private PermissionCheckable permissionChecker;

    // 블루투스 스캐너
    private BluetoothScannable bluetoothScanner;

    // 체크해야할 권한
    private String[] bluetoothPermissions = {
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_COARSE_LOCATION};

    // 블루투스 콜백
    private BluetoothListener bluetoothCallback;

    // 채팅 매니저
    private BluetoothChatManager bluetoothChatManager = BluetoothChatManager.getInstance();

    // 싱글턴
    public static BluetoothManager getInstance(Context context, BluetoothListener listener) {
        if (bluetoothManager == null) {
            bluetoothManager = new BluetoothManager(context, listener);
        }
        return bluetoothManager;
    }

    // 생성자
    public BluetoothManager(Context context, BluetoothListener listener) {
        bluetoothCallback = listener;

        // 퍼미션 체커
        permissionChecker = new PermissionChecker(context);
        // 디바이스 스캐너
        bluetoothScanner = new LeBluetoothSanner(btScannerListener);
        // 채팅 매니저 콜백
        bluetoothChatManager.setListener(this);
    }

    // 권한이 거부됨
    @Override
    public void deniedPermission() {
        RLog.d();
        Observable.just("")
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> bluetoothCallback.deniedPermission(), e -> e.printStackTrace());
    }

    // 블루투스 활성화 요청
    @Override
    public void requireEnableBt() {
        RLog.d();
        Observable.just("")
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> bluetoothCallback.requireEnableBt(), e -> e.printStackTrace());

    }

    @Override
    public void onSearchedDevice(BluetoothDevice device) {
        Observable.just(device)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bluetoothCallback::onSearchedDevice, e -> e.printStackTrace());
    }


    // 블루투스 스캔
    @Override
    public void startBluetoothDeviceScan() {
        // 퍼미션이 체크 완료되었는가?
        checkPermission(new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                // 스캔 시작
                scanBluetoothDevice();
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                // 퍼미션 거부됨
                deniedPermission();
            }
        });
    }

    @Override
    public void stopBluetoothDeviceScan() {
        // 스캔 중지
        bluetoothScanner.stopScanBluetoothDevice();
    }

    @Override
    public void connect(BluetoothDevice device) {
        // 스캔 중지
        stopBluetoothDeviceScan();
        // 블루투스 접속
        bluetoothChatManager.connect(device);
    }

    @Override
    public void disConnect() {
        if (bluetoothChatManager.isConnected()) bluetoothChatManager.disConnect();
    }

    // 퍼미션 체크
    private void checkPermission(PermissionListener listener) {
        permissionChecker.checkPermission(bluetoothPermissions, listener);
    }

    // 블루투스 스캔 시작
    private void scanBluetoothDevice() {
        RLog.d();
        // 블루투스 스캔 시작
        bluetoothScanner.scanBluetoothDevice();
    }

    // 블루투스 스캐너 리스너
    private final BluetoothScanner.BluetoothScanListener btScannerListener = new BluetoothScanner.BluetoothScanListener() {
        @Override
        public void requireEnableBt() {
            // 블루투스 이용가능 설정이 필요합니다.
            BluetoothManager.this.requireEnableBt();
        }

        @Override
        public void onSearchedDevice(BluetoothDevice device) {
            BluetoothManager.this.onSearchedDevice(device);
        }
    };

    @Override
    public void onConnectedSuccess(String deviceName) {
        Observable.just(deviceName)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(d -> bluetoothCallback.onConnectedSuccess(d), e -> e.printStackTrace());
    }

    @Override
    public void onConnecting(String deviceName) {
        Observable.just(deviceName)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(d -> bluetoothCallback.onConnecting(d), e -> e.printStackTrace());
    }

    @Override
    public void onInitalized(String deviceName) {
        Observable.just(deviceName)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(d -> bluetoothCallback.onInitalized(d), e -> e.printStackTrace());
    }

    @Override
    public void onMessageSend(byte[] writeBuf) {
        Observable.just(writeBuf)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(d -> bluetoothCallback.onMessageSend(d), e -> e.printStackTrace());
    }

    @Override
    public void onReadMessage(byte[] readBuf) {
        Observable.just(readBuf)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(d -> bluetoothCallback.onReadMessage(d), e -> e.printStackTrace());
    }

    @Override
    public void onStartConnect(String deviceName) {
        Observable.just(deviceName)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(d -> bluetoothCallback.onStartConnect(d), e -> e.printStackTrace());
    }

    @Override
    public void onConnectedFail(String name) {
        Observable.just(name)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(d -> bluetoothCallback.onConnectedFail(d), e -> e.printStackTrace());
    }
}
