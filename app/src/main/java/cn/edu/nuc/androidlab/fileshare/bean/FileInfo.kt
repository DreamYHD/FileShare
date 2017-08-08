package cn.edu.nuc.androidlab.fileshare.bean

import android.graphics.Bitmap
import org.json.JSONException
import org.json.JSONObject
import java.io.Serializable

/**
 * FileInfo
 *
 * Created by MurphySL on 2017/7/10.
 */

data class  FileInfo constructor(var path : String,
               var type : Int,
               var size : Long,
               var name : String,
               var extra : String? = null,
               var bitmap : Bitmap? = null,
               val procceed : Long? = null,
               val result : Int?= null) :Serializable{


    companion object {
        private val serialVersionUID = 8711368828010083044L

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

        fun toFileInfo(json : String) : FileInfo?{
            var fileInfo : FileInfo? = null
            try {
                val jsonObject = JSONObject(json)
                val path : String = jsonObject.getString("path")
                val size = jsonObject.getLong("size")
                val type = jsonObject.getInt("type")
                val name = jsonObject.getString("name")

                fileInfo = FileInfo(path, type, size, name)
            }catch (e : JSONException){
                e.printStackTrace()
            }
            return fileInfo
        }
    }

    fun toJsonString() : String {
        val jsonObject = JSONObject()
        jsonObject.put("path", path)
        jsonObject.put("type", type)
        jsonObject.put("size", size)
        jsonObject.put("name", name)
        return jsonObject.toString().trim()
    }

    override fun toString(): String {
        return "FileInfo(path='$path', type=$type, size=$size, name='$name', extra=$extra, procceed=$procceed, result=$result)"
    }
}