-- DROP VIEW vw_form_fields;
-- DROP TABLE code_types;
-- DROP TABLE code_values;
-- DROP TABLE form_fields;
-- DROP TABLE form_field_values;
-- DROP TABLE forms;
-- DROP TABLE projects;
-- DROP TABLE roles;
-- DROP TABLE user_projects;
-- DROP TABLE user_roles;
-- DROP TABLE users;

CREATE TABLE code_types (
    code_type_id SERIAL PRIMARY KEY,
    type VARCHAR(50) NOT NULL
);

CREATE TABLE code_values (
    code_value_id SERIAL PRIMARY KEY,
    form_field_id INT,
    code_type_id INT NOT NULL,
    sort_order INT NOT NULL,
    code_value INT NOT NULL,
    label VARCHAR(2000) NOT NULL,
    color_code VARCHAR(10),
    abbr VARCHAR(5),
    create_date TIMESTAMP NOT NULL,
    created_by VARCHAR(20) NOT NULL,
    modified_date TIMESTAMP NOT NULL,
    modified_by VARCHAR(20) NOT NULL
);

CREATE TABLE form_fields (
    form_field_id SERIAL PRIMARY KEY,
    form_id INT NOT NULL,
    field_label VARCHAR(100) NOT NULL,
    default_label VARCHAR(100) NOT NULL,
    description TEXT,
    data_name VARCHAR(100),
    data_length INT,
    sort_order INT NOT NULL,
    form_group_id INT,
    control_type_id INT NOT NULL,
    data_type_id INT NOT NULL,
    primary_key BOOLEAN,
    configurable BOOLEAN,
    required BOOLEAN,
    display_in_table BOOLEAN,
    hidden BOOLEAN,
    require_confirmation BOOLEAN,
    create_date TIMESTAMP NOT NULL,
    created_by VARCHAR(20) NOT NULL,
    modified_date TIMESTAMP NOT NULL,
    modified_by VARCHAR(20) NOT NULL
);

CREATE TABLE form_field_values (
    form_field_value_id SERIAL PRIMARY KEY,
    form_field_id INT NOT NULL,
    record_id INT NOT NULL,
    text_value TEXT,
    int_value INT,
    small_int_value SMALLINT,
    datetime_value TIMESTAMP,
    datetime_offset_value TIMESTAMPTZ,
    decimal_value DECIMAL(18, 0),
    create_date TIMESTAMP NOT NULL,
    created_by VARCHAR(20) NOT NULL,
    modified_date TIMESTAMP NOT NULL,
    modified_by VARCHAR(20) NOT NULL
);

CREATE TABLE forms (
    form_id SERIAL PRIMARY KEY,
    project_id INT,
    name VARCHAR(50) NOT NULL,
    data_name VARCHAR(50) NOT NULL,
    create_date TIMESTAMP NOT NULL,
    created_by VARCHAR(20) NOT NULL,
    modified_date TIMESTAMP NOT NULL,
    modified_by VARCHAR(20) NOT NULL
);

CREATE TABLE projects (
    project_id SERIAL PRIMARY KEY,
    project_name VARCHAR(255) NOT NULL,
    create_date TIMESTAMP NOT NULL,
    created_by VARCHAR(20) NOT NULL,
    modified_date TIMESTAMP NOT NULL,
    modified_by VARCHAR(20) NOT NULL
);

CREATE TABLE roles (
    role_id SERIAL PRIMARY KEY,
    role VARCHAR(255) NOT NULL,
    create_date TIMESTAMP NOT NULL,
    created_by VARCHAR(20) NOT NULL,
    modified_date TIMESTAMP NOT NULL,
    modified_by VARCHAR(20) NOT NULL
);

CREATE TABLE user_projects (
    user_project_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    project_id INT NOT NULL,
    role_id INT NOT NULL
);

CREATE TABLE user_roles (
    user_role_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    role_id INT NOT NULL,
    create_date TIMESTAMP NOT NULL,
    created_by VARCHAR(20) NOT NULL,
    modified_date TIMESTAMP NOT NULL,
    modified_by VARCHAR(20) NOT NULL
);

CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    create_date TIMESTAMP NOT NULL,
    created_by VARCHAR(20) NOT NULL,
    modified_date TIMESTAMP NOT NULL,
    modified_by VARCHAR(20) NOT NULL
);

-- ------------------------------------------------------
-- vw_form_fields
-- ----------------------------------------------------------
CREATE VIEW vw_form_fields AS
SELECT 
    p.project_name,
    f.name AS form_name,
    f.data_name AS table_name,
    COALESCE(ff.field_label, ff.default_label) AS field_label,
    ff.description,
    ff.data_name AS column_name,
    ff.data_length,
    ff.sort_order,
    ff.form_group_id,
    controltype.label AS control_type,
    datatype.label AS data_type,
    ff.primary_key,
    ff.configurable,
    ff.required,
    ff.display_in_table,
    ff.hidden,
    ff.require_confirmation
FROM forms f
JOIN form_fields ff ON f.form_id = ff.form_id
LEFT JOIN projects p ON f.project_id = p.project_id
LEFT JOIN code_values controltype ON ff.control_type_id = controltype.code_value_id AND controltype.code_type_id = 3
LEFT JOIN code_values datatype ON ff.data_type_id = datatype.code_value_id AND datatype.code_type_id = 2;
