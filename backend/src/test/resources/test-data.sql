INSERT INTO users (id, phone, password_hash, real_name, age, gender, role, status, created_at)
VALUES
  (1, '13800000001', '$2b$10$xBnP/U1gDwhBETdmDu7OC.71dhPfw34gqrZ6Ua6bsJoHz3MxEJ29C', '系统管理员', 35, 1, 'ADMIN', 1, CURRENT_TIMESTAMP),
  (2, '13800000002', '$2b$10$cWy84z3QZie0T6g4JLnadOeuj0PA86v/ay0yXIRr.vL1NHcGdd6ly', '导师甲', 28, 2, 'STAFF', 1, CURRENT_TIMESTAMP);

INSERT INTO rooms (id, floor, room_no, status)
VALUES (1, 1, '101', 1);

INSERT INTO beds (id, room_id, bed_no, status)
VALUES
  (1, 1, 'A铺', 'AVAILABLE'),
  (2, 1, 'B铺', 'AVAILABLE');

INSERT INTO customers (id, name, phone, age, gender, status, bed_id, checkin_date, note, created_at)
VALUES
  (1, '张三', '13900000001', 70, 1, 'DRAFT', NULL, NULL, '慢病关注', CURRENT_TIMESTAMP),
  (2, '李四', '13900000002', 74, 2, 'DISCHARGED', NULL, NULL, '', CURRENT_TIMESTAMP);

INSERT INTO care_level (id, name, description, status)
VALUES (1, '一级带教', '基础潮间带导览', 1);
