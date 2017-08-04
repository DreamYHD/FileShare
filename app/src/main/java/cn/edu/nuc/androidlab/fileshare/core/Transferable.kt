package cn.edu.nuc.androidlab.fileshare.core

/**
 * Created by MurphySL on 2017/8/4.
 */
interface Transferable{

    companion object {
        // 头部分割字符
        val separator : String = "::"

        val BYTE_SIZE_HEADER = 1024 * 10
        val BYTE_SIZE_SCREENSHOT = 1024 * 40
        val BYTE_SIZE_DATA = 1024 * 4

        val TYPE_FILE : Int = 0x01
        val TYPE_MSG : Int = 0x02

        //传输字节类型
        val UTF_8 : String = "UTF-8"
    }

    fun init()

    fun parseHeader()

    fun parseBody()

    fun destory()
}