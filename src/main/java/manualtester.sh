#!/bin/sh

# Shell script to run the Java SDK Manual tester

mvnRepo=~/.m2/repository/

CP=target/com.horizon.syncservice.client-1.0.0-tests.jar:
CP=${CP}${mvnRepo}com/google/code/gson/gson/2.8.0/gson-2.8.0.jar:
CP=${CP}${mvnRepo}com/squareup/retrofit2/retrofit/2.5.0/retrofit-2.5.0.jar:
CP=${CP}${mvnRepo}com/squareup/okhttp3/okhttp/3.12.0/okhttp-3.12.0.jar:
CP=${CP}${mvnRepo}com/fasterxml/jackson/core/jackson-annotations/2.9.0/jackson-annotations-2.9.0.jar:
CP=${CP}${mvnRepo}com/fasterxml/jackson/core/jackson-core/2.8.1/jackson-core-2.8.1.jar:
CP=${CP}${mvnRepo}com/fasterxml/jackson/datatype/jackson-datatype-jdk8/2.8.1/jackson-datatype-jdk8-2.8.1.jar:
CP=${CP}${mvnRepo}com/squareup/okio/okio/1.14.0/okio-1.14.0.jar:
CP=${CP}${mvnRepo}com/fasterxml/jackson/core/jackson-databind/2.7.2/jackson-databind-2.7.2.jar:
CP=${CP}${mvnRepo}com/squareup/retrofit2/converter-jackson/2.2.0/converter-jackson-2.2.0.jar:
CP=${CP}${mvnRepo}com/squareup/retrofit2/converter-scalars/2.2.0/converter-scalars-2.2.0.jar:
CP=${CP}${mvnRepo}commons-cli/commons-cli/1.3.1/commons-cli-1.3.1.jar

java -cp ${CP} com.horizon.syncservice.tests.ManualTester $*
