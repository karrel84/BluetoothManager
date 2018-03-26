package karrel.com.bluetoothmanager.view;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import karrel.com.bluetoothmanager.R;
import karrel.com.bluetoothmanager.databinding.ActivityMainBinding;
import karrel.com.bluetoothmanager.presenter.MainPresenter;
import karrel.com.bluetoothmanager.presenter.MainPresenterImpl;

public class MainActivity extends AppCompatActivity implements MainPresenter.View {

    private ActivityMainBinding binding;
    private MainPresenter presenter;

    private static final int REQUEST_ENABLE_BT = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        presenter = new MainPresenterImpl(this, this);

        setupButtonEvents();
    }

    // 버튼 이벤트
    private void setupButtonEvents() {
        binding.search.setOnClickListener(v -> presenter.searchBluetoothDevices());
        binding.connect.setOnClickListener(v -> presenter.connectBluetooth());
        binding.disconnect.setOnClickListener(v -> presenter.disconnectBluetooth());
    }

    @Override
    public void requireEnableBt() {
        Intent enableBtIntent = new Intent(android.bluetooth.BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                presenter.enabledBluetooth();
            } else {
                presenter.dissableBluetooth();
            }
        }
    }
}
