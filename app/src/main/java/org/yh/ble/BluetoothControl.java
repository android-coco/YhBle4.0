package org.yh.ble;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import org.yh.ble.manager.I_BluetoothCallback;
import org.yh.ble.manager.ServiceBluetoothManager;
import org.yh.ble.manager.YHBluetoothManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.yh.ble.CMDUtils.byteToInt;

/**
 * Created by yhlyl on 2017/5/16.
 * 面向Unity蓝牙功能的类控制器(https://github.com/android-coco/YhBle4.0)
 * UI使用
 */

public class BluetoothControl implements I_BluetoothCallback
{

    public static final String TAG = BluetoothControl.class.getSimpleName();

    //不支持蓝牙
    public static final int UNSUPPORT_BLUETOOTH = -1;
    //不支持低功耗
    public static final int UNSUPPORT_BLE = -2;
    //没有启用蓝牙
    public static final int BLUETOOTH_ENABLED = -3;
    //扫描到设备
    public static final int BLUETOOTH_SCAN_DEVICE = 0;
    //连接成功
    public static final int BLUETOOTH_CONNECTED = 1;
    //断开连接
    public static final int BLUETOOTH_DISCONNECTED = 2;
    //通信值有改变
    public static final int BLUETOOTH_VALUE_CHANGED = 3;
    //重连
    public static final int BLUETOOTH_RECONNECT = 4;
    //配对的设备
    public static final int BLUETOOTH_BONDE_DEVICE = 5;

