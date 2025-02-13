create table expenses
(
    id               bigserial not null primary key,
    expenses_sum     NUMERIC(8, 2),
    expenses_comment varchar(100),
    expenses_date    date      not null,

    processed_at     timestamp,
    created_at       timestamp not null,
    created_day_at   date      not null
);
create index idx_created_day_at on expenses (created_day_at);

create table expenses_history
(
    hist_id     bigserial not null primary key,
    expenses_id bigint not null references expenses (id),
    value_txt   text
);

insert into expenses
    (expenses_sum, expenses_comment, expenses_date, processed_at, created_at, created_day_at)
values
    (10.11, 'expenses_comment', now(), now(), now(), current_date),
    (10.12, 'expenses_comment', now(), now(), now(), current_date),
    (10.13, 'expenses_comment', now(), now(), now(), current_date)
;