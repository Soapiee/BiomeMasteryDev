create table if not exists TABLENAME (
    UUID varchar(36),
    LEVEL int default 0 not null,
    PROGRESS varchar(25),
    unique (UUID)
);