package jp.co.miosys.aiworldview.data_post_response;

import com.google.gson.annotations.SerializedName;

public class DataResponseLogin {

    @SerializedName("status")
    private String status;
    @SerializedName("token")
    private String token;
    @SerializedName("user_id")
    private int userId;
    @SerializedName("username")
    private String userName;
    @SerializedName("name")
    private String name;
    @SerializedName("company_id")
    private String companyId;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }
}
