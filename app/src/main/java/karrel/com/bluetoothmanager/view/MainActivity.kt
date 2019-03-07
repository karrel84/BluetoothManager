package karrel.com.bluetoothmanager.view

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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

    private fun setupRecyclerView() {
        recyclerView.adapter = adapter
    }

    // 버튼 이벤트
    private fun setupButtonEvents() {
        search.setOnClickListener { presenter.searchBluetoothDevices() }
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
                presenter.enabledBluetooth()
            } else {
                presenter.dissableBluetooth()
            }
        }
    }

    companion object {

        private const val REQUEST_ENABLE_BT = 1000
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
                itemView.setOnClickListener { presenter.connectBluetooth(data) }
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
