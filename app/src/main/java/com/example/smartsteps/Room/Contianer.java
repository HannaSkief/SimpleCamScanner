package com.example.smartsteps.Room;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "container")
public class Contianer implements Parcelable {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    private long id;

    private long parentId;

    private String name;

    private String type;

    private int priority;

    private String createdAt;

    public Contianer() {
    }

    public Contianer(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getParentId() {
        return parentId;
    }

    public void setParentId(long parentId) {
        this.parentId = parentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public  Contianer(Parcel in){
        this.id=in.readLong();
        this.parentId=in.readLong();
        this.name=in.readString();
        this.type=in.readString();
        this.priority=in.readInt();
        this.createdAt=in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(this.id);
        parcel.writeLong(this.parentId);
        parcel.writeString(this.name);
        parcel.writeString(this.type);
        parcel.writeInt(this.priority);
        parcel.writeString(this.createdAt);

    }

    public static final Parcelable.Creator CREATOR= new Creator(){
        @Override
        public Object createFromParcel(Parcel parcel) {
            return new Contianer(parcel);
        }

        @Override
        public Object[] newArray(int i) {
            return new Object[i];
        }
    };

}
