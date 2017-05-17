/**
 * @Project Name:BLERW
 * @File Name:CMDUtil.java
 * @Package Name:youten.redo.ble.util
 * @Date:2015-6-23上午10:23:27
 * @Copyright (c) 2015, yh_android@163.com All Rights Reserved.
 */

package org.yh.ble;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import org.yh.ble.manager.BluetoothConstant;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * @author yh
 * @ClassName: CMDUtils
 * @Description: 蓝牙命令根据自己的蓝牙协议定义
 * @date 2015-8-25 下午2:03:46
 */

public class CMDUtils
{
    protected static CMDUtils cmdUtils;
    // Bioland_BGM
    public static String DEVICE_NAME = "FitXxx_";
    // 现在是多个设备
    public static ArrayList<String> DEVICE_MAC = new ArrayList<String>();
    // 血糖仪
    public static String GATT_SERVICE_PRIMARY = "00001000-0000-1000-8000-00805f9b34fb";
    public static String CHARACTERISTIC_WRITEABLE = "00001001-0000-1000-8000-00805f9b34fb";
    public static String CHARACTERISTIC_NOTIFY = "00001002-0000-1000-8000-00805f9b34fb";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    public static String CHARACTERISTIC_READABLE = "00001003-0000-1000-8000-00805f9b34fb";
    public static String _FITSCA = "FitSca_";// 人体称
    public static String _FITSCS = "FitScs_";// 人体称
    public static String _FITPRE = "FitPre_";// 血压FitPre_
    public static String _FITPREK = "AES-XY";// 血压康中
    public static String _FITGLU = "FitGlu_";// 血糖
    public static String _FITBLD = "FitBld_";// 三合一
    public static String _FITTHM = "FitThm_";// 耳温
    public static String _FITSPO = "FitSpo_";// 血氧
    public static String _FITSCF = "FitScf_";// 食物秤
    public static String _FITPLU = "FitPlu_";// 心率
    public static String _FITFAT = "FitFat_";// 血脂
    public static String _FITUA = "FitUa_";// 尿酸
    public static String _ECG = "EL-";   //xin dian
    public static String _FIGPRS_GLU = "Fitgprs_xt_";// GPRS血糖
    public static String _FIGPRS_PRE = "Fitgprs_xy_";// GPRS血压


    public static CMDUtils getCMDUtils()
    {
        if (cmdUtils == null)
        {
            cmdUtils = new CMDUtils();
        }
        return cmdUtils;
    }

    /**
     * @param @param  name
     * @param @return
     * @return int 返回类型
     * @throws
     * @Title: getType
     * @Description: 获取设备类型
     * @author yh 作者
     */
    // 设备类型type：血压1，血糖2，耳温3，血氧4，人体称5，心率6 ，血脂7，尿酸8,食物秤9,手表10,视频11,血液三合一12，默认0
    public static int getType(String name)
    {

        if (name.startsWith(_FITPRE) || name.startsWith(_FITPREK)
                || name.startsWith(_FIGPRS_PRE))// 血压
        {
            return 1;
        } else if (name.startsWith(_FITGLU) || name.startsWith(_FIGPRS_GLU))// 血糖
        {
            // FitGlu_
            return 2;
        } else if (name.startsWith(_FITTHM))// 耳温
        {
            return 3;
        }
        // else if(name.startsWith(_FITSPO))// 血氧
        // {
        // return 4;
        // }
        else if (name.startsWith(_FITSCA) || name.startsWith(_FITSCS))// 人体称
        {
            return 5;
        } else if (name.startsWith(_FITPLU))// 心率仪
        {
            return 6;
        } else if (name.startsWith(_FITFAT))// 血脂
        {
            return 7;
        } else if (name.startsWith(_FITUA))// 尿酸
        {
            return 8;
        } else if (name.startsWith(_FITSCF))// 食物秤
        {
            return 9;
        } else if (name.startsWith(_FITBLD))// 食物秤
        {
            return 12;
        }
        return 0;
    }

