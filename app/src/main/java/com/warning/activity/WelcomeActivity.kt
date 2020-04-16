package com.warning.activity

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.annotation.NonNull
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import com.warning.R
import com.warning.common.CONST
import com.warning.common.PgyApplication
import com.warning.util.AuthorityUtil
import com.warning.util.OkHttpUtil
import com.warning.util.StatisticUtil
import kotlinx.android.synthetic.main.dialog_delete.*
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * 闪屏界面
 */
class WelcomeActivity : BaseActivity() {
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_welcome)
		okHttpTheme()
	}

	/**
	 * 申请多个权限
	 */
	private fun checkMultiAuthority() {
		if (Build.VERSION.SDK_INT < 23) {
			if (!TextUtils.isEmpty(TOKEN) && !TextUtils.isEmpty(UID)) {
				okHttpRefreshToken()
			}else {
				Handler().postDelayed({
						startActivity(Intent(this, ShawnMainActivity::class.java))
				finish()
				}, 1500)
			}
		}else {
			AuthorityUtil.deniedList.clear()
			for (i in 0 until AuthorityUtil.allPermissions.size) {
				if (ContextCompat.checkSelfPermission(this, AuthorityUtil.allPermissions[i]) != PackageManager.PERMISSION_GRANTED) {
					AuthorityUtil.deniedList.add(AuthorityUtil.allPermissions[i])
				}
			}
			if (AuthorityUtil.deniedList.isEmpty()) {//所有权限都授予
				if (!TextUtils.isEmpty(TOKEN) && !TextUtils.isEmpty(UID)) {
					okHttpRefreshToken()
				}else {
					Handler().postDelayed({
						startActivity(Intent(this, ShawnMainActivity::class.java))
						finish()
					}, 1500)
				}
			}else {
				val permissions = AuthorityUtil.deniedList.toTypedArray()//将list转成数组
				ActivityCompat.requestPermissions(this, permissions, AuthorityUtil.AUTHOR_MULTI)
			}
		}
	}

	override fun onRequestPermissionsResult(requestCode: Int, @NonNull permissions: Array<String>, @NonNull grantResults: IntArray) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		when (requestCode) {
			AuthorityUtil.AUTHOR_MULTI -> if (!TextUtils.isEmpty(TOKEN) && !TextUtils.isEmpty(UID)) {
				okHttpRefreshToken()
			} else {
				startActivity(Intent(this, ShawnMainActivity::class.java))
				finish()
			}
		}
	}

	/**
	 * 刷新token
	 */
	private fun okHttpRefreshToken() {
		Thread(Runnable {
			val url = "http://new.12379.tianqi.cn/Work/Refreshtoken"
			val builder = FormBody.Builder()
			builder.add("token", TOKEN)
			builder.add("uid", UID)
			val body = builder.build()
			OkHttpUtil.enqueue(Request.Builder().post(body).url(url).build(), object : Callback {
				override fun onFailure(call: Call, e: IOException) {
					runOnUiThread {
						promptDialog("获取用户信息失败", "登录失败")
					}
				}
				override fun onResponse(call: Call, response: Response) {
					if (!response.isSuccessful) {
						return
					}
					val result = response.body!!.string()
					runOnUiThread {
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
												downloadPortrait(PHOTO)
											}
										}

										saveUserInfo()
										StatisticUtil.OkhttpPushToken(PgyApplication.pushToken)
										startActivity(Intent(this@WelcomeActivity, ShawnMainActivity::class.java))
										finish()
									}
								}else {//失败
									if (!obje.isNull("msg")) {
										val msg = obje.getString("msg")
										promptDialog("获取用户信息失败", msg)
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
	 * 下载头像保存在本地
	 */
	private fun downloadPortrait(imgUrl : String) {
		Thread(Runnable {
			OkHttpUtil.enqueue(Request.Builder().url(imgUrl).build(), object : Callback {
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
	
	/**
	 * 刷新token失败后提示
	 * @param message 标题
	 * @param content 内容
	 */
	private fun promptDialog(message : String, content : String) {
		val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
		val view = inflater.inflate(R.layout.dialog_delete, null)
		val dialog = Dialog(this, R.style.CustomProgressDialog)
		dialog.setCancelable(false)
		dialog.setCanceledOnTouchOutside(false)
		dialog.setContentView(view)
		dialog.show()
		
		tvMessage.text = message
		tvContent.text = content
		tvContent.visibility = View.VISIBLE
		llNegative.setOnClickListener {
			dialog.dismiss()
			clearUserInfo()
			val file = File(CONST.PORTRAIT_ADDR)
			if (file.exists()) {
				file.delete()
			}
			startActivity(Intent(this, ShawnMainActivity::class.java))
			finish()
		}
		llPositive.setOnClickListener {
			dialog.dismiss()
			startActivityForResult(Intent(this, LoginActivity::class.java), 1001)
		}
	}
	
	override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return true
		}
		return super.onKeyDown(keyCode, event)
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		if (resultCode == RESULT_OK) {
			when(requestCode) {
				1001 -> {
					startActivity(Intent(this, ShawnMainActivity::class.java))
					finish()
				}
			}
		}
	}

	/**
	 * 获取主题
	 */
	private fun okHttpTheme() {
		val url = "https://decision-admin.tianqi.cn/Home/work2019/warning_theme_flag"
		Thread(Runnable {
			OkHttpUtil.enqueue(Request.Builder().url(url).build(), object : Callback {
				override fun onFailure(call: Call, e: IOException) {}
				@Throws(IOException::class)
				override fun onResponse(call: Call, response: Response) {
					if (!response.isSuccessful) {
						return
					}
					val result = response.body!!.string()
					if (!TextUtils.isEmpty(result)) {
						try {
							val obj = JSONObject(result)
							if (!obj.isNull("flag")) {
								PgyApplication.setTheme(obj.getString("flag"))
							}
						} catch (e: JSONException) {
							e.printStackTrace()
						}
					}
				}
			})
		}).start()


		checkMultiAuthority()
	}

}
