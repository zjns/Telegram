package org.telegram.tgnet.filter.msgobj;

import android.text.SpannableString;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaDataController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLRPC;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginCodeMessageFilter extends BaseMessageObjectFilter {
    private final Pattern filterRegex = Pattern.compile("^(?s)Login\\scode:\\s(\\d+)\\.\\sDo\\snot.*message\\.$");

    @Override
    public boolean check(MessageObject message) {
        boolean up = super.check(message);
        return up && message.messageOwner.message.startsWith("Login code:")
                && filterRegex.matcher(message.messageOwner.message).matches();
    }

    @Override
    public void filter(MessageObject messageObject) {
        TLRPC.Message message = messageObject.messageOwner;
        String codeMsg = LocaleController.getString(R.string.LoginCodeMessage, "zh_CN");
        Matcher matcher = filterRegex.matcher(message.message);
        if (!matcher.matches()) return;
        String loginCode = matcher.group(1);
        if (loginCode == null) return;
        codeMsg = String.format(codeMsg, loginCode);
        message.message = codeMsg;
        message.entities.clear();
        TLRPC.TL_messageEntityBold bold1 = new TLRPC.TL_messageEntityBold();
        bold1.offset = codeMsg.indexOf("验证码：");
        bold1.length = "验证码：".length();
        TLRPC.TL_messageEntityBold bold2 = new TLRPC.TL_messageEntityBold();
        bold2.offset = codeMsg.indexOf('勿');
        bold2.length = 1;
        TLRPC.TL_messageEntityCode code = new TLRPC.TL_messageEntityCode();
        code.offset = codeMsg.indexOf(loginCode);
        code.length = loginCode.length();
        message.entities.add(bold1);
        message.entities.add(bold2);
        message.entities.add(code);

        if (messageObject.messageText instanceof SpannableString) {
            SpannableString text = new SpannableString(codeMsg);
            MediaDataController.addTextStyleRuns(messageObject, codeMsg, text);
            messageObject.messageText = text;
            messageObject.generateLayout(null);
        } else {
            messageObject.messageText = codeMsg;
        }
    }
}
