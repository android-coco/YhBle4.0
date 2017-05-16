package org.yh.ble.adapter;

import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.util.List;

/**
 * Created by yhlyl on 2017/5/16.
 * (https://github.com/android-coco/YhBle4.0)
 * API 21以上支持，API 18-20请查看{@link YHBluetoothLeGatt}
 */
//标识该类在API 21使用
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class YHBluetoothPeripheral extends YHBluetooth
{
    //扫描回调
    private ScanCallback scanCallback;
    //扫描控制类
    private BluetoothLeScanner bluetoothLeScanner;
    //扫描过滤
    private List<ScanFilter> scanFilters;

    public YHBluetoothPeripheral(Context context, ScanCallback scanCallback)
    {
        super(context);
        this.scanCallback = scanCallback;
        createScanner();
    }

    /**
     * 创建扫描器
     */
    private boolean createScanner()
    {
        boolean scanner = false;
        if (isEnabled())
        {
            if (bluetoothLeScanner == null)
            {
                this.bluetoothLeScanner = getBluetoothAdapter().getBluetoothLeScanner();
                scanner = bluetoothLeScanner != null;
            }
            else
            {
                scanner = true;
            }
        }
        return scanner;
    }

    /**
     * @return
     * 是否有多个蓝牙适配器
     */
    public boolean isMultipleAdvertisementSupport()
    {
        if (isSupportBluetooth())
        {
            return getBluetoothAdapter().isMultipleAdvertisementSupported();
        }
        return false;
    }

    /**
     * 开始扫描
     */
    public void startScan()
    {
        if (createScanner())
        {
            setScanning(true);
            //开始扫描
            bluetoothLeScanner.startScan(scanFilters, buildScanSettings(), scanCallback);
            //定时停止扫描
            getHandler().postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    stopScanning();
                }
            }, getScanPeriod());
        }
    }

    /**
     * 设置扫描过滤
     *
     * @param scanFilters
     */
    public void setScanFilters(List<ScanFilter> scanFilters)
    {
        this.scanFilters = scanFilters;
    }

    /**
     * 设置扫描模式
     *
     * @return
     */
    private ScanSettings buildScanSettings()
    {
        ScanSettings.Builder builder = new ScanSettings.Builder();
        //低功率模式
        builder.setScanMode(ScanSettings.SCAN_MODE_LOW_POWER);
        return builder.build();
    }

    /**
     * 停止扫描
     */
    public void stopScanning()
    {
        if (isSupportBluetooth() && isEnabled())
        {
            setScanning(false);
            bluetoothLeScanner.stopScan(scanCallback);
        }
    }
}
