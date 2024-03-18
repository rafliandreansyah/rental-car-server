CREATE DATABASE rental_car_db;

CREATE TABLE "users"
(
    "id"            uuid UNIQUE PRIMARY KEY NOT NULL,
    "name"          varchar(100)            NOT NULL,
    "email"         varchar(255) UNIQUE     NOT NULL,
    "password"      varchar(255)            NOT NULL,
    "image_url"     varchar(255),
    "date_of_birth" timestamp,
    "phone_number"  varchar(100) UNIQUE     NOT NULL,
    "role"          varchar(50)             NOT NULL,
    "is_active"     boolean                 NOT NULL,
    "created_at"    timestamp DEFAULT (now()),
    "updated_at"    timestamp,
    "is_deleted"    boolean
);

CREATE TABLE "cars_authorization"
(
    "id"         uuid UNIQUE PRIMARY KEY NOT NULL,
    "user_id"    uuid,
    "car_id"     uuid,
    "created_at" timestamp DEFAULT (now()),
    "updated_at" timestamp
);

CREATE TABLE "cars"
(
    "id"            uuid UNIQUE PRIMARY KEY NOT NULL,
    "name"          varchar(255)            NOT NULL,
    "image_url"     varchar(255)            NOT NULL,
    "brand"         varchar(100)            NOT NULL,
    "year"          int                     NOT NULL,
    "capacity"      int                     NOT NULL,
    "cc"            int                     NOT NULL,
    "price_per_day" numeric(8, 2)           NOT NULL,
    "luggage"       int,
    "tax"           int                     NOT NULL DEFAULT 0,
    "discount"      int                     NOT NULL DEFAULT 0,
    "description"   text                    NOT NULL,
    "transmission"  varchar(50)             NOT NULL,
    "is_active"     boolean                 NOT NULL DEFAULT true,
    "created_at"    timestamp                        DEFAULT (now()),
    "updated_at"    timestamp,
    "is_deleted"    boolean
);

CREATE TABLE "ratings"
(
    "id"         uuid UNIQUE PRIMARY KEY NOT NULL,
    "car_id"     uuid,
    "user_id"    uuid,
    "rating"     numeric(5, 1)           NOT NULL,
    "comment"    text,
    "image_url"  varchar(255),
    "created_at" timestamp DEFAULT (now()),
    "updated_at" timestamp,
    "is_deleted" boolean
);


CREATE TABLE "cars_image_details"
(
    "id"         uuid UNIQUE PRIMARY KEY NOT NULL,
    "image_url"  varchar(255)            NOT NULL,
    "car_id"     uuid,
    "created_at" timestamp DEFAULT (now()),
    "updated_at" timestamp
);

CREATE TABLE "transactions"
(
    "id"            uuid UNIQUE PRIMARY KEY NOT NULL,
    "no_invoice"    varchar(255) UNIQUE     NOT NULL,
    "car_name"      varchar(255)            NOT NULL,
    "car_image_url" varchar(255)            NOT NULL,
    "car_brand"     varchar(100)            NOT NULL,
    "car_year"      int                     NOT NULL,
    "car_capacity"  int                     NOT NULL,
    "car_cc"        int                     NOT NULL,
    "start_date"    timestamp               NOT NULL,
    "end_date"      timestamp               NOT NULL,
    "duration_day"  int                     NOT NULL,
    "car_price"     numeric(8, 2)           NOT NULL,
    "car_tax"       int       DEFAULT 0,
    "car_discount"  int       DEFAULT 0,
    "total_price"   numeric(12, 2)          NOT NULL,
    "user_id"       uuid,
    "user_approved" varchar(100),
    "car_id"        uuid,
    "status"        varchar(50),
    "payment_image" varchar(255),
    "created_at"    timestamp DEFAULT (now()),
    "updated_at"    timestamp,
    "is_deleted"    boolean
);

CREATE TABLE "car_rented"
(
    "id"             uuid UNIQUE PRIMARY KEY NOT NULL,
    "car_id"         uuid,
    "transaction_id" uuid,
    "start_date"     timestamp,
    "end_date"       timestamp,
    "created_at"     timestamp,
    "updated_at"     timestamp,
    "is_deleted"     boolean
);

-- CREATE TABLE "logs"
-- (
--     "id"         uuid UNIQUE PRIMARY KEY NOT NULL,
--     "user_id"    uuid,
--     "url"        varchar(255)            NOT NULL,
--     "method"     varchar(50)             NOT NULL,
--     "status"     varchar(50)             NOT NULL,
--     "error_log"  text,
--     "activity"   varchar(255)            NOT NULL,
--     "created_at" timestamp DEFAULT (now()),
--     "updated_at" timestamp
-- );

CREATE TABLE "reset_token"
(
    "id"           uuid UNIQUE PRIMARY KEY NOT NULL,
    "user_id"      uuid,
    "token"        varchar(255)            NOT NULL,
    "expired_date" timestamp               NOT NULL,
    "created_at"   timestamp DEFAULT (now()),
    "updated_at"   timestamp
);

CREATE INDEX ON "users" ("name");

CREATE INDEX ON "users" ("email");

CREATE INDEX ON "cars" ("name");

CREATE INDEX ON "cars" ("brand");

CREATE INDEX ON "cars" ("year");

ALTER TABLE "reset_token"
    ADD FOREIGN KEY ("user_id") REFERENCES "users" ("id");

ALTER TABLE "cars_authorization"
    ADD FOREIGN KEY ("user_id") REFERENCES "users" ("id");

ALTER TABLE "cars_authorization"
    ADD FOREIGN KEY ("car_id") REFERENCES "cars" ("id");

ALTER TABLE "ratings"
    ADD FOREIGN KEY ("car_id") REFERENCES "cars" ("id");

ALTER TABLE "ratings"
    ADD FOREIGN KEY ("user_id") REFERENCES "users" ("id");

ALTER TABLE "cars_image_details"
    ADD FOREIGN KEY ("car_id") REFERENCES "cars" ("id");

ALTER TABLE "transactions"
    ADD FOREIGN KEY ("user_id") REFERENCES "users" ("id");

ALTER TABLE "transactions"
    ADD FOREIGN KEY ("car_id") REFERENCES "cars" ("id");

ALTER TABLE "car_rented"
    ADD FOREIGN KEY ("car_id") REFERENCES "cars" ("id");

ALTER TABLE "car_rented"
    ADD FOREIGN KEY ("transaction_id") REFERENCES "transactions" ("id");

-- ALTER TABLE "logs"
--     ADD FOREIGN KEY ("user_id") REFERENCES "users" ("id");


ALTER TABLE "transactions"
    ADD FOREIGN KEY ("car_id") REFERENCES "cars" ("id");

-- ALTER TABLE "logs" ADD FOREIGN KEY ("user_id") REFERENCES "users" ("id");

-- ALTER TABLE "cars" ADD COLUMN "luggage" int;

ALTER TABLE ratings
    ALTER COLUMN "comment" TYPE TEXT;