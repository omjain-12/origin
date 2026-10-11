@echo off
if not defined JAVA_HOME set "JAVA_HOME=C:\Program Files\Microsoft\jdk-21.0.12.101-hotspot"
set "PATH=%JAVA_HOME%\bin;C:\Users\omjai\.maven\apache-maven-3.9.9\bin;%PATH%"
"C:\Users\omjai\.maven\apache-maven-3.9.9\bin\mvn.cmd" -f backend\pom.xml %*
