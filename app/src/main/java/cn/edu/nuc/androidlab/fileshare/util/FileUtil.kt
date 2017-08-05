package cn.edu.nuc.androidlab.fileshare.util

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.graphics.BitmapCompat
import android.util.Log
import cn.edu.nuc.androidlab.fileshare.R
import cn.edu.nuc.androidlab.fileshare.bean.FileInfo
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileFilter

/**
 * File Util
 *
 * Created by MurphySL on 2017/7/6.
 */
object FileUtil{

    private val TAG = "FileUtil"

    val JPG  :  String = ".jpg"
    val JPEG : String  = ".jpeg"
    val PNG  : String  = ".png"
    val GIF  : String  = ".gif"
    val APK  : String  = ".apk"
    val MP3  : String  = ".mp3"
    val MP4  : String  = ".mp4"

    val APK_CODE = 0x01
    val IMG_CODE = 0x02
    val MUSIC_CODE = 0x03
    val VIDEO_CODE = 0x04
    val ERROR_CODE = 0x00


    /**
     * check if external storage device is available
     */
    fun hasSDCardMounted() : Boolean{
        val state : String? = Environment.getExternalStorageState()
        return state != null && state == Environment.MEDIA_MOUNTED
    }

    /**
     * get useable space size (API > 9)
     */
    fun getUseableSpace(file : File?) : Long{
        if (file == null) return -1
        else return file.freeSpace
    }

    /**
     * get file Size
     */
    fun getFileSize(path : String) : String = getFileSize(getFileByPath(path))

    fun getFileSize(file : File) : String{
        val len : Long = file.length()
        if(len == -1L)
            return ""
        else
            return byte2MemorySize(len)
    }

    /**
     * get file by abstract pathname
     */
    fun getFileByPath(path : String) : File = File(replaceSpace(path))

    /**
     * Tests whether the path denoted by this abstract pathname is a
     * directory.
     */
    fun isDir(path : String) : Boolean = isDir(getFileByPath(path))

    fun isDir(file : File) : Boolean = file.exists() && file.isDirectory

    /**
     * create not existed file, if it already existed, return it
     */
    fun createFile(path : String) : Boolean = createFile(getFileByPath(path))

    fun createFile(file : File) : Boolean {
        if(file.exists()) return true
        else return  file.createNewFile()
    }

    /**
     * create new file, if it already existed, delete it and create a new one
     */
    fun createNewFile(path : String) : Boolean = createNewFile(getFileByPath(path))

    fun createNewFile(file : File) : Boolean {
        if(!file.exists()){
            return file.createNewFile()
        }else{
            if(file.delete()){
                return file.createNewFile()
            }
            return false
        }
    }

    /**
     * get all suit filter file in storage device
     */
    fun filesWithFilter(filter : FileFilter) : HashMap<String, String>{
        val files : HashMap<String, String> = HashMap()
        if(hasSDCardMounted()){
            val dir = Environment.getExternalStorageDirectory()
            if(dir != null){
                Log.i(TAG, dir.listFiles().joinToString { it.absolutePath + "\n" })
                for(file in dir.listFiles()){
                    if(file.isDirectory){
                        files.putAll(filesInDirWithFilter(file, filter))
                    }else{
                        if(filter.accept(file.absoluteFile)){
                            Log.i(TAG, file.absolutePath)
                            files.put(file.absolutePath, file.name)
                        }
                    }
                }
            }
        }
        Log.i(TAG, "load file finish")
        return files
    }

    /**
     * get all suit filter file in dir
     */
    fun filesInDirWithFilter(dir : File, filter: FileFilter) : HashMap<String, String>{
        val files : HashMap<String, String> = HashMap()
        if(dir.isDirectory){
            for(f in dir.listFiles()){
                if(f.isDirectory){
                    files.putAll(filesInDirWithFilter(f, filter))
                }else{
                    if(filter.accept(f.absoluteFile))
                        files.put(f.absolutePath, f.name)
                }
            }
        }
        return files
    }


    fun getFileExtension(path : String) : String{
        val lastPoi = path.lastIndexOf(".")
        val lastSep = path.lastIndexOf("/")
        return path.substring(lastPoi)
    }

