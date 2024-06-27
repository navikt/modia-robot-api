FROM gcr.io/distroless/java21-debian12
USER nonroot

COPY build/libs/app.jar app.jar

CMD ["app.jar"]
