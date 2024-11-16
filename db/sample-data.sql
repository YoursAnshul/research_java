-- ------------------------------------------------------
-- code_types
-- ----------------------------------------------------------
INSERT INTO code_types (code_type_id, type)
VALUES (1, 'Data');

INSERT INTO code_types (code_type_id, type)
VALUES (2, 'DataType');

INSERT INTO code_types (code_type_id, type)
VALUES (3, 'ControlType');

-- reset the sequence to the max value of the primary key
SELECT setval(pg_get_serial_sequence('code_types', 'code_type_id'), MAX(code_type_id)) FROM code_types;

-- ------------------------------------------------------
-- code_values
-- ----------------------------------------------------------
INSERT INTO code_values (code_value_id, form_field_id, code_type_id, sort_order, code_value, label, color_code, abbr, create_date, created_by, modified_date, modified_by)
VALUES (1, NULL, 2, 1, 1, 'varchar', '', '', '2024-01-31 00:00:00', 'jmr110', '2024-01-31 00:00:00', 'jmr110');

INSERT INTO code_values (code_value_id, form_field_id, code_type_id, sort_order, code_value, label, color_code, abbr, create_date, created_by, modified_date, modified_by)
VALUES (2, NULL, 2, 2, 2, 'int', '', '', '2024-01-31 00:00:00', 'jmr110', '2024-01-31 00:00:00', 'jmr110');

INSERT INTO code_values (code_value_id, form_field_id, code_type_id, sort_order, code_value, label, color_code, abbr, create_date, created_by, modified_date, modified_by)
VALUES (3, NULL, 2, 3, 3, 'smallint', '', '', '2024-01-31 00:00:00', 'jmr110', '2024-01-31 00:00:00', 'jmr110');

INSERT INTO code_values (code_value_id, form_field_id, code_type_id, sort_order, code_value, label, color_code, abbr, create_date, created_by, modified_date, modified_by)
VALUES (4, NULL, 2, 4, 4, 'datetime', '', '', '2024-01-31 00:00:00', 'jmr110', '2024-01-31 00:00:00', 'jmr110');

INSERT INTO code_values (code_value_id, form_field_id, code_type_id, sort_order, code_value, label, color_code, abbr, create_date, created_by, modified_date, modified_by)
VALUES (5, NULL, 2, 5, 5, 'datetimeoffset', '', '', '2024-01-31 00:00:00', 'jmr110', '2024-01-31 00:00:00', 'jmr110');

INSERT INTO code_values (code_value_id, form_field_id, code_type_id, sort_order, code_value, label, color_code, abbr, create_date, created_by, modified_date, modified_by)
VALUES (6, NULL, 2, 6, 6, 'decimal', '', '', '2024-01-31 00:00:00', 'jmr110', '2024-01-31 00:00:00', 'jmr110');

INSERT INTO code_values (code_value_id, form_field_id, code_type_id, sort_order, code_value, label, color_code, abbr, create_date, created_by, modified_date, modified_by)
VALUES (7, NULL, 3, 1, 1, 'checkbox', '', '', '2024-01-31 00:00:00', 'jmr110', '2024-01-31 00:00:00', 'jmr110');

INSERT INTO code_values (code_value_id, form_field_id, code_type_id, sort_order, code_value, label, color_code, abbr, create_date, created_by, modified_date, modified_by)
VALUES (8, NULL, 3, 2, 2, 'colorbox', '', '', '2024-01-31 00:00:00', 'jmr110', '2024-01-31 00:00:00', 'jmr110');

INSERT INTO code_values (code_value_id, form_field_id, code_type_id, sort_order, code_value, label, color_code, abbr, create_date, created_by, modified_date, modified_by)
VALUES (9, NULL, 3, 3, 3, 'combobox', '', '', '2024-01-31 00:00:00', 'jmr110', '2024-01-31 00:00:00', 'jmr110');

INSERT INTO code_values (code_value_id, form_field_id, code_type_id, sort_order, code_value, label, color_code, abbr, create_date, created_by, modified_date, modified_by)
VALUES (10, NULL, 3, 4, 4, 'dateonly', '', '', '2024-01-31 00:00:00', 'jmr110', '2024-01-31 00:00:00', 'jmr110');

