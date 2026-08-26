SET NAMES utf8mb4;

USE neusoft_elderly_care;

INSERT INTO users (id, phone, password_hash, real_name, age, gender, role, status, created_at)
VALUES
  (1, '13800000001', '$2b$10$xBnP/U1gDwhBETdmDu7OC.71dhPfw34gqrZ6Ua6bsJoHz3MxEJ29C', '系统管理员', 35, 1, 'ADMIN', 1, NOW()),
  (2, '13800000002', '$2b$10$cWy84z3QZie0T6g4JLnadOeuj0PA86v/ay0yXIRr.vL1NHcGdd6ly', '导师甲', 28, 2, 'STAFF', 1, NOW())
ON DUPLICATE KEY UPDATE real_name = VALUES(real_name), status = VALUES(status);

INSERT INTO rooms (id, floor, room_no, status)
VALUES
  (1, 1, '101', 1),
  (2, 1, '102', 1)
ON DUPLICATE KEY UPDATE room_no = VALUES(room_no), status = VALUES(status);

INSERT INTO beds (id, room_id, bed_no, status)
VALUES
  (1, 1, 'A铺', 'AVAILABLE'),
  (2, 1, 'B铺', 'AVAILABLE'),
  (3, 2, 'A铺', 'AVAILABLE')
ON DUPLICATE KEY UPDATE status = VALUES(status);

INSERT INTO customers (id, name, phone, age, gender, status, bed_id, checkin_date, note, created_at)
VALUES
  (1, '张三', '13900000001', 70, 1, 'DRAFT', NULL, NULL, '首次入营，注意防晒', NOW()),
  (2, '李四', '13900000002', 74, 2, 'DISCHARGED', NULL, NULL, '可再次登记入营', NOW())
ON DUPLICATE KEY UPDATE status = VALUES(status), note = VALUES(note);

INSERT INTO customer_service_object (id, customer_id, manager_user_id, assigned_at)
VALUES
  (1, 1, 2, NOW())
ON DUPLICATE KEY UPDATE manager_user_id = VALUES(manager_user_id), assigned_at = VALUES(assigned_at);

INSERT INTO customer_service_focus (id, customer_id, service_name, purchase_date, expire_date, service_status, note, created_by, created_at)
VALUES
  (1, 1, '潮间带夜观拓展包', '2026-02-01', '2026-08-01', 'ACTIVE', '每周两次夜潮观测', 1, NOW())
ON DUPLICATE KEY UPDATE service_name = VALUES(service_name), service_status = VALUES(service_status), note = VALUES(note);

INSERT INTO care_level (id, name, description, status)
VALUES
  (1, '一级带教', '基础潮间带导览辅导', 1),
  (2, '二级带教', '进阶采样与观测辅导', 1)
ON DUPLICATE KEY UPDATE description = VALUES(description), status = VALUES(status);
