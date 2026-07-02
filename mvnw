#!/bin/sh
# ----------------------------------------------------------------------------
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
# ----------------------------------------------------------------------------

# ----------------------------------------------------------------------------
# Apache Maven Wrapper startup script, version 3.3.4
#
# Optional ENV vars
# -----------------
#   JAVA_HOME - location of a JDK home dir, requires that mvnw is executable
#   MAVEN_OPTS - parameters passed to the Java VM when running Maven
# ----------------------------------------------------------------------------

# Determine the project base directory (directory where mvnw resides)
MAVEN_PROJECTBASEDIR=$(
  SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
  echo "$SCRIPT_DIR"
)

WRAPPER_JAR="${MAVEN_PROJECTBASEDIR}/.mvn/wrapper/maven-wrapper.jar"

if [ ! -f "$WRAPPER_JAR" ]; then
  echo "ERROR: Cannot find $WRAPPER_JAR" >&2
  echo "Please download it from https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.4/maven-wrapper-3.3.4.jar" >&2
  exit 1
fi

# Find Java
if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
  JAVA="$JAVA_HOME/bin/java"
else
  JAVA="$(command -v java 2>/dev/null)"
fi

if [ -z "$JAVA" ] || [ ! -x "$JAVA" ]; then
  echo "ERROR: Cannot find Java. Please set JAVA_HOME or ensure java is on your PATH." >&2
  exit 1
fi

exec "$JAVA" \
  ${MAVEN_OPTS:-} \
  "-Dmaven.multiModuleProjectDirectory=${MAVEN_PROJECTBASEDIR}" \
  -cp "$WRAPPER_JAR" \
  org.apache.maven.wrapper.MavenWrapperMain \
  "$@"
