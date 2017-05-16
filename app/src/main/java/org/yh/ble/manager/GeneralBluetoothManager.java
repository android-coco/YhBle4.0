package org.yh.ble.manager;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Created by yhlyl on 2017/5/16.
 * 传统模式,不是用Service进行蓝牙操作(https://github.com/android-coco/YhBle4.0)
 */

public class GeneralBluetoothManager extends YHBluetoothManager
{
    //蓝牙设备mac地址
    private String mAddress;
    //蓝牙特征值
    private BluetoothGatt mBluetoothGatt;

    private GeneralBluetoothManager(Context context, I_BluetoothCallback bluetoothCallback)
    {
        super(context, bluetoothCallback);
    }

    /**
     * 获取BluetoothManager，防止多次创建
     *
     * @param context
     * @param bluetoothCallback
     * @return
     */
    public static YHBluetoothManager getYHManager(Context context, I_BluetoothCallback bluetoothCallback)
    {
        if (sBluetoothManager == null)
        {
            //线程安全
            synchronized (BluetoothManager.class)
            {
                if (sBluetoothManager == null)
                {
                    sBluetoothManager = new GeneralBluetoothManager(context, bluetoothCallback);
                }
            }
        }
        return sBluetoothManager;
    }


    /**
     * 连接设备，如果蓝牙服务未开启或者地址为空的话就返回false；如果地址存在是否连接成功取决与蓝牙底层
     *
     * @param address
     * @return 是否连接到
     */
    @Override
    public boolean connectDevice(String address)
    {
        if (arshowBluetooth.getBluetoothAdapter() == null || address == null)
        {
            return false;
        }
        //如果之前有连接过就直接连接，重新连接
        if (address.equals(mAddress) && mBluetoothGatt != null)
        {
            return mBluetoothGatt.connect();
        }
        BluetoothDevice device = arshowBluetooth.getBluetoothAdapter().getRemoteDevice(address);
        if (device == null)
        {
            return false;
        }
        //false表示直接连接，true表示远程设备可用之后连接
        mBluetoothGatt = device.connectGatt(context, false, mGattCallback);
        mAddress = address;
        return true;
    }

    /**
     * 连接、发现、通信回调
     */
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback()
    {
        /**
         * 连接状态改变回调
         * @param gatt
         * @param status
         * @param newState
         */
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
        {
            if (newState == BluetoothProfile.STATE_CONNECTED)
            {
                //阅读连接的远程设备的RSSI。
                gatt.readRemoteRssi();
                // 发现远程设备提供的服务及其特性和描述符
                gatt.discoverServices();
            }
            bluetoothCallback.connectionStateChange(gatt.getDevice(), newState);
        }

        /**
         * 当远程设备的远程服务列表，特征和描述符已被更新，即已发现新服务时，调用回调。表示可以与之通信了。
         * @param gatt
         * @param status
         */
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status)
        {
            if (status == BluetoothGatt.GATT_SUCCESS)
            {
                // 得到服务对象
                BluetoothGattService service = gatt.getService(BluetoothConstant.UUID_SERVICE);
                if (service == null)
                {
                    return;
                }

                // 得到此服务结点下Characteristic对象
                final BluetoothGattCharacteristic gattCharacteristic = service
                        .getCharacteristic(BluetoothConstant.UUID_CHARACTERISTIC);
                if (gattCharacteristic == null)
                {
                    return;
                }
                gatt.setCharacteristicNotification(gattCharacteristic, true);
                BluetoothGattDescriptor descriptor = gattCharacteristic
                        .getDescriptor(BluetoothConstant.UUID_DESCRIPTOR);
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                gatt.writeDescriptor(descriptor);

                bluetoothCallback.serviceDiscoveryed(gatt.getDevice(), status);
            } else
            {
                Log.e(TAG, "onServicesDiscovered received: " + status);
            }
        }

        /**
         * 由于远程特征通知而触发回调。
         * @param gatt
         * @param characteristic
         */
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic)
        {
            try
            {
                bluetoothCallback.valueChanged(new String(characteristic.getValue(), "UTF-8"));
            } catch (UnsupportedEncodingException e)
            {
                e.printStackTrace();
            }
        }

        /**
         * 返回远程设备的信号强度，最大值理论值为0
         * @param gatt
         * @param rssi
         * @param status
         */
        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status)
        {
            super.onReadRemoteRssi(gatt, rssi, status);
        }
    };


    /**
     * 断开设备
     */
    @Override
    public void disconnectDevice()
    {
        if (mBluetoothGatt != null)
        {
            mBluetoothGatt.disconnect();
        }
    }

    @Override
    public void closeDevice()
    {
        if (mBluetoothGatt != null)
        {
            mBluetoothGatt.close();
        }
    }

    /**
     * 销毁服务
     */
    @Override
    public void destroyBluetooth()
    {
        sBluetoothManager = null;
    }

    /**
     * 获取链接的设备
     *
     * @return
     */
    @Override
    public List<BluetoothDevice> getConnectedDevices(int profile)
    {
        return arshowBluetooth.getBluetoothManager().getConnectedDevices(profile);
    }
}
