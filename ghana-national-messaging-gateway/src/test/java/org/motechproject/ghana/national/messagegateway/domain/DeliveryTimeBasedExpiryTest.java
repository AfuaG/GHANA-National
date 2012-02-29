package org.motechproject.ghana.national.messagegateway.domain;

import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.motechproject.model.Time;
import org.motechproject.testing.utils.BaseUnitTest;
import org.motechproject.util.DateUtil;
import org.springframework.integration.Message;
import org.springframework.integration.MessageHeaders;
import org.springframework.integration.store.MessageGroup;
import org.springframework.integration.store.SimpleMessageGroup;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

public class DeliveryTimeBasedExpiryTest extends BaseUnitTest{

    @Test
    public void shouldExpireMessageGroupsBasedOnDeliveryDateOfMessagesInTheGroup() {
        final DeliveryTimeBasedExpiry deliveryTimeBasedExpiry = new DeliveryTimeBasedExpiry();

        MessageStore messageStore = mock(MessageStore.class);
        LocalDateTime generationTime = DateUtil.newDateTime(DateUtil.newDate(2000, 1, 1), new Time(10, 10)).toLocalDateTime();
        SimpleMessageGroup messageGroup = mockToReturnOnGroupWithOneSMS(messageStore, generationTime);

        mockCurrentDate(DateUtil.newDateTime(DateUtil.newDate(2000, 1, 8), new Time(10, 11)));
        assertThat(deliveryTimeBasedExpiry.expireMessageGroups(messageStore, 3000), is(equalTo(1)));
        verify(messageStore).expire(messageGroup);

        messageGroup = mockToReturnOnGroupWithOneSMS(messageStore, generationTime);
        mockCurrentDate(DateUtil.newDateTime(DateUtil.newDate(1999, 12, 29), new Time(9, 59)));
        assertThat(deliveryTimeBasedExpiry.expireMessageGroups(messageStore, 3000), is(equalTo(0)));
        verify(messageStore, never()).expire(messageGroup);

    }

    private SimpleMessageGroup mockToReturnOnGroupWithOneSMS(MessageStore messageStore, LocalDateTime generationTime) {
        List<TestMessage<SMS>> messages = Arrays.asList(new TestMessage<SMS>(SMS.fromText("text", "ph", generationTime, new NextMondayDispatcher(), null)));
        final SimpleMessageGroup messageGroup = new SimpleMessageGroup(messages, "groupId");
        final List<SimpleMessageGroup> messageGroups = Arrays.asList(messageGroup);

        final Iterator<SimpleMessageGroup> messageGroupIterator = messageGroups.iterator();

        when(messageStore.iterator()).thenReturn(new Iterator<MessageGroup>() {

            public boolean hasNext() {
                return messageGroupIterator.hasNext();
            }

            public MessageGroup next() {
                return messageGroupIterator.next();
            }

            public void remove() {
                throw new UnsupportedOperationException("Cannot remove MessageGroup from this iterator.");
            }

        });
        return messageGroup;
    }

    class TestMessage<T> implements Message<T>{

        private T obj;

        private TestMessage(T obj) {
            this.obj = obj;
        }

        @Override
        public MessageHeaders getHeaders() {
            return null;
        }

        @Override
        public T getPayload() {
            return obj;
        }
    }


}
