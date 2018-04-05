package com.vkyoungcn.learningtools.models;

public class CurrentState {
    private Color color = null;//当前对应的颜色阶段
    private short remainingMinutes = 0;//距离下一阶段还剩余多少分钟。
    private short remainingHours = 0;//距离下一阶段还剩余多少小时。
    private short remainingDays = 0;

    public static enum Color{
        COLOR_NEWLY,//新建组，没有初记Log,可能还没学习，建议绿色；
        COLOR_STILL_NOT,//还没到复习时间，建议对应无色；
        COLOR_AVAILABLE,//可以进行复习，应在xHxM内完成本次复习，建议蓝色；
        COLOR_MISSED_ONCE,//错过了上一次，应在xHxM内完成本次复习，建议橙色；
        COLOR_MISSED_TWICE,//连续错过两次，标红。
        COLOR_FULL,//完成12次记录，超4个月的记录,建议同样使用无色。
    }

    public CurrentState() {
    }

    public CurrentState(Color color, short remainingMinutes, short remainingHours) {
        this.color = color;
        this.remainingMinutes = remainingMinutes;
        this.remainingHours = remainingHours;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public short getRemainingMinutes() {
        return remainingMinutes;
    }

    public void setRemainingMinutes(short remainingMinutes) {
        this.remainingMinutes = remainingMinutes;
    }

    public short getRemainingHours() {
        return remainingHours;
    }

    public void setRemainingHours(short remainingHours) {
        this.remainingHours = remainingHours;
    }

    public short getRemainingDays() {
        return remainingDays;
    }

    public void setRemainingDays(short remainingDays) {
        this.remainingDays = remainingDays;
    }
}
