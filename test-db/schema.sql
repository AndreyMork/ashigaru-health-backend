CREATE EXTENSION citext;

CREATE DOMAIN _oms_number AS varchar(16) CHECK (value ~ '\d{16}');

CREATE TYPE _gender AS ENUM ('female', 'male', 'other');

CREATE TABLE patients (
  id integer PRIMARY KEY GENERATED ALWAYS AS IDENTITY,

  created_at timestamptz NOT NULL DEFAULT current_timestamp,

  first_name citext NOT NULL CHECK (first_name <> ''),
  last_name  citext NOT NULL CHECK (last_name <> ''),
  address    citext NOT NULL CHECK (address <> ''),

  gender    _gender NOT NULL,
  birthdate date    NOT NULL,

  oms_number _oms_number NOT NULL UNIQUE
);

CREATE FUNCTION random_oms () RETURNS _oms_number
LANGUAGE plpgsql VOLATILE
AS $$
  DECLARE
    oms_number varchar(16) = '';
  BEGIN
    FOR _ IN 1..16 LOOP
      oms_number := oms_number || trunc(random() * 10);
    END LOOP; 
  
    RETURN oms_number;
  END;
$$;
