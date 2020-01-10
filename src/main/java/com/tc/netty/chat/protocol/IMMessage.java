package com.tc.netty.chat.protocol;

import lombok.Data;
import org.msgpack.annotation.Message;

/**
 * 自定义消息实体
 * @author taosh
 * @create 2020-01-09 10:04
 */
@Message
@Data
public class IMMessage {
    /**IP地址及端口*/
    private String addr;
    /**命令类型[LOGIN]或者[SYSTEM]或者[LOGOUT]*/
    private String cmd;
    /**命令发送时间*/
    private long time;
    /**当前在线人数*/
    private int online;
    /**发送人*/
    private String sender;
    /**接收人*/
    private String receiver;
    /**消息内容*/
    private String content;
    /**终端*/
    private String terminal;

    public IMMessage() {
    }

    public IMMessage(String cmd, long time, int online, String content) {
        this.cmd = cmd;
        this.time = time;
        this.online = online;
        this.content = content;
        this.terminal = terminal;
    }

    public IMMessage(String cmd,String terminal,long time,String sender, String receiver){
        this.cmd = cmd;
        this.time = time;
        this.sender = sender;
        this.terminal = terminal;
        this.receiver = receiver;
    }

    public IMMessage(String cmd, long time, String sender, String receiver, String content) {
        this.cmd = cmd;
        this.time = time;
        this.sender = sender;
        this.content = content;
        this.terminal = terminal;
        this.receiver = receiver;
    }

    @Override
    public String toString() {
        return "IMMessage{" +
                "addr='" + addr + '\'' +
                ", cmd='" + cmd + '\'' +
                ", time=" + time +
                ", online=" + online +
                ", sender='" + sender + '\'' +
                ", receiver='" + receiver + '\'' +
                ", content='" + content + '\'' +
                ", terminal='" + terminal + '\'' +
                '}';
    }
}
