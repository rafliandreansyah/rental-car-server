CREATE DATABASE rental_car_db;

CREATE TABLE "users" (
  "id" uuid PRIMARY KEY,
  "name" varchar,
  "email" varchar,
  "password" varchar,
  "image_url" varchar,
  "date_of_birth" timestamp,
  "phone_number" varchar,
  "role" enum,
  "is_active" boolean,
  "created_at" timestamp,
  "updated_at" timestamp,
  "is_deleted" boolean,
  PRIMARY KEY ("id")
);

CREATE TABLE "cars_authorization" (
  "id" uuid[pk],
  "user_id" uuid,
  "car_id" uuid,
  "created_at" timestamp,
  "updated_at" timestamp,
  "is_deleted" boolean,
  PRIMARY KEY ("id")
);

CREATE TABLE "cars" (
  "id" uuid PRIMARY KEY,
  "name" varchar,
  "image_url" varchar,
  "brand" varchar,
  "year" int,
  "capacity" int,
  "cc" int,
  "price_per_day" double,
  "tax" double,
  "discount" double,
  "description" varchar,
  "transmission" enum,
  "is_active" boolean,
  "created_at" timestamp,
  "updated_at" timestamp,
  "is_deleted" boolean,
  PRIMARY KEY ("id")
);

CREATE TABLE "cars_image_details" (
  "id" uuid PRIMARY KEY,
  "image_url" varchar,
  "car_id" uuid,
  "created_at" timestamp,
  "updated_at" timestamp,
  "is_deleted" boolean
);

CREATE TABLE "transactions" (
  "id" uuid PRIMARY KEY,
  "no_invoice" varcharcar,
  "car_name" varchar,
  "car_image_url" varchar,
  "brand" varchar,
  "year" int,
  "capacity" int,
  "cc" int,
  "start_date" timestamp,
  "end_date" timestamp,
  "duration_day" int,
  "price" double,
  "tax" double,
  "discount" double,
  "total_price" double,
  "user_id" uuid,
  "user_approved" varchar,
  "car_id" uuid,
  "status" varchar,
  "payment_image" varchar,
  "created_at" timestamp,
  "updated_at" timestamp,
  "is_deleted" timestamp,
  PRIMARY KEY ("id")
);

CREATE TABLE "logs" (
  "id" uuid PRIMARY KEY,
  "user_id" uuid,
  "url" varchar,
  "method" varchar,
  "status" varchar,
  "error_log" varchar,
  "activity" varchar,
  "created_at" timestamp,
  "updated_at" timestamp,
  PRIMARY KEY ("id")
);

CREATE INDEX ON "users" ("name");

CREATE INDEX ON "users" ("email");

CREATE INDEX ON "cars" ("name");

CREATE INDEX ON "cars" ("brand");

CREATE INDEX ON "cars" ("year");

ALTER TABLE "cars_authorization" ADD FOREIGN KEY ("user_id") REFERENCES "users" ("id");

ALTER TABLE "cars_authorization" ADD FOREIGN KEY ("car_id") REFERENCES "cars" ("id");

ALTER TABLE "cars_image_details" ADD FOREIGN KEY ("car_id") REFERENCES "cars" ("id");

ALTER TABLE "transactions" ADD FOREIGN KEY ("user_id") REFERENCES "users" ("id");

ALTER TABLE "transactions" ADD FOREIGN KEY ("car_id") REFERENCES "cars" ("id");

ALTER TABLE "logs" ADD FOREIGN KEY ("user_id") REFERENCES "users" ("id");
