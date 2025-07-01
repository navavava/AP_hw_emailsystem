use byebye;

create table if not exists users
(
    id         int auto_increment primary key,
    first_name varchar(50)         not null,
    last_name  varchar(50)         not null,
    username   nvarchar(50) unique not null,
    password   nvarchar(20)        not null
);

create table if not exists emails
(
    id        int auto_increment primary key,
    user_id   int                         not null,
    code      nvarchar(6) unique          not null,
    subject   nvarchar(50)                not null,
    body      nvarchar(150)               not null,
    parent_id int,
    date      date default (current_date) not null,
    foreign key (parent_id) references emails (id),
    foreign key (user_id) references users (id)
);

create table if not exists user_emails
(
    id       int auto_increment primary key,
    email_id int                   not null,
    user_id  int                   not null,
    read_flag boolean default false not null,
    foreign key (email_id) references emails (id),
    foreign key (user_id) references users (id)
);
select * from user_emails
where read_flag = true;

update user_emails
set read_flag = true
where email_id = 6 and user_id = 5;