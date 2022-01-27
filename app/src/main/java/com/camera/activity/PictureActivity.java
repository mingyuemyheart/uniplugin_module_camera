package com.camera.activity;

import android.app.Activity;
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
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.camera.R;
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
 * 拍摄图片，支持多张
 * @author shawn_sun
 */
public class PictureActivity extends Activity implements SurfaceHolder.Callback, OnClickListener {

    private Context mContext = null;
    private SurfaceView surfaceView = null;
    private SurfaceHolder surfaceHolder = null;
    private Camera camera = null;//相机
    private int curCameraId = 0;//0是后置摄像头，1是前置摄像头
    private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA);
    private List<String> dataList = new ArrayList<>();
    private int displayW = 0;//屏幕宽
    private int displayH = 0;//屏幕高
    private int degree = 0;//保存的图片要旋转的角度
    private OrientationEventListener orienListener = null;//屏幕旋转方向监听器
    private String fileName = null;
    private boolean isTokePhotoFirst = true;//是否是第一次拍摄照片

    private ConstraintLayout reToUp,reToDown;
    private ImageView ivFlash;//闪光
    private ImageView ivStart;//拍摄按钮
    private ImageView ivDone;//完成录制按钮
    private ImageView imageView;
    private TextView tvNum,tvScale;
    private int scaleWidth = 3, scaleHeight = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_picture);
        mContext = this;
        checkMultiAuthority();
    }

    /**
     * 申请定位权限
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
            if (AuthorityUtil.deniedList.isEmpty()) {//所有权限都授予
                init();
            } else {
                String[] permissions = AuthorityUtil.deniedList.toArray(new String[AuthorityUtil.deniedList.size()]);//将list转成数组
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
                    boolean isAllGranted = true;//是否全部授权
                    for (int gResult : grantResults) {
                        if (gResult != PackageManager.PERMISSION_GRANTED) {
                            isAllGranted = false;
                            break;
                        }
                    }
                    if (isAllGranted) {//所有权限都授予
                        init();
                    }
                }
                break;
        }
    }

    private void init() {
        initWidget();
        initSurfaceView();
    }

    /**
     * 初始化控件
     */
    private void initWidget() {
        ivFlash = findViewById(R.id.ivFlash);
        ivFlash.setOnClickListener(this);
        ImageView ivSwitcher = findViewById(R.id.ivSwitcher);
        ivSwitcher.setOnClickListener(this);
        ivStart = findViewById(R.id.ivStart);
        ivStart.setOnClickListener(this);
        ivDone = findViewById(R.id.ivDone);
        ivDone.setOnClickListener(this);
        reToUp = findViewById(R.id.reToUp);
        reToDown = findViewById(R.id.reToDown);
        imageView = findViewById(R.id.imageView);
        imageView.setOnClickListener(this);
        tvNum = findViewById(R.id.tvNum);
        tvScale = findViewById(R.id.tvScale);
        tvScale.setOnClickListener(this);

        DisplayMetrics dm = getResources().getDisplayMetrics();
        displayW = dm.widthPixels;
        displayH = dm.heightPixels;
    }

    /**
     * 相机动画
     * @param flag false为初始化打开，true为拍照时动画
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
            }

            @Override
            public void onAnimationRepeat(Animation arg0) {
            }

            @Override
            public void onAnimationEnd(Animation arg0) {
                ivStart.setClickable(true);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        startAnimation(false);
    }

    /**
     * 初始化surfaceView
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

        //设置预览分辨率
        List<Size> preList = parameters.getSupportedPreviewSizes();
        int preWidth = 0;
        int preHeight = 0;
        int surWidth = 0;
        int surHeight = 0;
        for (int i = 0; i < preList.size(); i++) {
            int width = preList.get(i).width;
            int height = preList.get(i).height;
            Log.e("preWidth", "width="+width+",height="+height);
            if (preWidth <= width && preHeight <= height && (width*scaleWidth == height*scaleHeight)) {
                preWidth = width;
                preHeight = height;
                surWidth = Math.min(displayW, displayH);
                surHeight = surWidth * preWidth / preHeight;
            }
        }
        parameters.setPreviewSize(preWidth, preHeight);// 设置预览照片的大小
        Log.e("preWidth", "preWidth="+preWidth+",preHeight="+preHeight);
        if (surfaceView != null) {
            surfaceView.setLayoutParams(new ConstraintLayout.LayoutParams(surWidth, surHeight));//设置surfaceView大小
        }

        //设置图片分辨率
        int picWidth = 0;
        int picHeight = 0;
        List<Size> picList = parameters.getSupportedPictureSizes();
        for (int i = 0; i < picList.size(); i++) {
            int width = picList.get(i).width;
            int height = picList.get(i).height;
            Log.e("picWidth", "width="+width+",height="+height);
            if (height >= displayW && (width*scaleWidth == height*scaleHeight)) {
                picWidth = width;
                picHeight = height;
            }
        }
        parameters.setPictureSize(picWidth, picHeight);// 设置照片的大小
        Log.e("picWidth", "picWidth="+picWidth+",picHeight="+picHeight);

        List<String> focusList = parameters.getSupportedFocusModes();
        if (focusList.contains(Parameters.FOCUS_MODE_AUTO)) {
            parameters.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        }
        parameters.set("jpeg-quality", 80);// 设置JPG照片的质量
        camera.setParameters(parameters);
        camera.startPreview();
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
        if (camera != null) {
            //屏幕旋转方向监听器
            orienListener = new OrientationEventListener(mContext) {
                @Override
                public void onOrientationChanged(int orientations) {
                    Log.e("orientations", orientations+"");
                    CameraInfo info = new Camera.CameraInfo();
                    Camera.getCameraInfo(curCameraId, info);
                    if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {//前置摄像头
                        if (orientations > 325 || orientations <= 45) {
                            degree = 270;
                        } else if (orientations <= 135) {
                            degree = 180;
                        } else if (orientations < 225) {
                            degree = 90;
                        } else {
                            degree = 0;
                        }
                    } else {
                        if (orientations > 325 || orientations <= 45) {
                            degree = 90;
                        } else if (orientations <= 135) {
                            degree = 180;
                        } else if (orientations < 225) {
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

				if (isTokePhotoFirst) {
					fileName = sdf1.format(System.currentTimeMillis());
					isTokePhotoFirst = false;
				}
				File files = new File(getExternalFilesDir(null) + "/picture/" + fileName);
				if (!files.exists()) {
					files.mkdirs();
				}
				String picName = sdf1.format(System.currentTimeMillis());
				File file = new File(files.getPath() + File.separator + picName + CONST.PICTURETYPE);
                dataList.add(file.getAbsolutePath());
                int size = dataList.size();
                if (size > 0) {
                    tvNum.setText(size+"");
                    tvNum.setBackgroundResource(R.drawable.bg_corner_score);
                    tvNum.setVisibility(View.VISIBLE);
                } else {
                    tvNum.setVisibility(View.GONE);
                }

				try {
					new Thread(() -> {
                        try {
                            Bitmap b = BitmapFactory.decodeByteArray(data, 0, data.length);
                            Matrix matrix = new Matrix();
                            matrix.preRotate(degree);
                            final Bitmap bitmap = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
                            if (bitmap != null) {
                                runOnUiThread(() -> {
                                    imageView.setImageBitmap(bitmap);
                                    imageView.setVisibility(View.VISIBLE);
                                });
                                FileOutputStream fos = new FileOutputStream(file);
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
                                fos.write(data);
                                fos.flush();
                                fos.close();
                            }
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }).start();

					if (orienListener != null) {
						orienListener.disable();
					}
					c.stopPreview();
					c.startPreview();// 在拍照的时候相机是被占用的,拍照之后需要重新预览
				} catch (Exception e) {
					e.printStackTrace();
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
     * 切换摄像头
     * 0 是后置，1是前置
     */
    private void switchCamera(int cameraId) {
        if (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
            ivFlash.setVisibility(View.VISIBLE);
        } else if (cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            ivFlash.setVisibility(View.GONE);
        }

        releaseCamera();
        initCamera();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
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
        ivDone.setVisibility(View.VISIBLE);
        takePhoto();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivStart:
                clickStart();
                break;
            case R.id.ivFlash:
                if (camera != null) {
                    Camera.Parameters parameters = camera.getParameters();
                    String flashMode = parameters.getFlashMode();
                    if (flashMode != null) {
                        if (flashMode.equals(Camera.Parameters.FLASH_MODE_OFF)) {
                            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                            ivFlash.setImageResource(R.drawable.iv_flash_on);
                        } else {
                            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                            ivFlash.setImageResource(R.drawable.iv_flash_off);
                        }
                    }
                    camera.setParameters(parameters);
                }
                break;
            case R.id.tvScale:
                if (scaleWidth == 9) {
                    scaleWidth = 3;
                    scaleHeight = 4;
                    tvScale.setText("4:3");
                } else {
                    scaleWidth = 9;
                    scaleHeight = 16;
                    tvScale.setText("16:9");
                }
                initCamera();
                break;
            case R.id.ivSwitcher:
                int cameraCount = Camera.getNumberOfCameras();//得到摄像头的个数
                if (cameraCount > 1) {
                    if (curCameraId == 0) {
                        curCameraId = 1;
                    } else {
                        curCameraId = 0;
                    }
                    switchCamera(curCameraId);
                } else {
                    Toast.makeText(mContext, "该设备只有一个摄像头", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.imageView:
                if (dataList.size() > 0) {
                    Intent intent = new Intent(mContext, DisplayPictureActivity.class);
                    intent.putExtra("fileName", fileName);
                    startActivityForResult(intent, 1001);
                }
                break;
            case R.id.ivDone:
                ivDone.setVisibility(View.GONE);
                Intent intent = new Intent(mContext, DisplayPictureActivity.class);
                intent.putExtra("fileName", fileName);
                startActivity(intent);
                fileName = "";
                isTokePhotoFirst = true;
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
                case 1001:
                    if (data != null) {
                        ArrayList<PhotoDto> tempList = data.getParcelableArrayListExtra("dataList");
                        if (tempList != null) {
                            dataList.clear();
                            imageView.setVisibility(View.GONE);
                            for (int i = 0; i < tempList.size(); i++) {
                                PhotoDto dto = tempList.get(i);
                                if (i == tempList.size()-1) {
                                    if (!TextUtils.isEmpty(dto.url)) {
                                        Bitmap bitmap = BitmapFactory.decodeFile(dto.url);
                                        if (bitmap != null) {
                                            imageView.setImageBitmap(bitmap);
                                            imageView.setVisibility(View.VISIBLE);
                                        }
                                    }
                                }
                                dataList.add(dto.url);
                            }
                            int size = dataList.size();
                            if (size > 0) {
                                tvNum.setText(size+"");
                                tvNum.setBackgroundResource(R.drawable.bg_corner_score);
                                tvNum.setVisibility(View.VISIBLE);
                            } else {
                                tvNum.setVisibility(View.GONE);
                            }
                        }
                    }
                    break;
            }
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
                    if (tempZoom != 0) {
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
         * 计算两个手指间的距离
         */
        private float distance(MotionEvent event) {
            float dx = event.getX(1) - event.getX(0);
            float dy = event.getY(1) - event.getY(0);
            /** 使用勾股定理返回两点之间的距离 */
            return (float) Math.sqrt(dx * dx + dy * dy);
        }
    }

}
