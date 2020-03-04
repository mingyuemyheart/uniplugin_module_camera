package com.warning.activity;

/**
 * 在线播放视频
 */

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.socialize.UMShareAPI;
import com.warning.R;
import com.warning.adapter.OnlineVideoAdapter;
import com.warning.dto.PhotoDto;
import com.warning.util.CommonUtil;
import com.warning.util.DialogUtil;
import com.warning.util.EmojiMapUtil;
import com.warning.util.OkHttpUtil;
import com.warning.util.StatisticUtil;

import net.tsz.afinal.FinalBitmap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OnlineVideoActivity extends BaseActivity implements SurfaceHolder.Callback, OnPreparedListener, OnVideoSizeChangedListener,
OnCompletionListener, OnClickListener{
	
	private Context mContext = null;
	private SurfaceView surfaceView = null;
	private SurfaceHolder surfaceHolder = null;
	private MediaPlayer mPlayer = null;
	private SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
	private Timer timer = null;
	private int displayW = 0;//屏幕宽
	private int displayH = 0;//屏幕高
	private PhotoDto data = null;
	private ListView mListView = null;
	private OnlineVideoAdapter mAdapter = null;
	private List<PhotoDto> mList = new ArrayList<>();
	private int page = 1;
	private int pageSize = 1000;
	private Configuration configuration = null;//方向监听器
	private ProgressBar progressBar = null;
	private static final int HANDLER_PROCESS = 0;
	private static final int HANDLER_VISIBILITY = 1;
	private long delayTime = 5000;//延迟时间
	private boolean executeOnce = true;//只执行一次
	private LinearLayout llSurfaceView = null;
	
	//竖屏布局
	private TextView tvSubTitle = null;//标题
	private TextView tvPosition = null;//地址
	private TextView tvDate = null;//日期
	private TextView tvContent = null;//内容描述
	private ImageView ivPortrait = null;//头像
	private TextView tvNickName = null;
	private TextView tvPlayCount = null;//播放次数
	private TextView tvCommentCount = null;//评论次数
	private EditText etComment = null;
	private ImageView ivClear = null;
	private TextView tvSubmit = null;
	private ImageView ivPraise = null;//点赞
	private TextView tvPraise = null;//点赞次数
	private boolean isPraise = false;//该条视频当前登录用户是否点过赞
	private ImageView ivShare = null;//分享
	private RelativeLayout reContent = null;
	
	//横屏布局
	private ImageView ivBackLand = null;//返回按钮
	private ImageView ivPlayLand = null;//播放按钮
	private TextView tvStartTimeLand = null;//开始时间
	private TextView tvEndTimeLand = null;//结束时间
	private SeekBar seekBarLand = null;//进度条
	private ImageView ivInFull = null;//全屏按钮
	private RelativeLayout reTop = null;//屏幕上方区域
	private RelativeLayout reBottom = null;//屏幕下方区域
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_online_video);
		mContext = this;
		initWidget();
		initListView();
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
		tvSubTitle = (TextView) findViewById(R.id.tvSubTitle);
		tvSubTitle.setOnClickListener(this);
		tvPosition = (TextView) findViewById(R.id.tvPosition);
		tvDate = (TextView) findViewById(R.id.tvDate);
		tvContent = (TextView) findViewById(R.id.tvContent);
		ivPortrait = (ImageView) findViewById(R.id.ivPortrait);
		tvNickName = (TextView) findViewById(R.id.tvNickName);
		tvPlayCount = (TextView) findViewById(R.id.tvPlayCount);
		tvCommentCount = (TextView) findViewById(R.id.tvCommentCount);
		ivPraise = (ImageView) findViewById(R.id.ivPraise);
		ivPraise.setOnClickListener(this);
		tvPraise = (TextView) findViewById(R.id.tvPraise);
		tvPraise.setOnClickListener(this);
		ivShare = (ImageView) findViewById(R.id.ivShare);
		ivShare.setOnClickListener(this);
		etComment = (EditText) findViewById(R.id.etComment);
		etComment.addTextChangedListener(watcher);
		ivClear = (ImageView) findViewById(R.id.ivClear);
		ivClear.setOnClickListener(this);
		tvSubmit = (TextView) findViewById(R.id.tvSubmit);
		tvSubmit.setOnClickListener(this);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		reContent = (RelativeLayout) findViewById(R.id.reContent);
		
		//横屏布局
		ivBackLand = (ImageView) findViewById(R.id.ivBackLand);
		ivBackLand.setOnClickListener(this);
		ivPlayLand = (ImageView) findViewById(R.id.ivPlayLand);
		ivPlayLand.setOnClickListener(this);
		seekBarLand = (SeekBar) findViewById(R.id.seekBarLand);
		seekBarLand.setOnTouchListener(seekbarListener);
		tvStartTimeLand = (TextView) findViewById(R.id.tvStartTimeLand);
		tvStartTimeLand.setText("00:00");
		tvEndTimeLand = (TextView) findViewById(R.id.tvEndTimeLand);
		ivInFull = (ImageView) findViewById(R.id.ivInFull);
		ivInFull.setOnClickListener(this);
		reTop = (RelativeLayout) findViewById(R.id.reTop);
		reBottom = (RelativeLayout) findViewById(R.id.reBottom);
		llSurfaceView = (LinearLayout) findViewById(R.id.llSurfaceView);
		llSurfaceView.setOnClickListener(this);
		
		if (getIntent().hasExtra("data")) {
			data = getIntent().getExtras().getParcelable("data");
			if (data != null) {
				initSurfaceView();

				if (!TextUtils.isEmpty(data.title)) {
					tvSubTitle.setText(data.title);
				}
				if (!TextUtils.isEmpty(data.location)) {
					tvPosition.setText("拍摄地点："+data.location);
				}
				if (!TextUtils.isEmpty(data.content)) {
					tvContent.setText("内容描述："+data.content);
					tvContent.setVisibility(View.VISIBLE);
				}else {
					tvContent.setVisibility(View.GONE);
				}
				if (!TextUtils.isEmpty(data.workTime)) {
					tvDate.setText("拍摄时间："+data.workTime);
				}
				if (!TextUtils.isEmpty(data.portraitUrl)) {
					FinalBitmap finalBitmap = FinalBitmap.create(mContext);
					LayoutParams params = ivPortrait.getLayoutParams();
					if (params != null) {
						finalBitmap.display(ivPortrait, data.portraitUrl, null, params.width);
					}else {
						finalBitmap.display(ivPortrait, data.portraitUrl, null, 0);
					}
				}else {
					ivPortrait.setImageResource(R.drawable.iv_portrait);
				}
				if (!TextUtils.isEmpty(data.nickName)) {
					tvNickName.setText(data.nickName);
				}else if (!TextUtils.isEmpty(data.getUserName())) {
					tvNickName.setText(data.getUserName());
				}else if (!TextUtils.isEmpty(data.phoneNumber)) {
					if (data.phoneNumber.length() >= 7) {
						tvNickName.setText(data.phoneNumber.replace(data.phoneNumber.substring(3, 7), "****"));
					}else {
						tvNickName.setText(data.phoneNumber);
					}
				}
				if (!TextUtils.isEmpty(data.showTime)) {
					tvPlayCount.setText(data.showTime+"次播放");
				}
//				if (data.getCommentCount() != null) {
//					tvCommentCount.setText("评论" + "（"+data.getCommentCount()+"）");
//				}
//				if (data.praiseCount != null) {
//					tvPraise.setText(data.praiseCount);
//				}
				
				//获取评论列表和点赞状态
				OkhttpComment("http://new.12379.tianqi.cn/Work/getpinglun");
				
				//上传播放或浏览次数
				StatisticUtil.asyncQueryCount("http://new.12379.tianqi.cn/Work/addshowtime", data.videoId);
			}
		}
	}
	
	/**
	 * 评论监听
	 */
	private TextWatcher watcher = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		}
		
		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		}
		@Override
		public void afterTextChanged(Editable arg0) {
			if (!TextUtils.isEmpty(etComment.getText().toString())) {
				ivClear.setVisibility(View.VISIBLE);
				tvSubmit.setVisibility(View.VISIBLE);
			}else {
				ivClear.setVisibility(View.GONE);
				tvSubmit.setVisibility(View.GONE);
			}
		}
	};
	
	/**
	 * 初始化listview
	 */
	private void initListView() {
		mListView = (ListView) findViewById(R.id.listView);
		mAdapter = new OnlineVideoAdapter(mContext, mList);
		mListView.setAdapter(mAdapter);
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
	 * 显示竖屏，隐藏横屏
	 */
	private void showPort() {
		ivInFull.setImageResource(R.drawable.iv_out_full);
		changeVideo(mPlayer.getVideoWidth(), mPlayer.getVideoHeight());
	}
	
	/**
	 * 显示横屏，隐藏竖屏
	 */
	private void showLand() {
		ivInFull.setImageResource(R.drawable.iv_in_full);
		changeVideo(mPlayer.getVideoWidth(), mPlayer.getVideoHeight());
	}
	
	/**
	 * 初始化surfaceView
	 */
	@SuppressWarnings("deprecation")
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
        	if (data.getVideoUrl() != null) {
            	mPlayer.setDataSource(data.getVideoUrl());
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
						if(mPlayer == null) {
							return;
						}
				        if (mPlayer.isPlaying() && seekBarLand.isPressed() == false) {  
				        	handler.sendEmptyMessage(HANDLER_PROCESS);  
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
						progressBar.setVisibility(View.GONE);
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
	 * 获取评论列表和点赞状态
	 */
	private void OkhttpComment(final String url) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				FormBody.Builder builder = new FormBody.Builder();
				if (!TextUtils.isEmpty(BaseActivity.UID)) {
					builder.add("uid", BaseActivity.UID);
				}
				if (!TextUtils.isEmpty(BaseActivity.TOKEN)) {
					builder.add("token", BaseActivity.TOKEN);
				}
				builder.add("wid", data.videoId);
				builder.add("p", page+"");
				builder.add("size", pageSize+"");
				RequestBody body = builder.build();
				OkHttpUtil.enqueue(new Request.Builder().post(body).url(url).build(), new Callback() {
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
										JSONObject object = new JSONObject(result);
										if (object != null) {
											if (!object.isNull("status")) {
												String status  = object.getString("status");
												if (TextUtils.equals(status, "1")) {//成功
													reContent.setVisibility(View.VISIBLE);
													if (!object.isNull("iszan")) {
														int isZan = object.getInt("iszan");
														if (isZan == 0) {//没有点赞
															ivPraise.setImageResource(R.drawable.iv_unlike);
															isPraise = false;
														}else {//点过赞
															ivPraise.setImageResource(R.drawable.iv_like);
															isPraise = true;
														}
													}
													if (!object.isNull("countzan")) {
														String countZan = object.getString("countzan");
														if (!TextUtils.isEmpty(countZan)) {
															tvPraise.setText(countZan);
														}
													}
													if (!object.isNull("plcount")) {
														tvCommentCount.setText("评论 （"+object.getInt("plcount")+"）");
													}
													if (!object.isNull("pinglun")) {
														mList.clear();
														JSONArray array = object.getJSONArray("pinglun");
														for (int i = 0; i < array.length(); i++) {
															JSONObject itemObj = array.getJSONObject(i);
															PhotoDto dto = new PhotoDto();
															if (!itemObj.isNull("uid")) {
																dto.uid = itemObj.getString("uid");
															}
															if (!itemObj.isNull("id")) {
																dto.commentId = itemObj.getString("id");
															}
															if (!itemObj.isNull("picture")) {
																dto.portraitUrl = itemObj.getString("picture");
															}
															if (!itemObj.isNull("nickname")) {
																dto.nickName = itemObj.getString("nickname");
															}
															if (!itemObj.isNull("phonenumber")) {
																dto.phoneNumber = itemObj.getString("phonenumber");
															}
															if (!itemObj.isNull("createtime")) {
																dto.createTime = itemObj.getString("createtime");
															}
															if (!itemObj.isNull("content")) {
																dto.comment = EmojiMapUtil.replaceCheatSheetEmojis(itemObj.getString("content"));
															}
															mList.add(dto);
														}
														if (mAdapter != null) {
															mAdapter.notifyDataSetChanged();
														}
													}
												}else {
													//失败
													if (!object.isNull("msg")) {
														String msg = object.getString("msg");
														if (msg != null) {
															Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
														}
													}
												}
											}
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
	 * 点赞
	 */
	private void OkhttpPraise(final String url) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				FormBody.Builder builder = new FormBody.Builder();
				builder.add("token", BaseActivity.TOKEN);
				builder.add("uid", BaseActivity.UID);
				builder.add("wid", data.videoId);
				RequestBody body = builder.build();
				OkHttpUtil.enqueue(new Request.Builder().post(body).url(url).build(), new Callback() {
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
										final JSONObject object = new JSONObject(result);
										if (object != null) {
											if (!object.isNull("status")) {
												String status  = object.getString("status");
												if (TextUtils.equals(status, "1")) {//成功
													try {
														if (!object.isNull("iszan")) {
															int isZan = object.getInt("iszan");
															if (isZan == 0) {//没有点赞
																ivPraise.setImageResource(R.drawable.iv_unlike);
																isPraise = false;
															}else {//点过赞
																ivPraise.setImageResource(R.drawable.iv_like);
																isPraise = true;
															}
														}

														if (!object.isNull("countzan")) {
															String countZan = object.getString("countzan");
															if (!TextUtils.isEmpty(countZan)) {
																tvPraise.setText(countZan);
															}
														}
													} catch (JSONException e) {
														e.printStackTrace();
													}
												}
											}
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
	 * 添加一条评论
	 */
	private void OkhttpAddComment(final String url) {
		FormBody.Builder builder = new FormBody.Builder();
		builder.add("token", BaseActivity.TOKEN);
		builder.add("uid", BaseActivity.UID);
		builder.add("wid", data.videoId);
		builder.add("content", EmojiMapUtil.replaceUnicodeEmojis(etComment.getText().toString()));
		final RequestBody body = builder.build();
		new Thread(new Runnable() {
			@Override
			public void run() {
				OkHttpUtil.enqueue(new Request.Builder().post(body).url(url).build(), new Callback() {
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
										JSONObject object = new JSONObject(result);
										if (object != null) {
											if (!object.isNull("status")) {
												int status  = object.getInt("status");
												if (status == 1) {//成功
													etComment.setText("");

													//获取评论列表和点赞状态
													OkhttpComment("http://new.12379.tianqi.cn/Work/getpinglun");
												}
											}
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
	 * 隐藏虚拟键盘
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (etComment != null) {
			CommonUtil.hideInputSoft(etComment, mContext);
		}
		return super.onTouchEvent(event);
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
		case R.id.tvSubTitle:
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
			CommonUtil.hideInputSoft(etComment, mContext);
			dismissColunm();
			
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
		case R.id.ivPraise:
			if (TextUtils.isEmpty(BaseActivity.TOKEN)) {
				startActivityForResult(new Intent(mContext, LoginActivity.class), 1);
			}else {
				if (isPraise == false) {
					OkhttpPraise("http://new.12379.tianqi.cn/Work/setincZan");
				}else {
					Toast.makeText(mContext, "您已点过赞", Toast.LENGTH_SHORT).show();
				}
			}
			break;
		case R.id.ivShare:
			CommonUtil.share(OnlineVideoActivity.this, data.title, data.url, data.videoUrl);
			StatisticUtil.OkhttpShare("http://new.12379.tianqi.cn/Work/getfxzf", data.videoId, data.uid);
			break;
		case R.id.ivClear:
			if (etComment != null) {
				etComment.setText("");
			}
			break;
		case R.id.tvSubmit:
			if (TextUtils.isEmpty(BaseActivity.TOKEN)) {
				startActivityForResult(new Intent(mContext, LoginActivity.class), 2);
			}else {
				if (!TextUtils.isEmpty(etComment.getText().toString())) {
					OkhttpAddComment("http://new.12379.tianqi.cn/Work/addpinglun");
				}
			}
			break;

		default:
			break;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case 1://点赞
				DialogUtil.welcomeDialog(mContext);
				if (isPraise == false) {
					OkhttpPraise("http://new.12379.tianqi.cn/Work/setincZan");
				}else {
					Toast.makeText(mContext, "您已点过赞", Toast.LENGTH_SHORT).show();
				}
				break;
			case 2:
				DialogUtil.welcomeDialog(mContext);
				if (!TextUtils.isEmpty(etComment.getText().toString())) {
					OkhttpAddComment("http://new.12379.tianqi.cn/Work/addpinglun");
				}
				break;

			default:
				break;
			}
		}else {
			//umeng分享回调
			UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);
		}
	}
	
}
