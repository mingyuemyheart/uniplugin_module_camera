package com.warning.activity

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.View.OnClickListener
import com.warning.R
import com.warning.adapter.YiqingAdapter
import com.warning.adapter.YiqingPercentAdapter
import com.warning.dto.YiqingDto
import com.warning.util.OkHttpUtil
import kotlinx.android.synthetic.main.activity_yiqing_list.*
import kotlinx.android.synthetic.main.shawn_layout_title.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

/**
 * 疫情列表
 * @author shawn_sun
 */
class YiqingPercentActivity : BaseActivity(), OnClickListener {

	var mAdapter : YiqingPercentAdapter? = null
	val dataList : ArrayList<YiqingDto> = ArrayList()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_yiqing_percent)
		initWidget()
		initListView()
	}
	
	/**
	 * 初始化控件
	 */
	private fun initWidget() {
		llBack.setOnClickListener(this)
		tvTitle.text = "疫情比例"

		okhttpYiqing()
    }

	/**
	 * 初始化listview
	 */
	private fun initListView() {
		mAdapter = YiqingPercentAdapter(this, dataList)
		listView.adapter = mAdapter
	}

	private fun okhttpYiqing() {
		Thread(Runnable {
			val url = "http://warn-wx.tianqi.cn/Test/getworldqybl"
			OkHttpUtil.enqueue(Request.Builder().url(url).build(), object : Callback {
				override fun onFailure(call: Call, e: IOException) {}
				@Throws(IOException::class)
				override fun onResponse(call: Call, response: Response) {
					if (!response.isSuccessful) {
						return
					}
					val result = response.body!!.string()
					runOnUiThread(Runnable {
						if (!TextUtils.isEmpty(result)) {
							try {
								dataList.clear()
								val array = JSONArray(result)
								for (i in 0 until array.length()) {
									val dto = YiqingDto()
									val itemObj = array.getJSONObject(i)
									dto.count = "0"
									if (!itemObj.isNull("count")) {
										val count = itemObj.getString("count")
										if (TextUtils.equals(count, "None")) {
											dto.count = "0"
										} else {
											dto.count = count
										}
									}
									dto.death_count = "0"
									if (!itemObj.isNull("death_count")) {
										val death_count = itemObj.getString("death_count")
										if (TextUtils.equals(death_count, "None")) {
											dto.death_count = "0"
										} else {
											dto.death_count = death_count
										}
									}
									if (!itemObj.isNull("ratio")) {
										dto.ratio = itemObj.getString("ratio")
									}
									if (!itemObj.isNull("nameZn")) {
										dto.nameZn = itemObj.getString("nameZn")
									}
									if (!itemObj.isNull("infection_mortality")) {
										dto.infection_mortality = itemObj.getString("infection_mortality")
									}
									if (!itemObj.isNull("population")) {
										dto.population = itemObj.getString("population")
									}
									dataList.add(dto)
								}
								if (mAdapter != null) {
									mAdapter!!.notifyDataSetChanged()
								}

							} catch (e: JSONException) {
								e.printStackTrace()
							}
						}
					})
				}
			})
		}).start()
	}

	override fun onClick(v: View?) {
		when(v!!.id) {
			R.id.llBack -> finish()
		}
	}

}
