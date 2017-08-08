package cn.edu.nuc.androidlab.fileshare.core

import android.content.Context
import cn.edu.nuc.androidlab.fileshare.bean.FileInfo
import cn.edu.nuc.androidlab.fileshare.util.FileUtil
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.net.Socket

/**
 * Created by MurphySL on 2017/8/5.
 */
class SendTransfer(val context : Context,
                   val fileInfo : FileInfo,
                   serverIpAddress : String,
                   port : Int) : BaseTransfer(), Runnable{

    private val socket : Socket = Socket(serverIpAddress, port)
    private val bos : BufferedOutputStream = BufferedOutputStream(socket.getOutputStream())
    private var onSendListener : OnSendListener? = null

    override fun run() {
        try {
            onSendListener?.onStart()
            parseHeader()
            parseBody()
            destroy()
        }catch (e : Exception){
            onSendListener?.onFailure(e, fileInfo)
        }

    }

    fun setOnSendListener(onSendListener : OnSendListener){
        this.onSendListener = onSendListener
    }

    override fun parseHeader() {
        // 基本信息
        val headerSb : StringBuilder = StringBuilder()
        val jsonStr : String = "$TYPE_FILE$separator${fileInfo.toJsonString()}"
        headerSb.append(jsonStr)
        for(i in 0..(BYTE_SIZE_HEADER-jsonStr.toByteArray(Charsets.UTF_8).size)){
            headerSb.append(" ")
        }

        bos.write(headerSb.toString().toByteArray(Charsets.UTF_8))

        // 缩略图
        val thumbnail : StringBuilder = StringBuilder()
        var leftLen = 0
        fileInfo.bitmap?.let {
            val bytes = FileUtil.bitmapToByteArray(it)
            bos.write(bytes)
            leftLen = bytes.size
        }
        for (i in 0..(BYTE_SIZE_THUMBNAIL - leftLen)){
            thumbnail.append(" ")
        }
        bos.write(thumbnail.toString().toByteArray(Charsets.UTF_8))

    }

    override fun parseBody() {
        val file_size = fileInfo.size
        val fis = FileInputStream(File(fileInfo.path))

        var startTime = System.currentTimeMillis()
        var endTime = 0L
        var total = 0L
        var len = 0
        val bytes = ByteArray(BYTE_SIZE_DATA)

        len = fis.read(bytes)
        while(len != -1){
            bos.write(bytes, 0, len)
            total += len
            endTime = System.currentTimeMillis()
            if(endTime - startTime > 500){
                startTime = endTime
                onSendListener?.onProgress(total, file_size)
            }
            len = fis.read(bytes)
        }

        bos.flush()
        bos.close()
        onSendListener?.onSuccess(fileInfo)
    }

    override fun destroy() {
        bos.close()
        if(socket.isConnected){
            socket.close()
        }
    }

    interface OnSendListener{
        fun onStart()
        fun onProgress(progress : Long, total : Long)
        fun onSuccess(fileInfo : FileInfo)
        fun onFailure(t : Throwable, fileInfo: FileInfo)
    }

}