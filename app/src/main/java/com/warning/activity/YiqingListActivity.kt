package com.warning.activity

import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import com.warning.R
import com.warning.adapter.YiqingAdapter
import com.warning.dto.YiqingDto
import kotlinx.android.synthetic.main.activity_yiqing_list.*
import kotlinx.android.synthetic.main.shawn_layout_title.*
import java.util.*

/**
 * 疫情列表
 * @author shawn_sun
 */
class YiqingListActivity : BaseActivity(), OnClickListener {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_yiqing_list)
		initWidget()
		initListView()
	}
	
	/**
	 * 初始化控件
	 */
	private fun initWidget() {
		llBack.setOnClickListener(this)
		tvTitle.text = "全球疫情列表"
    }

	/**
	 * 初始化listview
	 */
	private fun initListView() {
		val dataList : ArrayList<YiqingDto> = ArrayList()
		dataList.addAll(intent.getParcelableArrayListExtra("dataList"))

		val mAdapter = YiqingAdapter(this, dataList)
		listView.adapter = mAdapter
	}

	override fun onClick(v: View?) {
		when(v!!.id) {
			R.id.llBack -> finish()
		}
	}

}
