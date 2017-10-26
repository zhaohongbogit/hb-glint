package tk.zhzephi.i2c;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;

import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();

    // IIC 设备名称
    private static final String I2C_DEVICE_NAME = "I2C1";
    // IIC 从设备地址
    private static final int I2C_ADDRESS = 0x01; //0x77

    private static final int READ_VALUE_START = 0x01;

    private I2cDevice mDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PeripheralManagerService service = new PeripheralManagerService();
        try {
            mDevice = service.openI2cDevice(I2C_DEVICE_NAME, I2C_ADDRESS);
        } catch (IOException e) {
            Log.w(TAG, "Unable to access I2C device", e);
        }
        testRead();
    }

    private void testRead() {
        try {
            byte[] value = readCalibration(mDevice, READ_VALUE_START);
            Log.d(TAG, "===============>读取气压计内容：" + value.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (mDevice != null) {
                mDevice.close();
                mDevice = null;
            }
        } catch (IOException e) {
            Log.w(TAG, "Unable to close I2C device", e);
        }
    }

    /**
     * 修改单个寄存器内容
     *
     * @param device
     * @param address
     * @throws IOException
     */
    public void setRegFlag(I2cDevice device, int address) throws IOException {
        byte value = device.readRegByte(address); //从从器件读取一个寄存器
        value |= 0x40; //设置bit 6
        device.writeRegByte(address, value); //将更新的值写回从器件
    }

    /**
     * 读取寄存器块
     *
     * @param device
     * @param startAddress
     * @return
     */
    public byte[] readCalibration(I2cDevice device, int startAddress) throws IOException {
        byte[] data = new byte[3]; //连续读取3个寄存器块
        device.readRegBuffer(startAddress, data, data.length);
        return data;
    }

}
