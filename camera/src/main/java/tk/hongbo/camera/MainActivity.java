package tk.hongbo.camera;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.nio.ByteBuffer;

import tk.hongbo.camera.stream.ServerSubThread;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private DoorbellCamera mCamera;
    private Handler mCameraHandler;
    private HandlerThread mCameraThread;

    private DatabaseReference mRef;
    private StorageReference mStorageRef;

    private Handler mCloudHandler;
    private HandlerThread mCloudThread;

    public static ServerSubThread subThread; //socket子线程

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRef = FirebaseDatabase.getInstance().getReference("logs");
        mStorageRef = FirebaseStorage.getInstance().getReference();

        mCameraThread = new HandlerThread("CameraBackground");
        mCameraThread.start();
        mCameraHandler = new Handler(mCameraThread.getLooper());

        mCloudThread = new HandlerThread("CloudThread");
        mCloudThread.start();
        mCloudHandler = new Handler(mCloudThread.getLooper());

        mCamera = DoorbellCamera.getInstance();
        mCamera.initializeCamera(this, mCameraHandler, mOnImageAvailableListener);

        /**
         * 初始化socket服务端
         */
        subThread = new ServerSubThread(() -> {
            switch (ServerSubThread.rec_data) {

            }
        });
        subThread.creatServer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCamera.shutDown();
        mCameraThread.quitSafely();
    }

    /**
     * Listener for new camera images.
     */
    private ImageReader.OnImageAvailableListener mOnImageAvailableListener = reader -> {
        // get image bytes
        Image image = reader.acquireLatestImage();
        ByteBuffer imageBuf = image.getPlanes()[0].getBuffer();
        final byte[] imageBytes = new byte[imageBuf.remaining()];
        imageBuf.get(imageBytes);
        image.close();

        onPictureTaken(imageBytes);
    };

    /**
     * Upload image data to Firebase as a doorbell event.
     */
    private void onPictureTaken(final byte[] imageBytes) {
        if (imageBytes != null) {
//            DatabaseReference log = mRef.push();
//            StorageReference seeImage = mStorageRef.child("images/" + log.getKey() + ".jpg");
//            seeImage.putBytes(imageBytes).addOnSuccessListener(taskSnapshot -> {
//                log.child("timestamp").setValue(ServerValue.TIMESTAMP);
//                seeImage.getDownloadUrl().addOnSuccessListener(uri -> {
//                    log.child("image").setValue(seeImage.toString());
//                });
//            }).addOnFailureListener(e -> {
//                log.removeValue();
//            });
            final Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            if (bitmap != null) {
                subThread.sendMessage(bitmap);
            }
        }
    }

    /**
     * 保存数据流为本地图片
     *
     * @param imageBytes
     */
    private void saveFile(final byte[] imageBytes) {
        if (imageBytes != null) {
            FileUtils.saveFile(imageBytes, getExternalFilesDir("camera").getAbsolutePath());
        }
    }
}
