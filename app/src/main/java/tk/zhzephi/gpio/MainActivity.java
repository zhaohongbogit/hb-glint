package tk.zhzephi.gpio;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String GPIO_NAME = "BCM5";

    Handler handler = new Handler();
    Gpio gpio;
    private int sequence = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupLedStrip();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyLedStrip();
    }

    private void setupLedStrip() {
        try {
            Log.d(TAG, "Initializing LED strip");
            PeripheralManagerService managerService = new PeripheralManagerService();
            gpio = managerService.openGpio(GPIO_NAME);
            gpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            Log.d(TAG, "Start GPIO");
            handler.postDelayed(runnable, 1000l);
        } catch (IOException e) {
            Log.e(TAG, "Error initializing LED strip", e);
        }
    }

    private void destroyLedStrip() {
        if (gpio != null) {
            try {
                gpio.close();
            } catch (IOException e) {
                Log.e(TAG, "Exception closing LED strip", e);
            } finally {
                gpio = null;
            }
        }
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (gpio == null) {
                return;
            }
            try {
                if (sequence == 1) {
                    //闪灯一次
                    glint();
                    sequence = 0;
                } else {
                    //连闪两次
                    glint2();
                    sequence = 1;
                }
                handler.postDelayed(runnable, 1000l);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * 闪光一次
     *
     * @throws IOException
     * @throws InterruptedException
     */
    private void glint() throws IOException, InterruptedException {
        gpio.setValue(true);
        Thread.sleep(150l);
        gpio.setValue(false);
    }

    /**
     * 连续闪光2次
     *
     * @throws InterruptedException
     * @throws IOException
     */
    private void glint2() throws InterruptedException, IOException {
        glint();
        Thread.sleep(150l);
        glint();
    }
}
