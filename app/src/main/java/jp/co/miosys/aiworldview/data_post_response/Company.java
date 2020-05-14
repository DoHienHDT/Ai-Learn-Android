package jp.co.miosys.aiworldview.data_post_response;

import com.google.gson.annotations.SerializedName;

public class Company {

    @SerializedName("id")
    private int id;
    @SerializedName("domain")
    private String domain;
    @SerializedName("kms")
    private String kms;
    @SerializedName("aisys")
    private String aisys;
    @SerializedName("code")
    private String code;
    @SerializedName("name")
    private String name;
    @SerializedName("nickname")
    private String nickName;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getKms() {
        return kms;
    }

    public void setKms(String kms) {
        this.kms = kms;
    }

    public String getAisys() {
        return aisys;
    }

    public void setAisys(String aisys) {
        this.aisys = aisys;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }
}