    /**
     * byte to fit memory size
     */
    fun byte2MemorySize(byteNum : Long) : String{
        val B = Math.pow(2.0, 10.0)
        val KB = Math.pow(2.0, 20.0)
        val MB = Math.pow(2.0, 30.0)
        val GB = Math.pow(2.0, 40.0)

        if(byteNum < 0){
            return "shouldn't be less than zero"
        }else if(byteNum < B){
            return String.format("%.2fB", byteNum + 0.0005)
        }else if(byteNum < KB){
            return String.format("%.2fKB", byteNum/ B + 0.0005)
        }else if(byteNum < MB){
            return String.format("%.2fMB", byteNum/ KB + 0.0005)
        }else if(byteNum < GB){
            return String.format(".2fGB", byteNum/ GB + 0.0005)
        }else{
            return "it's too large"
        }
    }

    /**
     * if string has whitespace
     */
    fun replaceSpace(s : String) : String{
        return s.replace(" ","")
    }

    fun isSpace(s : String?) : Boolean{
        if(s == null || "" == s)
            return true
        return s.toCharArray().any { it.isWhitespace() }
    }


    fun getSpecificTypeFiles(context : Context, extension : Array<String>) : List<FileInfo>{
        val fileInfoList : ArrayList<FileInfo> = ArrayList()

        val fileUri : Uri = MediaStore.Files.getContentUri("external")
        // 文件路径，包含后缀的文件名
        val projection : Array<String> = arrayOf(
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.TITLE)
        var selection : String = ""
        for (i in 0..extension.size - 1){
            if(i != 0){
                selection += " OR $selection "
            }else {
                selection += " ${MediaStore.Files.FileColumns.DATA} LIKE '%${extension[i]}' "
            }
        }
        val sortOrder = MediaStore.Files.FileColumns.DATE_MODIFIED


        val cursor : Cursor? = context.contentResolver.query(fileUri, projection, selection, null , sortOrder)
        if(cursor != null){
            while(cursor.moveToNext()){
                val path : String = cursor.getString(0)
                val type = getFileTypeCode(path)
                val size = File(path).length()
                val name = File(path).name
                val fileInfo : FileInfo = FileInfo(path = path, type = type, size = size, name = name)

                Log.i(TAG, fileInfo.toString())

                fileInfoList.add(fileInfo)
            }
        }

        cursor?.close()
        return fileInfoList
    }

    fun getVideoThumbnail(context : Context, path : String) : Bitmap {
        val bitmap = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Images.Thumbnails.MICRO_KIND)
        return ThumbnailUtils.extractThumbnail(bitmap, 100 , 100)
    }

    fun getApkThumbnail(context : Context, path : String) : Drawable{
        val pm : PackageManager = context.packageManager
        val info : PackageInfo? = pm.getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES)
        info?.let {
            val appInfo = info.applicationInfo
            appInfo.sourceDir = path
            appInfo.publicSourceDir = path
            return appInfo.loadIcon(pm)
        }
        return context.resources.getDrawable(R.mipmap.ic_launcher)
    }

    fun getFileTypeCode (path : String?) : Int{
        if(path == null) return ERROR_CODE
        if(path.endsWith(JPG) || path.endsWith(JPEG) || path.endsWith(PNG))
            return IMG_CODE
        if(path.endsWith(APK))
            return APK_CODE
        if(path.endsWith(MP3 )|| path.endsWith(MP4))
            return MUSIC_CODE
        if(path.endsWith(MP4))
            return VIDEO_CODE
        else
            return ERROR_CODE
    }

    fun drawableToBitmap(drawable : Drawable) : Bitmap{
        val w = drawable.intrinsicWidth
        val h = drawable.intrinsicHeight
        val config : Bitmap.Config = if(drawable.opacity != PixelFormat.OPAQUE) Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565

        val bitmap : Bitmap = Bitmap.createBitmap(w, h , config)
        val canvas : Canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, w, h)
        drawable.draw(canvas)

        return bitmap
    }

    fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        return baos.toByteArray()
    }


}
