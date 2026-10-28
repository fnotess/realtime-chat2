package com.sithija.chat.ws;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * What the WebSocket layer needs from storage. Defined here, implemented by the database code,
 * so the ws package doesn't depend on JDBC and its tests can use an in-memory fake.
 */
public interface MessageStore {

    /**
     * Stores the message and returns only after it has COMMITTED, so the caller can ack and
     * fan out safely. A retry with the same (sender, clientMsgId) returns the original message,
     * with duplicate = true, and uses no new seq.
     *
     * @throws UnknownRecipientException if no account has that username
     */
    StoredMessage save(String sender, String recipient, UUID clientMsgId, String text);


    /**
     * Raises the user's delivered (and, if read, read) watermark in the conversation to seq; never
     * lowers it. Empty if the user isn't a member or seq is outside 1..the conversation's last seq.
     */
    Optional<Receipt> recordReceipt(String user, long conversationId, long seq, boolean read);

    default StoredMessage saveGroup(String sender, long groupId, UUID clientMsgId, String text){
        throw new UnsupportedOperationException(
                "Group messaging not supported"
        );
    };

    /** The user's watermarks after the update, and the other member, who should be told. */
    record Receipt(long conversationId, String user, String otherUser, long deliveredSeq, long readSeq) { }

    record StoredMessage(long id, long conversationId, long seq, String from, String to,
                         UUID clientMsgId, String text, Instant createdAt, boolean duplicate) { }

    class UnknownRecipientException extends RuntimeException {
        public UnknownRecipientException(String username) {
            super("Unknown recipient: " + username);
        }
    }

    default List<String> groupMembers(long groupId){
        return List.of();
    };
}
