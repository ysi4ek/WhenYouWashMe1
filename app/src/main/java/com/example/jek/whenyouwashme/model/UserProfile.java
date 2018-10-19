package com.example.jek.whenyouwashme.model;

/**
 * Created by jek on 22.06.2017.
 */

public class UserProfile {
    private String avatarPath;
    private String userName;

    public String getAvatarPath() {
        return avatarPath;
    }

    public void setAvatarPath(String avatarPath) {
        this.avatarPath = avatarPath;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public String toString() {
        return "UserProfile{" +
                "avatarPath='" + avatarPath + '\'' +
                ", userName='" + userName + '\'' +
                '}';
    }
}
