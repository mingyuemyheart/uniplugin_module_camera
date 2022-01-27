package com.camera.activity;

import android.annotation.SuppressLint;
import android.content.Context;
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
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.camera.R;
import com.camera.common.CONST;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 预览、上传界面
 * @author shawn_sun
 */
public class DisplayVideoActivity extends BaseActivity implements SurfaceHolder.Callback, OnPreparedListener, OnVideoSizeChangedListener, OnCompletionListener, OnClickListener{
	
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

	//横屏布局
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
		setContentView(R.layout.activity_display_video);
		mContext = this;
		initWidget();
		initSurfaceView();
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

//		videoUrl = data.videoUrl;
		getThumbnail(videoUrl, new File(videoUrl).getName());
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

		default:
			break;
		}
	}

}
