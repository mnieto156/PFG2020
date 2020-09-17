FROM jenkins/jenkins:lts-slim
USER root
RUN mkdir -p /usr/java/packages/lib/ext 
WORKDIR /usr/java/packages/lib/ext
RUN curl -SL https://jdbc.postgresql.org/download/postgresql-42.2.5.jar -o postgresql-42.2.5.jar