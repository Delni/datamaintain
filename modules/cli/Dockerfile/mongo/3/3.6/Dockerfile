FROM openjdk:8-jre-slim-stretch

# From official mongo image and https://docs.mongodb.com/manual/tutorial/install-mongodb-on-ubuntu/#install-mongodb-community-edition-using-deb-packages
ARG MONGO_PACKAGE=mongodb-org
ARG MONGO_REPO=repo.mongodb.org
ENV MONGO_PACKAGE=${MONGO_PACKAGE} MONGO_REPO=${MONGO_REPO}

ARG MONGO_MAJOR
RUN test -n "$MONGO_MAJOR"
ENV MONGO_MAJOR $MONGO_MAJOR

RUN apt-get update \
    && apt-get install -y wget gnupg \
    && wget -qO - https://www.mongodb.org/static/pgp/server-${MONGO_MAJOR}.asc | apt-key add - \
    && echo "deb http://repo.mongodb.org/apt/debian stretch/mongodb-org/3.6 main" | tee /etc/apt/sources.list.d/mongodb-org-3.6.list \
    && apt-get update \
    && apt-get install -y ${MONGO_PACKAGE}-shell \
    && apt-get autoremove --purge -y wget gnupg \
    && rm -rf /var/lib/apt/lists/*

# Install datamaintain
COPY cli-*.tar /code/datamaintain-cli.tar

RUN mkdir /code/datamaintain \
    && tar xvf /code/datamaintain-cli.tar --strip-components=1 -C /code/datamaintain \
    && rm /code/datamaintain-cli.tar \
    && rm /code/datamaintain/bin/cli.bat

ENTRYPOINT ["/code/datamaintain/bin/cli"]
