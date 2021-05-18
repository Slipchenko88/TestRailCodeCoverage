create sequence hibernate_sequence start 1 increment 1

create table history (
test_case_id int8 not null,
history varchar(255),
history_key timestamp not null,
primary key (test_case_id, history_key)
);

create table testcase (
id int8 not null,
date timestamp,
priority int2,
project_id int2,
project_name varchar(255),
status varchar(255),
suite_id int2,
test_case_id int8,
title varchar(255),
primary key (id)
);

alter table if exists history
add constraint test_case_history_fk
foreign key (test_case_id) references testcase