package org.yh.ble.manager;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Build;

import org.yh.ble.adapter.YHBluetooth;
import org.yh.ble.adapter.YHBluetoothLeGatt;
import org.yh.ble.adapter.YHBluetoothPeripheral;

import java.util.List;
import java.util.Set;

/**
 * Created by yhlyl on 2017/5/16.
 * 自定义蓝牙 抽象类Manager(https://github.com/android-coco/YhBle4.0)
 */

public abstract class YHBluetoothManager
{
    protected final String TAG = this.getClass().getSimpleName();
    protected static YHBluetoothManager sBluetoothManager;

    //蓝牙适配器，Android 17版本就不支持了
    protected YHBluetooth arshowBluetooth;
    protected YHBluetoothLeGatt gatt;
    protected YHBluetoothPeripheral peripheral;

    protected I_BluetoothCallback bluetoothCallback;
    protected Context context;
    //连接状态断开
    public static final int STATE_DISCONNECTED = 0;
    //连接状态已经连接
    public static final int STATE_CONNECTED = 2;

    protected YHBluetoothManager(Context context, I_BluetoothCallback bluetoothCallback)
    {
        this.context = context;
        this.bluetoothCallback = bluetoothCallback;
        startBluetooth();
    }

    public abstract void destroyBluetooth();

    /**
     * 停止广播
     */
    public final void pauseBluetooth()
    {
        stopScan();
    }

    public abstract void disconnectDevice();

    public abstract boolean connectDevice(String address);

    /**
     * 是否开启了蓝牙
     *
     * @return
     */
    public final boolean isEnabled()
    {
        return arshowBluetooth.isEnabled();
    }

    /**
     * 获取连接的设备
     *
     * @return
     */
    public abstract List<BluetoothDevice> getConnectedDevices(int profile);

    /**
     * 创建蓝牙适配器
     */
    private final void startBluetooth()
    {
        //根据系统版本创建初始化蓝牙适配器
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 &&
                Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT_WATCH)
        {
            gatt();
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            peripheral();
        }
    }

    /**
     * 扫描蓝牙。如果需要停止扫描就停止扫描，否则只有等上一次的扫描结束了才会下一次扫描
     */
    public final void startScan()
    {
        if (arshowBluetooth == null)
        {
            return;
        }
        //API 18-20的低功耗蓝牙，而API 21以上貌似还不一样，需要研究
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 &&
                Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT_WATCH)
        {
            gatt.scanLeDevice();
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            peripheral.startScan();
        }
    }

    /**
     * 停止扫描
     */
    public final void stopScan()
    {
        if (arshowBluetooth == null)
        {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 &&
                Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT_WATCH)
        {
            gatt.stopLeScan();
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            peripheral.stopScanning();
        }
    }

    /**
     * API 18-20版本的蓝牙适配器
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void gatt()
    {
        gatt = new YHBluetoothLeGatt(context, new BluetoothAdapter.LeScanCallback()
        {
            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord)
            {
                addDevice(device, rssi);
            }
        });
        arshowBluetooth = gatt;
    }

    /**
     * API 21版本的蓝牙适配器
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void peripheral()
    {
        peripheral = new YHBluetoothPeripheral(context, new ScanCallback()
        {
            @Override
            public void onScanResult(int callbackType, ScanResult result)
            {
                super.onScanResult(callbackType, result);
                addDevice(result.getDevice(), result.getRssi());
            }

            @Override
            public void onScanFailed(int errorCode)
            {
                super.onScanFailed(errorCode);
            }
        });
        arshowBluetooth = peripheral;
    }

    /**
     * 添加扫描的设备
     *
     * @param device
     */
    private void addDevice(BluetoothDevice device, int rssi)
    {
        bluetoothCallback.scanSuccess(device, rssi);
    }

    public final boolean isSupportBluetooth()
    {
        return arshowBluetooth.isSupportBluetooth();
    }

    /**
     * 启用蓝牙
     *
     * @return
     */
    public final boolean enableBluetooth()
    {
        return arshowBluetooth.enableBluetooth();
    }

    /**
     * 是否支持低功耗
     *
     * @return 返回布尔值
     */
    public final boolean isSupportLE()
    {
        if (arshowBluetooth != null)
        {
            return arshowBluetooth.isSupportLE();
        }
        return false;
    }

    /**
     * 是否在扫描
     *
     * @return
     */
    public final boolean isScanning()
    {
        return arshowBluetooth.isScanning();
    }

    /**
     * *********************************************************
     *
     * 下面是连接相关的
     *
     * ********************************************************
     */

    /**
     * 获取配对的设备
     *
     * @return
     */
    public final Set<BluetoothDevice> getBondedDevices()
    {
        return arshowBluetooth.getBluetoothAdapter().getBondedDevices();
    }

    /**
     * 关闭设备。这个很关键特别时在部分手机如果不关闭的话，再多次连接设备的时候会无限断开。
     */
    public abstract void closeDevice();
}
