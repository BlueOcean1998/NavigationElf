package cn.zerokirby.api.data;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.widget.ImageView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 用户操作工具
 */
public class AvatarDataHelper {

    public static void initAvatarDataHelper() {
        databaseHelper = new DatabaseHelper(Constants.LOCAL_DATABASE, null, 1);
    }

    private static DatabaseHelper databaseHelper;
    private static SQLiteDatabase db;
    private static android.database.Cursor cursor;

    /**
     * 下载头像
     * 请不要在主线程使用该方法！！！
     *
     * @param userId 用户id
     */
    public static void downloadAvatar(String userId) {
        Response response = null;
        try {
            OkHttpClient client = new OkHttpClient();
            RequestBody requestBody = new FormBody.Builder()
                    .add("userId", userId).build();
            Request request = new Request.Builder()
                    .url(Constants.DOWNLOAD_AVATAR_URL)
                    .post(requestBody).build();
            response = client.newCall(request).execute();
            java.io.InputStream inputStream = Objects.requireNonNull(response.body()).byteStream();

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];//缓冲区大小
            int n;
            while (-1 != (n = inputStream.read(buffer))) {
                output.write(buffer, 0, n);
            }
            inputStream.close();
            output.close();
            byte[] bytes = output.toByteArray();

            saveAvatar(bytes);
        } catch (Exception ignored) {

        } finally {
            if (response != null) response.close();
        }
    }

    /**
     * 上传头像
     * 请不要在主线程使用该方法！！！
     */
    public static void uploadAvatar(String imagePath) {
        Response response = null;
        try {
            File file = new File(imagePath);
            final MediaType MEDIA_TYPE_JPEG = MediaType.parse("image/jpeg");//设置媒体类型
            final String userId = UserDataHelper.getUser().getUserId();//获取用户id
            OkHttpClient client = new OkHttpClient();
            RequestBody fileBody = RequestBody.create(MEDIA_TYPE_JPEG, file);//媒体类型为jpg
            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("userId", String.valueOf(userId))
                    .addFormDataPart("file", userId + ".jpg", fileBody).build();
            Request request = new Request.Builder()
                    .url(Constants.UPLOAD_AVATAR_URL)
                    .post(requestBody).build();
            response = client.newCall(request).execute();
        } catch (Exception ignored) {

        } finally {
            if (response != null) response.close();
        }
    }

    /**
     * 检查是否有授予读写外部存储权限
     * 若已授予则启动相册
     *
     * @param activity 启动检查权限的活动
     */
    public static void checkAvatarPermission(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA}, Constants.CHOOSE_PHOTO);
        } else {
            openAlbum(activity);
        }
    }

    /**
     * 开启相册
     *
     * @param activity 活动
     */
    public static void openAlbum(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");//接受图片类型
        activity.startActivityForResult(intent, Constants.CHOOSE_PHOTO);
    }

    /**
     * 图片缩放
     *
     * @param activity 启动图片缩放的活动
     * @param intentData 带有本地图片路径的intent
     * @return 输出到服务器的路径
     */
    public static Uri startPhotoZoom(Activity activity, Intent intentData) {
        File cropPhoto = new File(activity.getExternalCacheDir(), "crop.jpg");//创建临时文件
        try {
            if (cropPhoto.exists()) {//如果临时文件存在则删除，否则新建
                cropPhoto.delete();
            }
            cropPhoto.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Uri cropImageUri = Uri.fromFile(cropPhoto);//获取裁剪图片的地址
        Intent intent = new Intent("com.android.camera.action.CROP");//设置intent类型为裁剪图片
        intent.setDataAndType(intentData.getData(), "image/*");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); //添加这一句表示对目标应用临时授权该Uri所代表的文件
        }
        //下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
        intent.putExtra("crop", "true");
        intent.putExtra("scale", true);

        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);

        //输出的宽高
        intent.putExtra("outputX", 256);
        intent.putExtra("outputY", 256);

        intent.putExtra("return-data", false);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cropImageUri);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true); // no face detection
        activity.startActivityForResult(intent, Constants.PHOTO_REQUEST_CUT);

        return cropImageUri;
    }

    /**
     * 从数据库中获取位图头像
     *
     * @return 位图
     */
    public static Bitmap getBitmapAvatar() {
        byte[] avatarBytes = getAvatar();
        if (avatarBytes != null) {
            //将字节数组转化为位图，将位图显示为图片
            return BitmapFactory.decodeByteArray(avatarBytes, 0, avatarBytes.length);
        }
        return null;
    }

    /**
     * 解码并显示图片，然后将图片写入本地数据库
     *
     * @param avatar    头像控件
     * @param imagePath 图片存储路径
     */
    public static void showAvatarAndSave(ImageView avatar, String imagePath) {
        if (imagePath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);//解码位图
            avatar.setImageBitmap(bitmap);
            saveAvatar(bitmapToBytes(bitmap));
        }
    }

    /**
     * 图片转为二进制数据
     *
     * @param bitmap bitmap
     * @return byte[]
     */
    public static byte[] bitmapToBytes(Bitmap bitmap) {
        //将图片转化为位图
        int size = bitmap.getWidth() * bitmap.getHeight();
        //创建一个字节数组输出流,流的大小为size
        ByteArrayOutputStream bs = new ByteArrayOutputStream(size);
        try {
            //设置位图的压缩格式，质量为100%，并放入字节数组输出流中
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bs);
            //将字节数组输出流转化为字节数组byte[]
            return bs.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                bs.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "".getBytes();
    }

    /**
     * 保存头像
     *
     * @param avatarBytes 带有头像数据的比特串
     */
    public static void saveAvatar(byte[] avatarBytes) {
        try {
            db = databaseHelper.getWritableDatabase();
            db.execSQL("update User set avatar = ?",
                    new byte[][]{avatarBytes});
        } catch (Exception ignored) {

        } finally {
            if (db != null) db.close();
        }
    }

    /**
     * 获取头像
     *
     * @return 带有头像数据的比特串
     */
    public static byte[] getAvatar() {
        try {
            db = databaseHelper.getReadableDatabase();
            cursor = db.rawQuery("select avatar from User", null);
            if (cursor.moveToNext()) {
                return cursor.getBlob(cursor.getColumnIndex("avatar"));
            }
        } catch (Exception ignored) {

        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
        return "".getBytes();
    }

    /**
     * 关闭数据库，防止内存泄漏
     */
    public static void close() {
        if (databaseHelper != null) databaseHelper.close();
    }

}
