package com.warning.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.annotation.RequiresApi
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.view.View
import android.view.View.OnClickListener
import android.widget.Toast
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode
import com.warning.R
import com.warning.common.CONST
import com.warning.common.PgyApplication
import com.warning.util.AuthorityUtil
import com.warning.util.CommonUtil
import com.warning.util.OkHttpUtil
import com.warning.util.StatisticUtil
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_login.reTitle
import kotlinx.android.synthetic.main.shawn_layout_title.*
import okhttp3.*
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

/**
 * 登录界面
 */
class LoginActivity : BaseActivity(), OnClickListener {
	
	private var seconds = 60
	private var timer : Timer? = null
	private var lat : Double = 0.0
	private var lng : Double = 0.0

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_login)
		initWidget()
	}

	/**
	 * 初始化控件
	 */
	private fun initWidget() {
		reTitle.setBackgroundColor(Color.TRANSPARENT)
		tvTitle.text = "登录"
		tvLogin.setOnClickListener(this)
		llBack.setOnClickListener(this)
		tvSend.setOnClickListener(this)

		startLocation()
	}

	/**
	 * 开始定位
	 */
	private fun startLocation() {
		val mLocationOption = AMapLocationClientOption()//初始化定位参数
		val mLocationClient = AMapLocationClient(this)//初始化定位
		mLocationOption.locationMode = AMapLocationMode.Hight_Accuracy//设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
		mLocationOption.isNeedAddress = true//设置是否返回地址信息（默认返回地址信息）
		mLocationOption.isOnceLocation = true//设置是否只定位一次,默认为false
		mLocationOption.isMockEnable = false//设置是否允许模拟位置,默认为false，不允许模拟位置
		mLocationOption.interval = 2000//设置定位间隔,单位毫秒,默认为2000ms
		mLocationClient.setLocationOption(mLocationOption)//给定位客户端对象设置定位参数
		mLocationClient.setLocationListener { aMapLocation ->
			if (aMapLocation != null && aMapLocation.errorCode == 0) {
				lat = aMapLocation.latitude
				lng = aMapLocation.longitude
			}
		}
		mLocationClient.startLocation()//启动定位
	}

	/**
	 * 获取验证码
	 */
	private fun okHttpCode() {
		val url = "http://new.12379.tianqi.cn/Work/LoginSendcode"
		val builder = FormBody.Builder()
		builder.add("phonenumber", etUserName.text.toString().trim())
		val body = builder.build()
		Thread(Runnable {
			OkHttpUtil.enqueue(Request.Builder().post(body).url(url).build(), object : Callback {
				override fun onFailure(call: Call, e: IOException) {
					runOnUiThread {
						resetTimer()
						Toast.makeText(this@LoginActivity, "登录失败，重新登录试试", Toast.LENGTH_SHORT).show()
					}
				}
				override fun onResponse(call: Call, response: Response) {
					if (!response.isSuccessful) {
						return
					}
					val result = response.body!!.string()
					runOnUiThread {
						if (!TextUtils.isEmpty(result)) {
							val obj = JSONObject(result)
							if (!obj.isNull("status")) {
								val status  = obj.getInt("status")
								if (status == 1) {//成功发送验证码
									//发送验证码成功
									etPwd.isFocusable = true
									etPwd.isFocusableInTouchMode = true
									etPwd.requestFocus()
								}else {//发送验证码失败
									if (!obj.isNull("msg")) {
										resetTimer()
										Toast.makeText(this@LoginActivity, obj.getString("msg"), Toast.LENGTH_SHORT).show()
									}
								}
							}
						}
					}
				}
			})
		}).start()
	}

	@SuppressLint("HandlerLeak")
	private val handler : Handler = object : Handler() {
		override fun handleMessage(msg: Message?) {
			super.handleMessage(msg)
			when(msg!!.what) {
				101 -> {
					if (seconds <= 0) {
						resetTimer()
					} else {
						tvSend.text = seconds--.toString() + "s"
					}
				}
			}
		}
	}

	/**
	 * 验证登录信息
	 */
	private fun checkInfo() : Boolean {
		if (TextUtils.isEmpty(etUserName.text.toString())) {
			Toast.makeText(this, "请输入手机号码", Toast.LENGTH_SHORT).show()
			return false
		}
		if (TextUtils.isEmpty(etPwd.text.toString())) {
			Toast.makeText(this, "请输入手机验证码", Toast.LENGTH_SHORT).show()
			return false
		}
		return true
	}
	
	/**
	 * 登录接口
	 */
	private fun okhttpLogin() {
		val url = "http://new.12379.tianqi.cn/Work/Login"
		val builder = FormBody.Builder()
		builder.add("phonenumber", etUserName.text.toString().trim())
		builder.add("vcode", etPwd.text.toString().trim())
		builder.add("lat", lat.toString())
		builder.add("lon", lng.toString())
		builder.add("platform_type", "android")
		builder.add("os_version", Build.VERSION.RELEASE)
		builder.add("software_version", CommonUtil.getVersion(this))
		builder.add("mobile_type", Build.MODEL)
		val body = builder.build()
		Thread(Runnable {
			OkHttpUtil.enqueue(Request.Builder().post(body).url(url).build(), object : Callback {
				override fun onFailure(call: Call, e: IOException) {
					runOnUiThread {
						Toast.makeText(this@LoginActivity, "登录失败", Toast.LENGTH_SHORT).show()
					}
				}
				override fun onResponse(call: Call, response: Response) {
					if (!response.isSuccessful) {
						return
					}
					val result = response.body!!.string()
					runOnUiThread {
						cancelDialog()
						if (!TextUtils.isEmpty(result)) {
							val obje = JSONObject(result)
							if (!obje.isNull("status")) {
								val status = obje.getInt("status")
								if (status == 1) {//成功
									if (!obje.isNull("token")) {
										TOKEN = obje.getString("token")
									}
									if (!obje.isNull("info")) {
										val obj = JSONObject(obje.getString("info"))
										if (!obj.isNull("uid")) {
											UID = obj.getString("uid")
										}
										if (!obj.isNull("phonenumber")) {
											PHONENUMBER = obj.getString("phonenumber")
										}
										if (!obj.isNull("isadmin")) {
											ISINFOER = obj.getString("isadmin")
										}
										if (!obj.isNull("status")) {
											ISCHEKER = obj.getString("status")
										}
										if (!obj.isNull("nickname")) {
											NICKNAME = obj.getString("nickname")
										}
										if (!obj.isNull("realname")) {
											REALNAME = obj.getString("realname")
										}
										if (!obj.isNull("work")) {
											UNIT = obj.getString("work")
										}
										if (!obj.isNull("email")) {
											MAIL = obj.getString("email")
										}
										if (!obj.isNull("points")) {
											POINTS = obj.getString("points")
										}
										if (!obj.isNull("areas")) {
											AREAS = obj.getString("areas")
										}
										if (!obj.isNull("picture")) {
											PHOTO = obj.getString("picture")
											if (!TextUtils.isEmpty(PHOTO)) {
												checkAuthority()
											}
										}

										saveUserInfo()
										StatisticUtil.OkhttpPushToken(PgyApplication.pushToken)
										resetTimer()
										setResult(RESULT_OK)
										finish()
									}
								}else {
									//失败
									if (!obje.isNull("msg")) {
										val msg = obje.getString("msg")
										if (msg != null) {
											runOnUiThread {
												Toast.makeText(this@LoginActivity, msg, Toast.LENGTH_SHORT).show()
											}
										}
									}
								}
							}
						}
					}
				}
			})
		}).start()
	}
	
	/**
	 * 重置计时器
	 */
	private fun resetTimer() {
		if (timer != null) {
			timer!!.cancel()
			timer = null
		}
		seconds = 60
		tvSend.text = "获取验证码"
	}

	override fun onDestroy() {
		super.onDestroy()
		resetTimer()
	}

	override fun onClick(p0: View?) {
		when(p0!!.id) {
			R.id.llBack -> finish()
			R.id.tvSend -> {
				if (timer == null) {
					if (TextUtils.isEmpty(etUserName.text.toString())) {
						Toast.makeText(this, "请输入手机号码", Toast.LENGTH_SHORT).show()
						return
					}
					timer = Timer()
					timer!!.schedule(object : TimerTask() {
						override fun run() {
							handler.sendEmptyMessage(101)
						}
					}, 0, 1000)
					okHttpCode()
				}
			}
			R.id.tvLogin -> {
				if (checkInfo()) {
					showDialog()
					okhttpLogin()
				}
			}
		}
	}

	//需要申请的所有权限
	private val allPermissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

	//拒绝的权限集合
	private val deniedList = ArrayList<String>()
	/**
	 * 申请定位权限
	 */
	private fun checkAuthority() {
		if (Build.VERSION.SDK_INT < 23) {
			downloadPortrait()
		}else {
			deniedList.clear()
			for (i in 0 until allPermissions.size) {
				if (ContextCompat.checkSelfPermission(this, allPermissions[i]) != PackageManager.PERMISSION_GRANTED) {
					deniedList.add(allPermissions[i])
				}
			}
			if (deniedList.isEmpty()) {//所有权限都授予
				downloadPortrait()
			}else {
				val permissions = deniedList.toTypedArray()
				requestPermissions(permissions, AuthorityUtil.AUTHOR_LOCATION)
			}
		}
	}

	@RequiresApi(Build.VERSION_CODES.M)
	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		when(requestCode) {
			AuthorityUtil.AUTHOR_LOCATION -> {
				if (grantResults.isNotEmpty()) {
					var isAllGranted = true//是否全部授权
					for (i in 0 until grantResults.size) {
						if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
							isAllGranted = false
							break
						}
					}
					if (isAllGranted) {//所有权限都授予
						startLocation()
					}else {//只要有一个没有授权，就提示进入设置界面设置
						AuthorityUtil.intentAuthorSetting(this, "\""+getString(R.string.app_name)+"\""+"需要使用您的存储权限，是否前往设置？")
					}
				}else {
					for (i in 0 until permissions.size) {
						if (!this.shouldShowRequestPermissionRationale(permissions[i])) {
							AuthorityUtil.intentAuthorSetting(this, "\""+getString(R.string.app_name)+"\""+"需要使用您的存储权限，是否前往设置？")
							break
						}
					}
				}
			}
		}
	}

	/**
	 * 下载头像保存在本地
	 */
	private fun downloadPortrait() {
		Thread(Runnable {
			OkHttpUtil.enqueue(Request.Builder().url(PHOTO).build(), object : Callback {
				override fun onFailure(call: Call, e: IOException) {
				}
				override fun onResponse(call: Call, response: Response) {
					if (!response.isSuccessful) {
						return
					}
					val bytes = response.body!!.bytes()
					runOnUiThread {
						val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
						val files = File(CONST.SDCARD_PATH)
						if (!files.exists()) {
							files.mkdirs()
						}
						val fos = FileOutputStream(CONST.PORTRAIT_ADDR)
						if (bitmap != null) {
							bitmap.compress(CompressFormat.PNG, 100, fos)
							if (!bitmap.isRecycled) {
								bitmap.recycle()
							}
						}
					}
				}
			})
		}).start()
	}
	
}
