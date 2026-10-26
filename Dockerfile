FROM gcr.io/distroless/java21-debian13
USER nonroot

COPY build/install/*/lib /lib

ENTRYPOINT ["java", "-cp", "/lib/*", "no.nav.ApplicationKt"]
