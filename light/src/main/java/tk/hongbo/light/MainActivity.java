package tk.hongbo.light;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManager;

import java.io.IOException;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String GPIO_NAME = "GPIO2_IO05";

    private Gpio gpio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupServo();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        displayServo();
    }

    private void setupServo() {
        try {
            gpio = PeripheralManager.getInstance().openGpio(GPIO_NAME);
            gpio.setDirection(Gpio.DIRECTION_IN);
            gpio.setActiveType(Gpio.ACTIVE_LOW);
            gpio.setEdgeTriggerType(Gpio.EDGE_BOTH);
            gpio.registerGpioCallback(callback);
        } catch (IOException e) {
            Log.e(TAG, "Open gpio error", e);
        }
    }

    GpioCallback callback = new GpioCallback() {
        @Override
        public boolean onGpioEdge(Gpio gpio) {
            try {
                boolean value = gpio.getValue();
                Log.d(TAG, "The Servo value is " + value);
            } catch (IOException e) {
                Log.e(TAG, "Get value error", e);
            }
            return true;
        }
    };

    private void displayServo() {
        if (gpio != null) {
            gpio.unregisterGpioCallback(callback);
            try {
                gpio.close();
            } catch (IOException e) {
                Log.e(TAG, "Closing gpio error", e);
            } finally {
                gpio = null;
            }
        }
    }

}
