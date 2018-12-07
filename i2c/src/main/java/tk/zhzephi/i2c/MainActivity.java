package tk.zhzephi.i2c;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManager;

import java.io.IOException;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();

    // IIC 从设备地址
    private static final int I2C_ADDRESS = 0x77; //0x77

    private I2cDevice i2cDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PeripheralManager service = PeripheralManager.getInstance();
        try {
            i2cDevice = service.openI2cDevice(PortTools.getI2cName(), I2C_ADDRESS);
        } catch (IOException e) {
            Log.w(TAG, "Unable to access I2C device", e);
        }
        testWrite();
        testRead();
    }

    private void testWrite() {
        short t = 0x40;
        try {
            i2cDevice.writeRegWord(0xF3, t);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void testRead() {
        try {
            short value = i2cDevice.readRegWord(0xF3);
            Log.d(TAG, "===============>读取气压计内容：" + value);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (i2cDevice != null) {
                i2cDevice.close();
                i2cDevice = null;
            }
        } catch (IOException e) {
            Log.w(TAG, "Unable to close I2C device", e);
        }
    }
}
