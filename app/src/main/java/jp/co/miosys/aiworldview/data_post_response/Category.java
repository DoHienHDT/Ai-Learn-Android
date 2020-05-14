package jp.co.miosys.aiworldview.data_post_response;

import com.google.gson.annotations.SerializedName;

public class Category {

    @SerializedName("id")
    private int id;
    @SerializedName("parent_id")
    private int parentId;
    @SerializedName("name")
    private String name;
    @SerializedName("note")
    private String note;
    private boolean select;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public boolean isSelect() {
        return select;
    }

    public void setSelect(boolean select) {
        this.select = select;
    }
}
