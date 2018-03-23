package karrel.com.btconnector.btmanager;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.Context;

import com.gun0912.tedpermission.PermissionListener;
import com.karrel.mylibrary.RLog;

import java.util.ArrayList;

import karrel.com.btconnector.btscanner.BtScanner;
import karrel.com.btconnector.btscanner.BtScannerable;
import karrel.com.btconnector.btscanner.LeBtSanner;
import karrel.com.btconnector.chatmanager.BtChatListener;
import karrel.com.btconnector.chatmanager.BtChatManager;
import karrel.com.btconnector.permission.PermissionCheckable;
import karrel.com.btconnector.permission.PermissionChecker;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by Rell on 2018. 3. 23..
 * <p>
 * 블루투스 리스트 조회, 접속, 데이터 송/수신
 */

public class BtManager implements BtListener, BtManagerable {

    // 싱글턴
    private static BtManager mBtManager;

    // 퍼미션 체커
    private PermissionCheckable permissionChecker;

    // 블루투스 스캐너
    private BtScannerable btScanner;

    // 체크해야할 권한
    private String[] permissions = {
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_COARSE_LOCATION};

    // 블루투스 콜백
    private BtListener callback;

    // 채팅 매니저
    private BtChatManager chatManager = BtChatManager.getInstance();

    // 싱글턴
    public static BtManager getInstance(Context context, BtListener listener) {
        if (mBtManager == null) {
            mBtManager = new BtManager(context, listener);
        }
        return mBtManager;
    }

    // 생성자
    public BtManager(Context context, BtListener listener) {
        callback = listener;

        // 퍼미션 체커
        permissionChecker = new PermissionChecker(context);
        // 디바이스 스캐너
        btScanner = new LeBtSanner(btScannerListener);
        // 채팅 매니저 콜백
        chatManager.setListener(chatCallback);
    }

    // 권한이 거부됨
    @Override
    public void deniedPermission() {
        RLog.d();
        Observable.just("")
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> callback.deniedPermission(), e -> e.printStackTrace());
    }

    // 블루투스 활성화 요청
    @Override
    public void requireEnableBt() {
        RLog.d();
        Observable.just("")
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> callback.requireEnableBt(), e -> e.printStackTrace());

    }

    @Override
    public void onSearchedDevice(BluetoothDevice device) {
        Observable.just(device)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(callback::onSearchedDevice, e -> e.printStackTrace());
    }


    // 블루투스 스캔
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
                // 퍼미션 거부됨
                deniedPermission();
            }
        });
    }

    @Override
    public void connect(BluetoothDevice device) {
        // 스캔 중지
        btScanner.stopScan();
        // 블루투스 접속
        chatManager.connect(device);
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
            BtManager.this.requireEnableBt();
        }

        @Override
        public void onSearchedDevice(BluetoothDevice device) {
            BtManager.this.onSearchedDevice(device);
        }
    };

    // 채팅 매니저
    private final BtChatListener chatCallback = new BtChatListener() {
        @Override
        public void onConnected(String deviceName) {
            RLog.d();
        }

        @Override
        public void onConnecting(String deviceName) {
            RLog.d();
        }

        @Override
        public void onConnectFailed(String deviceName) {
            RLog.d();
        }

        @Override
        public void onMessageSend(byte[] writeBuf) {
            RLog.d();
        }

        @Override
        public void onReadMessage(byte[] readBuf) {
            RLog.d();
        }

        @Override
        public void onStartConnect(String deviceName) {
            RLog.d();
        }

        @Override
        public void onRetry(String deviceName, int cnt) {
            RLog.d();
        }
    };
}
