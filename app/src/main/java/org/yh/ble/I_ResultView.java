package org.yh.ble;

import android.bluetooth.BluetoothGatt;

/**
 * Created by yhlyl on 2017/5/16.
 * View层回调接口(https://github.com/android-coco/YhBle4.0)
 */

public interface I_ResultView<T>
{
    //开始
    void startView(String msg);
    //扫描成功 bean设备信息  rssi信号 广播数据scanRecord
    void successView(T bean, int rssi,byte[] scanRecord);
    //断开连接 error内容
    void disconnectedView(String error);
    //连接成功
    void connectedView();
    //完成发现服务
    void completeView(BluetoothGatt gatt);
    //数据
    void valueView(YhModel value);
}
