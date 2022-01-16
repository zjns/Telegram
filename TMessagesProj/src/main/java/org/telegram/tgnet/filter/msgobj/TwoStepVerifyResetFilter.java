package org.telegram.tgnet.filter.msgobj;

import android.text.SpannableString;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaDataController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.R;
import org.telegram.messenger.time.FastDateFormat;
import org.telegram.tgnet.TLRPC;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TwoStepVerifyResetFilter extends BaseMessageObjectFilter {
    private final Map<String, String> statusMap = new HashMap<>();
    private final Pattern pattern = Pattern.compile("^(?s)IMPORTANT\\.\\sRequest\\sto\\sreset\\spassword\\.\n\nDear\\s(.+),.*password\\son\\s(.+)\\.\\sIf.*below\\.\n\nLocation:\\s(.*\\(IP\\s=\\s([\\d.]+)\\))\nDevice:\\s(.*)\n\nYou\\shave\\s(\\d)\\s.*below\\.\n\nIf.*until\\s(.*),\\sit.*Team(\n\n(.)\\s*(.+))?$");
    private final FastDateFormat format = FastDateFormat.getInstance("yyyy年M月d日 ah:mm:ss", Locale.CHINA);
    private final FastDateFormat parseFormat = FastDateFormat.getInstance("dd/MM/yyyy 'at' HH:mm:ss z", Locale.ROOT);

    public TwoStepVerifyResetFilter() {
        statusMap.put("❌", "已取消");
    }

    @Override
    public boolean check(MessageObject message) {
        boolean up = super.check(message);
        return up && message.messageOwner.message.startsWith("IMPORTANT. Request to reset password.")
                && pattern.matcher(message.messageOwner.message).matches();
    }

    @Override
    public void filter(MessageObject messageObject) {
        TLRPC.Message message = messageObject.messageOwner;
        Matcher matcher = pattern.matcher(message.message);
        if (!matcher.matches()) return;
        String userName = matcher.group(1);
        String requestDate = matcher.group(2);
        String location = matcher.group(3);
        String ip = matcher.group(4);
        String device = matcher.group(5);
        String validDays = matcher.group(6);
        String deadlineDate = matcher.group(7);
        String emoji = null;
        String status = null;
        boolean askMode;
        if (matcher.group(8) != null) {
            askMode = false;
            emoji = matcher.group(9);
            status = matcher.group(10);
            if ("❌".equals(emoji)) {
                if ("Already Canceled".equals(status)) {
                    status = "早已取消";
                } else if ("Request Canceled".equals(status)) {
                    status = "已取消";
                } else {
                    status = statusMap.get(emoji);
                }
            } else {
                status = statusMap.get(emoji);
            }
        } else {
            askMode = true;
        }
        if (userName == null || requestDate == null || location == null || ip == null
                || device == null || validDays == null || deadlineDate == null
                || (!askMode && (emoji == null || status == null))) {
            return;
        }
        try {
            Date parse = parseFormat.parse(requestDate);
            if (parse != null) {
                requestDate = format.format(parse);
            }
            parse = parseFormat.parse(deadlineDate);
            if (parse != null) {
                deadlineDate = format.format(parse);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String resetMessage;
        if (askMode) {
            resetMessage = LocaleController.getString(R.string.TwoStepVerifyResetAsk, "zh_CN");
            resetMessage = String.format(resetMessage, userName, requestDate, location, device, validDays, deadlineDate);
        } else {
            resetMessage = LocaleController.getString(R.string.TwoStepVerifyReset, "zh_CN");
            resetMessage = String.format(resetMessage, userName, requestDate, location, device, validDays, deadlineDate, emoji, status);
        }

        message.message = resetMessage;

        message.entities.clear();
        TLRPC.TL_messageEntityBold bold1 = new TLRPC.TL_messageEntityBold();
        bold1.offset = resetMessage.indexOf("非常重要，请求重置密码。");
        bold1.length = "非常重要，请求重置密码。".length();
        TLRPC.TL_messageEntityBold bold2 = new TLRPC.TL_messageEntityBold();
        bold2.offset = resetMessage.indexOf(validDays + " 天");
        bold2.length = (validDays + " 天").length();
        TLRPC.TL_messageEntityBold bold3 = new TLRPC.TL_messageEntityBold();
        bold3.offset = resetMessage.indexOf("不知道当前密码");
        bold3.length = "不知道当前密码".length();
        TLRPC.TL_messageEntityUrl url = new TLRPC.TL_messageEntityUrl();
        url.offset = resetMessage.indexOf(ip);
        url.length = ip.length();
        message.entities.add(bold1);
        message.entities.add(bold2);
        message.entities.add(bold3);
        message.entities.add(url);

        if (messageObject.messageText instanceof SpannableString) {
            SpannableString text = new SpannableString(resetMessage);
            MediaDataController.addTextStyleRuns(messageObject, resetMessage, text);
            messageObject.messageText = text;
            messageObject.generateLayout(null);
        } else {
            messageObject.messageText = resetMessage;
        }

        if (askMode) {
            try {
                if (message.reply_markup.rows == null || message.reply_markup.rows.isEmpty())
                    return;
                ArrayList<TLRPC.KeyboardButton> buttons = message.reply_markup.rows.get(0).buttons;
                TLRPC.KeyboardButton button = buttons.get(0);
                if ("Cancel Reset Request".equals(button.text)) {
                    button.text = "取消重置请求";
                }
            } catch (Throwable ignored) {
            }
        }
    }
}
