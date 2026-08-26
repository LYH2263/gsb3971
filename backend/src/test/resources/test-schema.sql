CREATE TABLE users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  phone VARCHAR(20) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  real_name VARCHAR(50) NOT NULL,
  age INT NOT NULL,
  gender TINYINT NOT NULL,
  role VARCHAR(20) NOT NULL,
  status TINYINT NOT NULL,
  created_at TIMESTAMP NOT NULL
);

CREATE TABLE rooms (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  floor INT NOT NULL,
  room_no VARCHAR(20) NOT NULL,
  status TINYINT NOT NULL,
  UNIQUE (floor, room_no)
);

CREATE TABLE beds (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  room_id BIGINT NOT NULL,
  bed_no VARCHAR(20) NOT NULL,
  status VARCHAR(20) NOT NULL,
  UNIQUE (room_id, bed_no)
);

CREATE TABLE customers (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(50) NOT NULL,
  phone VARCHAR(20),
  age INT NOT NULL,
  gender TINYINT NOT NULL,
  status VARCHAR(20) NOT NULL,
  bed_id BIGINT,
  checkin_date DATE,
  note VARCHAR(255),
  created_at TIMESTAMP NOT NULL
);

CREATE TABLE meal_weekly_menu (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  week_start_date DATE NOT NULL UNIQUE,
  mon VARCHAR(255),
  tue VARCHAR(255),
  wed VARCHAR(255),
  thu VARCHAR(255),
  fri VARCHAR(255),
  sat VARCHAR(255),
  sun VARCHAR(255),
  updated_at TIMESTAMP NOT NULL
);

CREATE TABLE customer_meal_plan (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  customer_id BIGINT NOT NULL,
  week_start_date DATE NOT NULL,
  meal_type VARCHAR(20) NOT NULL,
  diet_taboo VARCHAR(255),
  note VARCHAR(255),
  created_by BIGINT,
  UNIQUE (customer_id, week_start_date)
);

CREATE TABLE customer_service_object (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  customer_id BIGINT NOT NULL UNIQUE,
  manager_user_id BIGINT NOT NULL,
  assigned_at TIMESTAMP NOT NULL
);

CREATE TABLE customer_service_focus (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  customer_id BIGINT NOT NULL,
  service_name VARCHAR(100) NOT NULL,
  purchase_date DATE NOT NULL,
  expire_date DATE,
  service_status VARCHAR(20) NOT NULL,
  note VARCHAR(255),
  created_by BIGINT NOT NULL,
  created_at TIMESTAMP NOT NULL
);

CREATE TABLE care_level (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(50) NOT NULL,
  description VARCHAR(255),
  status TINYINT NOT NULL
);

CREATE TABLE care_record (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  customer_id BIGINT NOT NULL,
  care_date TIMESTAMP NOT NULL,
  content VARCHAR(500) NOT NULL,
  performed_by BIGINT NOT NULL
);