    public static byte[] getCMD(byte cmd, byte len, byte[] datas)
    {
        byte[] x = new byte[len];
        // ----头
        x[0] = 0x7f;
        x[1] = (byte) (len / 256);
        x[2] = (byte) (len % 256);
        x[3] = 0x01;
        x[4] = 0x00;
        x[5] = cmd;
        int nall = 0x7f + x[1] + x[2] + x[3] + x[4] + cmd;
        if (null != datas)
        {
            // ---体
            for (int i = 0; i < datas.length; i++)
            {
                x[6 + i] = datas[i];
                nall += datas[i];
            }
        }
        nall += 0x8f;
        // --校验和
        x[len - 2] = (byte) (nall % 256);
        // --尾部
        x[len - 1] = (byte) 0x8f;
        return x;
    }

    /**
     * 日期时间
     *
     * @return
     */
    @SuppressLint("SimpleDateFormat")
    public static byte[] getTimeDatas()
    {
        SimpleDateFormat df = new SimpleDateFormat("yy-MM-dd-HH-mm-ss");
        String time = df.format(new Date());
        String[] ts = time.split("-");
        String times = "";
        for (int i = 0; i < ts.length; i++)
        {
            String temp = Integer.toHexString(Integer.parseInt(ts[i]));
            if (temp.length() < 2)
            {
                temp = "0" + temp;
            }
            times += temp;
        }
        return string2bytes(times);
    }

    /**
     * 体重称,命令
     *
     * @param higth
     * @param age
     * @param sex
     * @return
     */
    public static byte[] setFitScaData(int higth, int age, int sex)
    {
        byte[] times = getTimeDatas();
        byte[] temps = new byte[times.length + 3];
        for (int i = 0; i < times.length; i++)
        {
            temps[i] = times[i];
        }
        temps[times.length] = (byte) higth;
        temps[times.length + 1] = (byte) age;
        temps[temps.length - 1] = (byte) sex;
        return temps;
    }

    /**
     * 把字符串去空格后转换成byte数组。如"37   5a"转成[0x37][0x5A]
     *
     * @param s
     * @return
     */
    private static byte[] string2bytes(String s)
    {
        String ss = s.replace(" ", "");
        int string_len = ss.length();
        int len = string_len / 2;
        if (string_len % 2 == 1)
        {
            ss = "0" + ss;
            string_len++;
            len++;
        }
        byte[] a = new byte[len];
        for (int i = 0; i < len; i++)
        {
            a[i] = (byte) Integer.parseInt(ss.substring(2 * i, 2 * i + 2), 16);
        }
        return a;
    }

    /**
     * @param src 16进制字符串
     * @return 字节数组
     * @throws
     * @Title:hexString2Bytes
     * @Description:16进制字符串转字节数组
     */
    public static byte[] hexString2Bytes(String src)
    {
        int l = src.length() / 2;
        byte[] ret = new byte[l];
        for (int i = 0; i < l; i++)
        {
            ret[i] = (byte) Integer
                    .valueOf(src.substring(i * 2, i * 2 + 2), 16).byteValue();
        }
        return ret;
    }

    public static byte[] endCMD()
    {
        byte[] x = new byte[10];
        x[0] = 0x7F;
        x[1] = 0x0A;// 长度 Len L
        x[2] = 0x00;// 长度 Len H
        x[3] = 0x01;// 版本号 XVer
        x[4] = 0x66;// 产品型号代码 TNo L
        x[5] = 0x00;// 产品型号代码 TNo H
        x[6] = 0x64;// 设备电量，0-100,0没电，100满电 Batt
        x[7] = 0x04;// CMD
        x[8] = (byte) (x[0] + x[1] + x[2] + x[3] + x[4] + x[5] + x[6] + x[7] + 0x8F);// CHS
        x[9] = (byte) 0x8F;// 报文尾
        return x;
    }

