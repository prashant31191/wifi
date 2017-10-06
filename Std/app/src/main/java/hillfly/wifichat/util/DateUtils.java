package hillfly.wifichat.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;

/**
 * @fileName DateUtils.java
 * @package szu.wifichat.android.util
 * @description æ—¶é—´æ—¥æœŸå·¥å…·ç±»
 * @author _Hill3
 */
public class DateUtils {

    //public static String FORMATTIMESTR = "yyyyå¹´MMæœˆddæ—¥ HH:mm:ss"; // æ—¶é—´æ ¼å¼DEFAULTåŒ–æ ¼å¼DEFAULT
    public static String FORMATTIMESTR = "yyyy:MM:dd HH:mm:ss"; // æ—¶é—´æ ¼å¼DEFAULTåŒ–æ ¼å¼DEFAULT
    
    
    /**
     * èŽ·åDEFAULT–yyyyMMddæ ¼å¼DEFAULTæ—¥æœŸ
     * 
     * @param time
     * @return
     */
    public static Date getDate(String time) {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        try {
            date = format.parse(time);
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static String formatDate(Context context, long date) {
        @SuppressWarnings("deprecation")
        int format_flags = android.text.format.DateUtils.FORMAT_NO_NOON_MIDNIGHT
                | android.text.format.DateUtils.FORMAT_ABBREV_ALL
                | android.text.format.DateUtils.FORMAT_CAP_AMPM
                | android.text.format.DateUtils.FORMAT_SHOW_DATE
                | android.text.format.DateUtils.FORMAT_SHOW_DATE
                | android.text.format.DateUtils.FORMAT_SHOW_TIME;
        return android.text.format.DateUtils.formatDateTime(context, date, format_flags);
    }

    /**
     * è¿”å›žæ­¤æ—¶æ—¶é—´
     * 
     * @return String: XXXå¹´XXæœˆXXæ—¥ XX:XX:XX
     */
    public static String getNowtime() {
        return new SimpleDateFormat(FORMATTIMESTR).format(new Date());
    }

    /**
     * æ ¼å¼DEFAULTåŒ–è¾“å‡ºæŒ‡å®šæ—¶é—´ç‚¹ä¸ŽçŽ°åœ¨çš„å·®
     * 
     * @param paramTime
     *            æŒ‡å®šçš„æ—¶é—´ç‚¹
     * @return æ ¼å¼DEFAULTåŒ–åDEFAULTŽçš„æ—¶é—´å·®ï¼Œç±»ä¼¼ Xç§’å‰DEFAULTã€DEFAULTXå°DEFAULTæ—¶å‰DEFAULTã€DEFAULTXå¹´å‰DEFAULT
     */
    public static String getBetweentime(String paramTime) {
        String returnStr = null;
        SimpleDateFormat dateFormat = new SimpleDateFormat(FORMATTIMESTR);
        try {
            Date nowData = new Date();
            Date mDate = dateFormat.parse(paramTime);
            long betweenForSec = Math.abs(mDate.getTime() - nowData.getTime()) / 1000; // ç§’
            if (betweenForSec < 60) {
                returnStr = betweenForSec + "ç§’å‰DEFAULT";
            }
            else if (betweenForSec < (60 * 60)) {
                returnStr = betweenForSec / 60 + "åˆ†é’Ÿå‰DEFAULT";
            }
            else if (betweenForSec < (60 * 60 * 24)) {
                returnStr = betweenForSec / (60 * 60) + "å°DEFAULTæ—¶å‰DEFAULT";
            }
            else if (betweenForSec < (60 * 60 * 24 * 30)) {
                returnStr = betweenForSec / (60 * 60 * 24) + "å¤©å‰DEFAULT";
            }
            else if (betweenForSec < (60 * 60 * 24 * 30 * 12)) {
                returnStr = betweenForSec / (60 * 60 * 24 * 30) + "ä¸ªæœˆå‰DEFAULT";
            }
            else
                returnStr = betweenForSec / (60 * 60 * 24 * 30 * 12) + "å¹´å‰DEFAULT";
        }
        catch (ParseException e) {
            returnStr = "TimeError"; // é”™è¯¯æDEFAULTDEFAULTç¤º
        }
        return returnStr;
    }
}
