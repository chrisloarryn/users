create table if not exists public.users
(
    id          uuid         not null,
    created_at  timestamp(6),
    email       varchar(255) not null,
    is_active   boolean,
    last_login  timestamp(6),
    modified_at timestamp(6),
    name        varchar(255) not null,
    password    varchar(255) not null,
    token       uuid
);

alter table public.users
    owner to postgres;

alter table public.users
    add primary key (id);

alter table public.users
    add constraint uk_6dotkott2kjsp8vw4d0m25fb7
        unique (email);

create table if not exists public.phones
(
    id           bigserial,
    city_code    varchar(255),
    country_code varchar(255),
    created_at   timestamp(6),
    number       varchar(255),
    user_id      uuid not null
);

alter table public.phones
    owner to postgres;

alter table public.phones
    add primary key (id);

alter table public.phones
    add constraint fkmg6d77tgqfen7n1g763nvsqe3
        foreign key (user_id) references public.users;


