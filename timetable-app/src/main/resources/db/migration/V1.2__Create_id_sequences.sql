create sequence if not exists auditorium_id_seq start with 1 increment by 50
    minvalue 1 maxvalue 9223372036854775807;
create sequence if not exists course_id_seq start with 1 increment by 50
    minvalue 1 maxvalue 9223372036854775807;
create sequence if not exists group_id_seq start with 1 increment by 50
    minvalue 1 maxvalue 9223372036854775807;
create sequence if not exists professor_id_seq start with 1 increment by 50
    minvalue 1 maxvalue 9223372036854775807;
create sequence if not exists student_id_seq start with 1 increment by 50
    minvalue 1 maxvalue 9223372036854775807;
create sequence if not exists schedule_template_id_seq start with 1 increment
    by 50 minvalue 1 maxvalue 9223372036854775807;
create sequence if not exists schedule_id_seq start with 1 increment by 50
    minvalue 1 maxvalue 9223372036854775807;
create sequence if not exists rescheduling_option_id_seq start with 1
    increment by 50 minvalue 1 maxvalue 9223372036854775807;