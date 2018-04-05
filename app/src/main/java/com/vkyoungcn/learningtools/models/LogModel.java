package com.vkyoungcn.learningtools.models;


/*
* 对应各组任务的一次学习和复习的记录
* 可以整理成简单的字串形式"N#YYYY-MM-DD hh:mm:ss#false;"
* 记录内使用#分隔，每条记录以英文分号结尾。
*
* 注意，即时某次复习缺失，其记录次数也不应删除，否则影响计算。
* */
public class LogModel {
    private short n;//本次记忆对应的次数；其中初次记忆=0，第n次复习= n。
    private long timeInMilli;//本次记忆对应的完成时间
    private boolean MISS = false;//本次记忆是否未在规定时限内完成（未完成=miss=true）。

    public LogModel() {
    }

    public LogModel(short n, long timeInMilli, boolean MISS) {
        this.n = n;
        this.timeInMilli = timeInMilli;
        this.MISS = MISS;
    }

    public short getN() {
        return n;
    }

    public void setN(short n) {
        this.n = n;
    }

    public long getTimeInMilli() {
        return timeInMilli;
    }

    public void setTimeInMilli(long timeInMilli) {
        this.timeInMilli = timeInMilli;
    }

    public boolean isMISS() {
        return MISS;
    }

    public void setMISS(boolean MISS) {
        this.MISS = MISS;
    }
}
