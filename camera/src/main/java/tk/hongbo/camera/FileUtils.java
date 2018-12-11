package tk.hongbo.camera;

import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtils {
    /**
     * 保存字节码到本地文件
     *
     * @param imageBytes
     */
    public static void saveFile(byte[] imageBytes, String filePath) {
        File file = new File(filePath + File.separator + System.currentTimeMillis() + ".png");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                Log.e("FileUtils", "Failure, Create new file error", e);
                return;
            }
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write(imageBytes);
            fos.flush();
        } catch (FileNotFoundException e) {
            Log.e("FileUtils", "Failure, Not file", e);
        } catch (IOException e) {
            Log.e("FileUtils", "Failure, Write file error", e);
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
            }
        }
    }
}