INSERT INTO code_values (code_value_id, form_field_id, code_type_id, sort_order, code_value, label, color_code, abbr, create_date, created_by, modified_date, modified_by)
VALUES (11, NULL, 3, 5, 5, 'dob', '', '', '2024-01-31 00:00:00', 'jmr110', '2024-01-31 00:00:00', 'jmr110');

INSERT INTO code_values (code_value_id, form_field_id, code_type_id, sort_order, code_value, label, color_code, abbr, create_date, created_by, modified_date, modified_by)
VALUES (12, NULL, 3, 6, 6, 'longtextbox', '', '', '2024-01-31 00:00:00', 'jmr110', '2024-01-31 00:00:00', 'jmr110');

INSERT INTO code_values (code_value_id, form_field_id, code_type_id, sort_order, code_value, label, color_code, abbr, create_date, created_by, modified_date, modified_by)
VALUES (13, NULL, 3, 7, 7, 'number', '', '', '2024-01-31 00:00:00', 'jmr110', '2024-01-31 00:00:00', 'jmr110');

INSERT INTO code_values (code_value_id, form_field_id, code_type_id, sort_order, code_value, label, color_code, abbr, create_date, created_by, modified_date, modified_by)
VALUES (14, NULL, 3, 8, 8, 'selectbox', '', '', '2024-01-31 00:00:00', 'jmr110', '2024-01-31 00:00:00', 'jmr110');

INSERT INTO code_values (code_value_id, form_field_id, code_type_id, sort_order, code_value, label, color_code, abbr, create_date, created_by, modified_date, modified_by)
VALUES (15, NULL, 3, 9, 9, 'textbox', '', '', '2024-01-31 00:00:00', 'jmr110', '2024-01-31 00:00:00', 'jmr110');

INSERT INTO code_values (code_value_id, form_field_id, code_type_id, sort_order, code_value, label, color_code, abbr, create_date, created_by, modified_date, modified_by)
VALUES (16, NULL, 3, 10, 10, 'datetime', '', '', '2024-01-31 00:00:00', 'jmr110', '2024-01-31 00:00:00', 'jmr110');

-- reset the sequence to the max value of the primary key
SELECT setval(pg_get_serial_sequence('code_values', 'code_value_id'), MAX(code_value_id)) FROM code_values;

-- ------------------------------------------------------
-- forms
-- ----------------------------------------------------------
INSERT INTO forms (form_id, project_id, name, data_name, create_date, created_by, modified_date, modified_by) 
VALUES (1, 1, 'Followup CRF', 'tbsCRFFUP', '2024-01-31 00:00:00', 'jmr110', '2024-01-31 00:00:00', 'jmr110');

INSERT INTO forms (form_id, project_id, name, data_name, create_date, created_by, modified_date, modified_by) 
VALUES (2, 1, 'Participant Comm Log', 'tbsINTVWCOMMLOG', '2024-01-31 00:00:00', 'jmr110', '2024-01-31 00:00:00', 'jmr110');

INSERT INTO forms (form_id, project_id, name, data_name, create_date, created_by, modified_date, modified_by) 
VALUES (3, 1, 'Patient PHI', 'tbsPATIENTINFO', '2024-01-31 00:00:00', 'jmr110', '2024-01-31 00:00:00', 'jmr110');

INSERT INTO forms (form_id, project_id, name, data_name, create_date, created_by, modified_date, modified_by) 
VALUES (4, 1, 'Patient Log Form', 'tbsPATLOG', '2024-01-31 00:00:00', 'jmr110', '2024-01-31 00:00:00', 'jmr110');

INSERT INTO forms (form_id, project_id, name, data_name, create_date, created_by, modified_date, modified_by) 
VALUES (5, 1, 'Scheduler', 'tbsSCHEDULE', '2024-01-31 00:00:00', 'jmr110', '2024-01-31 00:00:00', 'jmr110');

INSERT INTO forms (form_id, project_id, name, data_name, create_date, created_by, modified_date, modified_by) 
VALUES (6, 1, 'SITEPROFILE', 'tbsSITEPROFILE', '2024-01-31 00:00:00', 'jmr110', '2024-01-31 00:00:00', 'jmr110');

-- reset the sequence to the max value of the primary key
SELECT setval(pg_get_serial_sequence('forms', 'form_id'), MAX(form_id)) FROM forms;

