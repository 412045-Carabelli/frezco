FROM ubuntu:22.04

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update && apt-get install -y \
    curl \
    gnupg2 \
    cron \
    gzip \
    && rm -rf /var/lib/apt/lists/*

# sqlcmd (mssql-tools18)
RUN curl -fsSL https://packages.microsoft.com/keys/microsoft.asc \
    | gpg --dearmor -o /usr/share/keyrings/microsoft-prod.gpg \
    && echo "deb [arch=amd64 signed-by=/usr/share/keyrings/microsoft-prod.gpg] \
       https://packages.microsoft.com/ubuntu/22.04/prod jammy main" \
    > /etc/apt/sources.list.d/mssql-release.list \
    && apt-get update \
    && ACCEPT_EULA=Y apt-get install -y mssql-tools18 unixodbc-dev \
    && rm -rf /var/lib/apt/lists/*

ENV PATH="/opt/mssql-tools18/bin:${PATH}"

RUN mkdir -p /backups /var/log

COPY backup.sh /backup.sh
RUN chmod +x /backup.sh

COPY crontab /etc/cron.d/frezco-backup
RUN chmod 0644 /etc/cron.d/frezco-backup && crontab /etc/cron.d/frezco-backup

RUN touch /var/log/frezco-backup.log

# cron no hereda las variables del contenedor: se vuelcan a /etc/backup.env al arrancar.
CMD ["bash", "-c", "printenv | grep -E '^DB_' > /etc/backup.env && cron && tail -f /var/log/frezco-backup.log"]
