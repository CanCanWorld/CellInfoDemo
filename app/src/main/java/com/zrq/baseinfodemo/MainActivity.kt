package com.zrq.baseinfodemo

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.telephony.*
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.zrq.baseinfodemo.databinding.ActivityMainBinding
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val handler = MyCrashHandler()
        Thread.setDefaultUncaughtExceptionHandler(handler)

        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        ActivityCompat.requestPermissions(this, PERMISSIONS, 1)
        initEvent()
    }

    private lateinit var mBinding: ActivityMainBinding

    private fun initEvent() {
        mBinding.apply {
            btnHead.setOnClickListener {
                showInfo()
            }
            btnAuto.setOnClickListener {
                if (!isOpen) {
                    isOpen = true
                    btnAuto.text = "停止"
                    Thread {
                        while (isOpen) {
                            Thread.sleep(500)
                            runOnUiThread {
                                showInfo()
                            }
                        }
                    }.start()
                } else {
                    isOpen = false
                    btnAuto.text = "自动点!"
                }
            }
            btnClear.setOnClickListener {
                tvInfo.text = ""
            }
        }
    }

    private var isOpen = false

    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    private fun showInfo() {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val infos = listGetTowerInfo()
        for (info in infos) {
            Log.d(TAG, "info: $info")
            val text = mBinding.tvInfo.text
            mBinding.tvInfo.text = "${sdf.format(Date().time)}: $info\n$text"
        }
    }

    private fun listGetTowerInfo(): List<String> {
//        var mcc = -1
        var mnc = -1
        var lac = -1
        var cellId = -1
        var rssi = -1
        var cellInfo = ""
        val tm = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
//        val operator = tm.networkOperator
//        mcc = operator.substring(0, 3).toInt()
        val list: MutableList<String> = ArrayList()
        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val infos = tm.allCellInfo
        if (infos == null) {
            val nullList = ArrayList<String>()
            nullList.add("获取为空")
            return nullList
        }
        for (info in infos) {
            when (info) {
                //判断主流通信技术
                //电信2g
                is CellInfoCdma -> {
                    val cellIdentityCdma = info.cellIdentity
                    mnc = cellIdentityCdma.systemId
                    lac = cellIdentityCdma.networkId
                    cellId = cellIdentityCdma.basestationId
                    rssi = info.cellSignalStrength.cdmaDbm
                    cellInfo = "CDMA"
                }
                //2g
                is CellInfoGsm -> {
                    val cellIdentityGsm = info.cellIdentity
                    mnc = cellIdentityGsm.mnc
                    lac = cellIdentityGsm.lac
                    cellId = cellIdentityGsm.cid
                    rssi = info.cellSignalStrength.dbm
                    cellInfo = "GSM"
                }
                //3g-4g
                is CellInfoLte -> {
                    val cellIdentityLte = info.cellIdentity
                    mnc = cellIdentityLte.mnc
                    lac = cellIdentityLte.tac
                    cellId = cellIdentityLte.ci
                    rssi = info.cellSignalStrength.dbm
                    cellInfo = "LTE"    //流量＋wifi, 流量
                }
                //3g
                is CellInfoWcdma -> {
                    val cellIdentityWcdma = info.cellIdentity
                    mnc = cellIdentityWcdma.mnc
                    lac = cellIdentityWcdma.lac
                    cellId = cellIdentityWcdma.cid
                    rssi = info.cellSignalStrength.dbm
                    cellInfo = "WCDMA"    //wifi
                }
                else -> {
                    Log.e(TAG, "get CellInfo error ")
                }
            }
            if (mnc == Int.MAX_VALUE) {
                break
            }
            val jsonObject = JSONObject()
            try {
                jsonObject.put("cellId", cellId)
                jsonObject.put("lac", lac)
                jsonObject.put("level", rssi)
                jsonObject.put("mnc", mnc)
                jsonObject.put("cellInfo", cellInfo)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            list.add(jsonObject.toString())
        }
        return list
    }

    companion object {
        const val TAG = "MainActivity"

        val PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_PHONE_STATE
        )
    }
}