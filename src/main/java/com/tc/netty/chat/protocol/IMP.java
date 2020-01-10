package com.tc.netty.chat.protocol;

/**
 * 自定义IM协议，即时通讯协议
 */
public enum IMP {
    /**系统消息*/
    SYSTEM("SYSTEM"),
    /**登录*/
    LOGIN("LOGIN"),
    /**登出*/
    LOGOUT("LOGOUT"),
    /**聊天消息*/
    CHAT("CHAT"),
    /**送花*/
    FLOWER("FLOWER");

    private String name;

    public static boolean isIMP(String content){
        return content.matches("^\\[(SYSTEM|LOGIN|LOGOUT|CHAT|FLOWER)\\]");
    }

    IMP(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
