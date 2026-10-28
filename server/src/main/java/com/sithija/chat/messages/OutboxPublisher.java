package com.sithija.chat.messages;

import com.sithija.chat.ws.MessageRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;

public class OutboxPublisher {

    private final MessageRepository repo;
    private final TransactionTemplate tx;

    private final OutboxRepository outboxRepository;

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);

    public OutboxPublisher(MessageRepository repo, TransactionTemplate tx, OutboxRepository outboxRepository) {
        this.repo = repo;
        this.tx = tx;
        this.outboxRepository = outboxRepository;
    }


    // pubhlsh pending
    @Scheduled
    public void publishPending(){

        //create a token
        UUID claimtoken = UUID.randomUUID();

        var events = outboxRepository.claimBatch(10, claimtoken);

        //iterate thru events publish and mark them on table
        for (var event:events){

            try {
                trasnport.publish(event);

                // redis accepeted
                boolean completed = outboxRepository.markPublished();

                if (!completed) {
                    log.warn("lost claim", event);
                }
            }
            catch(Exception e){
                // relevae the event id and token
                outboxRepository.releaseForRerty();
            }

        }
    }


}
