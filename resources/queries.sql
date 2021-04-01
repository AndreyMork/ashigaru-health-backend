-- :name get-patients :? :*
-- :quoting :ansi
SELECT
  :i*:columns
FROM
  patients
;

-- :name get-patient :? :1
-- :quoting :ansi
SELECT
  :i*:columns
FROM
  patients
WHERE
  id = :id
;

-- :name new-patient! :<! :1
INSERT INTO patients (
  first_name,
  last_name,
  patronim,
  birthdate,
  gender,
  address,
  oms_number
)
VALUES (
  :first_name,
  :last_name,
  :patronim,
  :birthdate,
  :gender,
  :address,
  :oms_number
)
RETURNING 
  :i*:returning
;

-- :name update-patient! :<! :1
UPDATE patients
SET :updates:values
WHERE id = :id
RETURNING 
  :i*:returning
;

-- :name delete-patient! :<! :1
DELETE FROM patients
WHERE id = :id
RETURNING id
;

-- :name create-test-db!
CREATE DATABASE :identifier:db-name
TEMPLATE :identifier:template-db-name;

-- :name drop-db!
DROP DATABASE :identifier:db-name;
