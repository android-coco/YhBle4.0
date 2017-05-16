package org.yh.ble;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements I_ResultView<BluetoothDevice>
{
    private BluetoothService bluetoothUnity;
    private DeviceBean deviceBean;
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
        this.bluetoothUnity = new BluetoothService(this, this);
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
    public void successView(BluetoothDevice bean, int rssi)
    {

    }

    @Override
    public void disconnectedView(String error)
    {

    }

    @Override
    public void connectedView()
    {

    }

    @Override
    public void completeView()
    {

    }

    @Override
    public void valueView(String value)
    {

    }
}
