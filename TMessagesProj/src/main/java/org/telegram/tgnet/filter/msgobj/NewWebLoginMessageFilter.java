package org.telegram.tgnet.filter.msgobj;

import android.text.SpannableString;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaDataController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLRPC;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NewWebLoginMessageFilter extends BaseMessageObjectFilter {
    private final Pattern pattern = Pattern.compile("^(?s)Web\\slogin\\scode\\.\\sDear\\s(.+),.*on\\s(.+)\\..*:\n(.+)\n\nDo.*on\\s(.+),.*message\\.$");

    @Override
    public boolean check(MessageObject message) {
        boolean up = super.check(message);
        return up && message.messageOwner.message.startsWith("Web login code.")
                && pattern.matcher(message.messageOwner.message).matches();
    }

    @Override
    public void filter(MessageObject messageObject) {
        String newWebLogin = LocaleController.getString(R.string.NewWebLoginMessage, "zh_CN");
        TLRPC.Message message = messageObject.messageOwner;
        Matcher matcher = pattern.matcher(message.message);
        if (!matcher.matches()) return;

        String user = matcher.group(1);
        String webUrl = matcher.group(2);
        String loginCode = matcher.group(3);
        String webUrlRepeat = matcher.group(4);
        if (user == null || webUrl == null || loginCode == null || webUrlRepeat == null) {
            return;
        }

        newWebLogin = String.format(newWebLogin, user, webUrl, loginCode, webUrlRepeat);
        message.message = newWebLogin;
        TLRPC.TL_messageEntityBold bold1 = new TLRPC.TL_messageEntityBold();
        bold1.offset = newWebLogin.indexOf("网页登录验证码。");
        bold1.length = "网页登录验证码。".length();
        TLRPC.TL_messageEntityBold bold2 = new TLRPC.TL_messageEntityBold();
        bold2.offset = newWebLogin.indexOf('勿');
        bold2.length = 1;
        TLRPC.TL_messageEntityBold bold3 = new TLRPC.TL_messageEntityBold();
        bold3.offset = newWebLogin.indexOf("此验证码可以被用来删除你的 Telegram 账号。");
        bold3.length = "此验证码可以被用来删除你的 Telegram 账号。".length();
        TLRPC.TL_messageEntityUrl url = new TLRPC.TL_messageEntityUrl();
        url.offset = newWebLogin.indexOf(webUrl);
        url.length = webUrl.length();
        TLRPC.TL_messageEntityUrl urlRepeat = new TLRPC.TL_messageEntityUrl();
        urlRepeat.offset = newWebLogin.lastIndexOf(webUrlRepeat);
        urlRepeat.length = webUrlRepeat.length();
        TLRPC.TL_messageEntityCode code = new TLRPC.TL_messageEntityCode();
        code.offset = newWebLogin.indexOf(loginCode);
        code.length = loginCode.length();
        message.entities.clear();
        message.entities.add(bold1);
        message.entities.add(bold2);
        message.entities.add(bold3);
        message.entities.add(url);
        message.entities.add(urlRepeat);
        message.entities.add(code);

        if (messageObject.messageText instanceof SpannableString) {
            SpannableString text = new SpannableString(newWebLogin);
            MediaDataController.addTextStyleRuns(messageObject, newWebLogin, text);
            messageObject.messageText = text;
            messageObject.generateLayout(null);
        } else {
            messageObject.messageText = newWebLogin;
        }
    }
}
