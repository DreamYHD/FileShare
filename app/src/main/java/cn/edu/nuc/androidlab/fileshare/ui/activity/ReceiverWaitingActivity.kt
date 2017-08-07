package cn.edu.nuc.androidlab.fileshare.ui.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import cn.edu.nuc.androidlab.fileshare.R
import cn.edu.nuc.androidlab.fileshare.util.ApUtil
import kotlinx.android.synthetic.main.activity_receiver_waiting.*

/**
 * Created by MurphySL on 2017/8/7.
 */
class ReceiverWaitingActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receiver_waiting)

        init()
    }

    private fun init() {
        //val rxPremissions = RxPermissions(this)

        ApUtil.configApState(this)

        if(ApUtil.isApEnabled(this)){
            status.text = "创建成功！"
        }
    }
}