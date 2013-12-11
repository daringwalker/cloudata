package com.cloudata.keyvalue.btree;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadOnlyTransaction extends Transaction {
    private static final Logger log = LoggerFactory.getLogger(ReadOnlyTransaction.class);

    final int rootPageId;

    public ReadOnlyTransaction(PageStore pageStore, int rootPageId) {
        super(pageStore, null);
        this.rootPageId = rootPageId;
    }

    @Override
    public Page getPage(Page parent, int pageNumber) {
        // TODO: Should we have a small cache?
        return pageStore.fetchPage(parent, pageNumber).page;
    }

    @Override
    protected Page getRootPage(Btree btree, boolean create) {
        if (rootPageId == 0) {
            if (!create) {
                return null;
            }
            throw new IllegalStateException();
        }

        return getPage(null, rootPageId);
    }
}
