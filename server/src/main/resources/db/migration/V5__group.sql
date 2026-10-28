--distinguish direct chats from groups
ALTER table conversations
ADD column kind TEXT not null default 'direct';

--add title column
ALTER table conversations
ADD column title TEXT;

--conversatio table drop not null in 2 user columns
ALTER table conversations
ALTER column user_a_id DROP not null

ALTER table conversations
ALTER column user_b_id DROP not null

--drop cancoal order contstrains

-- add constraint

--create conversation_members_table
CREATE TABLE conversation_members (
                       conversation_id bigint      NOT NULL REFERENCES conversations (id),
                       user_id       bigint      NOT NULL REFERENCES users (id),
                 PRIMARY KEY (conversation_id,user_id)
);