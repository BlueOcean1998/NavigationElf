package base.foxizz.util

import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import base.foxizz.BaseApplication.Companion.baseApplication

/**
 * Uri工具
 */
object UriUtil {
    /**
     * Whether the Uri authority is ExternalStorageProvider.
     *
     * @param uri The Uri to check.
     */
    private fun isExternalStorageDocument(uri: Uri) =
            "com.android.externalstorage.documents" == uri.authority

    /**
     * Whether the Uri authority is DownloadsProvider.
     *
     * @param uri The Uri to check.
     */
    private fun isDownloadsDocument(uri: Uri) =
            "com.android.providers.downloads.documents" == uri.authority

    /**
     * Whether the Uri authority is MediaProvider.
     *
     * @param uri The Uri to check.
     */
    private fun isMediaDocument(uri: Uri) =
            "com.android.providers.media.documents" == uri.authority

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param uri The Uri to query.
     */
    fun getPath(uri: Uri) = when { // DocumentProvider
        DocumentsContract.isDocumentUri(baseApplication, uri) -> {
            when { // ExternalStorageProvider
                isExternalStorageDocument(uri) -> {
                    val split = DocumentsContract.getDocumentId(uri).split(":").toTypedArray()
                    if ("primary".equals(split[0], true)) {
                        Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                    } else ""
                }
                isDownloadsDocument(uri) -> {
                    getDataColumn(ContentUris.withAppendedId(Uri.parse(
                            "content://downloads/public_downloads"),
                            DocumentsContract.getDocumentId(uri).toLong()),
                            null, null)
                }
                isMediaDocument(uri) -> {
                    val split = DocumentsContract.getDocumentId(uri).split(":".toRegex()).toTypedArray()
                    val contentUri = when (split[0]) {
                        "image" -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        "video" -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                        "audio" -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                        else -> Uri.parse("")
                    }
                    getDataColumn(contentUri, "_id=?", arrayOf(split[1]))
                }
                else -> ""
            }
        }
        "content".equals(uri.scheme, true) -> {
            getDataColumn(uri, null, null)
        }
        "file".equals(uri.scheme, true) -> {
            uri.path ?: ""
        }
        else -> ""
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    private fun getDataColumn(uri: Uri, selection: String?,
                              selectionArgs: Array<String>?): String {
        var cursor: Cursor? = null
        val column = "_data"
        try {
            cursor = baseApplication.contentResolver
                    .query(uri, arrayOf(column), selection, selectionArgs, null)
            if (cursor != null && cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(columnIndex)
            }
        } catch (e: Exception) {
        } finally {
            cursor?.close()
        }
        return ""
    }
}