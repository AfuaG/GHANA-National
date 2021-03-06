package org.motechproject.ghana.national.messagegateway.repositories;

import org.motechproject.ghana.national.messagegateway.domain.ExpiryStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.store.MessageGroup;
import org.springframework.integration.store.MessageGroupCallback;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.LinkedHashSet;

@Component("messageStore")
public class MessageStore extends EnhancedJdbcMessageStore {
    private ExpiryStrategy expiryStrategy;
    private Collection<MessageGroupCallback> expiryCallbacksCopy = new LinkedHashSet<MessageGroupCallback>();
    private Logger logger = LoggerFactory.getLogger(MessageStore.class);

    @Autowired
    public MessageStore(ExpiryStrategy expiryStrategy, @Qualifier("aggregatorDataSource") DataSource dataSource) {
        super(dataSource);
        this.expiryStrategy = expiryStrategy;
    }

    @Override
    public int expireMessageGroups(long timeout) {
        return expiryStrategy.expireMessageGroups(this, timeout);
    }

    @Override
    public void registerMessageGroupExpiryCallback(MessageGroupCallback callback) {
        expiryCallbacksCopy.add(callback);
        super.registerMessageGroupExpiryCallback(callback);
    }

    public void expire(MessageGroup group) {

        RuntimeException exception = null;

        for (MessageGroupCallback callback : expiryCallbacksCopy) {
            try {
                callback.execute(this, group);
            } catch (RuntimeException e) {
                if (exception == null) {
                    exception = e;
                }
                logger.error("Exception in expiry callback", e);
            }
        }

        if (exception != null) {
            throw exception;
        }
    }
}
