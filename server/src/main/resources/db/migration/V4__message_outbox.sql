CREATE TABLE message_outbox (
                          id              bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                          message_id     bigint      NOT NULL REFERENCES messages (id),
                          conversation_id bigint      NOT NULL REFERENCES conversations (id),
                          seq             bigint      NOT NULL,

                          status TEXT NOT NULL default 'pending'
                              CHECK ( status In (
                                  'pending','processing','published'
                                  ) ),

                          attempts INT not null default 0,
                          claim_token UUID,
                          lease_until TIMESTAMPZ,

                          CONSTRAINT outbox_claim_valid CHECK (
                              (
                                  status = 'processing'
                                  AND claim_token is not null
                                  and lease_until is not null
                                  )
                              or (
                                  status <> 'processing'
                                  And claim_token IS NULL
                                  AND lease_until IS NULL
                                  )
                              )
);