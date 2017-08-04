package cn.edu.nuc.androidlab.fileshare.bean

import android.graphics.Bitmap
import java.io.Serializable

/**
 * FileInfo
 *
 * Created by MurphySL on 2017/7/10.
 */

class  FileInfo @JvmOverloads constructor(var path : String,
               var type : Int,
               var size : Long,
               var name : String,
               var extra : String? = null,
               var bitmap : Bitmap? = null,
               val procceed : Long? = null,
               val result : Int?= null) :Serializable{

    companion object {
        @JvmStatic
        val APK : String = ".apk"

        @JvmStatic
        val JPG : String = ".jpg"

        @JvmStatic
        val JPEG : String = ".jpeg"

        @JvmStatic
        val MP3 : String = ".mp3"

        @JvmStatic
        val MP4 : String = ",mp4"
    }


    override fun toString(): String {
        return "FileInfo(path='$path', type=$type, size=$size, name='$name', extra=$extra, procceed=$procceed, result=$result)"
    }
}