# STAGE 1: Build & Package
FROM maven:3.9.13-amazoncorretto-21 AS build-stage
WORKDIR /build

COPY pom.xml .
COPY core/pom.xml core/
COPY core/common-api/pom.xml core/common-api/
COPY core/comment-service/pom.xml core/comment-service/

RUN mvn install -N && \
    cd core && mvn install -N && \
    cd common-api && mvn install -DskipTests && \
    cd ../comment-service && mvn dependency:go-offline -B

COPY . .
RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package \
        -pl core/comment-service \
        -am \
        -DskipTests \
        -Dspotless.skip

# STAGE 2: Unpack Spring Boot Fat JAR
FROM amazoncorretto:21-alpine AS unpack-stage
WORKDIR /unpack
COPY --from=build-stage /build/core/comment-service/target/*.jar service.jar
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