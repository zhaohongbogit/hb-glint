package tk.hongbo.camera;

import android.app.Activity;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;

import java.nio.ByteBuffer;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private DoorbellCamera mCamera;
    private Handler mCameraHandler;
    private HandlerThread mCameraThread;

    private int sequence = 0;

    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCameraThread = new HandlerThread("CameraBackground");
        mCameraThread.start();
        mCameraHandler = new Handler(mCameraThread.getLooper());

        mCamera = DoorbellCamera.getInstance();
        mCamera.initializeCamera(this, mCameraHandler, mOnImageAvailableListener);

        handler = new Handler();
        handler.post(thread); //立即进行拍照
    }

    Runnable thread = new Runnable() {
        @Override
        public void run() {
            mCamera.takePicture();
            sequence++;
            if (sequence < 10) {
                handler.postDelayed(thread, 5000);
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCamera.shutDown();
        mCameraThread.quitSafely();
    }

    /**
     * Listener for new camera images.
     */
    private ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image image = reader.acquireLatestImage();
            // get image bytes
            ByteBuffer imageBuf = image.getPlanes()[0].getBuffer();
            final byte[] imageBytes = new byte[imageBuf.remaining()];
            imageBuf.get(imageBytes);
            image.close();

            onPictureTaken(imageBytes);
        }
    };

    /**
     * Upload image data to Firebase as a doorbell event.
     */
    private void onPictureTaken(final byte[] imageBytes) {
        if (imageBytes != null) {
            FileUtils.saveFile(imageBytes, getExternalFilesDir("camera").getAbsolutePath());
        }
    }
}
