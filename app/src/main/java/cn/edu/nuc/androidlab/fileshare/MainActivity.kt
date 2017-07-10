package cn.edu.nuc.androidlab.fileshare

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.widget.Button
import cn.edu.nuc.androidlab.fileshare.ui.activity.ChooseFileActivity
import cn.edu.nuc.androidlab.fileshare.util.FileUtil

class MainActivity : AppCompatActivity() {
    private val TAG : String = this.javaClass.simpleName

    private lateinit var bt_send : Button
    private lateinit var bt_receive : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initView()

        initData()
    }

    private fun initView() {
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        val fab = findViewById(R.id.fab) as FloatingActionButton
        fab.setOnClickListener({ view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        })

        bt_send = findViewById(R.id.bt_send) as Button
        bt_send.setOnClickListener {
            startActivity(Intent(MainActivity@this, ChooseFileActivity::class.java))
        }
        bt_receive = findViewById(R.id.bt_receive) as Button
    }

    private fun initData() {
        Thread(Runnable {
            FileUtil.getSpecificTypeFiles(this, arrayOf(FileUtil.MP3))
        }).run()

    }

}
