FROM navikt/java:17-appdynamics
ENV APPD_ENABLED=true

COPY java-debug.sh /init-scripts/08-java-debug.sh

COPY build/libs/app.jar app.jar