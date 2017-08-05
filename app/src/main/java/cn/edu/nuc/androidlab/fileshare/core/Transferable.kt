package cn.edu.nuc.androidlab.fileshare.core

/**
 * Created by MurphySL on 2017/8/4.
 */
interface Transferable{

    fun parseHeader()

    fun parseBody()

    fun destroy()
}