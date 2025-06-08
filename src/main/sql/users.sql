use byebye;

create table users
(
    id         int auto_increment primary key,
    first_name varchar(50)   not null,
    last_name  varchar(50)   not null,
    age        int           not null,
    password   nvarchar(50)  not null,
    username   nvarchar(100) not null unique
);

create table email
(
    id              int auto_increment primary key,
    email_code      nvarchar(6)  not null unique,
    subject         nvarchar(50) not null,
    body            nvarchar(150),
    date            date         not null,
    parent_email_id int default null
);

create table user_emails
(
    id       int auto_increment primary key,
    user_id  int                            not null,
    email_id int                            not null,
    SorR     varchar(15) default 'receiver' not null,
    constraint ch_type check (SorR in ('sender', 'receiver'))

);