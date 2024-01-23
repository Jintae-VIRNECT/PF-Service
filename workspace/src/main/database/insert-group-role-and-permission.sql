INSERT INTO `group_role` (`group_role_id`, `description`, `role`, `created_at`, `updated_at`)
VALUES (1, 'MASTER', 'MASTER', '2020-01-20 14:59:23', '2020-01-20 14:59:25');
INSERT INTO `group_role` (`group_role_id`, `description`, `role`, `created_at`, `updated_at`)
VALUES (2, 'MANAGER', 'MANAGER', '2020-01-20 14:59:49', '2020-01-20 14:59:52');
INSERT INTO `group_role` (`group_role_id`, `description`, `role`, `created_at`, `updated_at`)
VALUES (3, 'MEMBER', 'MEMBER', '2020-01-20 15:00:21', '2020-01-20 15:00:20');

INSERT INTO `group_permission` (`group_permission_id`, `description`, `permission`, `created_at`, `updated_at`)
VALUES (1, 'Master Permission', 'ALL', '2020-01-30 16:00:51', '2020-01-30 16:00:52');
INSERT INTO `group_permission` (`group_permission_id`, `description`, `permission`, `created_at`, `updated_at`)
VALUES (2, 'Manager Permission', 'MEMBER MANAGEMENT', '2020-01-30 16:00:51', '2020-01-30 16:00:52');
INSERT INTO `group_permission` (`group_permission_id`, `description`, `permission`, `created_at`, `updated_at`)
VALUES (3, 'Manager Permission', 'CONTENTS MANAGEMENT', '2020-01-30 16:00:51', '2020-01-30 16:00:52');
INSERT INTO `group_permission` (`group_permission_id`, `description`, `permission`, `created_at`, `updated_at`)
VALUES (4, 'Manager Permission', 'PROCESS MANAGEMENT', '2020-01-30 16:00:51', '2020-01-30 16:00:52');
INSERT INTO `group_permission` (`group_permission_id`, `description`, `permission`, `created_at`, `updated_at`)
VALUES (5, 'Member Permission', 'NONE', '2020-01-30 16:00:51', '2020-01-30 16:00:52');
