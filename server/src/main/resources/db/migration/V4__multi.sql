ALTER table conversations
   ALTER column user_a_id DROP NOT NULL,
   ALTER column user_b_id DROP NOT NULL,
   ADD column name text UNIQUE;

CREATE TABLE conversation_members (
        conversation_id    bigint NOT NULL REFERENCES conversations (id),
        user_id            bigint NOT NULL REFERENCES users (id),
        PRIMARY KEY (conversation_id, user_id)
);