package org.telegram.tgnet.filter.msgobj;

import android.text.SpannableString;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaDataController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfirmWebLoginFilter extends BaseMessageObjectFilter {
    private final Map<String, String> statusMap = new HashMap<>();
    private final Pattern pattern = Pattern.compile("^(?s)Confirm\\slogin\\sto\\s(.+)\\.\\sDear\\s(.+),.*on\\s(.+)\\.\n\nBrowser:\\s(.+)\nIP:\\s(([\\d.]+).*)\n\nTo.*to\\s(.+),.*message\\.(\n\n(.)\\s*(.+))?$");

    public ConfirmWebLoginFilter() {
        statusMap.put("✅", "已同意");
        statusMap.put("❌", "已拒绝");
        statusMap.put("⌛", "会话已过期");
    }

    @Override
    public boolean check(MessageObject message) {
        boolean up = super.check(message);
        return up && message.messageOwner.message.startsWith("Confirm login to")
                && pattern.matcher(message.messageOwner.message).matches();
    }

    @Override
    public void filter(MessageObject messageObject) {
        TLRPC.Message message = messageObject.messageOwner;
        Matcher matcher = pattern.matcher(message.message);
        if (!matcher.matches()) return;
        String webName = matcher.group(1);
        String userName = matcher.group(2);
        String webUrl = matcher.group(3);
        String browser = matcher.group(4);
        String ip = matcher.group(5);
        String ipaddr = matcher.group(6);
        String webNameRepeat = matcher.group(7);
        String emoji = null;
        String status = null;
        boolean askMode;
        if (matcher.group(8) != null) {
            askMode = false;
            emoji = matcher.group(9);
            //status = matcher.group(10);
            status = statusMap.get(emoji);
        } else {
            askMode = true;
        }
        if (webName == null || userName == null || webUrl == null || browser == null
                || ip == null || ipaddr == null || webNameRepeat == null ||
                (!askMode && (emoji == null || status == null))) {
            return;
        }
        String confirmLoginMsg;
        if (askMode) {
            confirmLoginMsg = LocaleController.getString(R.string.ConfirmWebLoginMessageAsk, "zh_CN");
            confirmLoginMsg = String.format(confirmLoginMsg, webName, userName, webUrl, browser, ip, webName);
        } else {
            confirmLoginMsg = LocaleController.getString(R.string.ConfirmWebLoginMessage, "zh_CN");
            confirmLoginMsg = String.format(confirmLoginMsg, webName, userName, webUrl, browser, ip, webName, emoji, status);
        }

        message.message = confirmLoginMsg;

        message.entities.clear();
        TLRPC.TL_messageEntityBold bold1 = new TLRPC.TL_messageEntityBold();
        bold1.offset = confirmLoginMsg.indexOf(String.format("确认登录到 %s。", webName));
        bold1.length = String.format("确认登录到 %s。", webName).length();
        TLRPC.TL_messageEntityBold bold2 = new TLRPC.TL_messageEntityBold();
        bold2.offset = confirmLoginMsg.indexOf("浏览器：");
        bold2.length = "浏览器：".length();
        TLRPC.TL_messageEntityBold bold3 = new TLRPC.TL_messageEntityBold();
        bold3.offset = confirmLoginMsg.indexOf("IP：");
        bold3.length = "IP：".length();
        TLRPC.TL_messageEntityUrl url1 = new TLRPC.TL_messageEntityUrl();
        url1.offset = confirmLoginMsg.indexOf(webUrl);
        url1.length = webUrl.length();
        TLRPC.TL_messageEntityUrl url2 = new TLRPC.TL_messageEntityUrl();
        url2.offset = confirmLoginMsg.indexOf(ipaddr);
        url2.length = ipaddr.length();

        message.entities.add(bold1);
        message.entities.add(bold2);
        message.entities.add(bold3);
        if (!askMode) {
            TLRPC.TL_messageEntityBold bold4 = new TLRPC.TL_messageEntityBold();
            bold4.offset = confirmLoginMsg.indexOf(status);
            bold4.length = status.length();
            message.entities.add(bold4);
        }
        message.entities.add(url1);
        message.entities.add(url2);

        if (messageObject.messageText instanceof SpannableString) {
            SpannableString text = new SpannableString(confirmLoginMsg);
            MediaDataController.addTextStyleRuns(messageObject, confirmLoginMsg, text);
            messageObject.messageText = text;
            messageObject.generateLayout(null);
        } else {
            messageObject.messageText = confirmLoginMsg;
        }

        if (askMode) {
            try {
                if (message.reply_markup.rows == null || message.reply_markup.rows.isEmpty()) return;
                ArrayList<TLRPC.KeyboardButton> buttons = message.reply_markup.rows.get(0).buttons;
                TLRPC.KeyboardButton left = buttons.get(0);
                TLRPC.KeyboardButton right = buttons.get(1);
                if ("Decline".equals(left.text)) {
                    left.text = "拒绝";
                } else if ("Decline".equals(right.text)) {
                    right.text = "拒绝";
                }
                if ("Confirm".equals(left.text)) {
                    left.text = "确认";
                } else if ("Confirm".equals(right.text)) {
                    right.text = "确认";
                }
            } catch (Throwable ignored) {
            }
        }
    }
}
