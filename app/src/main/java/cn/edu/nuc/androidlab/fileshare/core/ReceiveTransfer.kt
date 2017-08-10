package cn.edu.nuc.androidlab.fileshare.core

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import cn.edu.nuc.androidlab.fileshare.bean.FileInfo
import cn.edu.nuc.androidlab.fileshare.util.FileUtil
import java.io.FileOutputStream
import java.io.InputStream
import java.net.Socket

/**
 * Created by MurphySL on 2017/8/9.
 */
class ReceiveTransfer(val context : Context, val socket: Socket) : BaseTransfer(), Runnable{
    private val TAG = this.javaClass.simpleName

    private val ins : InputStream = socket.getInputStream()
    private var onReceiveListener : OnReceiveListener? = null

    private var fileInfo : FileInfo? = null

    fun setOnReceiveListener(onReceiveListener: OnReceiveListener){
        this.onReceiveListener = onReceiveListener
    }

    override fun parseHeader() {
        val header = ByteArray(BYTE_SIZE_HEADER)
        var total = 0
        var len = -1

        // HEADER
        len = ins.read()
        while (len != -1){
            header[total] = len.toByte()
            total ++
            if(total == header.size){
                break
            }
            len = ins.read()
        }
        Log.i(TAG, "header size : $total")
        Log.i(TAG, "header : ${String(header, Charsets.UTF_8)}")

        //thumbnail
        val thumbnail = ByteArray(BYTE_SIZE_THUMBNAIL)
        total = 0
        len = -1
        len = ins.read()
        while(len != -1){
            thumbnail[total] = len.toByte()
            total ++
            if(total == thumbnail.size){
                break
            }
            len = ins.read()
        }
        Log.i(TAG, "thumbnail size : $total")

        val bitmap : Bitmap? = BitmapFactory.decodeByteArray(thumbnail, 0, thumbnail.size)
        bitmap?.let {
            onReceiveListener?.onGetThumbnail(bitmap)
        }

        val json = String(header, Charsets.UTF_8)
        Log.i(TAG, json)
        val fileInfoJson = json.substringAfter(separator)
        Log.i(TAG, fileInfoJson)
        fileInfo = FileInfo.toFileInfo(fileInfoJson)
        fileInfo?.let{
            onReceiveListener?.onGetFileInfo(it)
        }
    }

    override fun parseBody() {
        fileInfo?.let {
            Log.i(TAG, "parseBody")
            val fileSize = it.size
            val bos = FileOutputStream(FileUtil.createLocalFile(context, it.path))
            val startTime = System.currentTimeMillis()

            val bytes = ByteArray(BYTE_SIZE_DATA)
            var total = 0L
            var len = 0
            var currentTime = startTime

            len = ins.read(bytes)
            while(len != -1){
                bos.write(bytes, 0, len)
                total += len
                currentTime = System.currentTimeMillis()
                if(currentTime - startTime > 200){
                    currentTime = startTime
                    onReceiveListener?.onProgress(total, fileSize)
                }
                len = ins.read(bytes)
            }
            Log.i(TAG, "parse body end")

            val endTime = System.currentTimeMillis()
            onReceiveListener?.onSuccess(it)
        }
    }

    override fun destroy() {
        ins.close()
        if(socket.isConnected){
            socket.close()
        }
    }

    override fun run() {
        onReceiveListener?.let {
            it.onStart()

            try {
                parseHeader()

            }catch (e : Exception){
                e.printStackTrace()
                it.onFailure(e, null)
            }

            try {
                parseBody()
                destroy()
            }catch (e : Exception){
                e.printStackTrace()
            }
        }

    }


    interface OnReceiveListener{
        fun onStart()
        fun onGetFileInfo(fileInfo : FileInfo)
        fun onGetThumbnail(bitmap : Bitmap)
        fun onProgress(progress : Long, total : Long)
        fun onSuccess(fileInfo : FileInfo)
        fun onFailure(t : Throwable, fileInfo: FileInfo?)
    }

}