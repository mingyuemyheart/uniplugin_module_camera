package com.warning.activity;

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.View.OnClickListener
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings.LayoutAlgorithm
import android.webkit.WebView
import android.webkit.WebViewClient
import com.umeng.socialize.UMShareAPI
import com.warning.R
import com.warning.common.CONST
import com.warning.dto.NewsDto
import com.warning.util.CommonUtil
import com.warning.util.CommonUtil.getFromAssets
import kotlinx.android.synthetic.main.activity_zixun_detail.*
import kotlinx.android.synthetic.main.shawn_layout_title.*

/**
 * 资讯详情
 */
class ZixunDetailActivity : BaseActivity(), OnClickListener {
	
	private var data : NewsDto? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			WebView.enableSlowWholeDocumentDraw()
		}
		setContentView(R.layout.activity_zixun_detail)
		initRefreshLayout()
		initWidget()
		initWebView()
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
		tvTitle.text = "资讯详情"
		ivShare.setOnClickListener(this)
		
		data = intent.extras.getParcelable("data")
	}
	
	private fun refresh() {
		if (data != null && webView != null) {
			val css = getFromAssets(this, "news.css")
			val style = "<meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0,user-scalable=no\" /><style>$css</style>"
			val time = getString(R.string.publish_time)+": "+data!!.time
			val title = "<center><div><h3>"+data!!.title+"</h3><div style=\"margin-top:-10px; margin-bottom:10px;\">"+time+"</div></div></center><hr style=\"color:#ddd\" />"
			webView.loadDataWithBaseURL("", title+style+data!!.content, "text/html", "utf-8", "")
		}
	}

	/**
	 * 初始化webview
	 */
	private fun initWebView() {
		val webSettings = webView.settings
		//支持javascript
		webSettings.javaScriptEnabled = true
		// 设置可以支持缩放 
		webSettings.setSupportZoom(true)
		// 设置出现缩放工具 
		webSettings.builtInZoomControls = true
		webSettings.displayZoomControls = false
		//扩大比例的缩放
		webSettings.useWideViewPort = true
		//自适应屏幕
		webSettings.layoutAlgorithm = LayoutAlgorithm.SINGLE_COLUMN
		webSettings.loadWithOverviewMode = true
		refresh()

		webView.webChromeClient = object : WebChromeClient() {
			override fun onReceivedTitle(view: WebView?, title: String?) {
				super.onReceivedTitle(view, title)
			}
		}

		webView.webViewClient = object : WebViewClient() {
			override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
				return true
			}

			override fun onPageFinished(view: WebView?, url: String?) {
				super.onPageFinished(view, url)
				refreshLayout.isRefreshing = false
				ivShare.visibility = View.VISIBLE
			}
		}
	}

	override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (webView != null && webView.canGoBack()) {
				webView.goBack()
				return true
			}else {
				finish()
			}
		}
		return super.onKeyDown(keyCode, event)
	}

	override fun onClick(p0: View?) {
		when(p0!!.id) {
			R.id.llBack -> finish()
			R.id.ivShare -> {
				val bitmap1 = CommonUtil.captureWebView(webView)
				val bitmap2 = BitmapFactory.decodeResource(resources, R.drawable.iv_share_bottom)
				val bitmap = CommonUtil.mergeBitmap(this, bitmap1, bitmap2, false)
				CommonUtil.clearBitmap(bitmap1)
				CommonUtil.clearBitmap(bitmap2)
				CommonUtil.share(this, bitmap)
			}
		}
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		//umeng分享回调
		UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data)
	}

}
