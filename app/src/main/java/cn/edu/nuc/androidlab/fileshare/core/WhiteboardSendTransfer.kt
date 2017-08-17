package cn.edu.nuc.androidlab.fileshare.core

import android.content.Context
import android.util.Log
import cn.edu.nuc.androidlab.fileshare.bean.FileInfo
import java.io.File
import java.io.FileInputStream
import java.io.OutputStream
import java.net.Socket

/**
 * Created by MurphySL on 2017/8/13.
 */
class WhiteboardSendTransfer(val context : Context,
                             val filePath : File,
                             val serverAddress : String,
                             val port : Int) : BaseTransfer() , Runnable{
    override fun run() {
        parseHeader()
        parseBody()
        destroy()
    }

    private val TAG : String = this.javaClass.simpleName

    private val socket : Socket = Socket (serverAddress, port)
    private val bos : OutputStream = socket.getOutputStream()
    private var onSendListener : OnSendListener? = null

    fun setOnSendListener(onSendListener : OnSendListener){
        this.onSendListener = onSendListener
    }

    override fun parseHeader() {

    }

    override fun parseBody() {
        Log.i(TAG, "parseBody")
        val file_size = filePath.length()
        val fis = FileInputStream(filePath)

        var startTime = System.currentTimeMillis()
        var endTime = 0L
        var total = 0L // 已传部分
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
        onSendListener?.onSuccess(filePath.absolutePath)
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
        fun onSuccess(filPath : String)
        fun onFailure(t : Throwable, fileInfo: FileInfo)
    }

}