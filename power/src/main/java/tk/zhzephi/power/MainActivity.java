package tk.zhzephi.power;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String outputGpio = "BCM5";
    private static final String inputGpio = "BCM6";

    Gpio ledGpio; //LED灯
    Gpio switchGpio; //开关

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PeripheralManagerService service = new PeripheralManagerService();
        try {
            switchGpio = service.openGpio(inputGpio);
            switchGpio.setDirection(Gpio.DIRECTION_IN);
            switchGpio.setActiveType(Gpio.ACTIVE_LOW);

            ledGpio = service.openGpio(outputGpio);
            ledGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH); //将引脚初始化为高电平输出
            ledGpio.setActiveType(Gpio.ACTIVE_HIGH); //低电平为true
            ledGpio.setValue(false);

            switchGpio.setEdgeTriggerType(Gpio.EDGE_BOTH); //声明触发中断事件的状态变化,从低到高过渡中断
            switchGpio.registerGpioCallback(gpioCallback);
        } catch (IOException e) {
            Log.d(TAG, "初始化失败", e);
        }
    }

    GpioCallback gpioCallback = new GpioCallback() {
        @Override
        public boolean onGpioEdge(Gpio gpio) {
            try {
                Log.d(TAG, "=====Switch status is " + switchGpio.getValue());
                ledGpio.setValue(switchGpio.getValue());
            } catch (IOException e) {
                Log.d(TAG, "修改Led状态出错,", e);
            }
            return true;
        }

        @Override
        public void onGpioError(Gpio gpio, int error) {
            Log.d(TAG, gpio + ": 发生错误 " + error);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (switchGpio != null) {
            switchGpio.unregisterGpioCallback(gpioCallback);
            try {
                switchGpio.close();
            } catch (IOException e) {
                Log.d(TAG, "关闭开关出错,", e);
            } finally {
                switchGpio = null;
            }
        }
        if (ledGpio != null) {
            try {
                ledGpio.close();
            } catch (IOException e) {
                Log.d(TAG, "关闭LED出错,", e);
            } finally {
                ledGpio = null;
            }
        }
    }

}
