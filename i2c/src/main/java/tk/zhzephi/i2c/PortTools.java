package tk.zhzephi.i2c;

import android.util.Log;

import com.google.android.things.pio.PeripheralManagerService;

import java.util.List;

/**
 * 获取设备端口
 * Created by HONGBO on 2017/10/27 10:27.
 */

public class PortTools {

    private static final String TAG = PortTools.class.getSimpleName();

    public static String getI2cName() {
        PeripheralManagerService managerService = new PeripheralManagerService();
        List<String> list = managerService.getI2cBusList();
        if (list.isEmpty()) {
            Log.d(TAG, "No I2C bus available on this device.");
        } else {
            Log.d(TAG, "List of available devices: " + list);
            return list.get(0);
        }
        return "";
    }
}
