create table if not exists "title"
(
  "rec_no"     int          auto_increment,
  "created"    timestamp    default current_timestamp not null,
  "creator"    binary(16)   not null,
  "id"         varchar(255) primary key,
  "permission" varchar(255) not null,
  "content"    varchar(255) not null,
  "is_hidden"  bit          default 0 not null
);

create table if not exists "title_select"
(
  "rec_no"     int          auto_increment,
  "created"    timestamp    default current_timestamp not null,
  "title"      varchar(255) not null,
  "holder"     binary(16)   primary key,
  foreign key ("title") references "title"("id") on update cascade on delete cascade
);

create table if not exists "title_select_history"
(
  "rec_no"     int          primary key auto_increment,
  "created"    timestamp    default current_timestamp not null,
  "old_title"  varchar(255) not null,
  "holder"     binary(16)   not null,
  foreign key ("old_title") references "title"("id") on update cascade on delete cascade
);

drop index if exists idx_old_title;
create index idx_old_title ON "title_select_history"("old_title");
