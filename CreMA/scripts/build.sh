#!/usr/bin/env bash

export JAVA_HOME=$(/usr/libexec/java_home);
cd ..
mvn clean compile assembly:single