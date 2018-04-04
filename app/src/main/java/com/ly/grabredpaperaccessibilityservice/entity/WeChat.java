package com.ly.grabredpaperaccessibilityservice.entity;

/**
 * Created by 刘样大帅B on 2018/3/16.
 */

public class WeChat {
    String strVersion1; //微信版本
    String strVersion2; //微信版本号
    String strAppearRedPaper; //出现红包的界面类
    String btnOpen;          //红包界面 打开按钮ID
    String strDetailRedPaper; //红包详情类
    String tvNoReadNum;     //首页未读数量
    String tvFingerSlow;    //红包抢慢了 ID
    String tvChatTitle;     //聊天标题
    String btnRightAdd;     //聊天界面右下角的加号按钮
    String tvMainChat;      //主界面的 每一项的聊天内容
    String tvMainItem;     //主界面的每一项
    String tvOpenRedPackage;  //聊天内容的领取红包字
    String tvMoney;       //红包详情中的钱

    public WeChat() {
    }

    public String getTvOpenRedPackage() {
        return tvOpenRedPackage;
    }

    public void setTvOpenRedPackage(String tvOpenRedPackage) {
        this.tvOpenRedPackage = tvOpenRedPackage;
    }

    public WeChat(String strVersion1, String strVersion2, String strAppearRedPaper, String btnOpen, String strDetailRedPaper, String tvNoReadNum, String tvFingerSlow, String tvChatTitle, String btnRightAdd, String tvMainChat, String tvMainItem, String tvOpenRedPackage, String tvMoney) {
        this.strVersion1 = strVersion1;
        this.strVersion2 = strVersion2;
        this.strAppearRedPaper = strAppearRedPaper;
        this.btnOpen = btnOpen;
        this.strDetailRedPaper = strDetailRedPaper;
        this.tvNoReadNum = tvNoReadNum;
        this.tvFingerSlow = tvFingerSlow;
        this.tvChatTitle = tvChatTitle;
        this.btnRightAdd = btnRightAdd;
        this.tvMainChat = tvMainChat;
        this.tvMainItem = tvMainItem;
        this.tvOpenRedPackage = tvOpenRedPackage;
        this.tvMoney = tvMoney;

    }

    public String getTvMoney() {
        return tvMoney;
    }

    public void setTvMoney(String tvMoney) {
        this.tvMoney = tvMoney;
    }

    public String getTvMainItem() {
        return tvMainItem;
    }

    public void setTvMainItem(String tvMainItem) {
        this.tvMainItem = tvMainItem;
    }

    public String getStrVersion1() {
        return strVersion1;
    }

    public void setStrVersion1(String strVersion1) {
        this.strVersion1 = strVersion1;
    }

    public String getStrVersion2() {
        return strVersion2;
    }

    public void setStrVersion2(String strVersion2) {
        this.strVersion2 = strVersion2;
    }

    public String getStrAppearRedPaper() {
        return strAppearRedPaper;
    }

    public void setStrAppearRedPaper(String strAppearRedPaper) {
        this.strAppearRedPaper = strAppearRedPaper;
    }

    public String getBtnOpen() {
        return btnOpen;
    }

    public void setBtnOpen(String btnOpen) {
        this.btnOpen = btnOpen;
    }

    public String getStrDetailRedPaper() {
        return strDetailRedPaper;
    }

    public void setStrDetailRedPaper(String strDetailRedPaper) {
        this.strDetailRedPaper = strDetailRedPaper;
    }

    public String getTvNoReadNum() {
        return tvNoReadNum;
    }

    public void setTvNoReadNum(String tvNoReadNum) {
        this.tvNoReadNum = tvNoReadNum;
    }

    public String getTvFingerSlow() {
        return tvFingerSlow;
    }

    public void setTvFingerSlow(String tvFingerSlow) {
        this.tvFingerSlow = tvFingerSlow;
    }

    public String getTvChatTitle() {
        return tvChatTitle;
    }

    public void setTvChatTitle(String tvChatTitle) {
        this.tvChatTitle = tvChatTitle;
    }

    public String getBtnRightAdd() {
        return btnRightAdd;
    }

    public void setBtnRightAdd(String btnRightAdd) {
        this.btnRightAdd = btnRightAdd;
    }

    public String getTvMainChat() {
        return tvMainChat;
    }

    public void setTvMainChat(String tvMainChat) {
        this.tvMainChat = tvMainChat;
    }
}
