package com.warning.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore.Images.Thumbnails;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.sdk.android.vod.upload.VODUploadCallback;
import com.alibaba.sdk.android.vod.upload.VODUploadClient;
import com.alibaba.sdk.android.vod.upload.VODUploadClientImpl;
import com.alibaba.sdk.android.vod.upload.model.UploadFileInfo;
import com.alibaba.sdk.android.vod.upload.model.VodInfo;
import com.warning.R;
import com.warning.adapter.EventTypeAdapter;
import com.warning.adapter.WeatherTypeAdapter;
import com.warning.common.CONST;
import com.warning.dto.PhotoDto;
import com.warning.dto.UploadVideoDto;
import com.warning.util.CommonUtil;
import com.warning.util.OkHttpUtil;
import com.warning.view.UploadDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 预览、上传界面
 * @author shawn_sun
 *
 */
public class DisplayVideoActivity extends BaseActivity implements SurfaceHolder.Callback, OnPreparedListener,
OnVideoSizeChangedListener, OnCompletionListener, OnClickListener{
	
	private Context mContext = null;
	private SurfaceView surfaceView = null;
	private SurfaceHolder surfaceHolder = null;
	private MediaPlayer mPlayer = null;
	private String videoUrl = null;//视频路径
	private SimpleDateFormat sdf = new SimpleDateFormat("mm:ss", Locale.CHINA);
	private SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
	private SimpleDateFormat sdf3 = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA);
	private Timer timer = null;
	private int displayW = 0;//屏幕宽
	private int displayH = 0;//屏幕高
	private Configuration configuration = null;//方向监听器
	private static final int HANDLER_PROCESS = 0;
	private static final int HANDLER_VISIBILITY = 1;
	private long delayTime = 3000;//延迟时间
	private boolean executeOnce = true;//只执行一次
	private String lat = "0", lng = "0";
	private String proName = "", cityName = "", disName = "", roadName = "", aoiName = "";
	private String position = "";
	private UploadDialog uploadDialog = null;
	
	//竖屏布局
	private TextView tvDate = null;//日期
	private String subTitle = "";
	private EditText etTitle = null;//编辑视频标题
	private EditText etContent = null;
	private TextView tvTextCount = null;
	private ScrollView scrollView = null;//操作区域
	
	//横屏布局
	private ImageView ivPlayLand = null;//播放按钮
	private TextView tvStartTimeLand = null;//开始时间
	private TextView tvEndTimeLand = null;//结束时间
	private SeekBar seekBarLand = null;//进度条
	private ImageView ivInFull = null;//全屏按钮
	private RelativeLayout reTop = null;//屏幕上方区域
	private RelativeLayout reBottom = null;//屏幕下方区域
	
	private WeatherTypeAdapter adapter1 = null;
	private List<UploadVideoDto> list1 = new ArrayList<>();
	private String hot_flags = "";//天气类型
	private EventTypeAdapter adapter2 = null;
	private List<UploadVideoDto> list2 = new ArrayList<>();
	private String event_flags = "";//事件类型
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_display_video);
		mContext = this;
		initWidget();
		initGridView1();
		initGridView2();
		initSurfaceView();
	}

	private void showUploadDialog() {
		if (uploadDialog == null) {
			uploadDialog = new UploadDialog(mContext);
			uploadDialog.setCanceledOnTouchOutside(false);
		}
		uploadDialog.show();
	}

	private void cancelUploadDialog() {
		if (uploadDialog != null) {
			uploadDialog.cancel();
		}
	}

	private void setPercentUploadDialog(int percent) {
		if (uploadDialog != null) {
			uploadDialog.setPercent(percent);
		}
	}
	
	private void fullScreen(boolean enable) {
        if (enable) {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            getWindow().setAttributes(lp);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
		} else {
            WindowManager.LayoutParams attr = getWindow().getAttributes();
            attr.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().setAttributes(attr);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		configuration = newConfig;
		if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
			showPort();
			fullScreen(false);
		}else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			showLand();
			fullScreen(true);
		}
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		//竖屏布局
		TextView tvPosition = findViewById(R.id.tvPosition);
		tvDate = findViewById(R.id.tvDate);
		etTitle = findViewById(R.id.etTitle);
		etContent = findViewById(R.id.etContent);
		etContent.addTextChangedListener(contentWatcher);
		tvTextCount = findViewById(R.id.tvTextCount);
        TextView tvRemove = findViewById(R.id.tvRemove);
		tvRemove.setOnClickListener(this);
        TextView tvUpload = findViewById(R.id.tvUpload);
		tvUpload.setOnClickListener(this);
		scrollView = findViewById(R.id.scrollView);
		
		//横屏布局
		ImageView ivBackLand = findViewById(R.id.ivBackLand);
		ivBackLand.setOnClickListener(this);
		ivPlayLand = findViewById(R.id.ivPlayLand);
		ivPlayLand.setOnClickListener(this);
		seekBarLand = findViewById(R.id.seekBarLand);
		seekBarLand.setOnTouchListener(seekbarListener);
		tvStartTimeLand = findViewById(R.id.tvStartTimeLand);
		tvStartTimeLand.setText("00:00");
		tvEndTimeLand = findViewById(R.id.tvEndTimeLand);
		ivInFull = findViewById(R.id.ivInFull);
		ivInFull.setOnClickListener(this);
		reTop = findViewById(R.id.reTop);
		reBottom = findViewById(R.id.reBottom);
		LinearLayout llSurfaceView = findViewById(R.id.llSurfaceView);
		llSurfaceView.setOnClickListener(this);

		if (getIntent().hasExtra("data")) {
			PhotoDto data = getIntent().getExtras().getParcelable("data");
			if (data != null) {
				if (!TextUtils.isEmpty(data.workTime)) {
					SharedPreferences sp = getSharedPreferences(data.workTime, Context.MODE_PRIVATE);
					proName = sp.getString("proName", "");
					cityName = sp.getString("cityName", "");
					disName = sp.getString("disName", "");
					roadName = sp.getString("roadName", "");
					aoiName = sp.getString("aoiName", "");
					lat = sp.getString("lat", "");
					lng = sp.getString("lng", "");

					if (cityName.contains(proName)) {
						tvPosition.setText("拍摄地点："+cityName+disName+roadName+aoiName);
						position = cityName+disName+roadName+aoiName;
					}else {
						tvPosition.setText("拍摄地点："+proName+cityName+disName+roadName+aoiName);
						position = proName+cityName+disName+roadName+aoiName;
					}
					try {
						tvDate.setText("拍摄时间："+sdf2.format(sdf3.parse(data.workTime)));
					} catch (ParseException e) {
						e.printStackTrace();
					}
					subTitle = position+"发生";
					etTitle.setText(subTitle);
					etTitle.setSelection(subTitle.length());
					if (proName.contains("北京")) {
						proName = "北京";
					}else if (proName.contains("天津")) {
						proName = "天津";
					}else if (proName.contains("上海")) {
						proName = "上海";
					}else if (proName.contains("重庆")) {
						proName = "重庆";
					}else if (proName.contains("内蒙古")) {
						proName = "内蒙古";
					}else if (proName.contains("广西")) {
						proName = "广西";
					}else if (proName.contains("宁夏")) {
						proName = "宁夏";
					}else if (proName.contains("新疆")) {
						proName = "新疆";
					}else if (proName.contains("西藏")) {
						proName = "西藏";
					}
					tvUpload.setVisibility(View.VISIBLE);
				}
				
				videoUrl = data.videoUrl;
				getThumbnail(videoUrl, new File(videoUrl).getName());
			}
		}

		OkHttpLabel("http://new.12379.tianqi.cn/Work/getbiaoqian");
	}

	/**
	 * 输入内容监听器
	 */
	private TextWatcher contentWatcher = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		}
		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		}
		@Override
		public void afterTextChanged(Editable arg0) {
			if (etContent.getText().length() == 0) {
				tvTextCount.setText("(200字以内)");
			}else {
				int count = 200-etContent.getText().length();
				tvTextCount.setText("(还可输入"+count+"字)");
			}
		}
	};
	
	/**
	 * 初始化天气类型gridview
	 */
	private void initGridView1() {
        GridView gridView1 = findViewById(R.id.gridView1);
		adapter1 = new WeatherTypeAdapter(mContext, list1);
		gridView1.setAdapter(adapter1);
		ViewGroup.LayoutParams params = gridView1.getLayoutParams();
		params.height = 240;
		gridView1.setLayoutParams(params);
		gridView1.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				for (int i = 0; i < list1.size(); i++) {
					if (i == arg2) {
						if (!list1.get(i).isSelected) {
							list1.get(i).isSelected = true;
							etTitle.setText(subTitle+list1.get(i).weatherName);
							etTitle.setSelection(etTitle.getText().toString().length());
							
							if (TextUtils.equals(list1.get(i).eventType, "11000")) {//自然灾害
								list2.get(0).isSelected = true;
							}else if (TextUtils.equals(list1.get(i).eventType, "12000")) {//事故灾难
								list2.get(1).isSelected = true;
							}else if (TextUtils.equals(list1.get(i).eventType, "13000")) {//公共卫生
								list2.get(2).isSelected = true;
							}else if (TextUtils.equals(list1.get(i).eventType, "14000")) {//安全事件
								list2.get(3).isSelected = true;
							}
						}else {
							list1.get(i).isSelected = false;
							etTitle.setText(subTitle+"");
							etTitle.setSelection(etTitle.getText().toString().length());
							
							if (TextUtils.equals(list1.get(i).eventType, "11000")) {//自然灾害
								list2.get(0).isSelected = false;
							}else if (TextUtils.equals(list1.get(i).eventType, "12000")) {//事故灾难
								list2.get(1).isSelected = false;
							}else if (TextUtils.equals(list1.get(i).eventType, "13000")) {//公共卫生
								list2.get(2).isSelected = false;
							}else if (TextUtils.equals(list1.get(i).eventType, "14000")) {//安全事件
								list2.get(3).isSelected = false;
							}
						}
						
						if (adapter2 != null) {
							adapter2.notifyDataSetChanged();
						}
						
					}else {
						list1.get(i).isSelected = false;
					}
				}
				if (adapter1 != null) {
					adapter1.notifyDataSetChanged();
				}
			}
		});
	}
	
	/**
	 * 初始化事件类型gridview
	 */
	private void initGridView2() {
        GridView gridView2 = findViewById(R.id.gridView2);
		adapter2 = new EventTypeAdapter(mContext, list2);
		gridView2.setAdapter(adapter2);
		gridView2.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				for (int i = 0; i < list2.size(); i++) {
					if (i == arg2) {
						if (!list2.get(i).isSelected) {
							list2.get(i).isSelected = true;
						}else {
							list2.get(i).isSelected = false;
						}
					}
				}
				if (adapter2 != null) {
					adapter2.notifyDataSetChanged();
				}
			}
		});
	}
	
	/**
	 * 获取视频缩略图
	 * @param videoName 视屏名称
	 */
	private void getThumbnail(String url, String videoName) {
		Bitmap thumbBitmap = ThumbnailUtils.createVideoThumbnail(url, Thumbnails.MINI_KIND);
		thumbBitmap = ThumbnailUtils.extractThumbnail(thumbBitmap, 320, 240);
		File files = new File(CONST.THUMBNAIL_ADDR);
		if (!files.exists()) {
			files.mkdirs();
		}
		
		String fileName;
		if (videoName != null) {
			fileName = videoName.substring(0, videoName.length()-4);
		}else {
			fileName = sdf3.format(System.currentTimeMillis());
		}
	    File thumbnailFile = new File(CONST.THUMBNAIL_ADDR, fileName + ".jpg");
	    FileOutputStream fos;
	    try {
			fos = new FileOutputStream(thumbnailFile);
			thumbBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
			fos.flush();
		    fos.close();  
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 显示竖屏，隐藏横屏
	 */
	private void showPort() {
		scrollView.setVisibility(View.VISIBLE);
		ivInFull.setImageResource(R.drawable.iv_out_full);
		changeVideo(mPlayer.getVideoWidth(), mPlayer.getVideoHeight());
	}
	
	/**
	 * 显示横屏，隐藏竖屏
	 */
	private void showLand() {
		scrollView.setVisibility(View.GONE);
		ivInFull.setImageResource(R.drawable.iv_in_full);
		changeVideo(mPlayer.getVideoWidth(), mPlayer.getVideoHeight());
	}
	
	/**
	 * 禁止seekbar监听事件
	 */
	private OnTouchListener seekbarListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View arg0, MotionEvent arg1) {
			return true;
		}
	};
	
	/**
	 * 初始化surfaceView
	 */
	private void initSurfaceView() {
		surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
		surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback(this);
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getRealMetrics(dm);
		displayW = dm.widthPixels;
		displayH = dm.heightPixels;
		
		surfaceView.setLayoutParams(new LinearLayout.LayoutParams(displayW, displayW*9/16));
	}
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		surfaceHolder = holder;
		mPlayer = new MediaPlayer();
		mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		mPlayer.setDisplay(holder);
		mPlayer.setOnPreparedListener(this);
		mPlayer.setOnVideoSizeChangedListener(this);
		mPlayer.setOnCompletionListener(this);
        //设置显示视频显示在SurfaceView上
        try {
        	if (videoUrl != null) {
            	mPlayer.setDataSource(videoUrl);
            	Log.d("videoUrl", videoUrl);
            	mPlayer.prepareAsync();
			}
        } catch (Exception e) {
            e.printStackTrace();
        }
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int arg1, int arg2, int arg3) {
		surfaceHolder = holder;
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		surfaceHolder = holder;
		releaseTimer();
        releaseMediaPlayer();
	}
	
	@Override
	public void onPrepared(MediaPlayer player) {
		tvStartTimeLand.setText(sdf.format(player.getCurrentPosition()));
		tvEndTimeLand.setText(sdf.format(player.getDuration()));

		seekBarLand.setProgress(0);
		seekBarLand.setMax(player.getDuration()/1000);

    	startPlayVideo();
	}
	
	/**
	 * 开始播放视频
	 */
	private void startPlayVideo() {
		if (mPlayer != null) {
			if (mPlayer.isPlaying()) {
				ivPlayLand.setImageResource(R.drawable.iv_play);
				mPlayer.pause();
				releaseTimer();
			}else {
				ivPlayLand.setImageResource(R.drawable.iv_pause);
				mPlayer.start();
				
				timer = new Timer();
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						if(mPlayer==null){
							return;
						} 
				        if (mPlayer.isPlaying() && !seekBarLand.isPressed()) {
				            handler.sendEmptyMessage(0);  
				        }  
					}
				}, 0, 1000);
			}
		}
	}

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {  
	    public void handleMessage(Message msg) {  
	    	switch (msg.what) {
			case HANDLER_PROCESS:
				if (mPlayer != null) {
					int position = mPlayer.getCurrentPosition();  
					int duration = mPlayer.getDuration(); 
					
					if (position > 0) {
						if (executeOnce) {
							dismissColunm();
						}
					}
					
					if (duration > 0) {  
						long posLand = seekBarLand.getMax() * position / duration;  
						seekBarLand.setProgress((int) posLand);  
						tvStartTimeLand.setText(sdf.format(position));
					}  
				}
				break;
			case HANDLER_VISIBILITY:
				reTop.setVisibility(View.GONE);
				reBottom.setVisibility(View.GONE);
				ivPlayLand.setVisibility(View.GONE);
				break;

			default:
				break;
			}
	        
	    };  
	};  
	
	/**
	 * 启动线程,隐藏操作栏
	 */
	private void dismissColunm() {
		handler.removeMessages(HANDLER_VISIBILITY);
		Message msg = new Message();
		msg.what = HANDLER_VISIBILITY;
		handler.sendMessageDelayed(msg, delayTime);
		executeOnce = false;
	}
	
	/**
	 * 改变横竖屏切换是视频的比例
	 * @param videoW
	 * @param videoH
	 */
	private void changeVideo(int videoW, int videoH) {
		if (surfaceView != null) {
			if (mPlayer != null) {
				int standarH = displayW*9/16;//自定义高度
				if (configuration != null) {
					if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
						standarH = displayW*9/16;
					}else if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
						standarH = displayW;
					}
				}
				if (videoW == 0 || videoH == 0) {
					surfaceView.setLayoutParams(new LinearLayout.LayoutParams(standarH, standarH));
					return;
				}else {
					surfaceView.setLayoutParams(new LinearLayout.LayoutParams(videoW*standarH/videoH, standarH));
				}
			}
			
