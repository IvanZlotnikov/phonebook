create table if not exists users
(
    id       bigserial primary key,
    username varchar(50) not null unique,
    password varchar(100) not null,
    role varchar(50),
    enabled  boolean     not null default true
);

create table if not exists authorities
(
    username varchar(50) not null,
    authority varchar(50) not null,
    constraint fk_authorities_users foreign key (username) references users(username)
);

create unique index if not exists ix_auth_username on authorities(username,authority);