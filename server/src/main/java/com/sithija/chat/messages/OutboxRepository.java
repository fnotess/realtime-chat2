package com.sithija.chat.messages;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class OutboxRepository {


    private final JdbcClient jdbc;

    public record OutboxEvent(
            long id,
            long messageId,
            long conversationId,
            long seq,
            UUID claimToken,
            int attempts
    ){}


    public OutboxRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }


    //insert pedning task
    public void enqueue(MessageRepository.MessageRow m){
        jdbc.sql("""
                 insert in to message_outbox (message_id,coversation_id,seq)
                 values
                 (:messageId, :conversationId,:seq)
                """)
                .param("messageId", m.id())
                .param("conversationId", m.conversationId())
                .param("seq", m.seq())
                .update();
    }

    //resever availabke taskss for 1 minute atomically claim
    // it locks updates and return s

    public void claimBatch(int batchSize, UUID token){
       return jdbc.sql("""
               update message_outbox 
               set status = 'processing',
               claim_token = :token
               lease_until = now() + INTERVAL '60 second',
               attempts = attempts +1
               where in (
                   select id from message_outbox where status = 'pending' or (status= 'processing' and lease_until <now()) order by id
                   limit :batchsize
                   for update skip locked
                   )
                   returning 
                   id,message_id, conversation_id,seq, claim_token,attempts 
               """)
               .param("token",)
               .param("batchSize",)
               .query((rs,rowNum)->new OutboxEvent(

               ))
               .list();
    }

    // redis confirm publush  updated when trasnport confirms publictaion
    public void markPublished(long eventId, UUID claimToken){

        int updated = jdbc.sql("""
                update message_outbox
                set status = 'published',
                published_at = now(),
                claim_token =NULL,
                lease_until= NULL
                where id = :eventId
                and status = 'processing' and claim_token = :claimToken
                """)
                .param("eventId",eventId)
                .param("claimToken",claimToken)
                .update();

    }



    // redis publication fail
    public void releaseForRerty(long eventId, UUID claimToken){
            jdbc.sql("""
                    udpdate message_outbox
                    set_status = 'pending'
                    claim_token = null
                    lease_until =null
                    where id = :eventId
                and status = 'processing' and claim_token = :claimToken
                """)
                    .param("eventId",eventId)
                    .param("claimToken",claimToken)
                    .update();
    }




}
