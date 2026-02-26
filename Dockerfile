FROM openjdk:17.0.1-jdk-slim

RUN useradd -ms /bin/bash appuser

RUN apt-get update \
    && apt-get install -y \
        curl \
        libxrender1 \
        libjpeg62-turbo \
        fontconfig \
        libxtst6 \
        xfonts-75dpi \
        xfonts-base \
        xz-utils

COPY id-mapping-service-1.0.0.jar /opt/

RUN chown -R appuser:appuser /opt
USER appuser
WORKDIR /opt

CMD ["/bin/bash", "-c", "java -XX:+PrintFlagsFinal $JAVA_OPTIONS -XX:+UnlockExperimentalVMOptions -jar /opt/id-mapping-service-1.0.0.jar"]
