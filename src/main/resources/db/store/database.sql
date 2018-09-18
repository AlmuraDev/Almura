create table if not exists "store"
(
  "rec_no"       int          auto_increment,
  "created"      timestamp    default current_timestamp not null,
  "creator"      binary(16)   not null,
  "id"           varchar(255) primary key,
  "name"         varchar(255) not null,
  "permission"   varchar(255) not null,
  "is_hidden"    bit          default 0 not null
);

create table if not exists "store_selling_item"
(
  "rec_no"       int          primary key auto_increment,
  "created"      timestamp    default current_timestamp not null,
  "store"        varchar(255) not null,
  "domain"       varchar(255) not null,
  "path"         varchar(255) not null,
  "quantity"     int          default -1 not null,
  "metadata"     int          default 0 not null,
  "price"        decimal      default 0 not null,
  "index"        int          default 0 not null,
  "is_hidden"    bit          default 0 not null,
  foreign key ("store") references "store"("id") on update cascade on delete cascade
);

create table if not exists "store_selling_item_data"
(
  "rec_no"       int          auto_increment,
  "selling_item" int          primary key,
  "data"         varbinary    not null,
  foreign key ("selling_item") references "store_selling_item"("rec_no") on update cascade on delete cascade
);

create table if not exists "store_buying_item"
(
  "rec_no"       int          primary key auto_increment,
  "created"      timestamp    default current_timestamp not null,
  "store"        varchar(255) not null,
  "domain"       varchar(255) not null,
  "path"         varchar(255) not null,
  "quantity"     int          default 1 not null,
  "metadata"     int          default 0 not null,
  "price"        decimal      default 0 not null,
  "index"        int          default 0 not null,
  "is_hidden"    bit          default 0 not null,
  foreign key ("store") references "store"("id") on update cascade on delete cascade
);

create table if not exists "store_buying_item_data"
(
  "rec_no"       int          auto_increment,
  "buying_item"  int          primary key,
  "data"         varbinary    not null,
  foreign key ("buying_item") references "store_buying_item"("rec_no") on update cascade on delete cascade
);

create table if not exists "store_selling_transaction"
(
  "rec_no"       int          primary key auto_increment,
  "created"      timestamp    default current_timestamp not null,
  "selling_item" int          not null,
  "buyer"        binary(16)   not null,
  "quantity"     int          default 1 not null,
  foreign key ("selling_item") references "store_selling_item"("rec_no") on update cascade on delete cascade
);

create table if not exists "store_buying_transaction"
(
  "rec_no"       int          primary key auto_increment,
  "created"      timestamp    default current_timestamp not null,
  "buying_item"  int          not null,
  "buyer"        binary(16)   not null,
  "quantity"     int          default 1 not null,
  foreign key ("buying_item") references "store_buying_item"("rec_no") on update cascade on delete cascade
);

drop index if exists idx_store_selling_item_store;
create index idx_store_selling_item_store ON "store_selling_item"("store");

drop index if exists idx_store_buying_item_store;
create index idx_store_buying_item_store ON "store_buying_item"("store");

drop index if exists idx_store_selling_transaction_selling_item;
create index idx_store_selling_transaction_selling_item ON "store_selling_transaction"("selling_item");

drop index if exists idx_store_buying_transaction_buying_item;
create index idx_store_buying_transaction_buying_item ON "store_buying_transaction"("buying_item");
