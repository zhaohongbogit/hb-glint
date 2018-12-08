package tk.hongbo.aswitch;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.things.contrib.driver.button.Button;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManager;

import java.io.IOException;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String gpioButtonPinName = "GPIO2_IO00";
    private static final String gpioLedPinName = "GPIO2_IO05";
    private Button mButton;
    private Gpio led;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PeripheralManager peripheralManager = PeripheralManager.getInstance();
        try {
            led = peripheralManager.openGpio(gpioLedPinName);
            led.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
        } catch (IOException e) {
            Log.d(TAG, "Init led error", e);
        }
        setupButton();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyButton();
        if (led != null) {
            try {
                led.close();
            } catch (IOException e) {
                Log.e(TAG, "Close led error", e);
            } finally {
                led = null;
            }
        }
    }

    private void setupButton() {
        try {
            mButton = new Button(gpioButtonPinName, Button.LogicState.PRESSED_WHEN_LOW);
            mButton.setOnButtonEventListener(new Button.OnButtonEventListener() {
                @Override
                public void onButtonEvent(Button button, boolean pressed) {
                    Log.d(TAG, "按钮状态：" + pressed);
                    try {
                        led.setValue(pressed);
                    } catch (IOException e) {
                        Log.e(TAG, "Led set value error", e);
                    }
                }
            });
        } catch (IOException e) {
            Log.e(TAG, "初始化按钮失败", e);
        }
    }

    private void destroyButton() {
        if (mButton != null) {
            Log.i(TAG, "Closing button");
            try {
                mButton.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing button", e);
            } finally {
                mButton = null;
            }
        }
    }

}
