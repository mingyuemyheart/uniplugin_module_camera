package com.warning.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.media.AudioManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;
import com.warning.R;
import com.warning.adapter.CameraAdapter;
import com.warning.common.CONST;
import com.warning.dto.PhotoDto;
import com.warning.util.CommonUtil;

/**
 * 拍摄视频、图片
 * @author shawn_sun
 *
 */

public class CameraActivity extends Activity implements SurfaceHolder.Callback, OnClickListener, AMapLocationListener{
	
	private Context mContext = null;
	private SurfaceView surfaceView = null;
	private SurfaceHolder surfaceHolder = null;
	private MediaRecorder mRecorder = null;// 录制视频的类
	private Camera camera = null;//相机
	private TextView tvTime;//拍摄时间
	private TextView tvStayLandscape = null;//保持横屏提示
	private boolean isRecording = false;//是否正在录制
	private String intentVideoUrl = null;//传到预览界面url
	private int curCameraId = 0;//0是后置摄像头，1是前置摄像头
	private long mExitTime;//记录点击完返回按钮后的long型时间
	private boolean isRecorderOrCamera = true;//true为录像，false为拍照
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
	private boolean isFull = false;//判断gridview是否够9张
	private CameraAdapter mAdapter = null;
	private List<PhotoDto> mList = new ArrayList<>();
	private int displayW = 0;//屏幕宽
	private int displayH = 0;//屏幕高
	private int degree = 0;//保存的图片要旋转的角度
	private OrientationEventListener orienListener = null;//屏幕旋转方向监听器
	private RelativeLayout reToUp = null;
	private RelativeLayout reToDown = null;
	private AMapLocationClientOption mLocationOption = null;//声明mLocationOption对象
	private AMapLocationClient mLocationClient = null;//声明AMapLocationClient类对象
	private String fileName = null;
	private String proName = "", cityName = "", disName = "", roadName = "", aoiName = "";//定位城市名称
	private String lat = "0", lng = "0";
	private int miss = 0;//时间计数
	private TimeThread timeThread = null;
	private boolean isTokePhotoFirst = true;//是否是第一次拍摄照片
	
