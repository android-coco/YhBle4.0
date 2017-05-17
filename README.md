# YhBle4.0
蓝牙4.0项目总结,封装相关方法
1.权限
```
<!-- 需要硬件支持低功耗蓝牙 -->
    <uses-feature
        android:name="android.hardware.bluetooth_le"
        android:required="true"/>
    <!-- 蓝牙权限 -->
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
    <uses-permission android:name="android.permission.BLUETOOTH"/>
    <!-- Android 6.0以上蓝牙还需要位置权限 -->
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
```
2.控制器<br/>
    2.1 BluetoothControl<br/>
    createManager() <br/>
    ServiceBluetoothManager(Service模式管理连接)<br/>
    GeneralBluetoothManager（传统模式管理连接）<br/>
