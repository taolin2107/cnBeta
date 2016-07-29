package app.taolin.cnbeta.utils;

import android.content.Context;
import android.text.TextUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import app.taolin.cnbeta.R;

/**
 * @author taolin
 * @version v1.0
 * @date Jul 11, 2016.
 * @description
 */

public class ContentUtil {

    private static final int MINUTE = 60 * 1000;
    private static final int HOUR = 60 * MINUTE;
    private static final int DAY = 24 * HOUR;

    private static final SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

    public static String getContentListUrl(final String endSid) {
        StringBuilder sb = new StringBuilder();
        sb.append("app_key=10000");
        sb.append("&end_sid=").append(endSid);
        sb.append("&format=json");
        sb.append("&method=Article.Lists");
        sb.append("&timestamp=").append(System.currentTimeMillis());
        sb.append("&v=2.8.5");
        final String signed = EncryptUtil.md5(sb.toString() + "&mpuffgvbvbttn3Rc");
        sb.append("&sign=").append(signed);
        sb.insert(0, "http://api.cnbeta.com/capi?");
        return sb.toString();
    }

    public static String getContentUrl(final String sid) {
        StringBuilder sb = new StringBuilder();
        sb.append("app_key=10000");
        sb.append("&format=json");
        sb.append("&method=Article.NewsContent");
        sb.append("&sid=").append(sid);
        sb.append("&timestamp=").append(System.currentTimeMillis());
        sb.append("&v=2.8.5");
        final String signed = EncryptUtil.md5(sb.toString() + "&mpuffgvbvbttn3Rc");
        sb.append("&sign=").append(signed);
        sb.insert(0, "http://api.cnbeta.com/capi?");
        return sb.toString();
    }

    public static String getPrettyTime(Context context, String time) {
        try {
            Date pubTime = mDateFormat.parse(time);
            long pastTime = System.currentTimeMillis() - pubTime.getTime();
            if (pastTime < HOUR) {
                return context.getString(R.string.past_minutes, pastTime / MINUTE);
            } else if (pastTime < DAY) {
                return context.getString(R.string.past_hours, pastTime / HOUR);
            } else {
                return context.getString(R.string.past_days, pastTime / DAY);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getFormatTime() {
        return mDateFormat.format(new Date());
    }

    private static final String[] filter = {"<strong>", "</strong>", "<p.*?>"};

    public static String filterContent(String content) {
        if (TextUtils.isEmpty(content)) {
            return content;
        }
        for (String f: filter) {
            content = content.replaceAll(f, "");
        }
        content = content.replaceAll("<br/></p>|<br></p>|</p>", "<br>");
        content = content.trim();
        if (content.endsWith("<br>")) {
            content = content.substring(0, content.length() - 4);
        }
        return content;
    }
}
