package org.telegram.tgnet.filter.msgobj;

import android.text.SpannableString;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaDataController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLRPC;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebLoginViaBotFilter extends BaseMessageObjectFilter {
    private final Map<String, String> statusMap = new HashMap<>();
    private final Pattern pattern = Pattern.compile("^(?s)You\\shave\\ssuccessfully\\s.*on\\s(.+)\\svia\\s(@.+)\\.\\sThe.*picture\\.(\\s(@.+)\\salso.*Telegram\\.)?\n\nDevice:\\s(.*)\nIP:\\s(([\\d.]+).*)\n\n.*disconnect\\s(.+)\\.(\n\n(.)\\s*(.+))?$");

    public WebLoginViaBotFilter() {
        statusMap.put("✅", "已断开连接");
    }

    @Override
    public boolean check(MessageObject message) {
        boolean up = super.check(message);
        return up && message.messageOwner.message.startsWith("You have successfully")
                && pattern.matcher(message.messageOwner.message).matches();
    }

    @Override
    public void filter(MessageObject messageObject) {
        TLRPC.Message message = messageObject.messageOwner;
        Matcher matcher = pattern.matcher(message.message);
        if (!matcher.matches()) return;

        String webUrl = matcher.group(1);
        String mentionBot = matcher.group(2);
        boolean botNoPermMode;
        String mentionBotRepeat = null;
        if (matcher.group(3) == null) {
            botNoPermMode = true;
        } else {
            botNoPermMode = false;
            mentionBotRepeat = matcher.group(4);
        }
        String device = matcher.group(5);
        String ip = matcher.group(6);
        String ipaddr = matcher.group(7);
        String webUrlRepeat = matcher.group(8);
        String emoji = null;
        String status = null;
        boolean disconnectMode;
        if (matcher.group(9) != null) {
            disconnectMode = true;
            emoji = matcher.group(10);
            //status = matcher.group(11);
            status = statusMap.get(emoji);
        } else {
            disconnectMode = false;
        }

        if (webUrl == null || mentionBot == null || device == null
                || ip == null || ipaddr == null || webUrlRepeat == null
                || (disconnectMode && (emoji == null || status == null))
                || (!botNoPermMode && mentionBotRepeat == null)) {
            return;
        }

        String msg;
        if (disconnectMode && !botNoPermMode) {
            msg = LocaleController.getString(R.string.WebLoginViaBotMessageDisconnect, "zh_CN");
            msg = String.format(msg, mentionBot, webUrl, mentionBotRepeat, device, ip, webUrlRepeat, emoji, status);
        } else if (disconnectMode) {
            msg = LocaleController.getString(R.string.WebLoginViaBotMessageDisconnectNoPerm, "zh_CN");
            msg = String.format(msg, mentionBot, webUrl, device, ip, webUrlRepeat, emoji, status);
        } else if (!botNoPermMode) {
            msg = LocaleController.getString(R.string.WebLoginViaBotMessage, "zh_CN");
            msg = String.format(msg, mentionBot, webUrl, mentionBotRepeat, device, ip, webUrlRepeat);
        } else {
            msg = LocaleController.getString(R.string.WebLoginViaBotMessageBotNoPerm, "zh_CN");
            msg = String.format(msg, mentionBot, webUrl, device, ip, webUrlRepeat);
        }
        message.message = msg;
        message.entities.clear();

        TLRPC.TL_messageEntityBold bold1 = new TLRPC.TL_messageEntityBold();
        bold1.offset = msg.indexOf("设备：");
        bold1.length = "设备：".length();
        TLRPC.TL_messageEntityBold bold2 = new TLRPC.TL_messageEntityBold();
        bold2.offset = msg.indexOf("IP：");
        bold2.length = "IP：".length();
        TLRPC.TL_messageEntityUrl url1 = new TLRPC.TL_messageEntityUrl();
        url1.offset = msg.indexOf(webUrl);
        url1.length = webUrl.length();
        TLRPC.TL_messageEntityUrl url2 = new TLRPC.TL_messageEntityUrl();
        url2.offset = msg.lastIndexOf(webUrlRepeat);
        url2.length = webUrlRepeat.length();
        TLRPC.TL_messageEntityUrl url3 = new TLRPC.TL_messageEntityUrl();
        url3.offset = msg.indexOf(ipaddr);
        url3.length = ipaddr.length();
        TLRPC.TL_messageEntityMention mention1 = new TLRPC.TL_messageEntityMention();
        mention1.offset = msg.indexOf(mentionBot);
        mention1.length = mentionBot.length();

        message.entities.add(bold1);
        message.entities.add(bold2);
        message.entities.add(url1);
        message.entities.add(url2);
        message.entities.add(url3);
        message.entities.add(mention1);
        if (disconnectMode) {
            TLRPC.TL_messageEntityBold bold3 = new TLRPC.TL_messageEntityBold();
            bold3.offset = msg.indexOf(status);
            bold3.length = status.length();
            message.entities.add(bold3);
        }
        if (!botNoPermMode) {
            TLRPC.TL_messageEntityMention mention2 = new TLRPC.TL_messageEntityMention();
            mention2.offset = msg.lastIndexOf(mentionBotRepeat);
            mention2.length = mentionBotRepeat.length();
            message.entities.add(mention2);
        }

        if (messageObject.messageText instanceof SpannableString) {
            SpannableString text = new SpannableString(msg);
            MediaDataController.addTextStyleRuns(messageObject, msg, text);
            messageObject.messageText = text;
            messageObject.generateLayout(null);
        } else {
            messageObject.messageText = msg;
        }

        try {
            if (message.reply_markup.rows == null || message.reply_markup.rows.isEmpty()) return;
            TLRPC.KeyboardButton disconnectButton = message.reply_markup.rows.get(0).buttons.get(0);
            if ("Disconnect".equals(disconnectButton.text)) {
                disconnectButton.text = "断开连接";
            }
        } catch (Throwable ignored) {
        }
    }
}