    //蓝牙设备名前缀，固定前缀
    private static String FILTER_NAME = "SHOWBABY";
    //android6.0权限判断
    private static final String[] PERMISSONS = {Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    //过滤重复蓝牙设备信息
    private Set<BluetoothDevice> devices = new HashSet<>();
    //蓝牙管理
    private YHBluetoothManager bluetoothManager;

    private String mAddress;
    private Map<String, byte[]> valueMap;
    //是否是唤醒状态，返回应用
    private boolean mStop;
    private Context context;

    I_ResultView<BluetoothDevice> r;

    /**
     * 回掉接口对象和Context
     *
     * @param r
     * @param context
     */
    public BluetoothControl(I_ResultView<BluetoothDevice> r, Context context)
    {
        this.r = r;
        this.context = context;
        createManager();
    }

    /**
     * 创建蓝牙管理器，并给Unity发送消息
     */
    private void createManager()
    {
        if (bluetoothManager == null)
        {
            bluetoothManager = ServiceBluetoothManager.getYHManager(context, this);
        }
    }


    /**
     * 扫描蓝牙
     */

    public void startScan()
    {
        devices.clear();
        //支持并启用了开始扫描
        if (!bluetoothManager.isSupportBluetooth())
        {
            Log.e(TAG, context.getString(R.string.bluetooth_unsupported));
        } else if (!bluetoothManager.enableBluetooth())
        {
            Log.e(TAG, context.getString(R.string.bluetooth_enable));
        } else if (!bluetoothManager.isSupportLE())
        {
            Log.e(TAG, context.getString(R.string.bluetooth_not_ble));
        } else
        {
            bluetoothManager.startScan();
        }
    }

    /**
     * 停止扫描
     */
    public void stopScan()
    {
        bluetoothManager.stopScan();
    }
    byte[] spoSn;// 血氧SN号
    //扫描成功
    @Override
    public void scanSuccess(BluetoothDevice device, int rssi, byte[] scanRecord)
    {
        //存在重复的设备
        if (devices.add(device))
        {
            r.successView(device, rssi, scanRecord);
        }
        //根据设备名称做相关事情
        if (null != device.getName())
        {
            if (device.getName().startsWith("FitSpo_"))
            {
                int len = scanRecord[7] - 1;
                spoSn = new byte[len];
                for (int i = 0; i < len; i++)
                {
                    spoSn[i] = scanRecord[9 + i];
                }
            }
        }
    }

    //连接状态回调
    @Override
    public void connectionStateChange(BluetoothDevice device, int newState)
    {
        if (newState == ServiceBluetoothManager.STATE_CONNECTED)
        {
            r.connectedView();
        } else if (newState == ServiceBluetoothManager.STATE_DISCONNECTED)
        {
            stopBluetooth();
            r.disconnectedView(context.getString(R.string.bluetooth_disconnect));
        }
    }

    /**
     * 发现服务
     *
     * @param bluetoothDevice
     * @param status
     */
    boolean isSendMsg = true;
    private BluetoothGatt gatt;
    @Override
    public void serviceDiscoveryed(BluetoothDevice bluetoothDevice, int status)
    {
        this.gatt = bluetoothManager.getBluetoothGatt();
        r.completeView(this.gatt);
        //开始写操作
        final byte[] cmd0 = CMDUtils.getCMD((byte) 0x00, (byte) 0x0E, CMDUtils.getTimeDatas());
        new Thread()
        {
            @Override
            public void run()
            {
                while (isSendMsg)
                {
                    try
                    {
                        sleep(500);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                    if (isSendMsg)
                    {
                        Log.e(TAG, "发现服务  " + CMDUtils.sendMessage(gatt, cmd0) + "");
                    }
                }
            }
        }.start();
    }


    /**
     * 数据更新
     *
     * @param value
     */
    @Override
    public void valueChanged(byte[] value)
    {

        if (valueMap == null)
        {
            valueMap = new HashMap<>();
        }
        //valueMap.put("key", value);
        //r.valueView(value);
        //进行拼包操作
        jointPakge(value);
    }

    byte[] data_recv = new byte[128];// 组包数组
    boolean recv_head = false;// 是否有头
    int last_recv_len = 0;// 没有包头的包长度
    int pack_len;// 包长度
    private void jointPakge(byte[] data)
    {
        int recv_len = data.length;
        // recv header packet
        if (data[0] == 0x7f)
        {
            parse_header(data, recv_len);
        } else
        {
            if (!recv_head)
            {
                // LogUtils.e("没有包头：", Arrays.toString(data));
                return;
            }
            int i;
            for (i = 0; i < recv_len; i++)
            {
                data_recv[last_recv_len] = data[i];
                ++last_recv_len;
                if (data[i] == (byte) 0x8f)
                {
                    // LogUtils.e("组合包-完成：", Arrays.toString(data_recv));
                    // cat a full packet
                    recv_head = false;
                    last_recv_len = 0;
                    valueMap.put("key", data_recv);
                    receiveSpoData(data_recv);
                    break;
                }
            }
            if (i < data.length && data[i] == 0x7f)
            {
                byte[] even = new byte[data.length - i];
                for (int j = i; j < data.length; j++)
                {
                    even[j] = data[j];
                }
                parse_header(even, even.length);
            }
        }
    }

    private void parse_header(byte[] data, int recv_len)
    {
        if (recv_len >= 3)
        {
            // 兼容高低位
            if (data[1] > 0)
            {
                pack_len = byteToInt(data[2], data[1]);
            } else
            {
                pack_len = byteToInt(data[1], data[2]);
            }
            // LogUtils.e("xxxx:", pack_len + "   " + recv_len);
            // 完整的包
            if (pack_len == recv_len)
            {
                recv_head = false;
                // LogUtils.e("完整的包：", Arrays.toString(data));
                //根据设备名称做相关事情目前只有血氧
                valueMap.put("key", data);
                receiveSpoData(data);
            } else
            {
                recv_head = true;
                last_recv_len = recv_len;
                for (int i = 0; i < last_recv_len; i++)
                {
                    data_recv[i] = data[i];
                }
                // LogUtils.e("部分包：", Arrays.toString(data));
                // LogUtils.e("部分包-赋值：", Arrays.toString(data_recv));
            }

        } else
        {
            recv_head = true;
            last_recv_len = recv_len;
            for (int i = 0; i < last_recv_len; i++)
            {
                data_recv[i] = data[i];
            }
            // LogUtils.e("部分包：", Arrays.toString(data));
            // LogUtils.e("部分包-赋值：", Arrays.toString(data_recv));
        }

    }

    /**
     * 销毁
     */

    public void destroyBluetooth()
    {
        bluetoothManager.destroyBluetooth();
    }

    /**
     * 唤醒
     */
    public void restartBluetooth()
    {
        if (mStop && !TextUtils.isEmpty(mAddress))
        {
            r.startView("开始连接");
            //是否连接成功
            if (!bluetoothManager.connectDevice(mAddress))
            {
                bluetoothManager.startScan();
                r.disconnectedView("连接失败");
            }
            mStop = false;
        }
    }

    /**
     * 暂停
     */
    public void pauseBluetooth()
    {
        mStop = true;
        bluetoothManager.pauseBluetooth();
    }

    /**
     * 连接断开
     */
    public void disconnectDevice()
    {
        bluetoothManager.disconnectDevice();
        bluetoothManager.closeDevice();
    }

    /**
     * 发送蓝牙MAC地址进行通信
     *
     * @param address
     */
    public void connectDevice(String address)
    {
        mAddress = address;
        boolean connectDevice = bluetoothManager.connectDevice(address);
        Log.e(TAG, "connectDevice:" + connectDevice);
    }

    /**
     * 启用蓝牙
     *
     * @return
     */
    public boolean enableBluetooth()
    {
        return bluetoothManager.enableBluetooth();
    }

    /**
     * 停止一切蓝牙活动
     */
    private void stopBluetooth()
    {
        pauseBluetooth();
        disconnectDevice();
    }

    /**
     * 血氧
     *
     * @param datas
     */
    private void receiveSpoData(byte[] datas)
    {
        int pnum = byteToInt(datas[4], datas[5]);
        // int cmd_len = byteToInt(datas[1], datas[2]);
        int battery = datas[6];
        int cmmmand = datas[7];
        // LogUtils.e("血氧数据：", Arrays.toString(datas));
        FitSpoDataBean bleDataBean = new FitSpoDataBean();
        switch (cmmmand)
        {
            case 0x00:// 命令0
                isSendMsg =false;
                // 包头信息得到
                bleDataBean.setpCode(pnum + "");
                bleDataBean.setBatt(battery + "");
                bleDataBean.setCmd(cmmmand + "");
                int year = datas[8];
                int month = datas[9];
                // 包体得到
                bleDataBean.setDataTime(year + "年" + month + "月");
                // int sn_len = cmd_len - 12;
                // byte[] sn = new byte[sn_len];
                // if(datas.length >= 10 + sn_len)
                // {
                // for (int i = 0; i < sn_len; i++)
                // {
                // sn[i] = datas[10 + i];
                // }
                // bleDataBean.setSn(new String(sn));
                // }
                if (null != spoSn)
                {
                    bleDataBean.setSn(new String(spoSn));
                }
                break;
            case 0x01:
                isSendMsg = false;
                // 包头信息得到
                bleDataBean.setpCode(pnum + "");
                bleDataBean.setBatt(battery + "");
                bleDataBean.setCmd(cmmmand + "");
                // [127, 0, 8, 1, 0, 1, 24, -113]
                // [127, 0, 8, 1, 0, 1, -23, -113]
                CMDUtils.sendMessage(gatt,
                        CMDUtils.getCMD((byte) 0x01, (byte) 0x08, null));// 发送01命令
                break;
            case 0x02:
                isSendMsg = false;
                // 包头信息得到
                bleDataBean.setpCode(pnum + "");
                bleDataBean.setBatt(battery + "");
                bleDataBean.setCmd(cmmmand + "");
                // 包体得到
                bleDataBean.setSop(datas[8] + "");// %
                bleDataBean.setBpm(byteToInt(datas[9], datas[10]) + "");// bpm
                bleDataBean.setBlood_per(datas[11] + "");// %
                switch (datas[12])
                {
                    case 0:
                        bleDataBean.setHypoxaemic("成人模式");
                        break;
                    case 1:
                        bleDataBean.setHypoxaemic("新生儿模式");
                        break;
                    case 2:
                        bleDataBean.setHypoxaemic("动物模式");
                        break;
                    default:
                        break;
                }
                // [127, 0, 8, 1, 0, 2, 11, -113]
                CMDUtils.sendMessage(gatt,
                        CMDUtils.getCMD((byte) 0x02, (byte) 0x08, null));// 发送02命令
                break;
            case 0x03:
                isSendMsg = false;
                // 包头信息得到
                bleDataBean.setpCode(pnum + "");
                bleDataBean.setBatt(battery + "");
                bleDataBean.setCmd(cmmmand + "");
                // 包体得到
                bleDataBean.setSop(datas[8] + "");// %
                bleDataBean.setBpm(byteToInt(datas[9], datas[10]) + "");// bpm
                bleDataBean.setBlood_per(datas[11] + "");// %
                switch (datas[12])
                {
                    case 0:
                        bleDataBean.setHypoxaemic("成人模式");
                        break;
                    case 1:
                        bleDataBean.setHypoxaemic("新生儿模式");
                        break;
                    case 2:
                        bleDataBean.setHypoxaemic("动物模式");
                        break;
                    default:
                        break;
                }
                // [127, 0, 8, 1, 0, 3, 85, -113]
                CMDUtils.sendMessage(gatt,
                        CMDUtils.getCMD((byte) 0x03, (byte) 0x08, null));// 发送03命令
                break;
            case 0x04:// 命令04
                isSendMsg = false;
                // 包头信息得到
                bleDataBean.setpCode(pnum + "");
                bleDataBean.setBatt(battery + "");
                bleDataBean.setCmd(cmmmand + "");
                // [127, 0, 8, 1, 0, 4, -42, -113]
                CMDUtils.sendMessage(gatt,
                        CMDUtils.getCMD((byte) 0x04, (byte) 0x08, null));// 发送04命令
                break;
        }
        r.valueView(bleDataBean);//数据返回界面
    }
}
