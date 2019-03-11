package karrel.com.bluetoothmanager.view

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import karrel.com.bluetoothmanager.R
import karrel.com.bluetoothmanager.presenter.MainPresenter
import karrel.com.bluetoothmanager.presenter.MainPresenterImpl
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_bluetooth.view.*

class MainActivity : AppCompatActivity(), MainPresenter.View {

    private lateinit var presenter: MainPresenter
    private val arrayBluetoothInfo = ArrayList<BluetoothDevice>()
    private val adapter = RecyclerViewAdapter()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        presenter = MainPresenterImpl(this, this)
        setupRecyclerView()
        setupButtonEvents()
    }

    override fun onResume() {
        super.onResume()
        presenter.onResume()
    }

    override fun onStop() {
        super.onStop()
        presenter.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
    }

    private fun setupRecyclerView() {
        recyclerView.adapter = adapter
    }

    // 버튼 이벤트
    private fun setupButtonEvents() {
        search.setOnClickListener {

            var grantedPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
            grantedPermission = grantedPermission && ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED
            grantedPermission = grantedPermission && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

            if (grantedPermission) {
                presenter.searchBluetoothDevices()
            } else {
                // request permission
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_COARSE_LOCATION), BLUETOOTH_PERMISSION_REQUEST)
            }

        }
        disconnect.setOnClickListener { presenter.disconnectBluetooth() }
    }

    override fun requireEnableBt() {
        val enableBtIntent = Intent(android.bluetooth.BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
    }

    override fun clearLog() {
        arrayBluetoothInfo.clear()
    }

    override fun addSearchedDevice(device: BluetoothDevice?) {
        device?.let {
            arrayBluetoothInfo.add(it)
            adapter.notifyDataSetChanged()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                enabledBluetooth()
            } else {
                presenter.dissableBluetooth()
            }
        }
    }

    private fun enabledBluetooth() {

        var grantedPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
        grantedPermission = grantedPermission && ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED
        grantedPermission = grantedPermission && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (grantedPermission) {
            presenter.enabledBluetooth()
        } else {
            // request permission
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_COARSE_LOCATION), BLUETOOTH_PERMISSION_REQUEST)
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        if (requestCode == BLUETOOTH_PERMISSION_REQUEST) {
            var isAllGrant = true
            for (result in grantResults) {
                isAllGrant = isAllGrant and (result == PackageManager.PERMISSION_GRANTED)
            }
            if (isAllGrant) {
                enabledBluetooth()
            } else {
                Toast.makeText(this, "블루투스 권한이 승인되지 않았습니다.", Toast.LENGTH_SHORT).show()
            }
        }



        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    companion object {

        private const val REQUEST_ENABLE_BT = 1000
        private const val BLUETOOTH_PERMISSION_REQUEST = 1004
    }


    inner class RecyclerViewAdapter : RecyclerView.Adapter<RecyclerViewAdapter.BluetoothViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, position: Int): BluetoothViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bluetooth, parent, false)
            return BluetoothViewHolder(view)
        }

        override fun getItemCount(): Int = arrayBluetoothInfo.size

        override fun onBindViewHolder(viewholder: BluetoothViewHolder, position: Int) {
            viewholder.data = arrayBluetoothInfo[position]
        }


        inner class BluetoothViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val textName: TextView = itemView.textName
            private val textAddress: TextView = itemView.textAddress
            private val textState: TextView = itemView.textState

            var data: BluetoothDevice? = null
                set(value) {
                    field = value
                    updateUI(value)
                }

            init {
                itemView.setOnClickListener { data?.let { it1 -> presenter.connectBluetooth(it1) } }
            }

            private fun updateUI(device: BluetoothDevice?) {
                textName.text = device?.name
                textAddress.text = device?.address
                textState.text = device?.bondState.toString()
                val text = "device : ${device?.name}, address : ${device?.address}, bondState : ${device?.bondState}"

                println("onSearchedDevice : $text")
            }

        }
    }
}
