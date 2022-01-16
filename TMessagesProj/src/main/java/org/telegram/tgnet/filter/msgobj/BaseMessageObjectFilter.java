package org.telegram.tgnet.filter.msgobj;

import android.text.TextUtils;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;

public class BaseMessageObjectFilter implements IMessageObjectFilter {
    @Override
    public boolean check(MessageObject message) {
        boolean langMatch = LocaleController.getInstance().isChinaEnv();
        return langMatch && message != null
                && message.messageOwner != null
                && !TextUtils.isEmpty(message.messageText)
                && !TextUtils.isEmpty(message.messageOwner.message);
    }

    @Override
    public void filter(MessageObject messageObject) {
    }
}
