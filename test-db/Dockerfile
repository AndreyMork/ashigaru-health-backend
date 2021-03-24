FROM postgres:alpine

ENV POSTGRES_DB ehr
COPY schema.sql /docker-entrypoint-initdb.d/000-initdb.sql

HEALTHCHECK --interval=5s --timeout=5s --retries=5 \
  CMD ["pg_isready", "-U", "$POSTGRES_USER", "-d", "ehr"]

EXPOSE 5432
