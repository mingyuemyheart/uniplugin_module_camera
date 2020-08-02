package com.warning.activity

import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.View.OnClickListener
import com.warning.R
import com.warning.adapter.PdfAdapter
import com.warning.common.CONST
import com.warning.dto.NewsDto
import com.warning.manager.DBManager
import com.warning.util.CommonUtil
import com.warning.util.OkHttpUtil
import kotlinx.android.synthetic.main.activity_warning_detail.*
import kotlinx.android.synthetic.main.shawn_layout_title.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.*

/**
 * 预警详情
 */
class WarningDetailActivity : BaseActivity(), OnClickListener {
	
	private var html : String? = null
	private var mAdapter : PdfAdapter? = null
	private val pdfList : ArrayList<NewsDto> = ArrayList()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_warning_detail)
		initRefreshLayout()
		initWidget()
		initListView()
	}
	
	/**
	 * 初始化下拉刷新布局
	 */
	private fun initRefreshLayout() {
		refreshLayout.setColorSchemeResources(CONST.color1, CONST.color2, CONST.color3, CONST.color4)
		refreshLayout.setProgressViewEndTarget(true, 300)
		refreshLayout.isRefreshing = true
		refreshLayout.setOnRefreshListener {
			refresh()
		}
	}
	
	/**
	 * 初始化控件
	 */
	private fun initWidget() {
		llBack.setOnClickListener(this)
		tvTitle.text = getString(R.string.warning_detail)
		ivShare.setOnClickListener(this)
		ivShare.visibility = View.GONE

		if (intent.hasExtra("url")) {
			html = intent.getStringExtra("url")
			refresh()
		}
	}

	private fun initListView() {
		mAdapter = PdfAdapter(this, pdfList)
		listView.adapter = mAdapter
		listView.setOnItemClickListener { adapterView, view, i, l ->
			val data = pdfList[i]
			val intent : Intent?
			if (TextUtils.equals(data.show_type, "pdf")) {
				intent = Intent(this, ShawnPDFActivity::class.java)
				intent.putExtra(CONST.ACTIVITY_NAME, data.title)
				intent.putExtra(CONST.WEB_URL, data.url)
			} else {
				intent = Intent(this, WebviewActivity::class.java)
			}
			val bundle = Bundle()
			bundle.putParcelable("data", data)
			intent.putExtras(bundle)
			startActivity(intent)
		}
	}
	
	private fun refresh() {
		if (!TextUtils.isEmpty(html)) {
			okhttpWarningDetail(html)
		}
	}
	
	/**
	 * 初始化数据库
	 */
	private fun queryWarningGuide() {
		if (TextUtils.isEmpty(html)) {
			return
		}
		val array = html!!.split("-")
		val item2 = array[2]
		val type = item2.substring(0, 5)
		val color = item2.substring(5, 7)
		
		val dbManager = DBManager(this)
		dbManager.openDateBase()
		dbManager.closeDatabase()
		val database = SQLiteDatabase.openOrCreateDatabase(DBManager.DB_PATH + "/" + DBManager.DB_NAME, null)
		val cursor = database.rawQuery("select * from " + DBManager.TABLE_NAME2 + " where WarningId = " + "\"" + type+color + "\"",null)
		var content : String? = null
		for (i in 0 until cursor.count) {
			cursor.moveToPosition(i)
			content = cursor.getString(cursor.getColumnIndex("WarningGuide"))
		}
		if (!TextUtils.isEmpty(content)) {
			tvGuide.text = getString(R.string.warning_guide)+content
			tvGuide.visibility = View.VISIBLE
		}else {
			tvGuide.visibility = View.GONE
		}
	}
	
	/**
	 * 获取预警详情
	 */
	private fun okhttpWarningDetail(html : String?) {
		Thread(Runnable {
			val url = "https://decision-admin.tianqi.cn/Home/work2019/getDetailWarn/identifier/$html"
			OkHttpUtil.enqueue(Request.Builder().url(url).build(), object : Callback {
				override fun onFailure(call: Call, e: IOException) {
				}
				override fun onResponse(call: Call, response: Response) {
					if (!response.isSuccessful) {
						return
					}
					val result = response.body!!.string()
					runOnUiThread {
						if (!TextUtils.isEmpty(result)) {
							val obje = JSONObject(result)
							if (!obje.isNull("sendTime")) {
								tvTime.text = obje.getString("sendTime")
							}

							if (!obje.isNull("description")) {
								tvIntro.text = obje.getString("description")
							}

							val name = obje.getString("headline")
							if (!TextUtils.isEmpty(name)) {
								tvName.text = name.replace(getString(R.string.publish), getString(R.string.publish)+"\n")
							}

							var bitmap : Bitmap? = null
							if (obje.getString("severityCode") == CONST.blue[0]) {
								bitmap = CommonUtil.getImageFromAssetsFile(this@WarningDetailActivity,"warning/"+obje.getString("eventType")+CONST.blue[1]+CONST.imageSuffix)
								if (bitmap == null) {
									bitmap = CommonUtil.getImageFromAssetsFile(this@WarningDetailActivity,"warning/"+"default"+CONST.blue[1]+CONST.imageSuffix)
								}
							}else if (obje.getString("severityCode") == CONST.yellow[0]) {
								bitmap = CommonUtil.getImageFromAssetsFile(this@WarningDetailActivity,"warning/"+obje.getString("eventType")+CONST.yellow[1]+CONST.imageSuffix)
								if (bitmap == null) {
									bitmap = CommonUtil.getImageFromAssetsFile(this@WarningDetailActivity,"warning/"+"default"+CONST.yellow[1]+CONST.imageSuffix)
								}
							}else if (obje.getString("severityCode") == CONST.orange[0]) {
								bitmap = CommonUtil.getImageFromAssetsFile(this@WarningDetailActivity,"warning/"+obje.getString("eventType")+CONST.orange[1]+CONST.imageSuffix)
								if (bitmap == null) {
									bitmap = CommonUtil.getImageFromAssetsFile(this@WarningDetailActivity,"warning/"+"default"+CONST.orange[1]+CONST.imageSuffix)
								}
							}else if (obje.getString("severityCode") == CONST.red[0]) {
								bitmap = CommonUtil.getImageFromAssetsFile(this@WarningDetailActivity,"warning/"+obje.getString("eventType")+CONST.red[1]+CONST.imageSuffix)
								if (bitmap == null) {
									bitmap = CommonUtil.getImageFromAssetsFile(this@WarningDetailActivity,"warning/"+"default"+CONST.red[1]+CONST.imageSuffix)
								}
							}else if (obje.getString("severityCode") == CONST.unknown[0]) {
								bitmap = CommonUtil.getImageFromAssetsFile(this@WarningDetailActivity,"warning/default"+CONST.imageSuffix)
							}
							imageView.setImageBitmap(bitmap)

							if (!TextUtils.isEmpty(tvIntro.text.toString()) && !tvIntro.text.toString().contains("防御指南")) {
								queryWarningGuide()
							}

							scrollView.visibility = View.VISIBLE
							ivShare.visibility = View.VISIBLE
							refreshLayout.isRefreshing = false

							if (!obje.isNull("identifier")) {
								okHttpPdf(obje.getString("identifier"))
							}
						}
					}
				}
			})
		}).start()
	}

	private fun okHttpPdf(identifier : String) {
		Thread(Runnable {
			val url = "http://warn-wx.tianqi.cn/Test/getWarnAnnex?identifier=$identifier"
			OkHttpUtil.enqueue(Request.Builder().url(url).build(), object : Callback {
				override fun onFailure(call: Call, e: IOException) {
				}
				override fun onResponse(call: Call, response: Response) {
					if (!response.isSuccessful) {
						return
					}
					val result = response.body!!.string()
					runOnUiThread {
						if (!TextUtils.isEmpty(result)) {
							val obj = JSONObject(result)
							if (!obj.isNull("data")) {
								pdfList.clear()
								val array = obj.getJSONArray("data")
								for (i in 0 until array.length()) {
									val itemObj = array.getJSONObject(i)
									val dto = NewsDto()
									if (!itemObj.isNull("title")) {
										dto.title = itemObj.getString("title")
									}
									if (!itemObj.isNull("url")) {
										dto.url = itemObj.getString("url")
									}
									if (!itemObj.isNull("type")) {
										dto.show_type = itemObj.getString("type")
									}
									pdfList.add(dto)
								}
								if (mAdapter != null) {
									mAdapter!!.notifyDataSetChanged()
								}
								tvPdf.visibility = View.VISIBLE
							}
						}
					}
				}
			})
		}).start()
	}

	override fun onClick(p0: View?) {
		when(p0!!.id) {
			R.id.llBack -> finish()
			R.id.ivShare -> {
				val bitmap1 = CommonUtil.captureView(scrollView)
				val bitmap2 = BitmapFactory.decodeResource(resources, R.drawable.iv_share_bottom)
				val bitmap = CommonUtil.mergeBitmap(this, bitmap1, bitmap2, false)
				CommonUtil.clearBitmap(bitmap1)
				CommonUtil.clearBitmap(bitmap2)
				CommonUtil.share(this, bitmap)
			}
		}
	}

}
