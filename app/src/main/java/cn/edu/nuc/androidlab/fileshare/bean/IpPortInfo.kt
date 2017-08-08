package cn.edu.nuc.androidlab.fileshare.bean

import java.io.Serializable
import java.net.InetAddress

/**
 * Created by MurphySL on 2017/8/8.
 */
class IpPortInfo(val inetAddress : InetAddress, val port : Int) : Serializable{

    companion object {
        private val serialVersionUID = 8711368828010083044L
    }
}