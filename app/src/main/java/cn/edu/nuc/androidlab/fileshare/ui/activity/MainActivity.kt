package cn.edu.nuc.androidlab.fileshare.ui.activity

import android.content.Intent
import android.os.Bundle
import android.service.chooser.ChooserTargetService
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.widget.Button
import cn.edu.nuc.androidlab.fileshare.R
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity() {
    private val TAG : String = this.javaClass.simpleName

    private lateinit var bt_send : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initView()
    }

    private fun initView() {
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        bt_send = findViewById(R.id.bt_send) as Button
        bt_send.setOnClickListener {
            startActivity(Intent(MainActivity@this, ChooseFileActivity::class.java))
        }

        bt_receive.setOnClickListener {
            startActivity(Intent(MainActivity@this, ReceiverWaitingActivity::class.java))
        }

        whiteboard.setOnClickListener {
            startActivity(Intent(MainActivity@this, ChooseReceiverActivity::class.java))
        }

        client_whiteboard.setOnClickListener {
            startActivity(Intent(MainActivity@this, ChooseReceiverActivity::class.java))
        }
    }
}
