package karrel.com.btconnector.btscanner;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * Created by jylee on 2018. 4. 4..
 */

public class DiscoveryScanner extends AbBluetoothScanner {
    private Context context;
    private BroadcastReceiver discoveryReceiver;

    public DiscoveryScanner(BluetoothScanListener listener, Context context) {
        super(listener);
        this.context = context;
    }

    @Override
    public void stopScanBluetoothDevice() {
        if (checkStartedScan()) return;
        unregistBluetoothReceiver();
        stopScan();
    }

    @Override
    protected void startScanBluetoothDevice() {
        if (checkDiscovering()) return;
        registBluetoothReceiver();
        startScan();
    }

    private boolean checkStartedScan() {
        if (discoveryReceiver == null) return true;
        return false;
    }

    private boolean checkDiscovering() {
        if (bluetoothAdapter.isDiscovering()) {
            return true;
        }
        return false;
    }

    private void stopScan() {
        bluetoothAdapter.cancelDiscovery();
    }

    private void unregistBluetoothReceiver() {
        try {
            context.unregisterReceiver(discoveryReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            discoveryReceiver = null;
        }
    }

    private void startScan() {
        bluetoothAdapter.startDiscovery();
    }

    private void registBluetoothReceiver() {
        discoveryReceiver = new DiscoveryReceiver();
        context.registerReceiver(discoveryReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
    }

    class DiscoveryReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                bluetoothScanListener.onSearchedDevice(device);
            }
        }
    }
}