//			if (configuration != null) {
//				if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
//					surfaceView.setLayoutParams(new LinearLayout.LayoutParams(displayW, displayW*videoH/videoW));
//				}else if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//					surfaceView.setLayoutParams(new LinearLayout.LayoutParams(displayH, displayH*videoH/videoW));
//				}
//			}else {
//				surfaceView.setLayoutParams(new LinearLayout.LayoutParams(displayW, displayW*videoH/videoW));
//			}
		}
	}
	
	@Override
	public void onVideoSizeChanged(MediaPlayer player, int videoW, int videoH) {
		changeVideo(videoW, videoH);
	}
	
	@Override
	public void onCompletion(MediaPlayer player) {
		releaseTimer();
		ivPlayLand.setImageResource(R.drawable.iv_play);
		seekBarLand.setProgress(0);
		tvStartTimeLand.setText("00:00");
		handler.removeMessages(HANDLER_VISIBILITY);
		reTop.setVisibility(View.VISIBLE);
		reBottom.setVisibility(View.VISIBLE);
		ivPlayLand.setVisibility(View.VISIBLE);
	}
	
	/**
	 * 释放MediaPlayer资源
	 */
	private void releaseMediaPlayer() {
		if (mPlayer != null) {
			mPlayer.stop();
			mPlayer.release();
			mPlayer = null;
		}
	}
	
	/**
	 * 释放timer
	 */
	private void releaseTimer() {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	}
	
	@Override
    protected void onDestroy() {
        super.onDestroy();
		releaseTimer();
        releaseMediaPlayer();
    }
	
	/**
	 * 获取上传标签
	 */
	private void OkHttpLabel(final String url) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				OkHttpUtil.enqueue(new Request.Builder().url(url).build(), new Callback() {
					@Override
					public void onFailure(Call call, IOException e) {

					}

					@Override
					public void onResponse(Call call, Response response) throws IOException {
						if (!response.isSuccessful()) {
							return;
						}
						final String result = response.body().string();
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (!TextUtils.isEmpty(result)) {
									try {
										JSONArray array = new JSONArray(result);
										list2.clear();
										for (int i = 0; i < array.length(); i++) {
											JSONObject obj = array.getJSONObject(i);
											UploadVideoDto dto = new UploadVideoDto();
											if (!obj.isNull("name")) {
												dto.eventName = obj.getString("name");
											}
											if (!obj.isNull("code")) {
												dto.eventType = obj.getString("code");
											}
											dto.isSelected = false;

											if (!obj.isNull("info") && !TextUtils.isEmpty(obj.getString("info"))) {
												JSONArray itemArray = obj.getJSONArray("info");
												if (itemArray.length() > 0) {
													list1.clear();
												}
												for (int j = 0; j < itemArray.length(); j++) {
													UploadVideoDto itemDto = new UploadVideoDto();
													JSONObject itemObj = itemArray.getJSONObject(j);
													if (!itemObj.isNull("c")) {
														itemDto.count = itemObj.getString("c");
													}
													if (!itemObj.isNull("eventType")) {
														itemDto.weatherType = itemObj.getString("eventType");
													}
													if (!itemObj.isNull("name")) {
														itemDto.weatherName = itemObj.getString("name");
													}
													itemDto.eventType = dto.eventType;
													itemDto.isSelected = false;
													list1.add(itemDto);
												}

												if (adapter1 != null) {
													adapter1.notifyDataSetChanged();
												}

											}
											list2.add(dto);
										}

										if (adapter2 != null) {
											adapter2.notifyDataSetChanged();
										}

									} catch (JSONException e) {
										e.printStackTrace();
									}
								}
							}
						});
					}
				});
			}
		}).start();
	}
	
	/**
	 * 取消上传视频对话框
	 */
	private void deleteVideoDialog() {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.shawn_dialog_delete, null);
		TextView tvMessage = (TextView) view.findViewById(R.id.tvMessage);
		LinearLayout llNegative = (LinearLayout) view.findViewById(R.id.llNegative);
		LinearLayout llPositive = (LinearLayout) view.findViewById(R.id.llPositive);
		
		final Dialog dialog = new Dialog(mContext, R.style.CustomProgressDialog);
		dialog.setContentView(view);
		dialog.show();
		
		tvMessage.setText("确定不上传该视频？");
		llNegative.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});
		
		llPositive.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
				exit();
			}
		});
	}

	/**
	 * 上传视频对话框
	 */
	private void uploadVideoDialog() {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.upload_dialog, null);
		LinearLayout llPositive = (LinearLayout) view.findViewById(R.id.llPositive);
		
		final Dialog dialog = new Dialog(mContext, R.style.CustomProgressDialog);
		dialog.setContentView(view);
		dialog.show();
		
		llPositive.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});
	}

	/**
	 * 获取视频发布签名
	 */
	private void OkHttpSign() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				String url = "http://new.12379.tianqi.cn/Tensent/tensentSign";
				OkHttpUtil.enqueue(new Request.Builder().url(url).build(), new Callback() {
					@Override
					public void onFailure(Call call, IOException e) {
					}
					@Override
					public void onResponse(Call call, Response response) throws IOException {
						if (!response.isSuccessful()) {
							return;
						}
						final String result = response.body().string();
						if (!TextUtils.isEmpty(result)) {
							try {
								String uploadAddress="",uploadAuth="",videoId="";
								JSONObject obj = new JSONObject(result);
								if (!obj.isNull("UploadAddress")) {
									uploadAddress = obj.getString("UploadAddress");
								}
								if (!obj.isNull("UploadAuth")) {
									uploadAuth = obj.getString("UploadAuth");
								}
								if (!obj.isNull("VideoId")) {
									videoId = obj.getString("VideoId");
								}

								QQCloudPublish(uploadAddress,uploadAuth,videoId);
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					}
				});
			}
		}).start();
	}

	/**
	 * 上传到阿里云
	 */
	private void QQCloudPublish(final String uploadAddress, final String uploadAuth, final String videoId) {
		final File videoFile = new File(videoUrl);
		String fileName = videoFile.getName().substring(0, videoFile.getName().length()-4);
		String coverPath = CONST.THUMBNAIL_ADDR+File.separator+fileName+".jpg";

		//上传到阿里云
		final VODUploadClient uploadClient = new VODUploadClientImpl(getApplicationContext());
		VODUploadCallback callback = new VODUploadCallback() {
			@Override
			public void onUploadSucceed(UploadFileInfo info) {
				Log.e("onUploadSucceed", "onUploadSucceed");
				cancelUploadDialog();
				uploadVideo(videoId);
			}

			@Override
			public void onUploadFailed(UploadFileInfo info, String code, String message) {
				Log.e("onUploadFailed", "onUploadFailed");
			}

			@Override
			public void onUploadProgress(UploadFileInfo info, final long uploadedSize, final long totalSize) {
				Log.e("onUploadProgress", uploadedSize+"");
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						setPercentUploadDialog((int)(100*uploadedSize/totalSize));
					}
				});
			}

			@Override
			public void onUploadTokenExpired() {
				uploadClient.resumeWithAuth(uploadAuth);
			}

			@Override
			public void onUploadRetry(String code, String message) {
			}

			@Override
			public void onUploadRetryResume() {
			}

			@Override
			public void onUploadStarted(UploadFileInfo uploadFileInfo) {
				uploadClient.setUploadAuthAndAddress(uploadFileInfo, uploadAuth, uploadAddress);
			}
		};
		uploadClient.init(callback);

		VodInfo vodInfo = new VodInfo();
		vodInfo.setTitle(etTitle.getText().toString());
		vodInfo.setDesc(etTitle.getText().toString());
		vodInfo.setCoverUrl(coverPath);
		uploadClient.addFile(videoFile.getPath(), vodInfo);
		uploadClient.start();
	}

	/**
	 * 上传视频
	 */
	private void uploadVideo(final String fileid) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				String url = "http://new.12379.tianqi.cn/Work/ali_upload";//阿里云
				File videoFile = new File(videoUrl);
				final String fileName = videoFile.getName().substring(0, videoFile.getName().length()-4);
				FormBody.Builder builder = new FormBody.Builder();
				builder.add("token", BaseActivity.TOKEN);
				builder.add("uid", BaseActivity.UID);
				builder.add("isadmin", BaseActivity.ISINFOER);
				builder.add("title", etTitle.getText().toString());
				builder.add("content", etContent.getText().toString());
				builder.add("latlon", lat+","+lng);
				builder.add("hot_flags", hot_flags);
				builder.add("event_flags", event_flags);
				builder.add("location", position);
				builder.add("workstype", "video");
				builder.add("proname", proName);
				builder.add("cityname", cityName);
				builder.add("xianname", disName);
				if (fileName.length() == 14) {
					try {
						builder.add("work_time", sdf2.format(sdf3.parse(fileName)));
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}else {
					builder.add("work_time", tvDate.getText().toString());
				}
				builder.add("fileid", fileid);
				RequestBody body = builder.build();
				OkHttpUtil.enqueue(new Request.Builder().post(body).url(url).build(), new Callback() {
					@Override
					public void onFailure(Call call, IOException e) {
						Toast.makeText(mContext, "上传失败！", Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onResponse(Call call, Response response) throws IOException {
						if (!response.isSuccessful()) {
							return;
						}
						String result = response.body().string();
						//删除保存在本地的文件
						CommonUtil.deleteFile(CONST.THUMBNAIL_ADDR+File.separator+fileName+".jpg");//删除缩略图
						CommonUtil.deleteFile(CONST.VIDEO_ADDR+File.separator+fileName+".mp4");//删除视频

						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								Intent intent = new Intent();
								intent.putExtra("fileName", fileName);
								setResult(RESULT_OK, intent);

								uploadSuccessDialog();
							}
						});

					}
				});
			}
		}).start();
	}

	/**
	 * 上传视频成功对话框
	 */
	private void uploadSuccessDialog() {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.upload_success_dialog, null);
		LinearLayout llPositive = (LinearLayout) view.findViewById(R.id.llPositive);
		
		final Dialog dialog = new Dialog(mContext, R.style.CustomProgressDialog);
		dialog.setContentView(view);
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
		
		llPositive.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
				exit();
			}
		});
	}
	
	private void exit() {
		if (configuration == null) {
			releaseTimer();
	        releaseMediaPlayer();
			finish();
		}else {
			if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
				releaseTimer();
		        releaseMediaPlayer();
				finish();
			}else if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			}
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			exit();
		}
		return false;
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ivBackLand:
			exit();
			break;
		case R.id.llSurfaceView:
			if (mPlayer != null && mPlayer.isPlaying()) {
				if (reBottom.getVisibility() == View.VISIBLE) {
					reTop.setVisibility(View.GONE);
					reBottom.setVisibility(View.GONE);
					ivPlayLand.setVisibility(View.GONE);
				}else {
					reTop.setVisibility(View.VISIBLE);
					reBottom.setVisibility(View.VISIBLE);
					ivPlayLand.setVisibility(View.VISIBLE);
					dismissColunm();
				}
			}else {
				reTop.setVisibility(View.VISIBLE);
				reBottom.setVisibility(View.VISIBLE);
				ivPlayLand.setVisibility(View.VISIBLE);
			}
			break;
		case R.id.ivInFull:
			if (configuration == null) {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			}else {
				if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
				}else if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				}
			}
			break;
		case R.id.ivPlayLand:
			dismissColunm();
			startPlayVideo();
			break;
		case R.id.tvRemove:
			deleteVideoDialog();
			break;
		case R.id.tvUpload:
			hot_flags = "";
			for (int i = 0; i < list1.size(); i++) {
				if (list1.get(i).isSelected) {
					hot_flags = list1.get(i).weatherType;
				}
			}
			
			event_flags = "";
			for (int i = 0; i < list2.size(); i++) {
				if (list2.get(i).isSelected) {
					if (!TextUtils.isEmpty(event_flags)) {
						event_flags = event_flags+","+list2.get(i).eventType;
					}else {
						event_flags = list2.get(i).eventType;
					}
				}
			}
			
			if (TextUtils.isEmpty(etTitle.getText().toString()) || TextUtils.isEmpty(event_flags)) {
				uploadVideoDialog();
			}else {
				showUploadDialog();
				OkHttpSign();
			}
			break;

		default:
			break;
		}
	}

}
