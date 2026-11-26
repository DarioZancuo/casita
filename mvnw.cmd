@echo off
setlocal
set "MVNW_PROJECTBASEDIR=%~dp0"
if not defined JAVA_HOME (
  echo JAVA_HOME is not set. Install JDK 21 and set JAVA_HOME. 1>&2
  exit /b 1
)
set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
if not exist "%JAVA_EXE%" (
  echo JAVA_HOME does not point to a valid JDK: %JAVA_HOME% 1>&2
  exit /b 1
)
set "WRAPPER_JAR=%MVNW_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.jar"
if not exist "%WRAPPER_JAR%" (
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -UseBasicParsing 'https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.4/maven-wrapper-3.3.4.jar' -OutFile '%WRAPPER_JAR%'"
  if errorlevel 1 exit /b 1
)
"%JAVA_EXE%" -Dmaven.multiModuleProjectDirectory="%MVNW_PROJECTBASEDIR%" -classpath "%WRAPPER_JAR%" org.apache.maven.wrapper.MavenWrapperMain %*
endlocal