    /**
     * @param @param         gatt
     * @param @param         message
     * @param @return参数说明<br \>
     * @return boolean 返回类型 是否写成功<br \>
     * @throws
     * @Title: sendMessage<br \>
     * @Description: 发命令给终端</br>
     */
    @SuppressLint("NewApi")
    public static boolean sendMessage(BluetoothGatt gatt, byte[] message)
    {
        if (gatt == null || message == null || message.length == 0)
        {
            return false;
        }
        // 主服务
        BluetoothGattService service = gatt.getService(BluetoothConstant.UUID_SERVICE);
        if (service == null)
        {
            return false;
        }
        // 写服务
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(BluetoothConstant.UUID_CHARACTERISTIC);
        if (characteristic == null)
        {
            return false;
        }
        // byte[] data = encodeUtil.hexToBytes(message);
        // byte[] sendData = encodeUtil.encodeMessage(data);
        // data
        characteristic.setValue(message);
        // characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        return gatt.writeCharacteristic(characteristic);
    }

    /**
     * @param @param type
     * @return void 返回类型
     * @throws
     * @Title: initUUID
     * @Description: UUID初始化
     * @author yh
     */
    // 设备类型type：血压1，血糖2，耳温3，血氧4，人体称5，心率6 ，血脂7，尿酸8,食物秤9，默认0
    public static void initUUID(int type)
    {
        switch (type)
        {
            case 1:
                // FitPre_
                DEVICE_NAME = "FitPre_";// 血压
                GATT_SERVICE_PRIMARY = "00001000-0000-1000-8000-00805f9b34fb";
                CHARACTERISTIC_NOTIFY = "00001002-0000-1000-8000-00805f9b34fb";
                CHARACTERISTIC_READABLE = "00001003-0000-1000-8000-00805f9b34fb";
                CHARACTERISTIC_WRITEABLE = "00001001-0000-1000-8000-00805f9b34fb";
                break;
            case 2:
                DEVICE_NAME = "FitGlu_";// 血糖
                GATT_SERVICE_PRIMARY = "00001000-0000-1000-8000-00805f9b34fb";
                CHARACTERISTIC_NOTIFY = "00001002-0000-1000-8000-00805f9b34fb";
                CHARACTERISTIC_READABLE = "00001003-0000-1000-8000-00805f9b34fb";
                CHARACTERISTIC_WRITEABLE = "00001001-0000-1000-8000-00805f9b34fb";
                break;

            case 3:
                DEVICE_NAME = "FitThm_";// 耳温
                GATT_SERVICE_PRIMARY = "0000fc00-0000-1000-8000-00805f9b34fb";
                CHARACTERISTIC_NOTIFY = "0000fca1-0000-1000-8000-00805f9b34fb";
                CHARACTERISTIC_READABLE = "0000fc03-0000-1000-8000-00805f9b34fb";
                CHARACTERISTIC_WRITEABLE = "0000fca0-0000-1000-8000-00805f9b34fb";
                break;
            // case 4:
            // DEVICE_NAME = "FitSpo_";// 血氧
            // GATT_SERVICE_PRIMARY = "0000ffb0-0000-1000-8000-00805f9b34fb";
            // CHARACTERISTIC_NOTIFY = "0000ffb2-0000-1000-8000-00805f9b34fb";
            // CHARACTERISTIC_READABLE = "0000ffb3-0000-1000-8000-00805f9b34fb";
            // CHARACTERISTIC_WRITEABLE =
            // "0000ffb2-0000-1000-8000-00805f9b34fb";
            // break;
            case 5:
                // 香山
                DEVICE_NAME = "FitSca_";// 体重
                GATT_SERVICE_PRIMARY = "0000ffb0-0000-1000-8000-00805f9b34fb";
                CHARACTERISTIC_NOTIFY = "0000ffb2-0000-1000-8000-00805f9b34fb";
                CHARACTERISTIC_READABLE = "0000ffb2-0000-1000-8000-00805f9b34fb";
                CHARACTERISTIC_WRITEABLE = "0000ffb2-0000-1000-8000-00805f9b34fb";
                break;
            case 9:
                // 香山
                DEVICE_NAME = "FitScf_";// 食物
                GATT_SERVICE_PRIMARY = "0000ffb0-0000-1000-8000-00805f9b34fb";
                CHARACTERISTIC_NOTIFY = "0000ffb2-0000-1000-8000-00805f9b34fb";
                CHARACTERISTIC_READABLE = "0000ffb2-0000-1000-8000-00805f9b34fb";
                CHARACTERISTIC_WRITEABLE = "0000ffb2-0000-1000-8000-00805f9b34fb";
                break;
        }
    }

