create role pladmin with
	login
	superuser
	inherit
	createdb
	createrole
	password 'pladmin';

comment on role pladmin is 'Administrador de BBDD de  Procesadores de Lenguaje';

create database "ProcLen_DB"
	with owner pladmin;

comment on database "ProcLen_DB" is 'test database';

-- we don't know how to generate root <with-no-name> (class Root) :(
create sequence alumno_id_seq
	increment by 50;

alter sequence alumno_id_seq owner to pladmin;

create sequence commit_id_seq;

alter sequence commit_id_seq owner to pladmin;

create sequence error_id_seq;

alter sequence error_id_seq owner to pladmin;

create sequence hibernate_sequence;

alter sequence hibernate_sequence owner to pladmin;

create sequence curso_id_seq;

alter sequence curso_id_seq owner to pladmin;

create sequence coment_id_seq;

alter sequence coment_id_seq owner to pladmin;

create table alumnos
(
	alumno_id integer default nextval('public.alumno_id_seq'::regclass) not null
		constraint pk_alumnos
			primary key,
	nombre varchar(50) not null,
	apellido_1 varchar(100) not null,
	apellido_2 varchar(100),
	correo varchar(150) not null
);

alter table alumnos owner to pladmin;

create table role
(
	authority varchar(255) not null
		constraint role_pkey
			primary key,
	description varchar(255)
);

alter table role owner to pladmin;

create table users
(
	username varchar(255) not null
		constraint users_pkey
			primary key,
	account_non_expired boolean not null,
	account_non_locked boolean not null,
	credentials_non_expired boolean not null,
	enabled boolean not null,
	password varchar(255),
	alumno_alumno_id integer
		constraint fk_alumno_id_user
			references alumnos
);

alter table users owner to pladmin;

create table user_role
(
	username varchar(255) not null
		constraint fk_username
			references users,
	authority varchar(255) not null
		constraint fk_authority
			references role,
	constraint user_role_pkey
		primary key (username, authority)
);

alter table user_role owner to pladmin;

create table cursos
(
	curso_id integer not null
		constraint cursos_pkey
			primary key,
	anio varchar(255) not null,
	asignatura varchar(255) not null,
	cerrado boolean not null,
	base_repository varchar(255)
);

alter table cursos owner to pladmin;

create table curso_alumno
(
	alumno_id integer not null
		constraint fk_alumno_id
			references alumnos,
	curso_id integer not null
		constraint fk_curso_id
			references cursos,
	repositorio varchar(255),
	constraint curso_alumno_pkey
		primary key (alumno_id, curso_id)
);

alter table curso_alumno owner to pladmin;

create table commits
(
	commit_id integer default nextval('public.commit_id_seq'::regclass) not null
		constraint pk_commits
			primary key,
	alumno_id integer not null,
	commit_url varchar(250) not null,
	commit_fecha timestamp(2) default now() not null,
	commit_num_errores integer not null,
	commit_num_correctos integer not null,
	curso_id integer not null,
	constraint fk_alumno_id_curso_id_commit
		foreign key (alumno_id, curso_id) references curso_alumno
);

alter table commits owner to pladmin;

create table commits_errores
(
	error_id integer default nextval('public.error_id_seq'::regclass) not null
		constraint pk_commits_errores
			primary key,
	commit_id integer not null
		constraint fk_commit_id_error
			references commits
				on delete cascade,
	error_url varchar(500) not null,
	error_stage_name varchar(50) not null
);

alter table commits_errores owner to pladmin;

create unique index cursos_anio_asignatura_uindex
	on cursos (anio, asignatura);

create table comentarios
(
	comentario_id integer not null
		constraint comentario_pkey
			primary key,
	coment_fecha timestamp(2) default now() not null,
	contenido varchar(500) not null,
	commit_id integer not null
		constraint fk_commit_id_comentario
			references commits,
	username varchar(255) not null
		constraint fk_username_comentario
			references users
);

alter table comentarios owner to pladmin;

CREATE OR REPLACE FUNCTION public.nombre_completo(
	alumnos)
    RETURNS text
    LANGUAGE 'sql'

    COST 100
    VOLATILE 
AS $BODY$
	SELECT CONCAT($1.nombre,' ',$1.apellido_1,' ',$1.apellido_2 );
$BODY$;

ALTER FUNCTION public.nombre_completo(alumnos)
    OWNER TO pladmin;