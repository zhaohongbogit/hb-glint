package tk.hongbo.camera;

import android.app.Activity;
import android.graphics.Bitmap;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import id.zelory.compressor.Compressor;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private DoorbellCamera mCamera;
    private Handler mCameraHandler;
    private HandlerThread mCameraThread;

    private DatabaseReference mRef;
    private StorageReference mStorageRef;

    private Handler mCloudHandler;
    private HandlerThread mCloudThread;

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

        /**
         * 相机初始化
         */
        mCamera = DoorbellCamera.getInstance();
        mCamera.initializeCamera(this, mCameraHandler, mOnImageAvailableListener);
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

    private volatile boolean isUploading = false;

    /**
     * Upload image data to Firebase as a doorbell event.
     */
    private void onPictureTaken(final byte[] imageBytes) {
        if (isUploading) {
            return;
        }
        if (imageBytes != null) {
            isUploading = true;
            new Thread(() -> {
                //需要做图片压缩处理
                File file = new File(getExternalFilesDir("image") + File.separator + "chche.jpg");
                File newfile = new File(getExternalFilesDir("image") + File.separator + "chche_new.jpg");
                FileUtils.saveFile(imageBytes, file.getAbsolutePath());

                try {
                    if (file.exists()) {
                        Bitmap bitmap = new Compressor(this).compressToBitmap(file);
                        FileUtils.saveFile(bitmap, newfile.getAbsolutePath());
                        pushImageToFirebase(Uri.fromFile(newfile));
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error to compressor", e);
                }
            }).start();
        }
    }

    private void pushImageToFirebase(Uri localUri) {
        DatabaseReference ref = mRef.push();
        ref.child("timestamp").setValue("hongbo", (databaseError, databaseReference) -> {
            Log.d(TAG, databaseError.getMessage());
        });
//        StorageReference seeImage = mStorageRef.child("images/" + ref + ".jpg");
//        seeImage.putFile(localUri).addOnSuccessListener(taskSnapshot -> {
//            isUploading = false;
//            mRef.child(id).child("timestamp").setValue(ServerValue.TIMESTAMP);
//            seeImage.getDownloadUrl().addOnSuccessListener(uri -> {
//                mRef.child(id).child("image").setValue(seeImage.toString());
//            });
//        }).addOnFailureListener(e -> {
//            mRef.removeValue();
//            isUploading = false;
//        });
    }
}
