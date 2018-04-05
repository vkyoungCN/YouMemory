package com.vkyoungcn.learningtools.spiralCore;

import android.util.Log;

import com.vkyoungcn.learningtools.models.CurrentState;
import com.vkyoungcn.learningtools.models.LogModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/*
* 是对应一组任务的所有已完成记忆的Log的集合
* */
public class LogList {
    private static final String TAG = "LogList";

    /*
    * 将（从DB获取的）字串形式的Logs记录转换成List<LogModel>的形式
    * 记录为空或空指针时直接返回null；
    * */
    public static List<LogModel> textListLogToListLog(String textListLog){
        if(textListLog==null||textListLog.isEmpty())
            return null;
        List<LogModel> logsInListLogModel = null;

        String[] logsInStringArray =  textListLog.split(";");//各条log之间以英文分号结尾；其余位置没有该符号。

        for (String logS:logsInStringArray) {
            LogModel logModel = new LogModel();
//            android.util.Log.i(TAG, "textListLogToListLog: inside Loop, LogInSting is:"+logsInStringArray);
            String[] elements = logS.split("#");//记录内三段以#分隔
            logModel.setN(Short.parseShort(elements[0]));//第一项是数字N，次数，short型。

            SimpleDateFormat simpleDateFormat= new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            Date date = null;
            try {
                date = simpleDateFormat.parse(elements[1]);//第二项是日期时间，转Date、再转long。
//                android.util.Log.i(TAG, "textListLogToListLog: element[1]="+elements[1]);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            logModel.setTimeInMilli(date.getTime());
            logModel.setMISS(Boolean.parseBoolean(elements[2]));//第三项得到的直接是小写true或false。

            logsInListLogModel.add(logModel);
        }

        return logsInListLogModel;
    }


    /*
    * 根据当前时间，计算指定任务分组当前所处的颜色状态，以及到下一阶段还剩多长时间；
    * 参数传入与任务对应的CurrentState引用，直接修改；
    * 需要传入任务对应的list<LogModel>
    * 前24小时才是黄金时间；前72小时都很重要；建议加班记忆。
    * */
    //【注意，待】虽然向DB写数据应在onStop处进行，但应该在时间超过阶段节点时先将更新的Log-miss记录
    //附加给Group Manager，以免旧数据影响计算。
    //【待2】应该在时段截至前10或20分钟停止计时，不再等待复习，否则时间不足无法完成复习的。
    public static void setCurrentStateForGroup(CurrentState currentState, List<LogModel> list){

        LogModel log = null;
        long timeAmountMinutes = 0;
        int n = 0;
        Log.i(TAG, "setCurrentStateForGroup: before");

        if(list==null||list.size()==0){//新建分组，还没开始学习。尽快开始学习。
            Log.i(TAG, "setCurrentStateForGroup: list null or ==0, newly group?");
            currentState.setColor(CurrentState.Color.COLOR_NEWLY);
            return;
        }
        switch (list.size()){

            case 1://只有一次记录，此时应该有n==0，该记录是初次记忆时间
                //R1的计算比较特别，可直接与初记时间计算差值
                log =list.get(0);
                timeAmountMinutes = (System.currentTimeMillis()-log.getTimeInMilli())/(1000*60);

                if(timeAmountMinutes<30){
                    currentState.setColor(CurrentState.Color.COLOR_STILL_NOT);
                    currentState.setRemainingMinutes((short) timeAmountMinutes);
                    currentState.setRemainingHours((short) 0);
                    currentState.setRemainingDays((short) 0);

                }else if (timeAmountMinutes<60){//位于[30,60)半开半闭区间内，可以执行首次复习
                    currentState.setColor(CurrentState.Color.COLOR_AVAILABLE);
                    currentState.setRemainingMinutes((short)(60-timeAmountMinutes));
                    currentState.setRemainingHours((short) 0);
                    currentState.setRemainingDays((short) 0);
                    //R1的特殊之处还有：
                    //需要在+50min内进入（总+1h内完成）。
                }else if(timeAmountMinutes<360){//初记后，不到6小时
                    //只有初记记录，且时间已超过1小时（待，处理与开始复习的2小时时限相容问题），说明
                    //是错过了第一次（+30min）的复习，但此次不计算缺失。
                    currentState.setColor(CurrentState.Color.COLOR_STILL_NOT);//仍然算未到时间
                    currentState.setRemainingMinutes((short)((360-timeAmountMinutes)%60));
                    currentState.setRemainingHours((short)((360-timeAmountMinutes)/60));
                    currentState.setRemainingDays((short) 0);
                }else if(timeAmountMinutes<720){//初记后，6小时~12小时之间，应执行第二次复习R2。
                    currentState.setColor(CurrentState.Color.COLOR_AVAILABLE);
                    currentState.setRemainingMinutes((short)((720-timeAmountMinutes)%60));
                    currentState.setRemainingHours((short)((720-timeAmountMinutes)/60));
                    currentState.setRemainingDays((short) 0);
                }else if(timeAmountMinutes<1440){//初记后，12~24小时之间，应执行第三次复习R3，且记丢失一次。
                    currentState.setColor(CurrentState.Color.COLOR_MISSED_ONCE);
                    currentState.setRemainingMinutes((short)((1440-timeAmountMinutes)%60));
                    currentState.setRemainingHours((short)((1440-timeAmountMinutes)/60));
                    currentState.setRemainingDays((short) 0);
                }else{//超过一天没复习，凉了。
                    currentState.setColor(CurrentState.Color.COLOR_MISSED_TWICE);
                    currentState.setRemainingMinutes((short) 0);
                    currentState.setRemainingHours((short) 0);
                    currentState.setRemainingDays((short) 0);
                }
                break;//case == 1 (Log记录仅有1项)到此完成。
            case 2:  //log.size==2，有两项记录。应该是初记和30分钟记忆；
                log = list.get(0);
                timeAmountMinutes = (System.currentTimeMillis()-log.getTimeInMilli())/(1000*60);

                if(timeAmountMinutes<60){//此时间段内的复习已完成。
                    currentState.setColor(CurrentState.Color.COLOR_STILL_NOT);
                    currentState.setRemainingMinutes((short)0);
                    currentState.setRemainingHours((short)0);
                    currentState.setRemainingDays((short) 0);
                }else if(timeAmountMinutes<360){
                    currentState.setColor(CurrentState.Color.COLOR_STILL_NOT);
                    currentState.setRemainingMinutes((short)((360-timeAmountMinutes)%60));
                    currentState.setRemainingHours((short)((360-timeAmountMinutes)/60));
                    currentState.setRemainingDays((short) 0);
                }else if (timeAmountMinutes<720){//位于[360,720)半开半闭区间内，可以执行第二次复习
                    currentState.setColor(CurrentState.Color.COLOR_AVAILABLE);
                    currentState.setRemainingMinutes((short)((720-timeAmountMinutes)%60));
                    currentState.setRemainingHours((short)((720-timeAmountMinutes)/60));
                    currentState.setRemainingDays((short) 0);
                }else if(timeAmountMinutes<1440){//初记后，12小时以上，不到24小时；R2Miss
                    currentState.setColor(CurrentState.Color.COLOR_MISSED_ONCE);
                    currentState.setRemainingMinutes((short)((1440-timeAmountMinutes)%60));
                    currentState.setRemainingHours((short)((1440-timeAmountMinutes)/60));
                    currentState.setRemainingDays((short) 0);
                }else{//超过一天没复习（只在30分钟进行了复习），凉了。
                    currentState.setColor(CurrentState.Color.COLOR_MISSED_TWICE);
                    currentState.setRemainingMinutes((short) 0);
                    currentState.setRemainingHours((short) 0);
                    currentState.setRemainingDays((short) 0);
                }
                break;

            case 3:  //log.size==3，最后记录应是R2（第二次复习，6~12h）；
                n = 3;
                log = list.get(0);
                timeAmountMinutes = (System.currentTimeMillis()-log.getTimeInMilli())/(1000*60);

                if(timeAmountMinutes<(90*2^n)){//此时间段内的复习已完成。
                    currentState.setColor(CurrentState.Color.COLOR_STILL_NOT);
                    currentState.setRemainingMinutes((short)0);
                    currentState.setRemainingHours((short)0);
                    currentState.setRemainingDays((short) 0);
                }else if(timeAmountMinutes<(180*2^n)){//超过12小时，不足24小时，可以进行R3
                    currentState.setColor(CurrentState.Color.COLOR_AVAILABLE);
                    currentState.setRemainingMinutes((short)(((180*2^n)-timeAmountMinutes)%60));
                    currentState.setRemainingHours((short)(((180*2^n)-timeAmountMinutes)/60));
                    currentState.setRemainingDays((short) 0);
                }else if(timeAmountMinutes<(360*2^n)){//初记后，24小时以上，不到48小时；R3Miss
                    currentState.setColor(CurrentState.Color.COLOR_MISSED_ONCE);
                    currentState.setRemainingMinutes((short)(((360*2^n)-timeAmountMinutes)%60));
                    currentState.setRemainingHours((short)(((360*2^n)-timeAmountMinutes)/60));
                    currentState.setRemainingDays((short) 0);
                }else{//超过48小时，连续MISS两次复习，凉。
                    currentState.setColor(CurrentState.Color.COLOR_MISSED_TWICE);
                    currentState.setRemainingMinutes((short) 0);
                    currentState.setRemainingHours((short) 0);
                    currentState.setRemainingDays((short) 0);
                }
                break;

            case 4:  //log.size==4，最后记录应是R3（第三次复习，12~24h）；
                n=4;
                log = list.get(0);
                timeAmountMinutes = (System.currentTimeMillis()-log.getTimeInMilli())/(1000*60);

                if(timeAmountMinutes<(90*2^n)){//此时间段内的复习已完成。
                    currentState.setColor(CurrentState.Color.COLOR_STILL_NOT);
                    currentState.setRemainingMinutes((short)0);
                    currentState.setRemainingHours((short)0);
                    currentState.setRemainingDays((short) 0);
                }else if(timeAmountMinutes<(180*2^n)){//超过24小时，不足48小时，可以进行R4
                    currentState.setColor(CurrentState.Color.COLOR_AVAILABLE);
                    currentState.setRemainingMinutes((short)(((180*2^n)-timeAmountMinutes)%60));
                    currentState.setRemainingHours((short)(((180*2^n)-timeAmountMinutes)/60));
                    currentState.setRemainingDays((short) 0);
                }else if(timeAmountMinutes<(360*2^n)){//初记后，48小时以上，不到96小时；R4Miss
                    currentState.setColor(CurrentState.Color.COLOR_MISSED_ONCE);
                    currentState.setRemainingMinutes((short) 0);
                    currentState.setRemainingHours((short)((((360*2^n)-timeAmountMinutes)/60)%24));
                    currentState.setRemainingDays((short)(((360*2^n)-timeAmountMinutes)/(60*24)));
                }else{//超过96小时，连续MISS两次复习，凉。
                    currentState.setColor(CurrentState.Color.COLOR_MISSED_TWICE);
                    currentState.setRemainingMinutes((short) 0);
                    currentState.setRemainingHours((short) 0);
                    currentState.setRemainingDays((short) 0);
                }
                break;

            case 5:  //log.size==5，最后记录应是R4（第4次复习，24~48h）；
                n=5;
                log = list.get(0);
                timeAmountMinutes = (System.currentTimeMillis()-log.getTimeInMilli())/(1000*60);

                if(timeAmountMinutes<(90*2^n)){//此时间段内的复习已完成。
                    currentState.setColor(CurrentState.Color.COLOR_STILL_NOT);
                    currentState.setRemainingMinutes((short)0);
                    currentState.setRemainingHours((short)0);
                    currentState.setRemainingDays((short) 0);
                }else if(timeAmountMinutes<(180*2^n)){//超过48小时，不足96小时，可以进行R5
                    currentState.setColor(CurrentState.Color.COLOR_AVAILABLE);
                    currentState.setRemainingMinutes((short)0);
                    currentState.setRemainingHours((short)((((180*2^n)-timeAmountMinutes)/60)%24));
                    currentState.setRemainingDays((short)(((180*2^n)-timeAmountMinutes)/(60*24)));
                }else if(timeAmountMinutes<(360*2^n)){//初记后，4天以上，不到8天；R5Miss
                    currentState.setColor(CurrentState.Color.COLOR_MISSED_ONCE);
                    currentState.setRemainingMinutes((short)0);
                    currentState.setRemainingHours((short)((((360*2^n)-timeAmountMinutes)/60)%24));
                    currentState.setRemainingDays((short)(((360*2^n)-timeAmountMinutes)/(60*24)));
                }else{//超过8天，连续MISS两次复习，凉。
                    currentState.setColor(CurrentState.Color.COLOR_MISSED_TWICE);
                    currentState.setRemainingMinutes((short) 0);
                    currentState.setRemainingHours((short) 0);
                    currentState.setRemainingDays((short) 0);
                }
                break;

            case 6:  //log.size==6，最后记录应是R5（第5次复习，2~4天）；
                n=6;
                log = list.get(0);
                timeAmountMinutes = (System.currentTimeMillis()-log.getTimeInMilli())/(1000*60);

                if(timeAmountMinutes<(90*2^n)){//此时间段内的复习已完成。
                    currentState.setColor(CurrentState.Color.COLOR_STILL_NOT);
                    currentState.setRemainingMinutes((short)0);
                    currentState.setRemainingHours((short)0);
                    currentState.setRemainingDays((short) 0);
                }else if(timeAmountMinutes<(180*2^n)){
                    currentState.setColor(CurrentState.Color.COLOR_AVAILABLE);
                    currentState.setRemainingMinutes((short)0);
                    currentState.setRemainingHours((short)((((180*2^n)-timeAmountMinutes)/60)%24));
                    currentState.setRemainingDays((short)(((180*2^n)-timeAmountMinutes)/(60*24)));
                }else if(timeAmountMinutes<(360*2^n)){
                    currentState.setColor(CurrentState.Color.COLOR_MISSED_ONCE);
                    currentState.setRemainingMinutes((short)0);
                    currentState.setRemainingHours((short)((((360*2^n)-timeAmountMinutes)/60)%24));
                    currentState.setRemainingDays((short)(((360*2^n)-timeAmountMinutes)/(60*24)));
                }else{//连续MISS两次,凉。
                    currentState.setColor(CurrentState.Color.COLOR_MISSED_TWICE);
                    currentState.setRemainingMinutes((short) 0);
                    currentState.setRemainingHours((short) 0);
                    currentState.setRemainingDays((short) 0);
                }
                break;

            case 7:  //log.size==7，最后记录应是R6（第6次复习，4~8天）；
                n=7;
                log = list.get(0);
                timeAmountMinutes = (System.currentTimeMillis()-log.getTimeInMilli())/(1000*60);

                if(timeAmountMinutes<(90*2^n)){//此时间段内的复习已完成。
                    currentState.setColor(CurrentState.Color.COLOR_STILL_NOT);
                    currentState.setRemainingMinutes((short)0);
                    currentState.setRemainingHours((short)0);
                    currentState.setRemainingDays((short) 0);
                }else if(timeAmountMinutes<(180*2^n)){
                    currentState.setColor(CurrentState.Color.COLOR_AVAILABLE);
                    currentState.setRemainingMinutes((short)0);
                    currentState.setRemainingHours((short)((((180*2^n)-timeAmountMinutes)/60)%24));
                    currentState.setRemainingDays((short)(((180*2^n)-timeAmountMinutes)/(60*24)));
                }else if(timeAmountMinutes<(360*2^n)){
                    currentState.setColor(CurrentState.Color.COLOR_MISSED_ONCE);
                    currentState.setRemainingMinutes((short)0);
                    currentState.setRemainingHours((short)((((360*2^n)-timeAmountMinutes)/60)%24));
                    currentState.setRemainingDays((short)(((360*2^n)-timeAmountMinutes)/(60*24)));
                }else{//连续MISS两次,凉。
                    currentState.setColor(CurrentState.Color.COLOR_MISSED_TWICE);
                    currentState.setRemainingMinutes((short) 0);
                    currentState.setRemainingHours((short) 0);
                    currentState.setRemainingDays((short) 0);
                }
                break;

            case 8:  //log.size==8，最后记录应是R7（第7次复习，8~16天）；
                n=8;
                log = list.get(0);
                timeAmountMinutes = (System.currentTimeMillis()-log.getTimeInMilli())/(1000*60);

                if(timeAmountMinutes<(90*2^n)){//此时间段内的复习已完成。
                    currentState.setColor(CurrentState.Color.COLOR_STILL_NOT);
                    currentState.setRemainingMinutes((short)0);
                    currentState.setRemainingHours((short)0);
                    currentState.setRemainingDays((short) 0);
                }else if(timeAmountMinutes<(180*2^n)){//1个月之内都可以进行复习
                    currentState.setColor(CurrentState.Color.COLOR_AVAILABLE);
                    currentState.setRemainingMinutes((short)0);
                    currentState.setRemainingHours((short)((((180*2^n)-timeAmountMinutes)/60)%24));
                    currentState.setRemainingDays((short)(((180*2^n)-timeAmountMinutes)/(60*24)));
                }else{//超过1个月没复习，Miss第8次但此时已记忆多次不会再凉，后面都标橙色，不计时间。
                    currentState.setColor(CurrentState.Color.COLOR_MISSED_ONCE);
                    currentState.setRemainingMinutes((short)0);
                    currentState.setRemainingHours((short)0);
                    currentState.setRemainingDays((short)0);
                }
                break;

            case 9:  //log.size==9，最后记录应是R8（第8次复习，16~32天）；
                n=9;
                log = list.get(0);
                timeAmountMinutes = (System.currentTimeMillis()-log.getTimeInMilli())/(1000*60);

                if(timeAmountMinutes<(90*2^n)){//此时间段内的复习已完成。
                    currentState.setColor(CurrentState.Color.COLOR_STILL_NOT);
                    currentState.setRemainingMinutes((short)0);
                    currentState.setRemainingHours((short)0);
                    currentState.setRemainingDays((short)0);
                }else if(timeAmountMinutes<(180*2^n)){//1个月之内都可以进行复习
                    currentState.setColor(CurrentState.Color.COLOR_AVAILABLE);
                    currentState.setRemainingMinutes((short)0);
                    currentState.setRemainingHours((short)((((180*2^n)-timeAmountMinutes)/60)%24));
                    currentState.setRemainingDays((short)(((180*2^n)-timeAmountMinutes)/(60*24)));
                }else {//后期不会再出现红色状态，后面都标橙色，不计时间。
                    currentState.setColor(CurrentState.Color.COLOR_MISSED_ONCE);
                    currentState.setRemainingMinutes((short) 0);
                    currentState.setRemainingHours((short) 0);
                    currentState.setRemainingDays((short)(((180*2^n)-timeAmountMinutes)/(60*24)));
                }
                break;

            case 10:  //log.size==10，最后记录应是R9（第9次复习，32~64天）；
                n=10;
                log = list.get(0);
                timeAmountMinutes = (System.currentTimeMillis()-log.getTimeInMilli())/(1000*60);

                if(timeAmountMinutes<(90*2^n)){//此时间段内的复习已完成。
                    currentState.setColor(CurrentState.Color.COLOR_STILL_NOT);
                    currentState.setRemainingMinutes((short)0);
                    currentState.setRemainingHours((short)0);
                    currentState.setRemainingDays((short)0);
                }else if(timeAmountMinutes<(180*2^n)){//1个月之内都可以进行复习
                    currentState.setColor(CurrentState.Color.COLOR_AVAILABLE);
                    currentState.setRemainingMinutes((short)0);
                    currentState.setRemainingHours((short)((((180*2^n)-timeAmountMinutes)/60)%24));
                    currentState.setRemainingDays((short)(((180*2^n)-timeAmountMinutes)/(60*24)));
                }else {//后期不会再出现红色状态，后面都标橙色，不计时间。
                    currentState.setColor(CurrentState.Color.COLOR_MISSED_ONCE);
                    currentState.setRemainingMinutes((short) 0);
                    currentState.setRemainingHours((short) 0);
                    currentState.setRemainingDays((short)(((180*2^n)-timeAmountMinutes)/(60*24)));
                }
                break;

            case 11:  //log.size==11，最后记录应是R10（第10次复习，64~128天(2~4个月)）；
                n=11;
                log = list.get(0);
                timeAmountMinutes = (System.currentTimeMillis()-log.getTimeInMilli())/(1000*60);

                if(timeAmountMinutes<(90*2^n)){//此时间段内的复习已完成。
                    currentState.setColor(CurrentState.Color.COLOR_STILL_NOT);
                    currentState.setRemainingMinutes((short)0);
                    currentState.setRemainingHours((short)0);
                    currentState.setRemainingDays((short)0);
                }else if(timeAmountMinutes<(180*2^n)){//1个月之内都可以进行复习
                    currentState.setColor(CurrentState.Color.COLOR_AVAILABLE);
                    currentState.setRemainingMinutes((short)0);
                    currentState.setRemainingHours((short)((((180*2^n)-timeAmountMinutes)/60)%24));
                    currentState.setRemainingDays((short)(((180*2^n)-timeAmountMinutes)/(60*24)));
                }else {//后期不会再出现红色状态，后面都标橙色，不计时间。
                    currentState.setColor(CurrentState.Color.COLOR_MISSED_ONCE);
                    currentState.setRemainingMinutes((short) 0);
                    currentState.setRemainingHours((short) 0);
                    currentState.setRemainingDays((short)(((180*2^n)-timeAmountMinutes)/(60*24)));
                }
                break;

            case 12:  //log.size==12，最后记录应是R11（第11次复习，4~8个月)）；
                //最后一次复习，复习进程超4个月，很可能已在半年以上；所以只要复习到这里
                // （且中间没有连续缺失两次）就记为完成（但是如果中间有连续缺失，提示效果不好）
                    currentState.setColor(CurrentState.Color.COLOR_FULL);
                    currentState.setRemainingMinutes((short) 0);
                    currentState.setRemainingHours((short) 0);
                    currentState.setRemainingDays((short)0);
                break;
        }
        Log.i(TAG, "setCurrentStateForGroup: done.");
        return;

    }



}