	//横屏布局
	private SeekBar seekBarLeft = null;
	private SeekBar seekBarRight = null;
	private GridView mGridViewLand = null;
	private ImageView ivFlash = null;//闪光
	private ImageView ivSwitcherLand = null;//前置后置摄像头切换
	private ImageView ivStartLand = null;//拍摄按钮
	private ImageView ivChangeLand = null;//切换摄像机或者照相机
	private ImageView ivDoneLand = null;//完成录制按钮
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_camera);
		mContext = this;
		initWidget();
		initSurfaceView();
		loadGridViewData();
		initGridView();
	}
	
	/**
	 * 开始定位
	 */
	private void startLocation() {
        mLocationOption = new AMapLocationClientOption();//初始化定位参数
        mLocationClient = new AMapLocationClient(mContext);//初始化定位
        mLocationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);//设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setNeedAddress(true);//设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setOnceLocation(true);//设置是否只定位一次,默认为false
        mLocationOption.setWifiActiveScan(true);//设置是否强制刷新WIFI，默认为强制刷新
        mLocationOption.setMockEnable(false);//设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocationOption.setInterval(2000);//设置定位间隔,单位毫秒,默认为2000ms
        mLocationClient.setLocationOption(mLocationOption);//给定位客户端对象设置定位参数
        mLocationClient.setLocationListener(this);
        mLocationClient.startLocation();//启动定位
	}
	
	@Override
	public void onLocationChanged(AMapLocation amapLocation) {
        if (amapLocation != null && amapLocation.getErrorCode() == 0) {
        	lat = amapLocation.getLatitude()+"";
        	lng = amapLocation.getLongitude()+"";
        	proName = amapLocation.getProvince();
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
        	cityName = amapLocation.getCity();
        	disName = amapLocation.getDistrict();
        	roadName = amapLocation.getRoad();
        	aoiName = amapLocation.getAoiName();
        	ivStartLand.setVisibility(View.VISIBLE);
        }
	}

	/**
	 * 初始化控件
	 */
	private void initWidget() {
		//横屏布局
		ivFlash = (ImageView) findViewById(R.id.ivFlash);
		ivFlash.setOnClickListener(this);
		ivSwitcherLand = (ImageView) findViewById(R.id.ivSwitcherLand);
		ivSwitcherLand.setOnClickListener(this);
		ivStartLand = (ImageView) findViewById(R.id.ivStartLand);
		ivStartLand.setOnClickListener(this);
		ivChangeLand = (ImageView) findViewById(R.id.ivChangeLand);
		ivChangeLand.setOnClickListener(this);
		ivDoneLand = (ImageView) findViewById(R.id.ivDoneLand);
		ivDoneLand.setOnClickListener(this);
		tvTime = (TextView) findViewById(R.id.tvTime);
		tvStayLandscape = (TextView) findViewById(R.id.tvStayLandscape);
		reToUp = (RelativeLayout) findViewById(R.id.reToUp);
		reToDown = (RelativeLayout) findViewById(R.id.reToDown);
		seekBarLeft = (SeekBar) findViewById(R.id.seekBarLeft);
		seekBarLeft.setProgress(0);
		seekBarLeft.setMax(CONST.TIME);
		seekBarLeft.setOnTouchListener(seekbarListener);
		seekBarRight = (SeekBar) findViewById(R.id.seekBarRight);
		seekBarRight.setMax(CONST.TIME);
		seekBarRight.setProgress(seekBarRight.getMax());
		seekBarRight.setOnTouchListener(seekbarListener);
		
		DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(dm);
        displayW = dm.widthPixels;
        displayH = dm.heightPixels;

//		if (android.os.Build.MODEL.contains("Hisense")) {
//			ivFlash.setVisibility(View.GONE);
//		}
		
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				if (tvStayLandscape != null) {
					tvStayLandscape.setVisibility(View.GONE);
				}
			}
		}, 5000);
		
		startLocation();
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

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				if (miss == CONST.TIME) {
					ivDoneLand.setVisibility(View.GONE);
					completeRecorder();
					
					PhotoDto data = new PhotoDto();
					data.setWorkstype("video");
					data.setWorkTime(fileName);
					data.setVideoUrl(intentVideoUrl);
					Intent intent = new Intent(mContext, DisplayVideoActivity.class);
					Bundle bundle = new Bundle();
					bundle.putParcelable("data", data);
					intent.putExtras(bundle);
					startActivity(intent);
				} else {
					miss++;
					tvTime.setText(String.valueOf(CommonUtil.formatMiss2(miss)));
					seekBarLeft.setProgress(miss);
					seekBarRight.setProgress(CONST.TIME-miss);

					if (miss == CONST.TIME-30) {
						Toast.makeText(mContext, "最大拍摄时长2分钟", Toast.LENGTH_LONG).show();
					}else if (miss == CONST.TIME-10) {
						Toast.makeText(mContext, "10秒后拍摄将自动完成", Toast.LENGTH_LONG).show();
					}
				}
				break;
				case 1:
					miss = 0;
					tvTime.setText(String.valueOf(CommonUtil.formatMiss2(miss)));
					seekBarLeft.setProgress(miss);
					seekBarRight.setProgress(CONST.TIME-miss);
					break;

			default:
				break;
			}
		};
	};
	
	private class TimeThread extends Thread {
		static final int STATE_START = 0;
		static final int STATE_CANCEL = 1;
		private int state;
		
		@Override
		public void run() {
			super.run();
			this.state = STATE_START;
			while (true) {
				if (state == STATE_CANCEL) {
					break;
				}
				try {
					Thread.sleep(1000);
					Message msg = new Message();
					msg.what = state;
					handler.sendMessage(msg);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		public void cancel() {
			this.state = STATE_CANCEL;
		}
	}
	
	/**
	 * 开启时间监听线程
	 */
	private void startTime() {
		cancelTime();
		tvTime.setVisibility(View.VISIBLE);
		seekBarLeft.setVisibility(View.VISIBLE);
		seekBarRight.setVisibility(View.VISIBLE);
		timeThread = new TimeThread();
		timeThread.start();
	}
	
	/**
	 * 取消时间监听线程
	 */
	private void cancelTime() {
		if (timeThread != null) {
			timeThread.cancel();
			timeThread = null;
		}
		miss = 0;
		tvTime.setText(String.valueOf(CommonUtil.formatMiss2(miss)));
	}
	
	/**
	 * 相机动画
	 * @param flag false为初始化打开，true为拍照时动画
	 */
	private void startAnimation(boolean flag) {
		AnimationSet animup = new AnimationSet(true);
		TranslateAnimation mytranslateanimup0 = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF,0f,
				Animation.RELATIVE_TO_SELF,0f,
				Animation.RELATIVE_TO_SELF,-1.0f,
				Animation.RELATIVE_TO_SELF,0f);
		mytranslateanimup0.setDuration(500);
		TranslateAnimation mytranslateanimup1 = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF,0f,
				Animation.RELATIVE_TO_SELF,0f,
				Animation.RELATIVE_TO_SELF,0f,
				Animation.RELATIVE_TO_SELF,-1.0f);
		mytranslateanimup1.setDuration(500);
		mytranslateanimup1.setStartOffset(500);
		if (flag) {
			animup.addAnimation(mytranslateanimup0);
		}
		animup.addAnimation(mytranslateanimup1);
		animup.setFillAfter(true);
		reToUp.startAnimation(animup);
		
		AnimationSet animdn = new AnimationSet(true);
		TranslateAnimation mytranslateanimdn0 = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF,0f,
				Animation.RELATIVE_TO_SELF,0f,
				Animation.RELATIVE_TO_SELF,1.0f,
				Animation.RELATIVE_TO_SELF,0f);
		mytranslateanimdn0.setDuration(500);
		TranslateAnimation mytranslateanimdn1 = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF,0f,
				Animation.RELATIVE_TO_SELF,0f,
				Animation.RELATIVE_TO_SELF,0f,
				Animation.RELATIVE_TO_SELF,1.0f);
		mytranslateanimdn1.setDuration(500);
		mytranslateanimdn1.setStartOffset(500);
		if (flag) {
			animdn.addAnimation(mytranslateanimdn0);
		}
		animdn.addAnimation(mytranslateanimdn1);
		animdn.setFillAfter(true);
		reToDown.startAnimation(animdn);	
		animdn.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation arg0) {
				ivStartLand.setClickable(false);
				ivDoneLand.setClickable(false);
			}
			@Override
			public void onAnimationRepeat(Animation arg0) {
			}
			@Override
			public void onAnimationEnd(Animation arg0) {
				ivStartLand.setClickable(true);
				ivDoneLand.setClickable(true);
			}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		startAnimation(false);
	}
	
	/**
	 * 加载九宫格数据
	 */
	private void loadGridViewData() {
		mList.clear();
		for (int i = 0; i < 9; i++) {
			PhotoDto dto = new PhotoDto();
			dto.setState(false);
			dto.setWorkstype("imgs");
			mList.add(dto);
		}
	}
	
	/**
	 * 初始化gridview
	 */
	private void initGridView() {
		loadGridViewData();
		mGridViewLand = (GridView) findViewById(R.id.gridViewLand);
		mAdapter = new CameraAdapter(mContext, mList);
		mGridViewLand.setAdapter(mAdapter);
	}
	
	/**
	 * 初始化surfaceView
	 */
	@SuppressWarnings("deprecation")
	private void initSurfaceView() {
		surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
		surfaceView.setOnTouchListener(new TouchListener());
		surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback(this);
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		surfaceHolder = holder;
		initCamera();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int arg1, int arg2, int arg3) {
		surfaceHolder = holder;
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		surfaceHolder = holder;
		surfaceView = null;  
        surfaceHolder = null;  
		releaseCamera();
        releaseMediaRecorder();
	}
	
	/**
	 * 初始化camera
	 */
	private void initCamera() {
		camera = Camera.open(curCameraId);
		try {
			camera.setPreviewDisplay(surfaceHolder);
		} catch (IOException e) {
			e.printStackTrace();
		}  
        camera.setDisplayOrientation(CommonUtil.setCameraDisplayOrientation(this, curCameraId, camera));
        
        Camera.Parameters parameters = camera.getParameters();
        
        List<Size> preList = parameters.getSupportedPreviewSizes();
        int preWidth = 0;
        int preHeight = 0;
        int surWidth = 0;
        int surHeight = 0;
        for (int i = 0; i < preList.size(); i++) {
			if (Double.valueOf(preList.get(i).width)/Double.valueOf(preList.get(i).height) == Double.valueOf(displayW)/Double.valueOf(displayH)) {
				if (preWidth <= preList.get(i).width && preHeight <= preList.get(i).height) {
					preWidth = preList.get(i).width;
					preHeight = preList.get(i).height;
				}
				surWidth = Math.max(displayW, displayH);
				surHeight = surWidth * preHeight / preWidth ;
			}
		}
        if (preWidth > 0 && preHeight > 0) {
        	parameters.setPreviewSize(preWidth, preHeight);// 设置预览照片的大小
            if (surfaceView != null) {
        		surfaceView.setLayoutParams(new RelativeLayout.LayoutParams(surWidth, surHeight));//设置surfaceView大小
    		}
		}else {
			parameters.setPreviewSize(preList.get(0).width, preList.get(0).height);// 设置预览照片的大小
		}
        
        List<int[]> list = new ArrayList<>();
        List<Size> picList = parameters.getSupportedPictureSizes();
        for (int i = 0; i < picList.size(); i++) {
			if (picList.get(i).width >= 400 && picList.get(i).width < displayW && picList.get(i).height >= 400 && picList.get(i).height < displayH) {
				list.add(new int[]{picList.get(i).width, picList.get(i).height});
			}
		}
        
        int width = list.get(0)[0];
        int height = list.get(0)[1];
        for (int i = 0; i < list.size(); i++) {
        	if (width >= list.get(i)[0] && height >= list.get(i)[1]) {
        		width = list.get(i)[0];
        		height = list.get(i)[1];
			}
		}
        parameters.setPictureSize(width, height);// 设置照片的大小
        
        List<String> focusList = parameters.getSupportedFocusModes();
        if (focusList.contains(Parameters.FOCUS_MODE_AUTO)) {
        	parameters.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        }
        parameters.set("jpeg-quality", 80);// 设置JPG照片的质量
        camera.setParameters(parameters);
        camera.startPreview(); 
	}
	
	private void initMediaRecorder() {
		if (mRecorder == null) {
			mRecorder = new MediaRecorder();
		}
		mRecorder.reset();
		mRecorder.setCamera(camera);
		mRecorder.setOrientationHint(CommonUtil.setCameraDisplayOrientation(this, curCameraId, camera));//对保存后的视频设置正确方向
		mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);// 视频源
		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC); // 录音源为麦克风
		mRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_720P));
		mRecorder.setVideoFrameRate(20);
		mRecorder.setVideoEncodingBitRate(3000000);
		mRecorder.setPreviewDisplay(surfaceHolder.getSurface());// 预览
	}
	
	/**
	 * 播放声音
	 */
	private void playSound(boolean startUpload) {
		ToneGenerator tone = new ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME);
		if (startUpload) {
			tone.startTone(ToneGenerator.TONE_PROP_BEEP);
		} else {
			tone.startTone(ToneGenerator.TONE_PROP_BEEP2);
		}
	}
	
	/**
	 * camera拍照
	 */
	private void takePhoto() {
		if (isFull) {
			Toast.makeText(mContext, "最多只能拍9张照片", Toast.LENGTH_SHORT).show();
			return;
		}
		if (camera != null) {
			//屏幕旋转方向监听器
			orienListener = new OrientationEventListener(mContext) {
				@Override
				public void onOrientationChanged(int orientations) {
					CameraInfo info = new Camera.CameraInfo();
					Camera.getCameraInfo (curCameraId , info);
					if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {//前置摄像头
						if (orientations > 325 || orientations <= 45) {
							degree = 270;
						} else if (orientations > 45 && orientations <= 135) {
							degree = 180;
						} else if (orientations > 135 && orientations < 225) {
							degree = 90;
						} else {
							degree = 0;
						}
					}else {
						if (orientations > 325 || orientations <= 45) {
							degree = 90;
						} else if (orientations > 45 && orientations <= 135) {
							degree = 180;
						} else if (orientations > 135 && orientations < 225) {
							degree = 270;
						} else {
							degree = 0;
						}
					}
				}
			};
			if (orienListener != null) {
				orienListener.enable();
			}
			startAnimation(true);
			camera.takePicture(null, null, new PictureCallback() {
				@Override
				public void onPictureTaken(byte[] data, Camera c) {
//					playSound(true);
					Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
					Matrix matrix = new Matrix();
					matrix.preRotate(degree);
					bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

					if (isTokePhotoFirst) {
						fileName = sdf.format(System.currentTimeMillis());
						isTokePhotoFirst = false;
					}
					File files = new File(CONST.PICTURE_ADDR+File.separator+fileName);
					if (!files.exists()) {
						files.mkdirs();
					}
					String picName = sdf.format(System.currentTimeMillis());
				    File file = new File(files.getPath()+File.separator+picName+CONST.PICTURETYPE);

					//保存本地拍摄照片信息，这里是照片的文件夹
					SharedPreferences sp = getSharedPreferences(fileName, Context.MODE_PRIVATE);
					SharedPreferences.Editor editor = sp.edit();
					editor.putString("fileName", fileName);
					editor.putString("proName", proName);
					editor.putString("cityName", cityName);
					editor.putString("disName", disName);
					editor.putString("roadName", roadName);
					editor.putString("aoiName", aoiName);
					editor.putString("lat", lat);
					editor.putString("lng", lng);
					editor.apply();
				    
					try {
						asynCompressBitmap(bitmap, file);  
					    
					    if (orienListener != null) {
							orienListener.disable();
						}
					    
					    for (int i = 0; i < mList.size(); i++) {
					    	if (i == mList.size()-1) {
								isFull = true;
							}
							if (mList.get(i).isState() == false) {
								PhotoDto dto = new PhotoDto();
							    dto.setState(true);
								dto.setWorkstype("imgs");
							    dto.setUrl(CONST.PICTURE_ADDR + fileName + CONST.PICTURETYPE);
							    mList.set(i, dto);
							    break;
							}
						}
					    if (mAdapter != null) {
							mAdapter.notifyDataSetChanged();
						}
					    
					    c.stopPreview();
					    c.startPreview();// 在拍照的时候相机是被占用的,拍照之后需要重新预览
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				}
			});
		}
	}
	
	/**
	 * 释放摄像头
	 */
	private void releaseCamera() {
		if (camera != null) {
			camera.setPreviewCallback(null);
			camera.stopPreview();
			camera.release();
			camera = null;
			if (orienListener != null) {
				orienListener.disable();
			}
		} 
	}
	
	/**
	 * 异步压缩保存图片到本地
	 * @param bitmap
	 * @param file
	 */
	private void asynCompressBitmap(Bitmap bitmap, File file) {
		AsynLoadTask task = new AsynLoadTask(bitmap, file);  
        task.execute();
	}
	
	private class AsynLoadTask extends AsyncTask<Void, Bitmap, Void> {
		
		private Bitmap bitmap;
		private File file;

		private AsynLoadTask(Bitmap bitmap, File file) {
			this.bitmap = bitmap;
			this.file = file;
		}

		@Override
		protected void onPreExecute() {
		}
		
		@Override
		protected void onProgressUpdate(Bitmap... values) {
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				FileOutputStream fos = new FileOutputStream(file);
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
//			fos.write(data);
//		    fos.flush();
				fos.close();  
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

	}

	/**
	 * 开始录制
	 */
	private void startRecorder() {
		ivStartLand.setClickable(false);
		ivDoneLand.setClickable(false);
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				ivStartLand.setClickable(true);
				ivDoneLand.setClickable(true);
			}
		}, 5000);
        releaseMediaRecorder();
		initMediaRecorder();
		File files = new File(CONST.VIDEO_ADDR);
		if (!files.exists()) {
			files.mkdirs();
		}

		fileName = sdf.format(System.currentTimeMillis());
		intentVideoUrl = files.getPath() + File.separator + fileName + CONST.VIDEOTYPE;
		mRecorder.setOutputFile(intentVideoUrl);// 保存路径

		//保存本地拍摄视频信息
		SharedPreferences sp = getSharedPreferences(fileName, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sp.edit();
		editor.putString("fileName", fileName);
		editor.putString("proName", proName);
		editor.putString("cityName", cityName);
		editor.putString("disName", disName);
		editor.putString("roadName", roadName);
		editor.putString("aoiName", aoiName);
		editor.putString("lat", lat);
		editor.putString("lng", lng);
		editor.apply();
		
		camera.unlock();
		try {
			mRecorder.prepare();
			mRecorder.start();
			startTime();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * 保存缩略图
	 * @param fileName
	 * @param videoUrl
	 */
	private void compressThumbnail(String fileName, String videoUrl) {
		Bitmap thumbBitmap = CommonUtil.getVideoThumbnail(videoUrl, displayW/4, displayW/4, MediaStore.Images.Thumbnails.MINI_KIND);
		File thumbnails = new File(CONST.THUMBNAIL_ADDR);
		if (!thumbnails.exists()) {
			thumbnails.mkdirs();
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
	 * 释放MediaRecorder
	 */
	private void releaseMediaRecorder() {
		if (mRecorder != null) {
			mRecorder.release();
			mRecorder = null;
		}
	}
	
	/**
	 * 切换摄像头
	 * 0 是后置，1是前置
	 */
	private void switchCamera(int cameraId) {
        if(cameraId  == Camera.CameraInfo.CAMERA_FACING_BACK) {
        	ivFlash.setVisibility(View.VISIBLE);
        }else if(cameraId  == Camera.CameraInfo.CAMERA_FACING_FRONT) {
        	ivFlash.setVisibility(View.GONE);
        }
        
        releaseCamera();
        initCamera();
        releaseMediaRecorder();
        initMediaRecorder();
	}
	
	/**
	 * 广播，通知相册更新视频和照片
	 */
	private void sendMediaBroadcast() {
		ContentValues cv = new ContentValues(2);
		cv.put(MediaStore.Video.Media.MIME_TYPE, "image/jpeg");
		cv.put(MediaStore.Video.Media.DATA, intentVideoUrl);
		boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
		Intent intent = null;
		Uri uri = null;
		if (isKitKat) {
			intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
			uri = Uri.fromFile(new File(intentVideoUrl));
//			intent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_DIR");
//			uri = Uri.fromFile(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + CONST.VIDEO_ADDR));
		}else {
			intent = new Intent(Intent.ACTION_MEDIA_MOUNTED);
			uri = Uri.parse("file://"+ Environment.getExternalStorageDirectory());
		}
		intent.setData(uri);
		sendBroadcast(intent);
	}
	
	/**
	 * 完成录制视频
	 */
	private void completeRecorder() {
		if (isRecording) {
			sendMediaBroadcast();
			
			if (mRecorder != null) {
				mRecorder.reset();
				mRecorder.release();
			}
			
			ivStartLand.setBackgroundResource(R.drawable.iv_start);
			ivChangeLand.setVisibility(View.VISIBLE);
			ivSwitcherLand.setVisibility(View.VISIBLE);
			cancelTime();
			isRecording = false;
		}

		compressThumbnail(fileName, intentVideoUrl);
	}
	
	/**
	 * 完成拍照
	 */
	private void completeTakePhoto() {
		List<PhotoDto> selectList = new ArrayList<>();
		for (int i = 0; i < mList.size(); i++) {
			if (mList.get(i).isState()) {
				selectList.add(mList.get(i));//把所有数据加载到照片墙list里
			}
		}
	    mList.clear();
	    ivChangeLand.setVisibility(View.VISIBLE);
		ivDoneLand.setVisibility(View.GONE);
		isFull = false;
	    PhotoDto data = new PhotoDto();
		data.setWorkstype("imgs");
		data.setWorkTime(fileName);
		Intent intent = new Intent(mContext, DisplayPictureActivity.class);
		Bundle bundle = new Bundle();
		bundle.putParcelable("data", data);
		intent.putExtras(bundle);
		startActivity(intent);
		
		fileName = "";
		isTokePhotoFirst = true;
	}
	
	/**
	 * 退出
	 */
	private void exit() {
		if (isRecording) {
			if ((System.currentTimeMillis() - mExitTime) > 2000) {
				Toast.makeText(mContext, "再按一次停止录像", Toast.LENGTH_SHORT).show();
				mExitTime = System.currentTimeMillis();
			} else {
				completeRecorder();
				startAnimation(true);
				finish();
			}
		}else {
			startAnimation(true);
			finish();
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		loadGridViewData();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			exit();
		}else if (keyCode == KeyEvent.KEYCODE_ENTER) {
			clickStart();
			return false;
		}else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
			return true;
		}else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
			return true;
		}
		return false;
	}
	
	private void clickStart() {
		if (isRecorderOrCamera) {//拍视频
			if (isRecording) {
				completeRecorder();
			}else {
				ivStartLand.setBackgroundResource(R.drawable.iv_stop);
				ivChangeLand.setVisibility(View.GONE);
				ivDoneLand.setVisibility(View.VISIBLE);
				ivSwitcherLand.setVisibility(View.GONE);
				startRecorder();
				isRecording = true;
			}
		}else {//拍照皮
			mGridViewLand.setVisibility(View.VISIBLE);
			ivChangeLand.setVisibility(View.GONE);
			ivDoneLand.setVisibility(View.VISIBLE);
			takePhoto();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ivStartLand:
			clickStart();
			break;
		case R.id.ivChangeLand:
			if (isRecorderOrCamera) {
				isRecorderOrCamera = false;
				ivChangeLand.setImageResource(R.drawable.iv_shexiang);
				mGridViewLand.setVisibility(View.VISIBLE);
				tvTime.setVisibility(View.GONE);
				seekBarLeft.setVisibility(View.GONE);
				seekBarRight.setVisibility(View.GONE);
			}else {
				isRecorderOrCamera = true;
				ivChangeLand.setImageResource(R.drawable.iv_paizhao);
				mGridViewLand.setVisibility(View.GONE);
				tvTime.setVisibility(View.VISIBLE);
				seekBarLeft.setVisibility(View.VISIBLE);
				seekBarRight.setVisibility(View.VISIBLE);
			}
			ivDoneLand.setVisibility(View.GONE);
			break;
		case R.id.ivFlash:
			if (camera != null) {
				Camera.Parameters parameters = camera.getParameters();
				String flashMode = parameters.getFlashMode();
				if (flashMode != null) {
					if (flashMode.equals(Camera.Parameters.FLASH_MODE_OFF)) {
						parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
						ivFlash.setImageResource(R.drawable.iv_flash_on);
					}else {
						parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
						ivFlash.setImageResource(R.drawable.iv_flash_off);
					}
				}
//					if (android.os.Build.MODEL.contains("Hisense")) {
//						return;
//					}
				camera.setParameters(parameters);
			}
			break;
		case R.id.ivSwitcherLand:
	        int cameraCount = Camera.getNumberOfCameras();//得到摄像头的个数
	        if (cameraCount > 1) {
	        	if (curCameraId == 0) {
					curCameraId = 1;
				}else {
					curCameraId = 0;
				}
				switchCamera(curCameraId);
			}else {
				Toast.makeText(mContext, "该设备只有一个摄像头", Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.ivDoneLand:
			if (isRecorderOrCamera) {
				if (isRecording) {
					if ((System.currentTimeMillis() - mExitTime) > 2000) {
						Toast.makeText(mContext, "再按一次停止录像", Toast.LENGTH_SHORT).show();
						mExitTime = System.currentTimeMillis();
					} else {
						ivDoneLand.setVisibility(View.GONE);
						completeRecorder();
						
						PhotoDto data = new PhotoDto();
						data.setWorkstype("video");
						data.setWorkTime(fileName);
						data.setVideoUrl(intentVideoUrl);
						Intent intent = new Intent(mContext, DisplayVideoActivity.class);
						Bundle bundle = new Bundle();
						bundle.putParcelable("data", data);
						intent.putExtras(bundle);
						startActivity(intent);
					}
				}else {
					ivDoneLand.setVisibility(View.GONE);
					PhotoDto data = new PhotoDto();
					data.setWorkstype("video");
					data.setWorkTime(fileName);
					data.setVideoUrl(intentVideoUrl);
					Intent intent = new Intent(mContext, DisplayVideoActivity.class);
					Bundle bundle = new Bundle();
					bundle.putParcelable("data", data);
					intent.putExtras(bundle);
					startActivity(intent);
				}
			}else {
				completeTakePhoto();
			}
			break;

		default:
			break;
		}
	}
	
	private class TouchListener implements OnTouchListener {
		
		private float startDistance = 0;//初始两点间距离

		@Override
		public boolean onTouch(View arg0, MotionEvent event) {
			Camera.Parameters parameters = camera.getParameters();
			if (!parameters.isZoomSupported()) {
				return true;
			}
			switch (event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:
				break;
			case MotionEvent.ACTION_POINTER_DOWN:
				startDistance = distance(event);
				break;
			case MotionEvent.ACTION_MOVE:
				if (event.getPointerCount() < 2) {//只有同时触屏两个点的时候才执行
					return true;
				}
				float endDistance = distance(event);// 结束两点间距离
				int tempZoom = (int) ((endDistance - startDistance) / 20f);
				if (tempZoom >= 1 || tempZoom <= -1) {
					int zoom = parameters.getZoom() + tempZoom;
					if (zoom > parameters.getMaxZoom()) {
						zoom = parameters.getMaxZoom();
					}
					if (zoom < 0) {
						zoom = 0;
					}
					parameters.setZoom(zoom);
//					if (!android.os.Build.MODEL.contains("Hisense")) {
						camera.setParameters(parameters);
//					}
					startDistance = endDistance;
				}
				break;
			case MotionEvent.ACTION_UP:
				break;
			}
			return true;
		}
		
		/** 计算两个手指间的距离 */
		private float distance(MotionEvent event) {
			float dx = event.getX(1) - event.getX(0);
			float dy = event.getY(1) - event.getY(0);
			/** 使用勾股定理返回两点之间的距离 */
			return (float) Math.sqrt(dx * dx + dy * dy);
		}
	}
	
}
