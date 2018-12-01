package tk.zhzephi.i2c;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();

    // IIC 从设备地址
    private static final int I2C_ADDRESS = 0x77; //0x77
    private static final int REGISTER_TEMPERATURE_CALIBRATION_1 = 0x88;
    private static final int REGISTER_TEMPERATURE_CALIBRATION_2 = 0x8A;
    private static final int REGISTER_TEMPERATURE_CALIBRATION_3 = 0x8C;

    private static final int REGISTER_TEMPERATURE_RAW_VALUE_START = 0xFA;
    private static final int REGISTER_TEMPERATURE_RAW_VALUE_SIZE = 3;

    private I2cDevice i2cDevice;

    private final short[] calibrationData = new short[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PeripheralManagerService service = new PeripheralManagerService();
        try {
            i2cDevice = service.openI2cDevice(PortTools.getI2cName(), I2C_ADDRESS);
//            calibrationData[0] = i2cDevice.readRegWord(REGISTER_TEMPERATURE_CALIBRATION_1);
//            calibrationData[1] = i2cDevice.readRegWord(REGISTER_TEMPERATURE_CALIBRATION_2);
//            calibrationData[2] = i2cDevice.readRegWord(REGISTER_TEMPERATURE_CALIBRATION_3);

//            byte[] data = new byte[REGISTER_TEMPERATURE_RAW_VALUE_SIZE];
//            i2cDevice.readRegBuffer(REGISTER_TEMPERATURE_RAW_VALUE_START, data, REGISTER_TEMPERATURE_RAW_VALUE_SIZE);
//            if (data.length != 0) {
//                float temperature = compensateTemperature(readSample(data));
//                Log.d(TAG, "读取温度信息为：" + temperature);
//            }
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
//            byte[] bytes = new byte[3];
//            i2cDevice.read(bytes, 1);
//            byte[] value = readCalibration(i2cDevice, READ_VALUE_START);
            short value = i2cDevice.readRegWord(0xF3);
//            byte value = i2cDevice.readRegByte(0x01);
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

    private int readSample(byte[] data) {
        // msb[7:0] lsb[7:0] xlsb[7:4]
        int msb = data[0] & 0xff;
        int lsb = data[1] & 0xff;
        int xlsb = data[2] & 0xf0;
        // Convert to 20bit integer
        return (msb << 16 | lsb << 8 | xlsb) >> 4;
    }

    private float compensateTemperature(int rawTemp) {
        float digT1 = calibrationData[0];
        float digT2 = calibrationData[1];
        float digT3 = calibrationData[2];
        float adcT = (float) rawTemp;

        float varX1 = adcT / 16384f - digT1 / 1024f;
        float varX2 = varX1 * digT2;

        float varY1 = adcT / 131072f - digT1 / 8192f;
        float varY2 = varY1 * varY1;
        float varY3 = varY2 * digT3;

        return (varX2 + varY3) / 5120f;
    }

}
