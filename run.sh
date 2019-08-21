#!/bin/bash

/usr/lib/jvm/default-java/bin/java -Dfile.encoding=UTF-8 -classpath /home/alex/multifacet_java/target/classes:/home/alex/.m2/repository/com/google/code/gson/gson/2.8.5/gson-2.8.5.jar:/home/alex/.m2/repository/net/librec/librec-core/2.0.0/librec-core-2.0.0.jar:/home/alex/.m2/repository/commons-logging/commons-logging/1.2/commons-logging-1.2.jar:/home/alex/.m2/repository/commons-lang/commons-lang/2.6/commons-lang-2.6.jar:/home/alex/.m2/repository/commons-cli/commons-cli/1.3/commons-cli-1.3.jar:/home/alex/.m2/repository/com/google/guava/guava/15.0/guava-15.0.jar:/home/alex/.m2/repository/junit/junit/4.12/junit-4.12.jar:/home/alex/.m2/repository/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar agpar.multifacet.Main "${1}"


