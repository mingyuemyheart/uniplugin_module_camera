package com.camera.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.camera.R;
import com.camera.adapter.CameraAdapter;
import com.camera.common.CONST;
import com.camera.dto.PhotoDto;
import com.camera.util.AuthorityUtil;
import com.camera.util.CommonUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * ?????????????????????
 * @author shawn_sun
 */
public class VideoActivity extends Activity implements SurfaceHolder.Callback, OnClickListener {

    private Context mContext = null;
    private SurfaceView surfaceView = null;
    private SurfaceHolder surfaceHolder = null;
    private MediaRecorder mRecorder = null;// ??????????????????
    private Camera camera = null;//??????
    private boolean isRecording = false;//??????????????????
    private String intentVideoUrl = null;//??????????????????url
    private int curCameraId = 0;//0?????????????????????1??????????????????
    private long mExitTime;//?????????????????????????????????long?????????
    private boolean isRecorderOrCamera = false;//true????????????false?????????
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA);
    private boolean isFull = false;//??????gridview?????????9???
    private CameraAdapter mAdapter = null;
    private List<PhotoDto> mList = new ArrayList<>();
    private int displayW = 0;//?????????
    private int displayH = 0;//?????????
    private int degree = 0;//?????????????????????????????????
    private OrientationEventListener orienListener = null;//???????????????????????????
    private ConstraintLayout reToUp = null;
    private ConstraintLayout reToDown = null;
    private String fileName = null;
    private int miss = 0;//????????????
    private TimeThread timeThread = null;
    private boolean isTokePhotoFirst = true;//??????????????????????????????

    private TextView tvTime;//????????????
    private GridView mgridView = null;
    private ImageView ivFlash = null;//??????
    private ImageView ivSwitcher = null;//???????????????????????????
    private ImageView ivStart = null;//????????????
    private ImageView ivChange = null;//??????????????????????????????
    private ImageView ivDone = null;//??????????????????

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_video);
        mContext = this;
        checkMultiAuthority();
    }

    /**
     * ??????????????????
     */
    private void checkMultiAuthority() {
        if (Build.VERSION.SDK_INT < 23) {
            init();
        } else {
            AuthorityUtil.deniedList.clear();
            for (String permission : AuthorityUtil.allPermissions) {
                if (ContextCompat.checkSelfPermission(mContext, permission) != PackageManager.PERMISSION_GRANTED) {
                    AuthorityUtil.deniedList.add(permission);
                }
            }
            if (AuthorityUtil.deniedList.isEmpty()) {//?????????????????????
                init();
            } else {
                String[] permissions = AuthorityUtil.deniedList.toArray(new String[AuthorityUtil.deniedList.size()]);//???list????????????
                ActivityCompat.requestPermissions(this, permissions, AuthorityUtil.AUTHOR_MULTI);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case AuthorityUtil.AUTHOR_MULTI:
                if (grantResults.length > 0) {
                    boolean isAllGranted = true;//??????????????????
                    for (int gResult : grantResults) {
                        if (gResult != PackageManager.PERMISSION_GRANTED) {
                            isAllGranted = false;
                            break;
                        }
                    }
                    if (isAllGranted) {//?????????????????????
                        init();
                    }
                }
                break;
        }
    }

    private void init() {
        initGridView();
        initWidget();
        initSurfaceView();
    }

    /**
     * ???????????????
     */
    private void initWidget() {
        ivFlash = findViewById(R.id.ivFlash);
        ivFlash.setOnClickListener(this);
        ivSwitcher = findViewById(R.id.ivSwitcher);
        ivSwitcher.setOnClickListener(this);
        ivStart = findViewById(R.id.ivStart);
        ivStart.setOnClickListener(this);
        ivChange = findViewById(R.id.ivChange);
        ivChange.setOnClickListener(this);
        ivDone = findViewById(R.id.ivDone);
        ivDone.setOnClickListener(this);
        tvTime = findViewById(R.id.tvTime);
        reToUp = findViewById(R.id.reToUp);
        reToDown = findViewById(R.id.reToDown);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(dm);
        displayW = dm.widthPixels;
        displayH = dm.heightPixels;

        if (isRecorderOrCamera) {
            ivChange.setImageResource(R.drawable.iv_shexiang);
            mgridView.setVisibility(View.VISIBLE);
            tvTime.setVisibility(View.GONE);
        } else {
            ivChange.setImageResource(R.drawable.iv_paizhao);
            mgridView.setVisibility(View.GONE);
            tvTime.setVisibility(View.VISIBLE);
        }
    }

    /**
     * ??????seekbar????????????
     */
    private OnTouchListener seekbarListener = (arg0, arg1) -> true;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    if (miss == CONST.TIME) {
                        ivDone.setVisibility(View.GONE);
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
                        tvTime.setText(CommonUtil.formatMiss2(miss));

                        if (miss == CONST.TIME - 30) {
                            Toast.makeText(mContext, "??????????????????2??????", Toast.LENGTH_LONG).show();
                        } else if (miss == CONST.TIME - 10) {
                            Toast.makeText(mContext, "10???????????????????????????", Toast.LENGTH_LONG).show();
                        }
                    }
                    break;
                case 1:
                    miss = 0;
                    tvTime.setText(CommonUtil.formatMiss2(miss));
                    break;

                default:
                    break;
            }
        }
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
     * ????????????????????????
     */
    private void startTime() {
        cancelTime();
        tvTime.setVisibility(View.VISIBLE);
        timeThread = new TimeThread();
        timeThread.start();
    }

    /**
     * ????????????????????????
     */
    private void cancelTime() {
        if (timeThread != null) {
            timeThread.cancel();
            timeThread = null;
        }
        miss = 0;
        tvTime.setText(CommonUtil.formatMiss2(miss));
    }

    /**
     * ????????????
     * @param flag false?????????????????????true??????????????????
     */
    private void startAnimation(boolean flag) {
        AnimationSet animup = new AnimationSet(true);
        TranslateAnimation mytranslateanimup0 = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, -1.0f,
                Animation.RELATIVE_TO_SELF, 0f);
        mytranslateanimup0.setDuration(500);
        TranslateAnimation mytranslateanimup1 = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, -1.0f);
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
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 1.0f,
                Animation.RELATIVE_TO_SELF, 0f);
        mytranslateanimdn0.setDuration(500);
        TranslateAnimation mytranslateanimdn1 = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 1.0f);
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
                ivStart.setClickable(false);
                ivDone.setClickable(false);
            }

            @Override
            public void onAnimationRepeat(Animation arg0) {
            }

            @Override
            public void onAnimationEnd(Animation arg0) {
                ivStart.setClickable(true);
                ivDone.setClickable(true);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        startAnimation(false);
    }

    /**
     * ?????????????????????
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
     * ?????????gridview
     */
    private void initGridView() {
        loadGridViewData();
        mgridView = findViewById(R.id.gridView);
        mAdapter = new CameraAdapter(mContext, mList);
        mgridView.setAdapter(mAdapter);
    }

    /**
     * ?????????surfaceView
     */
    @SuppressWarnings("deprecation")
    private void initSurfaceView() {
        surfaceView = findViewById(R.id.surfaceView);
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
     * ?????????camera
     */
    private void initCamera() {
        camera = Camera.open(curCameraId);
        try {
            camera.setPreviewDisplay(surfaceHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        camera.setDisplayOrientation(CommonUtil.setCameraDisplayOrientation(this, curCameraId, camera));

        Parameters parameters = camera.getParameters();

        List<Size> preList = parameters.getSupportedPreviewSizes();
        int preWidth = 0;
        int preHeight = 0;
        int surWidth = 0;
        int surHeight = 0;
        for (int i = 0; i < preList.size(); i++) {
            if (Double.valueOf(preList.get(i).width) / Double.valueOf(preList.get(i).height) == Double.valueOf(displayW) / Double.valueOf(displayH)) {
                if (preWidth <= preList.get(i).width && preHeight <= preList.get(i).height) {
                    preWidth = preList.get(i).width;
                    preHeight = preList.get(i).height;
                }
                surWidth = Math.max(displayW, displayH);
                surHeight = surWidth * preHeight / preWidth;
            }
        }
        if (preWidth > 0 && preHeight > 0) {
            parameters.setPreviewSize(preWidth, preHeight);// ???????????????????????????
            if (surfaceView != null) {
                surfaceView.setLayoutParams(new RelativeLayout.LayoutParams(surWidth, surHeight));//??????surfaceView??????
            }
        } else {
            parameters.setPreviewSize(preList.get(0).width, preList.get(0).height);// ???????????????????????????
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
        parameters.setPictureSize(width, height);// ?????????????????????

        List<String> focusList = parameters.getSupportedFocusModes();
        if (focusList.contains(Parameters.FOCUS_MODE_AUTO)) {
            parameters.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        }
        parameters.set("jpeg-quality", 80);// ??????JPG???????????????
        camera.setParameters(parameters);
        camera.startPreview();
    }

    private void initMediaRecorder() {
        if (mRecorder == null) {
            mRecorder = new MediaRecorder();
        }
        mRecorder.reset();
        mRecorder.setCamera(camera);
        mRecorder.setOrientationHint(CommonUtil.setCameraDisplayOrientation(this, curCameraId, camera));//???????????????????????????????????????
        mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);// ?????????
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC); // ?????????????????????
        mRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_720P));
//		mRecorder.setVideoFrameRate(20);
        mRecorder.setVideoEncodingBitRate(3000000);
        mRecorder.setPreviewDisplay(surfaceHolder.getSurface());// ??????
    }

    /**
     * ????????????
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
     * camera??????
     */
    private void takePhoto() {
        if (isFull) {
            Toast.makeText(mContext, "???????????????9?????????", Toast.LENGTH_SHORT).show();
            return;
        }
        if (camera != null) {
            //???????????????????????????
            orienListener = new OrientationEventListener(mContext) {
                @Override
                public void onOrientationChanged(int orientations) {
                    CameraInfo info = new CameraInfo();
                    Camera.getCameraInfo(curCameraId, info);
                    if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {//???????????????
                        if (orientations > 325 || orientations <= 45) {
                            degree = 270;
                        } else if (orientations > 45 && orientations <= 135) {
                            degree = 180;
                        } else if (orientations > 135 && orientations < 225) {
                            degree = 90;
                        } else {
                            degree = 0;
                        }
                    } else {
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
            camera.takePicture(null, null, (data, c) -> {
//					playSound(true);
				Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
				Matrix matrix = new Matrix();
				matrix.preRotate(degree);
				bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

				if (isTokePhotoFirst) {
					fileName = sdf.format(System.currentTimeMillis());
					isTokePhotoFirst = false;
				}
				File files = new File(CONST.PICTURE_ADDR + File.separator + fileName);
				if (!files.exists()) {
					files.mkdirs();
				}
				String picName = sdf.format(System.currentTimeMillis());
				File file = new File(files.getPath() + File.separator + picName + CONST.PICTURETYPE);

				try {
					asynCompressBitmap(bitmap, file);

					if (orienListener != null) {
						orienListener.disable();
					}

					for (int i = 0; i < mList.size(); i++) {
						if (i == mList.size() - 1) {
							isFull = true;
						}
						if (!mList.get(i).isState()) {
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
					c.startPreview();// ???????????????????????????????????????,??????????????????????????????
				} catch (Exception e) {
					e.printStackTrace();
				}

			});
        }
    }

    /**
     * ???????????????
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
     * ?????????????????????????????????
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
     * ????????????
     */
    private void startRecorder() {
        ivStart.setClickable(false);
        ivDone.setClickable(false);
        new Handler().postDelayed(() -> {
			ivStart.setClickable(true);
			ivDone.setClickable(true);
		}, 5000);
        releaseMediaRecorder();
        initMediaRecorder();
        File files = new File(CONST.VIDEO_ADDR);
        if (!files.exists()) {
            files.mkdirs();
        }

        fileName = sdf.format(System.currentTimeMillis());
        intentVideoUrl = files.getPath() + File.separator + fileName + CONST.VIDEOTYPE;
        mRecorder.setOutputFile(intentVideoUrl);// ????????????

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
     * ???????????????
     *
     * @param fileName
     * @param videoUrl
     */
    private void compressThumbnail(String fileName, String videoUrl) {
        Bitmap thumbBitmap = CommonUtil.getVideoThumbnail(videoUrl, displayW / 4, displayW / 4, MediaStore.Images.Thumbnails.MINI_KIND);
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
     * ??????MediaRecorder
     */
    private void releaseMediaRecorder() {
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }
    }

    /**
     * ???????????????
     * 0 ????????????1?????????
     */
    private void switchCamera(int cameraId) {
        if (cameraId == CameraInfo.CAMERA_FACING_BACK) {
            ivFlash.setVisibility(View.VISIBLE);
        } else if (cameraId == CameraInfo.CAMERA_FACING_FRONT) {
            ivFlash.setVisibility(View.GONE);
        }

        releaseCamera();
        initCamera();
        releaseMediaRecorder();
        initMediaRecorder();
    }

    /**
     * ??????????????????????????????????????????
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
        } else {
            intent = new Intent(Intent.ACTION_MEDIA_MOUNTED);
            uri = Uri.parse("file://" + Environment.getExternalStorageDirectory());
        }
        intent.setData(uri);
        sendBroadcast(intent);
    }

    /**
     * ??????????????????
     */
    private void completeRecorder() {
        if (isRecording) {
            sendMediaBroadcast();

            if (mRecorder != null) {
                mRecorder.reset();
                mRecorder.release();
            }

            ivStart.setBackgroundResource(R.drawable.iv_start);
            ivChange.setVisibility(View.VISIBLE);
            ivSwitcher.setVisibility(View.VISIBLE);
            cancelTime();
            isRecording = false;
        }

        compressThumbnail(fileName, intentVideoUrl);
    }

    /**
     * ????????????
     */
    private void completeTakePhoto() {
//        List<PhotoDto> selectList = new ArrayList<>();
//        for (int i = 0; i < mList.size(); i++) {
//            if (mList.get(i).isState()) {
//                selectList.add(mList.get(i));//?????????????????????????????????list???
//            }
//        }
        mList.clear();
        ivChange.setVisibility(View.VISIBLE);
        ivDone.setVisibility(View.GONE);
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
     * ??????
     */
    private void exit() {
        if (isRecording) {
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                Toast.makeText(mContext, "????????????????????????", Toast.LENGTH_SHORT).show();
                mExitTime = System.currentTimeMillis();
            } else {
                completeRecorder();
                startAnimation(true);
                finish();
            }
        } else {
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
        } else if (keyCode == KeyEvent.KEYCODE_ENTER) {
            clickStart();
            return false;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            return true;
        }
        return false;
    }

    private void clickStart() {
        if (isRecorderOrCamera) {//?????????
            if (isRecording) {
                completeRecorder();
            } else {
                ivStart.setBackgroundResource(R.drawable.iv_stop);
                ivChange.setVisibility(View.GONE);
                ivDone.setVisibility(View.VISIBLE);
                ivSwitcher.setVisibility(View.GONE);
                startRecorder();
                isRecording = true;
            }
        } else {//?????????
            mgridView.setVisibility(View.VISIBLE);
            ivChange.setVisibility(View.GONE);
            ivDone.setVisibility(View.VISIBLE);
            takePhoto();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivStart:
                clickStart();
                break;
            case R.id.ivChange:
                isRecorderOrCamera = !isRecorderOrCamera;
                if (isRecorderOrCamera) {
                    ivChange.setImageResource(R.drawable.iv_shexiang);
                    mgridView.setVisibility(View.VISIBLE);
                    tvTime.setVisibility(View.GONE);
                } else {
                    ivChange.setImageResource(R.drawable.iv_paizhao);
                    mgridView.setVisibility(View.GONE);
                    tvTime.setVisibility(View.VISIBLE);
                }
                ivDone.setVisibility(View.GONE);
                break;
            case R.id.ivFlash:
                if (camera != null) {
                    Parameters parameters = camera.getParameters();
                    String flashMode = parameters.getFlashMode();
                    if (flashMode != null) {
                        if (flashMode.equals(Parameters.FLASH_MODE_OFF)) {
                            parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
                            ivFlash.setImageResource(R.drawable.iv_flash_on);
                        } else {
                            parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
                            ivFlash.setImageResource(R.drawable.iv_flash_off);
                        }
                    }
                    camera.setParameters(parameters);
                }
                break;
            case R.id.ivSwitcher:
                int cameraCount = Camera.getNumberOfCameras();//????????????????????????
                if (cameraCount > 1) {
                    if (curCameraId == 0) {
                        curCameraId = 1;
                    } else {
                        curCameraId = 0;
                    }
                    switchCamera(curCameraId);
                } else {
                    Toast.makeText(mContext, "??????????????????????????????", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.ivDone:
                if (isRecorderOrCamera) {
                    if (isRecording) {
                        if ((System.currentTimeMillis() - mExitTime) > 2000) {
                            Toast.makeText(mContext, "????????????????????????", Toast.LENGTH_SHORT).show();
                            mExitTime = System.currentTimeMillis();
                        } else {
                            ivDone.setVisibility(View.GONE);
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
                    } else {
                        ivDone.setVisibility(View.GONE);
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
                } else {
                    completeTakePhoto();
                }
                break;

            default:
                break;
        }
    }

    private class TouchListener implements OnTouchListener {

        private float startDistance = 0;//?????????????????????

        @Override
        public boolean onTouch(View arg0, MotionEvent event) {
            Parameters parameters = camera.getParameters();
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
                    if (event.getPointerCount() < 2) {//?????????????????????????????????????????????
                        return true;
                    }
                    float endDistance = distance(event);// ?????????????????????
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
                        camera.setParameters(parameters);
                        startDistance = endDistance;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    break;
            }
            return true;
        }

        /**
         * ??????????????????????????????
         */
        private float distance(MotionEvent event) {
            float dx = event.getX(1) - event.getX(0);
            float dy = event.getY(1) - event.getY(0);
            /** ????????????????????????????????????????????? */
            return (float) Math.sqrt(dx * dx + dy * dy);
        }
    }

}
