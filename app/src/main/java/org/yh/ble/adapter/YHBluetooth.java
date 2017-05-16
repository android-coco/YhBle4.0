package org.yh.ble.adapter;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;

/**
 * Created by yhlyl on 2017/5/16.
 * 蓝牙管理器(https://github.com/android-coco/YhBle4.0)
 */

public abstract class YHBluetooth
{
    //用于停止扫描时间，默认时间15秒
    private int scanPeriod = 15000;
    protected Context context;
    //蓝牙适配器
    private BluetoothAdapter bluetoothAdapter;
    //蓝牙Manager
    private final BluetoothManager bluetoothManager;
    private Handler handler;
    //正在扫描
    private boolean scanning;

    public YHBluetooth(Context context)
    {
        this.context = context;
        bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        enableBluetooth();
        this.handler = new Handler();
    }

    /**
     * 启用蓝牙
     */
    public boolean enableBluetooth()
    {
        if (isSupportBluetooth())
        {
            return bluetoothAdapter.enable();
        } else
        {
            return false;
        }
    }

    /**
     * 禁用蓝牙
     */
    public boolean disableBluetooth()
    {
        if (isSupportBluetooth())
        {
            return bluetoothAdapter.disable();
        } else
        {
            return false;
        }
    }

    /**
     * 是否支持蓝牙
     *
     * @return
     */
    public boolean isSupportBluetooth()
    {
        return bluetoothAdapter != null;
    }

    /**
     * 获取蓝牙适配器
     *
     * @return
     */
    public BluetoothAdapter getBluetoothAdapter()
    {
        return bluetoothAdapter;
    }
    /**
     * 获取蓝牙管理器
     *
     * @return
     */
    public BluetoothManager getBluetoothManager()
    {
        return bluetoothManager;
    }
    /**
     * 是否启用了
     *
     * @return
     */
    public boolean isEnabled()
    {
        if (isSupportBluetooth())
        {
            return bluetoothAdapter.isEnabled();
        }
        return false;
    }

    /**
     * 检查是否支持BLE
     *
     * @return
     */
    public boolean isSupportLE()
    {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    public boolean isScanning()
    {
        return scanning;
    }

    public void setScanning(boolean scanning)
    {
        this.scanning = scanning;
    }

    public int getScanPeriod()
    {
        return scanPeriod;
    }

    public void setScanPeriod(int scanPeriod)
    {
        this.scanPeriod = scanPeriod;
    }

    public Handler getHandler()
    {
        return handler;
    }


}
