package org.yh.ble.manager;

import android.bluetooth.BluetoothDevice;

/**
 * Created by yhlyl on 2017/5/16
 * 与蓝牙相关的回调(https://github.com/android-coco/YhBle4.0)
 */

public interface I_BluetoothCallback<T>
{
    /**
     * 扫描成功
     *
     * @param bluetoothDevice
     */
    void scanSuccess(BluetoothDevice bluetoothDevice, int rssi,byte[] scanRecord);

    /**
     * 连接状态改变
     *
     * @param bluetoothDevice
     */
    void connectionStateChange(BluetoothDevice bluetoothDevice, int newState);

    /**
     * 发现服务
     */
    void serviceDiscoveryed(BluetoothDevice bluetoothDevice, int status);

    /**
     * 监听蓝牙通信值
     *
     * @param value
     */
    void valueChanged(byte[] value);
}
