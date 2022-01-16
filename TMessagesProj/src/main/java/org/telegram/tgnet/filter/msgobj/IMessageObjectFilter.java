package org.telegram.tgnet.filter.msgobj;

import org.telegram.messenger.MessageObject;

public interface IMessageObjectFilter {
    boolean check(MessageObject message);

    void filter(MessageObject messageObject);
}
