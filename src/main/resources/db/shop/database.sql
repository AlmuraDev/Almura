create table if not exists "shop"
(
  "rec_no"     int          auto_increment,
  "created"    timestamp    default current_timestamp not null,
  "creator"    binary(16)   not null,
  "id"         varchar(255) primary key,
  "name"       varchar(255) not null
);

create table if not exists "shop_store_item"
(
  "rec_no"     int          primary key auto_increment,
  "created"    timestamp    default current_timestamp not null,
  "shop"       varchar(255) not null,
  "seller"     binary(16)   not null,
  "item_type"  varchar(255) not null,
  "quantity"   int          default -1 not null,
  "price"      decimal      default 0 not null,
  "index"      int          default 0 not null,
  "is_hidden"  bit          default 0 not null,
  foreign key ("shop") references "shop"("id") on update cascade on delete cascade
);

create table if not exists "shop_store_item_data"
(
  "rec_no"     int          auto_increment,
  "store_item" int          primary key,
  "data"       varbinary    not null,
  foreign key ("store_item") references "shop_store_item"("rec_no") on update cascade on delete cascade
);

create table if not exists "shop_transaction"
(
  "rec_no"     int          primary key auto_increment,
  "created"    timestamp    default current_timestamp not null,
  "shop_item"  int          not null,
  "buyer"      binary(16)   not null,
  "quantity"   int          default 1 not null,
  foreign key ("shop_item") references "shop_store_item"("rec_no") on update cascade on delete cascade
);
