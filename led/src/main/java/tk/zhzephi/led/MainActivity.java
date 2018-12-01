package tk.zhzephi.led;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManager;

import java.io.IOException;

/**
 * Skeleton of an Android Things activity.
 * <p>
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 * <p>
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

    Gpio gpio;

    private int sum = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PeripheralManager service = PeripheralManager.getInstance();
        try {
            gpio = service.openGpio("BCM5");
            gpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            gpio.setValue(true);
            new Handler().post(runnable);
        } catch (IOException e) {
            Log.d(TAG, "Init gpio error", e);
        }
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            while (true) {
                try {
                    if (sum > 6) {
                        sum = 0;
                    }
                    sum++;
                    for (int i = 0; i < sum; i++) {
                        glint();
                        Thread.sleep(100);
                    }
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Log.d(TAG, "InterruptedException", e);
                } catch (IOException e) {
                    Log.d(TAG, "IOException", e);
                }
            }
        }
    };

    private void glint() throws IOException, InterruptedException {
        gpio.setValue(true);
        Thread.sleep(100);
        gpio.setValue(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (gpio != null) {
                gpio.close();
            }
        } catch (IOException e) {
            Log.d(TAG, "Close gpio error", e);
        } finally {
            gpio = null;
        }
    }
}
