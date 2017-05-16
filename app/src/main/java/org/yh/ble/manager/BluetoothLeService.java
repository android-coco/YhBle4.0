package org.yh.ble.manager;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import java.util.List;

/**
 * Created by yhlyl on 2017/5/16.
 * 蓝牙服务(https://github.com/android-coco/YhBle4.0)
 */

public class BluetoothLeService extends Service
{
    private final static String TAG = BluetoothLeService.class.getSimpleName();
    //action
    public final static String ACTION_GATT_CONNECTION_CHANGED = "org.yh.ble" +
            "ACTION_GATT_CONNECTION_CHANGED";
    public final static String ACTION_GATT_CONNECTED = "org.yh.ble" +
            ".ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "org.yh.ble" +
            ".ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "org.yh.ble" +
            ".ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "org.yh.ble" +
            ".ACTION_DATA_AVAILABLE";
    //extra
    public final static String EXTRA_DATA = "org.yh.ble.EXTRA_DATA";
    public final static String EXTRA_DATA_DEVICE = "org.yh.ble" +
            ".EXTRA_DATA_DEVICE";
    public final static String EXTRA_DATA_NEW_STATE = "org.yh.ble" +
            ".EXTRA_DATA_NEW_STATE";
    public final static String EXTRA_DATA_STATUS = "org.yh.ble" +
            ".EXTRA_DATA_STATUS";
    //蓝牙管理
    private BluetoothManager mBluetoothManager;
    //蓝牙适配器
    private BluetoothAdapter mBluetoothAdapter;
    //蓝牙特征
    private BluetoothGatt mBluetoothGatt;
    private final IBinder mBinder = new BluetoothBinder();
    //蓝牙地址
    private String mAddress;

    public class BluetoothBinder extends Binder
    {
        public BluetoothLeService getService()
        {
            return BluetoothLeService.this;
        }
    }

    public BluetoothLeService()
    {
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        close();
        return super.onUnbind(intent);
    }

    /**
     * 初始化蓝牙
     *
     * @return
     */
    public boolean init()
    {
        if (mBluetoothManager == null)
        {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null)
            {
                return false;
            }
        }
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null)
        {
            return false;
        }
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
            Log.e(TAG, "onConnectionStateChange：" +
                    (Thread.currentThread() == Looper.getMainLooper().getThread()));
            Intent intent = new Intent();
            if (newState == BluetoothProfile.STATE_CONNECTED)
            {
                //连接的远程设备的RSSI。可以理解为信号
                gatt.readRemoteRssi();
                // 发现远程设备提供的服务及其特性和描述符
                gatt.discoverServices();
            }
            intent.setAction(ACTION_GATT_CONNECTION_CHANGED);
            intent.putExtra(EXTRA_DATA_NEW_STATE, newState);
            intent.putExtra(EXTRA_DATA_DEVICE, gatt.getDevice());
            sendBroadcast(intent);
        }

        /**
         * 当远程设备的远程服务列表，特征和描述符已被更新，即已发现新服务时，调用回调。表示可以与之通信了。
         * @param gatt
         * @param status
         */
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status)
        {
            Log.e(TAG, "onServicesDiscovered：" +
                    (Thread.currentThread() == Looper.getMainLooper().getThread()));
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

                Intent intent = new Intent(ACTION_GATT_SERVICES_DISCOVERED);
                intent.putExtra(EXTRA_DATA_STATUS, status);
                intent.putExtra(EXTRA_DATA_DEVICE, gatt.getDevice());
                sendBroadcast(intent);
            }
            else
            {
                Log.e(getClass().getSimpleName(), "onServicesDiscovered received: " + status);
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
            Log.e(TAG, "onCharacteristicChanged：" +
                    (Thread.currentThread() == Looper.getMainLooper().getThread()));
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }

    };

    /**
     * 发送数据广播
     *
     * @param action
     * @param characteristic
     */
    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic)
    {
        final Intent intent = new Intent(action);

        // This is special handling for the Heart Rate Measurement profile.  Data parsing is
        // carried out as per profile specifications:
        // http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer
        // .aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
        if (BluetoothConstant.UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid()))
        {
            int flag = characteristic.getProperties();
            int format = -1;
            if ((flag & 0x01) != 0)
            {
                format = BluetoothGattCharacteristic.FORMAT_UINT16;
                Log.e(TAG, "Heart rate format UINT16.");
            }
            else
            {
                format = BluetoothGattCharacteristic.FORMAT_UINT8;
                Log.e(getClass().getSimpleName(), "Heart rate format UINT8.");
            }
            final int heartRate = characteristic.getIntValue(format, 1);
            Log.e(TAG, String.format("Received heart rate: %d", heartRate));
            intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
        }
        else
        {
            // 对于所有其他配置文件,写在十六进制数据格式化
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0)
            {
                intent.putExtra(EXTRA_DATA, new String(data));
            }
        }
        sendBroadcast(intent);
    }

    /**
     * 连接设备，有重连的功能
     *
     * @param address 远程连接的MAC地址
     * @return true表示开始连接成功，但真正的连接状态则是通过{mGattCallback#onConnectionStateChange}
     */
    public boolean connect(String address)
    {
        if (mBluetoothAdapter == null || address == null)
        {
            return false;
        }
        //如果之前有连接过就直接连接，重新连接
        if (mAddress != null && address.equals(mAddress) && mBluetoothGatt != null)
        {
            return mBluetoothGatt.connect();
        }
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null)
        {
            return false;
        }
        //false表示直接连接，true表示远程设备可用之后连接
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        mAddress = address;
        return true;
    }

    /**
     * 断开
     */
    public void disconnect()
    {
        if (mBluetoothGatt != null && mBluetoothAdapter != null)
        {
            mBluetoothGatt.disconnect();
        }
    }

    /**
     * 关闭蓝牙
     */
    public void close()
    {
        if (mBluetoothGatt != null)
        {
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }
    }

    /**
     * 获取链接的设备
     *
     * @return
     */
    public List<BluetoothDevice> getConnectedDevices(int profile)
    {
        return mBluetoothManager.getConnectedDevices(profile);
    }
}
