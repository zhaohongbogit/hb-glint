package tk.zhzephi.pwm2;

import android.app.Activity;
import android.os.Bundle;

/**
 * SG90 脉冲周期为20毫秒，也就是Hz=50。
 * SG90支持的旋转角度是180度。脉宽0.5ms-2.5ms对应的角度-90到+90度，对应的占空比为2.5% - 12.5% 。所以7.5%对应了舵机在中立点（0度）。
 */
public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String PWM_BUS = "PWM0";
    PwmEngine engine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        engine = new PwmEngine(PWM_BUS);
        engine.setDegree(180);
//        engine.startSwing(0, 150, 0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (engine != null) {
            engine.close();
        }
    }
}
