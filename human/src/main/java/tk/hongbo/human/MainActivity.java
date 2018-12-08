package tk.hongbo.human;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManager;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Skeleton of an Android Things activity.
 * <p>
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 *
 * <pre>{@code
 * PeripheralManagerService service = new PeripheralManagerService();
 * mLedGpio = service.openGpio("BCM6");
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
 * mLedGpio.setValue(true);
 * }</pre>
 * <p>
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 *
 * @see <a href="https://github.com/androidthings/contrib-drivers#readme">https://github.com/androidthings/contrib-drivers#readme</a>
 */
public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String GPIO_HUMAN = "GPIO2_IO05";

    TextView tvMsg;

    private Gpio humanGpio; //接收人体感应信号

    private int sequence = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvMsg = findViewById(R.id.msg);

        stepHuman();
    }

    @Override
    protected void onStop() {
        humanGpio.unregisterGpioCallback(gpioCallback);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (humanGpio != null) {
            try {
                humanGpio.close();
            } catch (IOException e) {
                Log.e(TAG, "Error close gpio", e);
            } finally {
                humanGpio = null;
            }
        }
        super.onDestroy();
    }

    private void stepHuman() {
        try {
            humanGpio = PeripheralManager.getInstance().openGpio(GPIO_HUMAN);
            humanGpio.setDirection(Gpio.DIRECTION_IN);
            humanGpio.setActiveType(Gpio.ACTIVE_LOW);
            humanGpio.setEdgeTriggerType(Gpio.EDGE_BOTH);
            humanGpio.registerGpioCallback(gpioCallback);
        } catch (IOException e) {
            Log.e(TAG, "Error init gpio", e);
        }
    }

    GpioCallback gpioCallback = new GpioCallback() {
        @Override
        public boolean onGpioEdge(Gpio gpio) {
            try {
                if (gpio.getValue()) {
                    String oldStr = tvMsg.getText().toString();
                    String newStr = (++sequence) + new SimpleDateFormat("-> yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
                    Log.d(TAG, newStr);
                    tvMsg.setText(oldStr + "\r\n" + newStr);
                }
            } catch (IOException e) {
                Log.e(TAG, "Error get value", e);
            }
            return true; //返回true，代表还需要继续监听
        }
    };
}
