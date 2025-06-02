create table chunk (
    chunk_index integer,
    file_id bigint,
    id bigint not null auto_increment,
    chunk_checksum varchar(255),
    primary key (id)
) engine=InnoDB;

create table file_entity (
    chunk_size integer,
    file_size integer,
    total_chunks integer,
    uploaded bit,
    id bigint not null auto_increment,
    transfer_id bigint,
    user_id bigint,
    file_checksum varchar(40),
    file_name varchar(255),
    file_type varchar(255),
    primary key (id)
) engine=InnoDB;

create table refresh_tokens (
    creation_date datetime(6) not null,
    expires_at datetime(6) not null,
    user_id bigint,
    id uuid not null,
    primary key (id)
) engine=InnoDB;

create table shared_links (
    downloads integer not null,
    max_downloads integer not null,
    created_at bigint not null,
    expires_at datetime(6) not null,
    id bigint not null auto_increment,
    updated_at bigint not null,
    user_id bigint,
    download_link varchar(255) not null,
    url varchar(255) not null,
    uuid varchar(255) not null,
    primary key (id)
) engine=InnoDB;

create table transfer (
    active bit not null,
    end_time datetime(6),
    id bigint not null auto_increment,
    shared_link_id bigint,
    start_time datetime(6),
    user_id bigint,
    upload_path varchar(255),
    primary key (id)
) engine=InnoDB;

create table users (
    active bit not null,
    id bigint not null auto_increment,
    email varchar(255),
    password varchar(255),
    username varchar(255),
    role enum ('ADMIN','USER'),
    primary key (id)
) engine=InnoDB;

alter table if exists refresh_tokens 
    add constraint UK7tdcd6ab5wsgoudnvj7xf1b7l unique (user_id);

alter table if exists transfer 
    add constraint UKdc5pcfclk5ibq6ehjdfiufm7k unique (shared_link_id);

alter table if exists chunk 
    add constraint FKoapsw0weighp8u9vapgsyxuon 
    foreign key (file_id) 
    references file_entity (id);

alter table if exists file_entity 
    add constraint FKt88sjwtmw4u8vliggbxrpcre1 
    foreign key (transfer_id) 
    references transfer (id);

alter table if exists file_entity 
    add constraint FKm04nsoi97gotvf2b1q307tncv 
    foreign key (user_id) 
    references users (id);

alter table if exists refresh_tokens 
    add constraint FK1lih5y2npsf8u5o3vhdb9y0os 
    foreign key (user_id) 
    references users (id);

alter table if exists shared_links 
    add constraint FK1onbky0hip6ds617ih50gjr92 
    foreign key (user_id) 
    references users (id);

alter table if exists transfer 
    add constraint FKgjy3x3w46c3p8qwki3rxquuf9 
    foreign key (shared_link_id) 
    references shared_links (id);

alter table if exists transfer 
    add constraint FKn2acb0d1fpgdl317kk98615df 
    foreign key (user_id) 
    references users (id);
