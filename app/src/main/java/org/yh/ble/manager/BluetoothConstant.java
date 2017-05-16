package org.yh.ble.manager;

import java.util.UUID;

/**
 * Created by yhlyl on 2017/5/16.
 * UUID 蓝牙特征值 根据不同蓝牙设备修改(https://github.com/android-coco/YhBle4.0)
 */

public final class BluetoothConstant
{
    public static UUID UUID_SERVICE = UUID
            .fromString("0000fff0-0000-1000-8000-00805f9b34fb"); // 服务UUID
    public static UUID UUID_CHARACTERISTIC = UUID
            .fromString("0000fff4-0000-1000-8000-00805f9b34fb"); // 蓝牙发数据特征UUID
    public static UUID UUID_RECEIVER_CHAR = UUID
            .fromString("0000fff1-0000-1000-8000-00805f9b34fb"); // 蓝牙收数据特征UUID
    public static UUID UUID_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    public static UUID UUID_HEART_RATE_MEASUREMENT = UUID
            .fromString("00002a37-0000-1000-8000-00805f9b34fb");
}
