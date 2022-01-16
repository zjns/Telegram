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

public class TwoStepVerifyDisabledFilter extends BaseMessageObjectFilter {
    private final Pattern pattern = Pattern.compile("^(?s)Two-Step\\sVerification\\s.*Dear\\s(.*),.*disabled\\son\\s(\\d{2})/(\\d{2})/(\\d{4})\\sat\\s(\\d{2}):(\\d{2}):(\\d{2})\\s(.*)\\.\n\nDevice:\\s(.*)\nLocation:\\s(.*\\(IP\\s=\\s([\\d.]+)\\))\n\n.*Sessions\\)\\.$");
    private final FastDateFormat format = FastDateFormat.getInstance("yyyy年M月d日 ah:mm:ss", Locale.CHINA);

    @Override
    public boolean check(MessageObject message) {
        boolean up = super.check(message);
        return up && message.messageOwner.message.startsWith("Two-Step Verification disabled")
                && pattern.matcher(message.messageOwner.message).matches();
    }

    @Override
    public void filter(MessageObject messageObject) {
        TLRPC.Message message = messageObject.messageOwner;
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

        if (userName == null || day == null || month == null || year == null
                || hour == null || minute == null || second == null || timezone == null
                || device == null || location == null || ip == null) {
            return;
        }

        String msg = LocaleController.getString(R.string.TwoStepVerifyDisabled, "zh_CN");
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(timezone));
        calendar.set(Calendar.YEAR, Integer.parseInt(year));
        calendar.set(Calendar.MONTH, Integer.parseInt(month) - 1);
        calendar.set(Calendar.DATE, Integer.parseInt(day));
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hour));
        calendar.set(Calendar.MINUTE, Integer.parseInt(minute));
        calendar.set(Calendar.SECOND, Integer.parseInt(second));
        String time = format.format(calendar.getTime());
        msg = String.format(msg, userName, time, device, location);

        message.message = msg;
        message.entities.clear();
        TLRPC.TL_messageEntityBold bold1 = new TLRPC.TL_messageEntityBold();
        bold1.offset = msg.indexOf("两步验证已禁用。");
        bold1.length = "两步验证已禁用。".length();
        TLRPC.TL_messageEntityBold bold2 = new TLRPC.TL_messageEntityBold();
        bold2.offset = msg.indexOf("设置 > 已登录设备");
        bold2.length = "设置 > 已登录设备".length();
        TLRPC.TL_messageEntityBold bold3 = new TLRPC.TL_messageEntityBold();
        bold3.offset = msg.indexOf("设置 > 隐私和安全 > 已登录设备");
        bold3.length = "设置 > 隐私和安全 > 已登录设备".length();
        TLRPC.TL_messageEntityUrl url = new TLRPC.TL_messageEntityUrl();
        url.offset = msg.indexOf(ip);
        url.length = ip.length();
        message.entities.add(bold1);
        message.entities.add(bold2);
        message.entities.add(bold3);
        message.entities.add(url);

        if (messageObject.messageText instanceof SpannableString) {
            SpannableString text = new SpannableString(msg);
            MediaDataController.addTextStyleRuns(messageObject, msg, text);
            messageObject.messageText = text;
            messageObject.generateLayout(null);
        } else {
            messageObject.messageText = msg;
        }
    }
}
