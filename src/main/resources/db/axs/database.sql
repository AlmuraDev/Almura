create table if not exists "axs"
(
  "rec_no"           int          auto_increment,
  "created"          timestamp    default current_timestamp not null,
  "creator"          binary(16)   not null,
  "id"               varchar(255) primary key,
  "name"             varchar(255) not null,
  "permission"       varchar(255) not null,
  "is_hidden"        bit          default 0 not null
);

create table if not exists "axs_list_item"
(
  "rec_no"             int          primary key auto_increment,
  "created"            timestamp    default current_timestamp not null,
  "axs"                varchar(255) not null,
  "seller"             binary(16)   not null,
  "item_type"          varchar(255) not null,
  "quantity"           int          default 1 not null,
  "metadata"           int          default 0 not null,
  "index"              int          default 0 not null,
  "is_hidden"          bit          default 0 not null,
  foreign key ("axs") references "axs"("id") on update cascade on delete cascade
);

create table if not exists "axs_list_item_data"
(
  "rec_no"             int          auto_increment,
  "list_item"          int          primary key,
  "data"               varbinary    not null,
  foreign key ("list_item") references "axs_list_item"("rec_no") on update cascade on delete cascade
);

create table if not exists "axs_for_sale_item"
(
  "rec_no"             int          primary key auto_increment,
  "created"            timestamp    default current_timestamp not null,
  "list_item"          int          not null,
  "price"              decimal      default 0 not null,
  "is_hidden"          bit          default 0 not null,
  foreign key ("list_item") references "axs_list_item"("rec_no") on update cascade on delete cascade
);

create table if not exists "axs_transaction"
(
  "rec_no"             int          primary key auto_increment,
  "created"            timestamp    default current_timestamp not null,
  "for_sale_item"      int          not null,
  "buyer"              binary(16)   not null,
  "price"              decimal      default 0 not null,
  "quantity"           int          default 1 not null,
  foreign key ("for_sale_item") references "axs_for_sale_item"("rec_no") on update cascade on delete cascade
);

drop index if exists idx_axs_list_item_axs;
create index idx_axs_list_item_axs ON "axs_list_item"("axs");

drop index if exists idx_axs_for_sale_item_list_item;
create index idx_axs_for_sale_item_list_item ON "axs_for_sale_item"("list_item");

drop index if exists idx_axs_transaction_for_sale_item;
create index idx_axs_transaction_for_sale_item ON "axs_transaction"("for_sale_item");
