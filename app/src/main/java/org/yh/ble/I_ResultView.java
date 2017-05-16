package org.yh.ble;

/**
 * Created by yhlyl on 2017/5/16.
 * View层回调接口(https://github.com/android-coco/YhBle4.0)
 */

public interface I_ResultView<T>
{
    //开始
    void startView(String msg);
    //扫描成功 bean设备信息  rssi信号
    void successView(T bean, int rssi);
    //断开连接 error内容
    void disconnectedView(String error);
    //连接成功
    void connectedView();
    //完成发现服务
    void completeView();
    //数据
    void valueView(String value);
}
