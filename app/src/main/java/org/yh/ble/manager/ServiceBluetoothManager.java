package org.yh.ble.manager;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;

/**
 * Created by yhlyl on 2017/5/16.
 * 服务模式 推荐(https://github.com/android-coco/YhBle4.0)
 */

public class ServiceBluetoothManager extends YHBluetoothManager
{
    private BluetoothLeService mBluetoothLeService;
    /**
     * 连接是否可用
     */
    private boolean available;

    private ServiceBluetoothManager(Context context, I_BluetoothCallback bluetoothCallback)
    {
        super(context, bluetoothCallback);
        this.createBluetooth();
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
            synchronized (BluetoothManager.class)
            {
                if (sBluetoothManager == null)
                {
                    sBluetoothManager = new ServiceBluetoothManager(context, bluetoothCallback);
                }
            }
        }
        return sBluetoothManager;
    }

    @Override
    public List<BluetoothDevice> getConnectedDevices(int profile)
    {
        if (mBluetoothLeService != null)
        {
            mBluetoothLeService.getConnectedDevices(profile);
        }
        return null;
    }

    @Override
    public void closeDevice()
    {
        if (mBluetoothLeService != null)
        {
            mBluetoothLeService.close();
        }
    }
    //启动服务,连接都又Service去管理
    private final ServiceConnection mServiceConnection = new ServiceConnection()
    {
        /**
         * 与服务建立连接
         *
         * @param name
         * @param service
         */
        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            mBluetoothLeService = ((BluetoothLeService.BluetoothBinder) service).getService();
            available = mBluetoothLeService.init();
        }

        /**
         * 断开连接
         *
         * @param name
         */
        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            mBluetoothLeService = null;
            available = false;
        }
    };

    /**
     * 是否可用
     *
     * @return
     */
    public boolean isAvailable()
    {
        return available;
    }

    /**
     * 监听蓝牙状态广播
     */
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, final Intent intent)
        {
            Log.e(TAG, "onCharacteristicChanged：" +
                    (Thread.currentThread() == Looper.getMainLooper().getThread()));
            final String action = intent.getAction();
            BluetoothDevice device = intent
                    .getParcelableExtra(BluetoothLeService.EXTRA_DATA_DEVICE);
            if (BluetoothLeService.ACTION_GATT_CONNECTION_CHANGED.equals(action))
            {
                int newState = intent.getIntExtra(BluetoothLeService.EXTRA_DATA_NEW_STATE, -1);
                bluetoothCallback.connectionStateChange(device, newState);
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action))
            {
                int status = intent.getIntExtra(BluetoothLeService.EXTRA_DATA_STATUS, -1);
                bluetoothCallback.serviceDiscoveryed(device, status);
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action))
            {
                bluetoothCallback
                        .valueChanged(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };

    /**
     * 连接设备，如果服务未开启或者地址为空的话就返回false；如果地址存在是否连接成功取决与蓝牙底层
     *
     * @param address
     * @return 是否连接到
     */
    @Override
    public boolean connectDevice(String address)
    {
        boolean connect;
        if (mBluetoothLeService == null || TextUtils.isEmpty(address))
        {
            connect = false;
        } else
        {
            stopScan();
            connect = mBluetoothLeService.connect(address);
        }
        return connect;
    }

    /**
     * 断开设备
     */
    @Override
    public void disconnectDevice()
    {
        if (mBluetoothLeService != null)
        {
            mBluetoothLeService.disconnect();
        }
    }

    /**
     * 创建服务
     */
    private void createBluetooth()
    {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTION_CHANGED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        context.registerReceiver(mGattUpdateReceiver, intentFilter);
        context.bindService(new Intent(context, BluetoothLeService.class), mServiceConnection,
                Context.BIND_AUTO_CREATE);
    }

    /**
     * 销毁服务
     */
    @Override
    public void destroyBluetooth()
    {
        context.unregisterReceiver(mGattUpdateReceiver);
        context.unbindService(mServiceConnection);
        sBluetoothManager = null;
    }
}
