INSERT INTO users (username, email, password, rol, uuid)
VALUES
    ('kevinTest', 'kevin@gritfit.com', '$2a$10$8.UnVuGSHs5BD3W9GQ6Mwe9uS.K55.Xp.jR.V4.u9.Vv.Vv.Vv.Vv', 'ADMIN', gen_random_uuid()),
    ('Usuario Test', 'user@test.com', '$2a$10$8.UnVuGSHs5BD3W9GQ6Mwe9uS.K55.Xp.jR.V4.u9.Vv.Vv.Vv.Vv', 'USER', gen_random_uuid());