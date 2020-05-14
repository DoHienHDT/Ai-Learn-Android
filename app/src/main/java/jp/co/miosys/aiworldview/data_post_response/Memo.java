package jp.co.miosys.aiworldview.data_post_response;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Memo implements Serializable {
    @SerializedName("category_id")
    private int categoryId;
    @SerializedName("category_name")
    private String categoryName;
    @SerializedName("user_id")
    private int userId;
    @SerializedName("user_name")
    private String userName;
    @SerializedName("kml_id")
    private int kmlId;
    @SerializedName("memo_at")
    private int memoAt;
    @SerializedName("memo_at_str")
    private String memoAtStr;
    @SerializedName("content")
    private String content;
    @SerializedName("link_video")
    private String linkVideo;
    @SerializedName("lat")
    private String lat;
    @SerializedName("lng")
    private String lng;
    @SerializedName("collection_time")
    private String collectionTime;
    private String imagePin;

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getKmlId() {
        return kmlId;
    }

    public void setKmlId(int kmlId) {
        this.kmlId = kmlId;
    }

    public int getMemoAt() {
        return memoAt;
    }

    public void setMemoAt(int memoAt) {
        this.memoAt = memoAt;
    }

    public String getMemoAtStr() {
        return memoAtStr;
    }

    public void setMemoAtStr(String memoAtStr) {
        this.memoAtStr = memoAtStr;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getCollectionTime() {
        return collectionTime;
    }

    public void setCollectionTime(String collectionTime) {
        this.collectionTime = collectionTime;
    }

    public String getLinkVideo() {
        return linkVideo;
    }

    public void setLinkVideo(String linkVideo) {
        this.linkVideo = linkVideo;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getImagePin() {
        return imagePin;
    }

    public void setImagePin(String imagePin) {
        this.imagePin = imagePin;
    }
}
