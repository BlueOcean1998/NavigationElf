package cn.zerokirby.api.data

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import cn.zerokirby.api.data.DatabaseHelper.Companion.databaseHelper
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.*

/**
 * 头像数据帮助类
 */
object AvatarDataHelper {
    /**
     * 下载头像
     * 请不要在主线程使用该方法！！！
     *
     * @param userId 用户id
     */
    fun downloadAvatar(userId: String) {
        var response: Response? = null
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        try {
            val client = OkHttpClient()
            val requestBody: RequestBody = FormBody.Builder().add("userId", userId).build()
            val request: Request = Request.Builder()
                    .url(Constants.DOWNLOAD_AVATAR_URL)
                    .post(requestBody).build()
            response = client.newCall(request).execute()
            inputStream = response.body?.byteStream()
            outputStream = ByteArrayOutputStream()
            if (inputStream == null) return
            val buffer = ByteArray(1024) //缓冲区大小
            var n: Int
            while (-1 != inputStream.read(buffer).also { n = it }) {
                outputStream.write(buffer, 0, n)
            }
            val bytes = outputStream.toByteArray()
            saveAvatar(bytes, userId)
        } catch (ignored: Exception) {
        } finally {
            inputStream?.close()
            outputStream?.close()
            response?.close()
        }
    }

    /**
     * 上传头像
     * 请不要在主线程使用该方法！！！
     */
    fun uploadAvatar(imagePath: String) {
        var response: Response? = null
        try {
            val file = File(imagePath)
            val mediaTypeJpeg: MediaType = "image/jpeg".toMediaType() //设置媒体类型
            val userId = UserDataHelper.loginUserId //获取用户id
            val client = OkHttpClient()
            val fileBody = file.asRequestBody(mediaTypeJpeg) //媒体类型为jpg
            val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("userId", userId)
                    .addFormDataPart("file", "$userId.jpg", fileBody).build()
            val request: Request = Request.Builder()
                    .url(Constants.UPLOAD_AVATAR_URL)
                    .post(requestBody).build()
            response = client.newCall(request).execute()
        } catch (ignored: Exception) {
        } finally {
            response?.close()
        }
    }

    /**
     * 申请读写外部存储权限，若已有则打开相册
     *
     * @param activity 启动检查权限的活动
     */
    fun checkPermissionsAndOpenAlbum(activity: Activity) {
        val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
        if (ContextCompat.checkSelfPermission(activity, permission)
                != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(activity, arrayOf(permission), Constants.CHOOSE_PHOTO)
        else openAlbum(activity)
    }

    /**
     * 开启相册
     *
     * @param activity 活动
     */
    fun openAlbum(activity: Activity) {
        val data = Intent(Intent.ACTION_GET_CONTENT)
        data.data = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI //设置外部存储url
        data.type = "image/*" //设置只显示图片类型的文件
        activity.startActivityForResult(data, Constants.CHOOSE_PHOTO)
    }

    /**
     * 调用系统方法裁剪图片
     *
     * @param activity   启动图片缩放的活动
     * @param data 带有本地图片路径的intent
     * @return 输出到服务器的路径
     */
    fun cropImage(activity: Activity, data: Intent): Uri {
        //创建临时文件，Android11后必须使用公共目录
        val cropImagePath = File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "NavigationElf")
        if (!cropImagePath.exists()) cropImagePath.mkdir()
        val cropImageFile = File(cropImagePath, "crop_image.png")
        if (cropImageFile.exists()) cropImageFile.delete()
        cropImageFile.createNewFile()
        val cropImageUri = Uri.fromFile(cropImageFile)

        data.action = "com.android.camera.action.CROP" //设置intent类型为裁剪图片
        data.putExtra(MediaStore.EXTRA_OUTPUT, cropImageUri) //设置临时文件uri

        //设置初始裁剪比例
        data.putExtra("aspectX", 1)
        data.putExtra("aspectY", 1)
        //设置裁剪后的宽高
        data.putExtra("outputX", 256)
        data.putExtra("outputY", 256)

        activity.startActivityForResult(data, Constants.PHOTO_REQUEST_CUT)

        return cropImageUri
    }

    /**
     * 从数据库中获取位图头像
     *
     * @return 位图
     */
    fun getBitmapAvatar(userId: String): Bitmap {
        val avatarBytes = getAvatar(userId)
        return BitmapFactory.decodeByteArray(avatarBytes, 0, avatarBytes.size)
    }

    /**
     * 解码并显示图片，然后将图片写入本地数据库
     *
     * @param avatar    头像控件
     * @param imagePath 图片存储路径
     */
    fun showAvatarAndSave(avatar: ImageView, imagePath: String, userId: String) {
        val bitmap = BitmapFactory.decodeFile(imagePath) //解码位图
        avatar.setImageBitmap(bitmap)
        saveAvatar(bitmapToBytes(bitmap), userId)
    }

    /**
     * 图片转为二进制数据
     *
     * @param bitmap bitmap
     * @return 二进制比特串
     */
    private fun bitmapToBytes(bitmap: Bitmap): ByteArray {
        //将图片转化为位图
        val size = bitmap.width * bitmap.height
        //创建一个字节数组输出流,流的大小为size
        val bs = ByteArrayOutputStream(size)
        try {
            //设置位图的压缩格式，质量为100%，并放入字节数组输出流中
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bs)
            //将字节数组输出流转化为字节数组byte[]
            return bs.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                bs.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return "".toByteArray()
    }

    /**
     * 保存头像
     *
     * @param avatarBytes 带有头像数据的比特串
     */
    fun saveAvatar(avatarBytes: ByteArray, userId: String) {
        try {
            databaseHelper.writableDatabase.use { db ->
                db.execSQL("update User set avatar = ? where user_id = ?",
                        arrayOf<Any>(avatarBytes, userId))
            }
        } catch (ignored: Exception) {
        }
    }

    /**
     * 获取头像
     *
     * @return 带有头像数据的比特串
     */
    private fun getAvatar(userId: String): ByteArray {
        try {
            databaseHelper.readableDatabase.use { db ->
                db.rawQuery("select avatar from User where user_id = ?",
                        arrayOf(userId)).use { cursor ->
                    if (cursor.moveToNext()) {
                        return cursor.getBlob(cursor.getColumnIndex("avatar"))
                    }
                }
            }
        } catch (ignored: Exception) {
        }
        return "".toByteArray()
    }
}