    /**
     * @param @param  n
     * @param @return
     * @return byte[] 返回类型
     * @throws
     * @Title: int2byte
     * @Description: int转byte
     * @author Administrator 作者
     */
    public static byte[] int2byte(int n)
    {
        short low_short = (short) (n % 65536);// 低位
        short hig_short = (short) (n / 65536);// 高位

        byte low_low_short = (byte) (low_short % 256);// 低位 数据的低位
        byte hig_low_short = (byte) (low_short / 256);// 高位 数据的高位

        byte low_hig_short = (byte) (hig_short % 256);//
        byte hig_hig_short = (byte) (hig_short / 256);

        byte int_str[] = new byte[4];

        int_str[0] = low_low_short;
        int_str[1] = hig_low_short;
        int_str[2] = low_hig_short;
        int_str[3] = hig_hig_short;
        return int_str;
    }

    /**
     * @param @param  cmd 命令号
     * @param @param  uid 用户ID
     * @param @return byte数组
     * @return byte[] 返回类型
     * @throws
     * @Title: IMCom
     * @Description: IM通讯拼包
     * @author hjl
     */
    public static byte[] IMCom(byte cmd, String uid)
    {
        byte[] uidArray = int2byte(Integer.parseInt(uid));
        int lenth = 8 + uidArray.length;
        byte[] iMCom = new byte[lenth];
        byte as = 0;
        iMCom[0] = 0x7f;
        iMCom[1] = (byte) (lenth % 0xff);
        iMCom[2] = (byte) (lenth / 0xff);
        iMCom[3] = cmd;
        iMCom[4] = 0x00;
        iMCom[lenth - 3] = 0x00;
        iMCom[lenth - 1] = 0x7f;
        as = (byte) (iMCom[0] + iMCom[1] + iMCom[2] + iMCom[3] + iMCom[4] + iMCom[lenth - 1]);
        for (int i = 0; i < uidArray.length; i++)
        {
            iMCom[5 + i] = uidArray[i];
            as = (byte) (as + iMCom[5 + i]);
        }
        iMCom[lenth - 2] = as;

        return iMCom;
    }

    /**
     * @param @param  par
     * @param @return
     * @return byte[] 返回类型
     * @throws
     * @Title: parsintIM
     * @Description: IM通讯拼包
     * @author hjl
     */
    public static byte[] parsintIM(byte[] par)
    {

        if (par[0] == 0x7f)
        {
            int lenth = byteToInt(par[2], par[1]);
            if (lenth >= par.length)
            {
                return null;
            }
            byte[] IMPar = new byte[lenth];
            if (par[lenth - 1] == 0x7f)
            {
                for (int i = 0; i < lenth; i++)
                {
                    IMPar[i] = par[i];
                }
                return IMPar;
            }
        }
        return null;
    }

    /**
     * @param @param  higth
     * @param @param  low
     * @param @return
     * @return int 返回类型
     * @throws
     * @Title: byteToInt
     * @Description: byte转int
     * @author hjl
     */
    public static int byteToInt(byte higth, byte low)
    {
        return higth * 256 + (low < 0 ? (256 + low) : low);
    }

    byte[] data_recv = new byte[128];// 组包数组
    boolean recv_head = false;// 是否有头
    int last_recv_len = 0;// 没有包头的包长度
    int pack_len;// 包长度

    public byte[] printByte(byte[] data)
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
                return null;
            }
            int i;
            for (i = 0; i < recv_len; i++)
            {
                data_recv[last_recv_len] = data[i];
                ++last_recv_len;
                if (data[i] == (byte) 0x8f)
                {
                    // LogUtils.e("组合包-完成：", Arrays.toString(data_recv));
                    recv_head = false;
                    last_recv_len = 0;
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
        return data_recv;
    }


    private byte[] parse_header(byte[] data, int recv_len)
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
                return data;
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
        return data_recv;
    }
}
