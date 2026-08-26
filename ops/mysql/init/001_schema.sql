SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS neusoft_elderly_care
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
USE neusoft_elderly_care;

CREATE TABLE IF NOT EXISTS users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  phone VARCHAR(20) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  real_name VARCHAR(50) NOT NULL,
  age INT NOT NULL,
  gender TINYINT NOT NULL,
  role VARCHAR(20) NOT NULL,
  status TINYINT NOT NULL,
  created_at DATETIME NOT NULL,
  UNIQUE KEY uk_users_phone (phone)
);

CREATE TABLE IF NOT EXISTS rooms (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  floor INT NOT NULL,
  room_no VARCHAR(20) NOT NULL,
  status TINYINT NOT NULL,
  UNIQUE KEY uk_rooms_floor_room_no (floor, room_no)
);

CREATE TABLE IF NOT EXISTS beds (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  room_id BIGINT NOT NULL,
  bed_no VARCHAR(20) NOT NULL,
  status VARCHAR(20) NOT NULL,
  UNIQUE KEY uk_beds_room_bed_no (room_id, bed_no),
  CONSTRAINT fk_beds_room_id FOREIGN KEY (room_id) REFERENCES rooms(id)
);

CREATE TABLE IF NOT EXISTS customers (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL,
  phone VARCHAR(20) NULL,
  age INT NOT NULL,
  gender TINYINT NOT NULL,
  status VARCHAR(20) NOT NULL,
  bed_id BIGINT NULL,
  checkin_date DATE NULL,
  note VARCHAR(255) NULL,
  created_at DATETIME NOT NULL,
  CONSTRAINT fk_customers_bed_id FOREIGN KEY (bed_id) REFERENCES beds(id)
);

CREATE TABLE IF NOT EXISTS meal_weekly_menu (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  week_start_date DATE NOT NULL,
  mon VARCHAR(255),
  tue VARCHAR(255),
  wed VARCHAR(255),
  thu VARCHAR(255),
  fri VARCHAR(255),
  sat VARCHAR(255),
  sun VARCHAR(255),
  updated_at DATETIME NOT NULL,
  UNIQUE KEY uk_weekly_menu_week_start_date (week_start_date)
);

CREATE TABLE IF NOT EXISTS customer_meal_plan (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  customer_id BIGINT NOT NULL,
  week_start_date DATE NOT NULL,
  meal_type VARCHAR(20) NOT NULL,
  diet_taboo VARCHAR(255),
  note VARCHAR(255),
  created_by BIGINT,
  UNIQUE KEY uk_customer_meal_plan (customer_id, week_start_date),
  CONSTRAINT fk_customer_meal_plan_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
  CONSTRAINT fk_customer_meal_plan_user FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS customer_service_object (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  customer_id BIGINT NOT NULL,
  manager_user_id BIGINT NOT NULL,
  assigned_at DATETIME NOT NULL,
  UNIQUE KEY uk_customer_service_object_customer (customer_id),
  CONSTRAINT fk_customer_service_object_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
  CONSTRAINT fk_customer_service_object_manager FOREIGN KEY (manager_user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS customer_service_focus (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  customer_id BIGINT NOT NULL,
  service_name VARCHAR(100) NOT NULL,
  purchase_date DATE NOT NULL,
  expire_date DATE NULL,
  service_status VARCHAR(20) NOT NULL,
  note VARCHAR(255) NULL,
  created_by BIGINT NOT NULL,
  created_at DATETIME NOT NULL,
  CONSTRAINT fk_customer_service_focus_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
  CONSTRAINT fk_customer_service_focus_user FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS care_level (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL,
  description VARCHAR(255),
  status TINYINT NOT NULL
);

CREATE TABLE IF NOT EXISTS care_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  customer_id BIGINT NOT NULL,
  care_date DATETIME NOT NULL,
  content VARCHAR(500) NOT NULL,
  performed_by BIGINT NOT NULL,
  CONSTRAINT fk_care_record_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
  CONSTRAINT fk_care_record_user FOREIGN KEY (performed_by) REFERENCES users(id)
);
