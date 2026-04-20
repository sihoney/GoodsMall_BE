create schema if not exists notification_service;

create table if not exists notification_service.notification (
    notification_id uuid primary key,
    event_id uuid not null,
    trace_id varchar(100),
    member_id uuid not null,
    type varchar(50) not null,
    title varchar(255) not null,
    content text not null,
    reference_id uuid,
    reference_type varchar(50),
    status varchar(30) not null,
    is_read boolean not null default false,
    created_at timestamp not null,
    status_changed_at timestamp not null
);

create unique index if not exists uq_notification_event_id
    on notification_service.notification (event_id);

create index if not exists idx_notification_member_created_at
    on notification_service.notification (member_id, created_at desc);

create index if not exists idx_notification_member_is_read
    on notification_service.notification (member_id, is_read);
