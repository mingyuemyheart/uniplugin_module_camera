package com.warning.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.umeng.socialize.UMShareAPI;
import com.warning.R;
import com.warning.common.CONST;
import com.warning.dto.NewsDto;
import com.warning.util.CommonUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * 资讯详情
 */
public class ZixunDetailActivity extends BaseActivity implements OnClickListener{
	
	private ImageView ivShare;
	private WebView webView;
	private NewsDto data;
	private SwipeRefreshLayout refreshLayout;//下拉刷新布局
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			WebView.enableSlowWholeDocumentDraw();
		}
		setContentView(R.layout.activity_zixun_detail);
		initRefreshLayout();
		initWidget();
		initWebView();
	}
	
	/**
	 * 初始化下拉刷新布局
	 */
	private void initRefreshLayout() {
		refreshLayout = findViewById(R.id.refreshLayout);
		refreshLayout.setColorSchemeResources(CONST.color1, CONST.color2, CONST.color3, CONST.color4);
		refreshLayout.setProgressViewEndTarget(true, 300);
		refreshLayout.setRefreshing(true);
		refreshLayout.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				refresh();
			}
		});
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		LinearLayout llBack = findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		TextView tvTitle = findViewById(R.id.tvTitle);
		tvTitle.setText("资讯详情");
		ivShare = findViewById(R.id.ivShare);
		ivShare.setOnClickListener(this);
		
		data = getIntent().getExtras().getParcelable("data");
	}
	
	private void refresh() {
		if (data != null && webView != null) {
			String css = getFromAssets("news.css");
			String style = "<meta charset=\"UTF-8\">"+ "<meta name=\"viewport\" content=\"initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0,user-scalable=no\" />" +"<style>"+ css +"</style>";
			String time = getString(R.string.publish_time)+": "+data.time;
			String title = "<center><div><h3>"+data.title+"</h3><div style=\"margin-top:-10px; margin-bottom:10px;\">"+time+"</div></div></center><hr style=\"color:#ddd\" />";
			webView.loadDataWithBaseURL("", title+style+data.content, "text/html", "utf-8", "");
		}
	}

	/**
	 * 初始化webview
	 */
	private void initWebView() {
		webView = findViewById(R.id.webView);
		WebSettings webSettings = webView.getSettings();
		//支持javascript
		webSettings.setJavaScriptEnabled(true); 
		// 设置可以支持缩放 
		webSettings.setSupportZoom(true); 
		// 设置出现缩放工具 
		webSettings.setBuiltInZoomControls(true);
		webSettings.setDisplayZoomControls(false);
		//扩大比例的缩放
		webSettings.setUseWideViewPort(true);
		//自适应屏幕
		webSettings.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
		webSettings.setLoadWithOverviewMode(true);
//		webView.loadUrl(url);
		
		refresh();
		
		webView.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onReceivedTitle(WebView view, String title) {
				super.onReceivedTitle(view, title);
			}
		});
		
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String itemUrl) {
				return true;
			}
			
			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				refreshLayout.setRefreshing(false);
				ivShare.setVisibility(View.VISIBLE);
				
//				String html = "function subStringLocationLatitude(sourceString){"+
//					    "var index = sourceString.indexOf('.');"+
//					    "if(index > 0){"+
//					    "var temp = sourceString.substr(index+1);"+
//					    "sourceString = sourceString.substr(0, index);"+
//					    "var target = temp.substr(0, 2);"+
//					    "if(temp.length == 1){"+
//					    "target =target  + '0';}"+
//					    "if(temp.length >= 3){"+
//					    "var str = temp.substr(1,2);"+
//					    "if(str/1000 >= 0.05){"+
//					    "target = (target/10 + 0.1)*10;}}"+
//					    "sourceString = sourceString + '.' +target;}"+
//					    "if(index <= 0 ){"+
//					    "sourceString =sourceString  + '.00';}"+
//					    "var targetString=sourceString.substr(0,1) ;"+
//					    "if(targetString=='-'){"+
//					    "var modleString=sourceString.substring(1,sourceString.length)"+
//					    "document.write('南纬'+modleString+'度，');}else{"+
//					    "document.write('北纬'+sourceString+'度，');}}"+
//					    
//						"function subStringLocationLongitude(sourceString){"+
//						"var index = sourceString.indexOf('.');"+
//						"if(index > 0){"+
//						"var temp = sourceString.substr(index+1);"+
//						"sourceString = sourceString.substr(0, index);"+
//						"var target = temp.substr(0, 2);"+
//						"if(temp.length == 1){"+
//						"target =target  + '0';}"+
//						"if(temp.length >= 3){"+
//						"var str = temp.substr(1,2);"+
//						"if(str/1000 >= 0.05){"+
//						"target = (target/10 + 0.1)*10;}}"+
//						"sourceString = sourceString + '.' +target;}"+
//						"if(index <= 0 ){"+
//						"sourceString =sourceString  + '.00';}"+
//						"var targetString=sourceString.substr(0,1) ;"+
//						"if(targetString=='-'){"+
//						"var modleString=sourceString.substring(1,sourceString.length)"+
//						"document.write('西经'+modleString+'度');}else{"+
//						"document.write('东经'+sourceString+'度');}}"+
//						
//						"function origTime(title,time){"+
//						"var tindex = time.indexOf('-');"+
//						"var index = title.indexOf('秒');"+
//						"var indexMin = title.indexOf('分');"+
//						"if(index > 0){"+
//						"document.write(time.substr(0,tindex)+'年'+title.substr(0,index+1));"+
//						"} else if (index <= 0 && indexMin > 0) {"+
//						"document.write(time.substr(0,tindex)+'年'+title.substr(0,indexMin+1));}}"+
//						
//						"function shengdu(shengdu){"+
//						"var index = shengdu.indexOf('.');"+
//						"if(index > 0){"+
//						"var target = shengdu.substr(0, index);"+
//						"var str = shengdu.substr(index + 1);"+
//						"if(str > 0.5){"+
//						"target = target + 1;}"+
//						"document.write(target);}else{"+
//						"document.write(shengdu);}}";
//				webView.evaluateJavascript(html, null);
			}
		});
	}
	
	private String getFromAssets(String fileName) {
		String Result = "";
		try {
			InputStreamReader inputReader = new InputStreamReader(getResources().getAssets().open(fileName));
			BufferedReader bufReader = new BufferedReader(inputReader);
			String line = "";
			while ((line = bufReader.readLine()) != null)
				Result += line;
			return Result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Result;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (webView != null && webView.canGoBack()) {
				webView.goBack();
				return true;
			}else {
				finish();
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			finish();
			break;
		case R.id.ivShare:
			Bitmap bitmap1 = CommonUtil.captureWebView(webView);
			Bitmap bitmap2 = BitmapFactory.decodeResource(getResources(), R.drawable.iv_share_bottom);
			Bitmap bitmap = CommonUtil.mergeBitmap(this, bitmap1, bitmap2, false);
			CommonUtil.clearBitmap(bitmap1);
			CommonUtil.clearBitmap(bitmap2);
			CommonUtil.share(this, bitmap);
//			CommonUtil.share(ZixunDetailActivity.this, data.title, data.imgUrl, data.url);
			break;

		default:
			break;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
 		//umeng分享回调
		UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);
	}
	
}
