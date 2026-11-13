create table if not exists "bank_account"
(
  "rec_no"       int          primary key auto_increment,
  "created"      timestamp    default current_timestamp not null,
  "owner"        binary(16)   not null,
  "name"         varchar(64)  not null,
  "economy_id"   varchar(64)  not null
);

create table if not exists "bank_account_member"
(
  "rec_no"       int          primary key auto_increment,
  "account"      int          not null,
  "member"       binary(16)   not null,
  "can_withdraw" bit          default 0 not null,
  foreign key ("account") references "bank_account"("rec_no") on update cascade on delete cascade
);

create table if not exists "bank_transaction"
(
  "rec_no"       int          primary key auto_increment,
  "created"      timestamp    default current_timestamp not null,
  "account"      int          not null,
  "account_name" varchar(64)  not null,
  "owner"        binary(16)   not null,
  "actor"        binary(16)   not null,
  "type"         varchar(32)  not null,
  "amount"       decimal      default 0 not null
);
