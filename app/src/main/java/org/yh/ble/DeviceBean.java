package org.yh.ble;

import android.bluetooth.BluetoothDevice;

/**
 * Created by Xanthium on 2017/2/14.
 */

public class DeviceBean
{
    //设备信息
    private BluetoothDevice device;
    private boolean checked;
    //信号强度值
    private int rssi;

    public DeviceBean(BluetoothDevice device, int rssi)
    {
        this.device = device;
        this.rssi = rssi;
    }

    public BluetoothDevice getDevice()
    {
        return device;
    }

    public void setDevice(BluetoothDevice device)
    {
        this.device = device;
    }

    public boolean isChecked()
    {
        return checked;
    }

    public void setChecked(boolean checked)
    {
        this.checked = checked;
    }

    public int getRssi()
    {
        return rssi;
    }

    public void setRssi(int rssi)
    {
        this.rssi = rssi;
    }

}