-- ------------------------------------------------------
-- form_fields
-- ----------------------------------------------------------
INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (0, 1, '', 'table record key', 'table record key', 'crffupkey', 4, 1, NULL, 15, 2, TRUE, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (1, 1, '', 'foreign key to tbsPATLOG', 'foreign key to tbsPATLOG', 'upid', 4, 2, NULL, 15, 2, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (2, 1, '', 'schedule interval', 'schedule interval', 'intervl', 2, 3, NULL, 14, 3, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (3, 1, '', 'date of the questionnaire', 'date of the questionnaire', 'qxdt', 8, 4, NULL, 16, 4, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (4, 1, '', 'status of the questionnaire', 'status of the questionnaire', 'qstatus', 2, 5, NULL, 14, 3, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (5, 1, '', 'reason not done/incomplete or complete by proxy', 'reason not done/incomplete or complete by proxy', 'missreas', 2, 6, NULL, 14, 3, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (6, 1, '', 'reported date of death', 'reported date of death', 'dthdt', 8, 7, NULL, 16, 4, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (7, 1, '', 'Was death in hospital?', 'Was death in hospital?', 'dthosp', 2, 8, NULL, 14, 3, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (8, 1, '', 'reason unable to contact', 'reason unable to contact', 'utlutcrsn', 2, 9, NULL, 14, 3, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (9, 1, '', 'date last contact alive, month', 'date last contact alive, month', 'lastcondtm', 3, 10, NULL, 14, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (10, 1, '', 'date last contact alive, day', 'date last contact alive, day', 'lastcondtd', 3, 11, NULL, 14, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (11, 1, '', 'date last contact alive, year', 'date last contact alive, year', 'lastcondty', 4, 12, NULL, 14, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (12, 1, '', 'missing reason if other', 'missing reason if other', 'missoth', 150, 13, NULL, 15, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (13, 1, '', 'WHO ANSWERED QUESTIONS', 'WHO ANSWERED QUESTIONS', 'infosrc', 2, 14, NULL, 14, 3, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (14, 1, '', 'Relationship of proxy', 'Relationship of proxy', 'proxyrel', 2, 15, NULL, 14, 3, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (15, 1, '', 'TYPE OF ADMINISTRATION', 'TYPE OF ADMINISTRATION', 'admntype', 2, 16, NULL, 14, 3, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (16, 1, '', 'language', 'language', 'crflang', 2, 17, NULL, 14, 3, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (17, 1, '', 'Residence Of participant at time summary completed', 'Residence Of participant at time summary completed', 'residenc', 2, 18, NULL, 14, 3, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (18, 1, '', 'interviewer', 'interviewer', 'interviewer', 50, 19, NULL, 15, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (19, 1, '', 'comments', 'comments', 'comments', 255, 20, NULL, 15, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (20, 2, '', 'table record key', 'table record key', 'commid', 4, 1, NULL, 15, 2, TRUE, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (21, 2, '', 'foreign key to tbsPATLOG', 'foreign key to tbsPATLOG', 'upid', 4, 2, NULL, 15, 2, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (22, 2, '', 'start date/time of call', 'start date/time of call', 'calldt', 8, 3, NULL, 16, 4, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (23, 2, '', 'inteval', 'inteval', 'intervl', 2, 4, NULL, 14, 3, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (24, 2, '', 'Interviewer', 'Interviewer', 'intervrw', 50, 5, NULL, 15, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (25, 2, '', 'Method of communication', 'Method of communication', 'commmethd', 2, 6, NULL, 14, 3, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (26, 2, '', 'Details', 'Details', 'commdetail', 50, 7, NULL, 14, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (27, 2, '', 'Details if other', 'Details if other', 'commdetailoth', 50, 8, NULL, 15, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (28, 2, '', 'Outcome', 'Outcome', 'outcome', 2, 9, NULL, 14, 3, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (29, 2, '', 'Who was called', 'Who was called', 'whocompltd', 2, 10, NULL, 14, 3, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (30, 2, '', 'Who was called if other', 'Who was called if other', 'whocompltdoth', 50, 11, NULL, 15, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (31, 2, '', 'Callback needed?', 'Callback needed?', 'callback', 2, 12, NULL, 14, 3, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (32, 2, '', 'call back date', 'call back date', 'callbackdt', 8, 13, NULL, 16, 4, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (33, 2, '', 'call back time', 'call back time', 'callbacktm', 8, 14, NULL, 16, 4, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (34, 2, '', 'Standard Comment', 'Standard Comment', 'stdcomment', 2, 15, NULL, 14, 3, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (35, 2, '', 'comment', 'comment', 'comment', 254, 16, NULL, 15, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (36, 2, '', 'end date/time of call', 'end date/time of call', 'callenddt', 8, 17, NULL, 16, 4, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (37, 3, '', 'foreign key to tbsPATLOG', 'foreign key to tbsPATLOG', 'upid', 4, 1, NULL, 15, 2, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (38, 3, '', 'First name', 'First name', 'fname', 30, 2, NULL, 15, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (39, 3, '', 'middle name', 'middle name', 'mname', 30, 3, NULL, 15, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (40, 3, '', 'last name', 'last name', 'lname', 30, 4, NULL, 15, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (41, 3, '', 'Address line 1', 'Address line 1', 'addrlin1', 50, 5, NULL, 15, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (42, 3, '', 'Address line 2', 'Address line 2', 'addrlin2', 50, 6, NULL, 15, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (43, 3, '', 'Address line 3', 'Address line 3', 'addrlin3', 50, 7, NULL, 15, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (44, 3, '', 'city', 'city', 'city', 50, 8, NULL, 15, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (45, 3, '', 'state', 'state', 'state', 50, 9, NULL, 15, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (46, 3, '', 'country', 'country', 'country', 50, 10, NULL, 14, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (47, 3, '', 'zip', 'zip', 'zip', 12, 11, NULL, 15, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (48, 3, '', 'home phone #', 'home phone #', 'homephon', 20, 12, NULL, 15, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (49, 3, '', 'work phone #', 'work phone #', 'workphon', 20, 13, NULL, 15, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (50, 3, '', 'best time to call', 'best time to call', 'besttime', 2, 14, NULL, 14, 3, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (51, 3, '', 'Significant other last name', 'Significant other last name', 'solname', 30, 15, NULL, 15, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (52, 3, '', 'Significant other first name', 'Significant other first name', 'sofname', 30, 16, NULL, 15, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (53, 3, '', 'Significant other middle name', 'Significant other middle name', 'somname', 30, 17, NULL, 15, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (54, 3, '', 'alternate street', 'alternate street', 'altstreet', 50, 18, NULL, 15, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (55, 3, '', 'alternate city', 'alternate city', 'altcity', 50, 19, NULL, 15, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (56, 3, '', 'alternate state', 'alternate state', 'altstate', 50, 20, NULL, 15, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (57, 3, '', 'alternate country', 'alternate country', 'altcountry', 50, 21, NULL, 14, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (58, 3, '', 'alternate xui', 'alternate xui', 'altzip', 11, 22, NULL, 15, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (59, 3, '', 'alternate home phone', 'alternate home phone', 'althomephon', 20, 23, NULL, 15, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (60, 3, '', 'cell phone', 'cell phone', 'cellphone', 20, 24, NULL, 15, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (61, 3, '', 'email address', 'email address', 'emailaddress', 100, 25, NULL, 15, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (62, 3, '', 'Can text this number?', 'Can text this number?', 'cantext', 4, 26, NULL, 14, 2, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (63, 3, '', 'Can call work?', 'Can call work?', 'callwork', 4, 27, NULL, 14, 2, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (64, 3, '', 'Best time to call at work', 'Best time to call at work', 'besttmwrk', 2, 28, NULL, 14, 3, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (65, 3, '', 'Best time to call cell', 'Best time to call cell', 'besttmcell', 2, 29, NULL, 14, 3, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (66, 3, '', 'Significant other home phone', 'Significant other home phone', 'sohomephon', 50, 30, NULL, 15, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (67, 3, '', 'Significant other cell phone', 'Significant other cell phone', 'socellphon', 50, 31, NULL, 15, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (68, 3, '', 'Significant other best time to call home', 'Significant other best time to call home', 'sobesttmhom', 2, 32, NULL, 14, 3, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (69, 3, '', 'Significant other best time to call cell', 'Significant other best time to call cell', 'sobesttmcell', 2, 33, NULL, 14, 3, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (70, 3, '', 'Can text this number?', 'Can text this number?', 'socantext', 4, 34, NULL, 14, 2, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (71, 3, '', 'instructions for contacting', 'instructions for contacting', 'instructions', 1500, 35, NULL, 15, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (72, 3, '', 'Contact info is verified', 'Contact info is verified', 'verified', 2, 36, NULL, 14, 3, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (73, 3, '', 'Issues noted', 'Issues noted', 'verifiedissues', 5000, 37, NULL, 15, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (74, 3, '', 'Best time to call home notes', 'Best time to call home notes', 'besttimetxt', 100, 38, NULL, 15, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (75, 3, '', 'Best time to call cell notes', 'Best time to call cell notes', 'besttmcelltxt', 100, 39, NULL, 15, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (76, 3, '', 'Best time to call work notes', 'Best time to call work notes', 'besttmwrktxt', 100, 40, NULL, 15, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (77, 3, '', 'Best time to call significant other home notes', 'Best time to call significant other home notes', 'sobesttmhomtxt', 100, 41, NULL, 15, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (78, 3, '', 'Best time to call significant other cell notes', 'Best time to call significant other cell notes', 'sobesttmcelltxt', 100, 42, NULL, 15, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (79, 3, '', 'Family Sur name', 'Family Sur name', 'fsurname', 50, 43, NULL, 15, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (80, 3, '', 'Occupation', 'Occupation', 'occupation', 50, 44, NULL, 15, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (81, 3, '', 'State of birth', 'State of birth', 'stofbirth', 50, 45, NULL, 15, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (82, 3, '', 'Alternate best time to call home', 'Alternate best time to call home', 'altbesttmhom', 2, 46, NULL, 14, 3, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (83, 3, '', 'Alternate best time to call home notes', 'Alternate best time to call home notes', 'altbesttmhomtxt', 100, 47, NULL, 15, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (84, 3, '', 'Is this a shared phone?', 'Is this a shared phone?', 'sharedphone', 2, 48, NULL, 14, 3, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (85, 3, '', 'is this a shared cell phone?', 'is this a shared cell phone?', 'sharedcell', 2, 49, NULL, 14, 3, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (86, 4, '', 'Internal Participant identifier', 'Internal Participant identifier', 'upid', 4, 1, NULL, 15, 2, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (87, 4, '', 'foreign key to tbsSITEPROFILE', 'foreign key to tbsSITEPROFILE', 'siteprofilekey', 10, 2, NULL, 14, 2, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (88, 4, '', 'Participant Study Number', 'Participant Study Number', 'studyno', 20, 3, NULL, 15, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (89, 4, '', 'Participant Status (active, withdrawn, deceased, etc.)', 'Participant Status (active, withdrawn, deceased, etc.)', 'ptstat', 2, 4, NULL, 14, 3, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (90, 4, '', 'Date of Participant Status', 'Date of Participant Status', 'ptstatdt', 8, 5, NULL, 16, 4, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (91, 4, '', 'Source of Participant Status', 'Source of Participant Status', 'ptstatsrc', 25, 6, NULL, 15, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (92, 4, '', 'Participant SubStatus', 'Participant SubStatus', 'ptsubstat', 2, 7, NULL, 14, 3, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (93, 4, '', 'Participant SubStatus date', 'Participant SubStatus date', 'ptsubstatdt', 8, 8, NULL, 16, 4, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (94, 4, '', 'Screening Date', 'Screening Date', 'screendt', 8, 9, NULL, 16, 4, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (95, 4, '', 'Anchor date for schedules', 'Anchor date for schedules', 'anchordt', 8, 10, NULL, 16, 4, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (96, 4, '', 'Baseline admit date', 'Baseline admit date', 'baselineadmitdt', 8, 11, NULL, 16, 4, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (97, 4, '', 'Baseline discharge date', 'Baseline discharge date', 'baselinedischargedt', 8, 12, NULL, 16, 4, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (98, 4, '', 'Participant initials', 'Participant initials', 'ptinit', 3, 13, NULL, 15, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (99, 4, '', 'Sex', 'Sex', 'sex', 2, 14, NULL, 14, 3, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (100, 4, '', 'Date of Birth', 'Date of Birth', 'dob', 8, 15, NULL, 16, 4, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (101, 4, '', 'Consent Status', 'Consent Status', 'consent', 2, 16, NULL, 14, 3, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (102, 4, '', 'Consent Status as of date', 'Consent Status as of date', 'consentstatdt', 8, 17, NULL, 16, 4, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (103, 4, '', 'Consent Signed Date', 'Consent Signed Date', 'consentdt', 8, 18, NULL, 16, 4, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (104, 4, '', 'Does consent expire?', 'Does consent expire?', 'conxpire', 2, 19, NULL, 14, 3, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (105, 4, '', 'Consent expiration date', 'Consent expiration date', 'conxpiredt', 8, 20, NULL, 16, 4, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (106, 4, '', 'QOL Status', 'QOL Status', 'qolstat', 2, 21, NULL, 14, 3, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (107, 4, '', 'QOL Status Date', 'QOL Status Date', 'qolstatdt', 8, 22, NULL, 16, 4, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (108, 4, '', 'QOL Followup by', 'QOL Followup by', 'qolfup', 2, 23, NULL, 14, 3, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (109, 4, '', 'Is Participant Info Form required?', 'Is Participant Info Form required?', 'patinforeqd', 2, 24, NULL, 14, 3, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (110, 4, '', 'Participant Info Form Status', 'Participant Info Form Status', 'ptinfostat', 2, 25, NULL, 14, 3, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (111, 4, '', 'Participant Info Form Status as of date', 'Participant Info Form Status as of date', 'ptinfostatdt', 8, 26, NULL, 16, 4, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (112, 4, '', 'Import Date', 'Import Date', 'importdt', 8, 27, NULL, 16, 4, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (113, 4, '', 'Participant timezone relative to EST (EST=0)', 'Participant timezone relative to EST (EST=0)', 'timezone', 5, 28, NULL, 15, 6, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (114, 4, '', 'Language', 'Language', 'lang', 2, 29, NULL, 14, 3, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (115, 4, '', 'Other language', 'Other language', 'langspec', 50, 30, NULL, 15, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (116, 4, '', 'Death index searched?', 'Death index searched?', 'dthindx', 2, 31, NULL, 14, 3, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (117, 4, '', 'Date Death index searched?', 'Date Death index searched?', 'dthindxdt', 8, 32, NULL, 16, 4, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (118, 4, '', 'Death found in index?', 'Death found in index?', 'dthindxfnd', 2, 33, NULL, 14, 3, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (119, 4, '', 'Hipaa revocation', 'Hipaa revocation', 'hipaa_revoc', 2, 34, NULL, 14, 3, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (120, 4, '', 'Hipaa revocation date', 'Hipaa revocation date', 'hipaa_revocdt', 8, 35, NULL, 16, 4, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (121, 5, '', 'Unique record identifier', 'Unique record identifier', 'schedulekey', 4, 1, NULL, 15, 2, TRUE, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (122, 5, '', 'Internal Participant ID', 'Internal Participant ID', 'upid', 4, 2, NULL, 15, 2, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (123, 5, '', 'Visit Sequence Number', 'Visit Sequence Number', 'visseq', 2, 3, NULL, 15, 3, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (124, 5, '', 'Scheduled Interval', 'Scheduled Interval', 'intervl', 2, 4, NULL, 15, 3, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (125, 5, '', 'Scheduled Item Description', 'Scheduled Item Description', 'scheddescription', 100, 5, NULL, 15, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (126, 5, '', 'Schedule Type (QOL, EuroQOL, CRFFUP, RRF)', 'Schedule Type (QOL, EuroQOL, CRFFUP, RRF)', 'schedtype', 2, 6, NULL, 14, 3, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (127, 5, '', 'Notify Date', 'Notify Date', 'notifydt', 8, 7, NULL, 16, 4, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (128, 5, '', 'Low Window Visit Date', 'Low Window Visit Date', 'lowvisdt', 8, 8, NULL, 16, 4, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (129, 5, '', 'Scheduled Visit Date', 'Scheduled Visit Date', 'visdt', 8, 9, NULL, 16, 4, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (130, 5, '', 'Late Date', 'Late Date', 'latedt', 8, 10, NULL, 16, 4, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (131, 5, '', 'Expiration Date', 'Expiration Date', 'expiredt', 8, 11, NULL, 16, 4, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (132, 5, '', 'Expected', 'Expected', 'expected', 2, 12, NULL, 14, 3, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (133, 5, '', 'Not Expected Reason', 'Not Expected Reason', 'nereason', 25, 13, NULL, 14, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (134, 5, '', 'NOT USED', 'NOT USED', 'interviewer', 50, 14, NULL, 15, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (135, 5, '', 'NOT USED', 'NOT USED', 'assigndt', 8, 15, NULL, 16, 4, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (136, 5, '', 'NOT USED', 'NOT USED', 'comment', 255, 16, NULL, 15, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (137, 6, '', 'siteprofilekey', 'siteprofilekey', 'siteprofilekey', 4, 1, NULL, 15, 2, TRUE, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (138, 6, '', 'siteno', 'siteno', 'siteno', 10, 2, NULL, 15, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (139, 6, '', 'sitename', 'sitename', 'sitename', 255, 3, NULL, 15, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (140, 6, '', 'siteaddr1', 'siteaddr1', 'siteaddr1', 150, 4, NULL, 15, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (141, 6, '', 'siteaddr2', 'siteaddr2', 'siteaddr2', 150, 5, NULL, 15, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (142, 6, '', 'siteaddr3', 'siteaddr3', 'siteaddr3', 150, 6, NULL, 15, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (143, 6, '', 'siteaddr4', 'siteaddr4', 'siteaddr4', 150, 7, NULL, 15, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (144, 6, '', 'sitephon', 'sitephon', 'sitephon', 25, 8, NULL, 15, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (145, 6, '', 'sitefax', 'sitefax', 'sitefax', 25, 9, NULL, 15, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (146, 6, '', 'city', 'city', 'city', 50, 10, NULL, 15, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (147, 6, '', 'state', 'state', 'state', 50, 11, NULL, 15, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (148, 6, '', 'country', 'country', 'country', 50, 12, NULL, 15, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (149, 6, '', 'postalcode', 'postalcode', 'postalcode', 25, 13, NULL, 15, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (150, 6, '', 'participation', 'participation', 'participation', 2, 14, NULL, 14, 3, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (151, 6, '', 'participationdt', 'participationdt', 'participationdt', 8, 15, NULL, 16, 4, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (152, 6, '', 'siteqolstat', 'siteqolstat', 'siteqolstat', 2, 16, NULL, 14, 3, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (153, 6, '', 'sitepatinforeqd', 'sitepatinforeqd', 'sitepatinforeqd', 2, 17, NULL, 14, 3, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (154, 6, '', 'siteqolfup', 'siteqolfup', 'siteqolfup', 2, 18, NULL, 14, 3, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (155, 6, '', 'qolmethod', 'qolmethod', 'qolmethod', 2, 19, NULL, 14, 3, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (156, 6, '', 'consentreqd', 'consentreqd', 'consentreqd', 2, 20, NULL, 14, 3, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (157, 6, '', 'conxpiredflt', 'conxpiredflt', 'conxpiredflt', 2, 21, NULL, 14, 3, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (158, 6, '', 'conxpirenum', 'conxpirenum', 'conxpirenum', 2, 22, NULL, 14, 3, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (159, 6, '', 'conxpireunit', 'conxpireunit', 'conxpireunit', 4, 23, NULL, 15, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (160, 6, '', 'importdt', 'importdt', 'importdt', 8, 24, NULL, 16, 4, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (161, 6, '', 'timezone', 'timezone', 'timezone', 5, 25, NULL, 15, 6, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (162, 6, '', 'Comments', 'Comments', 'comments', 5000, 26, NULL, 15, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (163, 6, '', 'Site assigned to personnel', 'Site assigned to personnel', 'assignto', 50, 27, NULL, 15, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (164, 6, '', 'optional fields for data entry', 'optional fields for data entry', 'optional1', 50, 28, NULL, 15, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (165, 6, '', 'optional fields for data entry', 'optional fields for data entry', 'optional2', 50, 29, NULL, 15, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

INSERT INTO form_fields (form_field_id, form_id, field_label, default_label, description, data_name, data_length, sort_order, form_group_id, control_type_id, data_type_id, primary_key, configurable, required, display_in_table, hidden, require_confirmation, create_date, created_by, modified_date, modified_by)
VALUES (166, 6, '', 'optional fields for data entry', 'optional fields for data entry', 'optional3', 50, 30, NULL, 15, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2024-01-31T00:00:00.000', 'jmr110', '2024-01-31T00:00:00.000', 'jmr110');

-- reset the sequence to the max value of the primary key
SELECT setval(pg_get_serial_sequence('form_fields', 'form_field_id'), MAX(form_field_id)) FROM form_fields;
