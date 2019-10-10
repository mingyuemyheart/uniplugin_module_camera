package com.warning.adapter;

/**
 * 在线预览视频
 */

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.tsz.afinal.FinalBitmap;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Paint;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.warning.R;
import com.warning.activity.BaseActivity;
import com.warning.dto.PhotoDto;
import com.warning.util.OkHttpUtil;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OnlineVideoAdapter extends BaseAdapter{
	
	private Context mContext = null;
	private LayoutInflater mInflater = null;
	private List<PhotoDto> mArrayList = new ArrayList<>();

	private final class ViewHolder{
		ImageView ivPortrait;
		TextView tvUserName;
		TextView tvTime;
		TextView tvComment;
		TextView tvDelete;
	}
	
	private ViewHolder mHolder = null;
	
	public OnlineVideoAdapter(Context context, List<PhotoDto> mArrayList) {
		mContext = context;
		this.mArrayList = mArrayList;
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return mArrayList.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.adapter_online_video, null);
			mHolder = new ViewHolder();
			mHolder.ivPortrait = (ImageView) convertView.findViewById(R.id.ivPortrait);
			mHolder.tvUserName = (TextView) convertView.findViewById(R.id.tvUserName);
			mHolder.tvTime = (TextView) convertView.findViewById(R.id.tvTime);
			mHolder.tvComment = (TextView) convertView.findViewById(R.id.tvComment);
			mHolder.tvDelete = (TextView) convertView.findViewById(R.id.tvDelete);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}

		final PhotoDto dto = mArrayList.get(position);
		if (!TextUtils.isEmpty(dto.nickName)) {
			mHolder.tvUserName.setText(dto.nickName);
		}else if (!TextUtils.isEmpty(dto.getUserName())) {
			mHolder.tvUserName.setText(dto.getUserName());
		}else if (!TextUtils.isEmpty(dto.phoneNumber)) {
			if (dto.phoneNumber.length() >= 7) {
				mHolder.tvUserName.setText(dto.phoneNumber.replace(dto.phoneNumber.substring(3, 7), "****"));
			}else {
				mHolder.tvUserName.setText(dto.phoneNumber);
			}
		}

		if (!TextUtils.isEmpty(dto.createTime)) {
			mHolder.tvTime.setText(dto.createTime);
		}
		if (!TextUtils.isEmpty(dto.comment)) {
			mHolder.tvComment.setText(dto.comment);
		}

		if (TextUtils.equals(dto.uid, BaseActivity.UID)) {
			mHolder.tvDelete.setVisibility(View.VISIBLE);
			mHolder.tvDelete.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
			mHolder.tvDelete.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					deleteMessage("确定删除该条评论？", dto.commentId, position);
				}
			});
		}else {
			mHolder.tvDelete.setVisibility(View.GONE);
		}

		if (!TextUtils.isEmpty(dto.portraitUrl)) {
			LayoutParams lp = mHolder.ivPortrait.getLayoutParams();
			int width = lp.width;
			FinalBitmap finalBitmap = FinalBitmap.create(mContext);
			finalBitmap.display(mHolder.ivPortrait, dto.portraitUrl, null, width);
		}else {
			mHolder.ivPortrait.setImageResource(R.drawable.iv_portrait);
		}

		return convertView;
	}

	/**
	 * 删除评论
	 * @param message
	 */
	private void deleteMessage(String message, final String commentId, final int index) {
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.shawn_dialog_delete, null);
		TextView tvMessage = (TextView) view.findViewById(R.id.tvMessage);
		LinearLayout llNegative = (LinearLayout) view.findViewById(R.id.llNegative);
		LinearLayout llPositive = (LinearLayout) view.findViewById(R.id.llPositive);

		final Dialog dialog = new Dialog(mContext, R.style.CustomProgressDialog);
		dialog.setContentView(view);
		dialog.show();

		tvMessage.setText(message);
		llNegative.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});

		llPositive.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
				mArrayList.remove(index);
				notifyDataSetChanged();
				OkHttpDeleteMessage("http://new.12379.tianqi.cn/Work/delpinglun", commentId);
			}
		});
	}

	/**
	 * 删除评论接口
	 * @param url
	 */
	private void OkHttpDeleteMessage(final String url, String commentId) {
		FormBody.Builder builder = new FormBody.Builder();
		builder.add("token", BaseActivity.TOKEN);
		builder.add("uid", BaseActivity.UID);
		builder.add("pid", commentId);
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
					}
				});
			}
		}).start();
	}
	
}
