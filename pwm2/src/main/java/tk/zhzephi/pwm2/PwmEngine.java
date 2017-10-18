package tk.zhzephi.pwm2;

import android.util.Log;

import com.google.android.things.pio.PeripheralManagerService;
import com.google.android.things.pio.Pwm;

import java.io.IOException;

/**
 * Pwm舵机控制引擎（SG92R辉盛）
 * Created by HONGBO on 2017/10/17 16:45.
 */

public class PwmEngine {

    private Pwm mPwm;

    private final static int STATE_START = 1;
    private final static int STATE_STOP = 2;
    private int mState = STATE_STOP;
    private Thread swingThread;
    private static final int BEGIN_PLACE = 0;

    public PwmEngine(String pwmName) {
        PeripheralManagerService service = new PeripheralManagerService();
        try {
            mPwm = service.openPwm(pwmName);
            mPwm.setPwmFrequencyHz(50);
            mPwm.setEnabled(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 控制舵机转到对应角度，degree在[0,180]区间内
     *
     * @param degree
     */
    public void setDegree(double degree) {
        if (degree < 0 || degree > 180) {
            return;
        }
        try {
            mPwm.setPwmDutyCycle(2.5 + 10 * degree / 180);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 控制舵机转到对应角度，degree在[0,180]区间内
     * gap用来控制完成转动的时间，也就是控制转动的速度。
     * gap的单位是毫秒，ms。
     * 一次偏转需要多个脉冲周期，每个转动的脉冲信号后，gap的时间间隔里会发出平信号。
     *
     * @param degree
     * @param gap
     */
    private void setDegree(double degree, int gap) {
        if (degree < 0 || degree > 180) {
            return;
        }
        try {
            mPwm.setPwmDutyCycle(2.5 + 10 * degree / 180);
            Thread.sleep(gap);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 控制舵机在begin和end之间摆动.
     * gapTime控制摆动的速度，最快的速度是gapTime =0；gapTime是每次脉冲信号后，发出平信号的时间间隔。
     * 会创建新的线程执行。
     *
     * @param begin
     * @param end
     * @param gapTime
     */
    public void startSwing(final double begin, final double end, final int gapTime) {
        if (begin < 0 || begin > 180 || end < 0 || end > 180 || gapTime < 0) {
            return;
        }
        if (STATE_STOP == mState) {
            mState = STATE_START;
            swingThread = new Thread() {
                @Override
                public void run() {
                    try {
                        swing(begin, end, gapTime);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            if (null != swingThread) {
                swingThread.start();
            }
        }
    }

    private void swing(double begin, double end, int gapTime) throws IOException {
        if (STATE_START == mState)
            setDegree(BEGIN_PLACE);
        while (STATE_START == mState) {
            setDegree(begin, gapTime);
            setDegree(end, gapTime);
        }
    }

    /**
     * 停止摆动，回收资源
     */
    public void stopSwing() {
        if (STATE_START == mState) {
            mState = STATE_STOP;
            Log.d("---->", "stop start");
            if (null != swingThread) {
                try {
                    Log.d("---->", "wait recognition thread exit");
                    swingThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    swingThread = null;
                }
            }
            Log.d("---->", "stop end");
        }
    }

    /**
     * 关闭PWM端口连接
     */
    public void close() {
        if (mPwm != null) {
            try {
                mPwm.setEnabled(false);
                mPwm.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                mPwm = null;
            }
        }
    }
}
