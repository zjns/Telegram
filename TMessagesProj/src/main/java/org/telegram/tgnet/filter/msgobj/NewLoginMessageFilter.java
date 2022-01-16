package org.telegram.tgnet.filter.msgobj;

import android.text.SpannableString;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaDataController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.R;
import org.telegram.messenger.time.FastDateFormat;
import org.telegram.tgnet.TLRPC;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NewLoginMessageFilter extends BaseMessageObjectFilter {
    private final Pattern pattern = Pattern.compile("^(?s)New\\slogin\\.\\sDear\\s(.+),.*on\\s(\\d{2})/(\\d{2})/(\\d{4})\\sat\\s(\\d{2}):(\\d{2}):(\\d{2})\\s(.+)\\.\n\nDevice:\\s(.+)\nLocation:\\s(.+\\(IP\\s=\\s([\\d.]+)\\))\n.*Sessions\\)\\.$");
    private final FastDateFormat format = FastDateFormat.getInstance("yyyy年M月d日 ah:mm:ss", Locale.CHINA);

    @Override
    public boolean check(MessageObject message) {
        boolean up = super.check(message);
        return up && message.messageOwner.message.startsWith("New login.")
                && pattern.matcher(message.messageOwner.message).matches();
    }

    @Override
    public void filter(MessageObject messageObject) {
        TLRPC.Message message = messageObject.messageOwner;
        String newDeviceMsg = LocaleController.getString(R.string.NewDeviceMessage, "zh_CN");
        Matcher matcher = pattern.matcher(message.message);
        if (!matcher.matches()) return;
        String userName = matcher.group(1);
        String day = matcher.group(2);
        String month = matcher.group(3);
        String year = matcher.group(4);
        String hour = matcher.group(5);
        String minute = matcher.group(6);
        String second = matcher.group(7);
        String timezone = matcher.group(8);
        String device = matcher.group(9);
        String location = matcher.group(10);
        String ip = matcher.group(11);
        if (userName == null || day == null || month == null || year == null || hour == null
                || minute == null || second == null || timezone == null || device == null
                || location == null || ip == null) {
            return;
        }
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(timezone));
        calendar.set(Calendar.YEAR, Integer.parseInt(year));
        calendar.set(Calendar.MONTH, Integer.parseInt(month) - 1);
        calendar.set(Calendar.DATE, Integer.parseInt(day));
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hour));
        calendar.set(Calendar.MINUTE, Integer.parseInt(minute));
        calendar.set(Calendar.SECOND, Integer.parseInt(second));
        String time = format.format(calendar.getTime());
        newDeviceMsg = String.format(newDeviceMsg, userName, time, device, location);
        message.message = newDeviceMsg;
        message.entities.clear();
        TLRPC.TL_messageEntityBold bold1 = new TLRPC.TL_messageEntityBold();
        bold1.offset = newDeviceMsg.indexOf("新设备登录。");
        bold1.length = "新设备登录。".length();
        message.entities.add(bold1);
        TLRPC.TL_messageEntityBold bold2 = new TLRPC.TL_messageEntityBold();
        bold2.offset = newDeviceMsg.indexOf("设置 > 已登录设备");
        bold2.length = "设置 > 已登录设备".length();
        message.entities.add(bold2);
        TLRPC.TL_messageEntityBold bold3 = new TLRPC.TL_messageEntityBold();
        bold3.offset = newDeviceMsg.indexOf("设置 > 隐私和安全 > 已登录设备");
        bold3.length = "设置 > 隐私和安全 > 已登录设备".length();
        message.entities.add(bold3);
        TLRPC.TL_messageEntityUrl url = new TLRPC.TL_messageEntityUrl();
        url.offset = newDeviceMsg.indexOf(ip);
        url.length = ip.length();
        message.entities.add(url);

        if (messageObject.messageText instanceof SpannableString) {
            SpannableString text = new SpannableString(newDeviceMsg);
            MediaDataController.addTextStyleRuns(messageObject, newDeviceMsg, text);
            messageObject.messageText = text;
            messageObject.generateLayout(null);
        } else {
            messageObject.messageText = newDeviceMsg;
        }
    }
}
