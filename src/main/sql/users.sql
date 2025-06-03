use week14hw_db;
create table users(
    id int auto_increment primary key,
    first_name varchar(50) not null,
    last_name varchar(50) not null,
    age int not null,
    password nvarchar(50) not null,
    username nvarchar(100) not null unique
);