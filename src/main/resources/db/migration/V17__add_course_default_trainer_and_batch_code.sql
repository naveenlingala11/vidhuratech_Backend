alter table batches add column code varchar(120);

alter table course_trainer_assignments add column batch_id bigint;

alter table courses add column default_trainer_id bigint;

alter table batches add constraint uk_batches_code unique (code);

alter table course_trainer_assignments
    add constraint fk_course_trainer_assignment_batch
        foreign key (batch_id) references batches(id);

alter table courses
    add constraint fk_course_default_trainer
        foreign key (default_trainer_id) references users(id);