@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@REM ----------------------------------------------------------------------------
@REM Apache Maven Wrapper startup batch script, version 3.3.4
@REM
@REM Optional ENV vars
@REM -----------------
@REM   JAVA_HOME - location of a JDK home dir
@REM   MAVEN_OPTS - parameters passed to the Java VM when running Maven
@REM ----------------------------------------------------------------------------

@if "%DEBUG%"=="" @echo off
%1\msdos.exe -e

set WRAPPER_JAR="%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"

if not exist %WRAPPER_JAR% (
  echo ERROR: Cannot find %WRAPPER_JAR%
  echo Please download it from https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.4/maven-wrapper-3.3.4.jar
  exit /b 1
)

if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if %ERRORLEVEL% equ 0 goto execute

echo ERROR: Cannot find java. Please set the JAVA_HOME environment variable.
exit /b 1

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist %JAVA_EXE% goto execute

echo ERROR: Cannot find %JAVA_EXE%
exit /b 1

:execute
set MAVEN_CMD_LINE_ARGS=%*

%JAVA_EXE% %MAVEN_OPTS% "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" -cp %WRAPPER_JAR% org.apache.maven.wrapper.MavenWrapperMain %MAVEN_CMD_LINE_ARGS%
