# STAGE 1: Build & Package
FROM maven:3.9.13-amazoncorretto-21 AS build-stage
WORKDIR /build

COPY pom.xml .
COPY stats/pom.xml stats/
COPY stats/stats-dto/pom.xml stats/stats-dto/
COPY stats/stats-client/pom.xml stats/stats-client/
COPY core/pom.xml core/
COPY core/common-api/pom.xml core/common-api/
COPY core/event-service/pom.xml core/event-service/

RUN mvn install -N && \
    mvn -f stats/pom.xml install -N && \
    mvn -f core/pom.xml install -N && \
    mvn -f stats/stats-dto/pom.xml install -DskipTests && \
    mvn -f core/common-api/pom.xml install -DskipTests && \
    mvn -f stats/stats-client/pom.xml install -DskipTests && \
    mvn -f core/event-service/pom.xml dependency:go-offline -B

COPY . .
RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package \
        -pl :event-service \
        -am \
        -DskipTests \
        -Dspotless.skip

# STAGE 2: Unpack Spring Boot Fat JAR
FROM amazoncorretto:21-alpine AS unpack-stage
WORKDIR /unpack
COPY --from=build-stage /build/core/event-service/target/*.jar service.jar
RUN java -Djarmode=tools -jar service.jar extract --layers --destination exploded

# STAGE 3: Runtime Image
FROM amazoncorretto:21-alpine

VOLUME /tmp

RUN adduser -D -h /app service-runner
USER service-runner
WORKDIR /app

COPY --from=unpack-stage /unpack/exploded/dependencies/ ./
COPY --from=unpack-stage /unpack/exploded/spring-boot-loader/ ./
COPY --from=unpack-stage /unpack/exploded/snapshot-dependencies/ ./
COPY --from=unpack-stage /unpack/exploded/application/ ./

RUN java -XX:ArchiveClassesAtExit=service.jsa -Dspring.context.exit=onRefresh -jar service.jar || true

ENV CDS_OPTS="-XX:SharedArchiveFile=service.jsa -Xlog:class+load:file=/tmp/classes.log"
ENV CRASH_OPTS="-XX:ErrorFile=/tmp/jvm_crash.log"
ENV HEAP_DUMP_OPTS="-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp/heapdump.hprof"
ENV EXIT_ON_OOM="-XX:+CrashOnOutOfMemoryError"
ENV NATIVE_MEM_OPTS="-XX:NativeMemoryTracking=summary -XX:+UnlockDiagnosticVMOptions -XX:+PrintNMTStatistics"

ENTRYPOINT ["sh", "-c", "exec java \
    $HEAP_DUMP_OPTS \
    $EXIT_ON_OOM \
    $CRASH_OPTS \
    $NATIVE_MEM_OPTS \
    $CDS_OPTS \
    -jar service.jar"]