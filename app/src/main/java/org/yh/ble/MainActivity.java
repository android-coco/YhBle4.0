package org.yh.ble;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class MainActivity extends AppCompatActivity implements I_ResultView<BluetoothDevice>
{
    private static final String TAG = MainActivity.class.getSimpleName();
    private BluetoothControl bluetoothUnity;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        scan();
    }

    /**
     * 初始化
     */
    private void init()
    {
        this.bluetoothUnity = new BluetoothControl(this, this);
    }

    /**
     * 开始扫描
     */
    public void scan()
    {
        bluetoothUnity.startScan();
    }

    @Override
    public void startView(String msg)
    {
    }


    @Override
    public void successView(BluetoothDevice bean, int rssi, byte[] scanRecord)
    {
        bluetoothUnity.connectDevice(bean.getAddress());
    }

    @Override
    public void disconnectedView(String error)
    {
        Log.e(TAG, "disconnectedView:" + error);
    }

    @Override
    public void connectedView()
    {
        Log.e(TAG, "connectedView");
    }

    @Override
    public void completeView(final BluetoothGatt gatt)
    {
        //可发送数据了
    }

    @Override
    public void valueView(YhModel value)
    {
        Log.e(TAG,  "数据:" + value);
    }


    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        bluetoothUnity.destroyBluetooth();
    }
}
