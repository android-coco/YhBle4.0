package org.yh.ble.adapter;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;

/**
 * Created by yhlyl on 2017/5/16.
 * (https://github.com/android-coco/YhBle4.0)
 * 支持API 18-20，从18开始Android支持低功耗BLE了，其实扫描的核心还是使用{@link BluetoothLeScanner}，
 * API 21以上请查看{@link YHBluetoothPeripheral}。
 */

public class YHBluetoothLeGatt extends YHBluetooth
{
    //蓝牙扫描回调
    private BluetoothAdapter.LeScanCallback leScanCallback;
    public YHBluetoothLeGatt(Context context, BluetoothAdapter.LeScanCallback leScanCallback)
    {
        super(context);
        this.leScanCallback = leScanCallback;
    }

    /**
     * 扫描设备
     */
    public void scanLeDevice()
    {
        if (isSupportBluetooth() && isEnabled())
        {
            setScanning(true);
            getBluetoothAdapter().startLeScan(leScanCallback);
            getHandler().postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    stopLeScan();
                }
            }, getScanPeriod());
        }
    }

    /**
     * 停止扫描
     */
    public void stopLeScan()
    {
        if (isSupportBluetooth() && isEnabled())
        {
            setScanning(false);
            getBluetoothAdapter().stopLeScan(leScanCallback);
        }
    }

}
