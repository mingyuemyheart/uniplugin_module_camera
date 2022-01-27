package com.camera.dto;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class PhotoDto implements Parcelable {

	public boolean state = false;// false为没有拍照，true为拍照完成
	public String url;// 图片或者缩略图路径
	public String videoUrl;// 视频路径
	public boolean isSelected = false;

	public String sd, hd, fhd;//视频的表情、高清、超清格式
	public String createTime;// 上传视频或图片的时间
	public int section;
	public String location;// 地址信息
	public String workstype;// 区分imgs或者video
	public List<String> urlList = new ArrayList<>();
	public String userName;// 用户名
	public String praiseCount;// 点赞次数
	public String commentCount;// 评论次数
	public String videoId;// 视频id
	public String title;// 标题
	public String uid;//上传的作品对应的uid
	public String content;//内容
	public String comment;// 评论内容
	public String commentId;//评论id
	public String msgName;
	public String msgTime;
	public String msgUrl;
	public String msgId;//消息id
	public String msgContent;// 消息内容
	public String workTime;// 录制或者拍照时间
	public int score;// 积分
	public String scoreName;
	public String scoreType;
	public String workId;// 作品id
	public String shareTimes;//转发次数
	public String portraitUrl;// 头像url
	public int workCount;// 作品数量
	public String status = null;//审核状态，1为未审核，2为通过，3为拒绝
	public String refuseReason;//拒绝原因
	public boolean isShowCircle = false;
	public boolean isShowCorrect = false;
	public String nickName;//昵称
	public String phoneNumber;//手机号
	public String lat;
	public String lng;
	public String showTime;//视频或者图片浏览次数
	public List<PhotoDto> picList = new ArrayList<>();
	public boolean isDelete = false;//长按本地未上传视频，删除
	public boolean isTop = false;//判断第一个是否为大图
	public String isRead;//已读1，未读0，默认0

	//获取本地所有视频文件
	public String fileName;//文件名称
	public String filePath;//文件路径
	
	public List<String> getUrlList() {
		return urlList;
	}

	public void setUrlList(List<String> urlList) {
		this.urlList = urlList;
	}

	public int getWorkCount() {
		return workCount;
	}

	public void setWorkCount(int workCount) {
		this.workCount = workCount;
	}

	public String getPortraitUrl() {
		return portraitUrl;
	}

	public void setPortraitUrl(String portraitUrl) {
		this.portraitUrl = portraitUrl;
	}

	public String getWorkId() {
		return workId;
	}

	public void setWorkId(String workId) {
		this.workId = workId;
	}

	public String getMsgContent() {
		return msgContent;
	}

	public void setMsgContent(String msgContent) {
		this.msgContent = msgContent;
	}

	public String getWorkTime() {
		return workTime;
	}

	public void setWorkTime(String workTime) {
		this.workTime = workTime;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getVideoId() {
		return videoId;
	}

	public void setVideoId(String videoId) {
		this.videoId = videoId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPraiseCount() {
		return praiseCount;
	}

	public void setPraiseCount(String praiseCount) {
		this.praiseCount = praiseCount;
	}

	public String getCommentCount() {
		return commentCount;
	}

	public void setCommentCount(String commentCount) {
		this.commentCount = commentCount;
	}

	public String getVideoUrl() {
		return videoUrl;
	}

	public void setVideoUrl(String videoUrl) {
		this.videoUrl = videoUrl;
	}

	public String getWorkstype() {
		return workstype;
	}

	public void setWorkstype(String workstype) {
		this.workstype = workstype;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public int getSection() {
		return section;
	}

	public void setSection(int section) {
		this.section = section;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public boolean isState() {
		return state;
	}

	public void setState(boolean state) {
		this.state = state;
	}


	public PhotoDto() {
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeByte(this.state ? (byte) 1 : (byte) 0);
		dest.writeString(this.url);
		dest.writeString(this.videoUrl);
		dest.writeByte(this.isSelected ? (byte) 1 : (byte) 0);
		dest.writeString(this.sd);
		dest.writeString(this.hd);
		dest.writeString(this.fhd);
		dest.writeString(this.createTime);
		dest.writeInt(this.section);
		dest.writeString(this.location);
		dest.writeString(this.workstype);
		dest.writeStringList(this.urlList);
		dest.writeString(this.userName);
		dest.writeString(this.praiseCount);
		dest.writeString(this.commentCount);
		dest.writeString(this.videoId);
		dest.writeString(this.title);
		dest.writeString(this.uid);
		dest.writeString(this.content);
		dest.writeString(this.comment);
		dest.writeString(this.commentId);
		dest.writeString(this.msgName);
		dest.writeString(this.msgTime);
		dest.writeString(this.msgUrl);
		dest.writeString(this.msgId);
		dest.writeString(this.msgContent);
		dest.writeString(this.workTime);
		dest.writeInt(this.score);
		dest.writeString(this.scoreName);
		dest.writeString(this.scoreType);
		dest.writeString(this.workId);
		dest.writeString(this.shareTimes);
		dest.writeString(this.portraitUrl);
		dest.writeInt(this.workCount);
		dest.writeString(this.status);
		dest.writeString(this.refuseReason);
		dest.writeByte(this.isShowCircle ? (byte) 1 : (byte) 0);
		dest.writeByte(this.isShowCorrect ? (byte) 1 : (byte) 0);
		dest.writeString(this.nickName);
		dest.writeString(this.phoneNumber);
		dest.writeString(this.lat);
		dest.writeString(this.lng);
		dest.writeString(this.showTime);
		dest.writeTypedList(this.picList);
		dest.writeByte(this.isDelete ? (byte) 1 : (byte) 0);
		dest.writeByte(this.isTop ? (byte) 1 : (byte) 0);
		dest.writeString(this.isRead);
		dest.writeString(this.fileName);
		dest.writeString(this.filePath);
	}

	protected PhotoDto(Parcel in) {
		this.state = in.readByte() != 0;
		this.url = in.readString();
		this.videoUrl = in.readString();
		this.isSelected = in.readByte() != 0;
		this.sd = in.readString();
		this.hd = in.readString();
		this.fhd = in.readString();
		this.createTime = in.readString();
		this.section = in.readInt();
		this.location = in.readString();
		this.workstype = in.readString();
		this.urlList = in.createStringArrayList();
		this.userName = in.readString();
		this.praiseCount = in.readString();
		this.commentCount = in.readString();
		this.videoId = in.readString();
		this.title = in.readString();
		this.uid = in.readString();
		this.content = in.readString();
		this.comment = in.readString();
		this.commentId = in.readString();
		this.msgName = in.readString();
		this.msgTime = in.readString();
		this.msgUrl = in.readString();
		this.msgId = in.readString();
		this.msgContent = in.readString();
		this.workTime = in.readString();
		this.score = in.readInt();
		this.scoreName = in.readString();
		this.scoreType = in.readString();
		this.workId = in.readString();
		this.shareTimes = in.readString();
		this.portraitUrl = in.readString();
		this.workCount = in.readInt();
		this.status = in.readString();
		this.refuseReason = in.readString();
		this.isShowCircle = in.readByte() != 0;
		this.isShowCorrect = in.readByte() != 0;
		this.nickName = in.readString();
		this.phoneNumber = in.readString();
		this.lat = in.readString();
		this.lng = in.readString();
		this.showTime = in.readString();
		this.picList = in.createTypedArrayList(PhotoDto.CREATOR);
		this.isDelete = in.readByte() != 0;
		this.isTop = in.readByte() != 0;
		this.isRead = in.readString();
		this.fileName = in.readString();
		this.filePath = in.readString();
	}

	public static final Creator<PhotoDto> CREATOR = new Creator<PhotoDto>() {
		@Override
		public PhotoDto createFromParcel(Parcel source) {
			return new PhotoDto(source);
		}

		@Override
		public PhotoDto[] newArray(int size) {
			return new PhotoDto[size];
		}
	};
}
