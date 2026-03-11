@echo on
setlocal enabledelayedexpansion
REM Run this from project root. This script will try to discover JavaFX libs and the MySQL connector JAR.

echo Project root: %~dp0

REM Discover JavaFX lib folder candidates
set "JAVAFX_LIB="
if exist "%~dp0lib\javafx\lib" set "JAVAFX_LIB=%~dp0lib\javafx\lib"
if not defined JAVAFX_LIB if exist "%~dp0lib\javafx" set "JAVAFX_LIB=%~dp0lib\javafx"
if not defined JAVAFX_LIB if exist "%~dp0lib" set "JAVAFX_LIB=%~dp0lib"

if not defined JAVAFX_LIB (
  echo JavaFX libraries not found under lib\. Please extract JavaFX SDK into lib\javafx\lib or place jars in lib\javafx.
)

echo Using JavaFX lib path: %JAVAFX_LIB%

REM Discover MySQL connector JAR (pick first matching file in lib)
set "MYSQL_JAR="
for %%F in ("%~dp0lib\mysql-connector*.jar" "%~dp0lib\mysql-connector-*.jar" "%~dp0lib\mysql-connector-j-*.jar" "%~dp0lib\mysql-*.jar") do (
  if exist %%~fF (
    set "MYSQL_JAR=%%~fF"
    goto :found_mysql
  )
)
:found_mysql

if not defined MYSQL_JAR (
  echo MySQL connector JAR not found in lib\. Please place the connector JAR in the lib folder.
  pause
  exit /b 1
)

echo Found MySQL JAR: %MYSQL_JAR%

if not exist "%~dp0bin" mkdir "%~dp0bin"

echo Compiling Java sources...
if defined JAVAFX_LIB (
  javac --module-path "%JAVAFX_LIB%" --add-modules javafx.controls,javafx.fxml -d "%~dp0bin" src\*.java -cp "%MYSQL_JAR%"
) else (
  javac -d "%~dp0bin" src\*.java -cp "%MYSQL_JAR%"
)

if errorlevel 1 (
  echo Compile failed.
  pause
  exit /b 1
)

echo Running application...
set "PRISM_OPTS=-Dprism.order=sw -Dprism.verbose=false"
set "EXTRA_OPTS="
if defined HEADLESS (
  echo Headless mode requested: enabling Monocle headless platform
  set "EXTRA_OPTS=-Dglass.platform=Monocle -Dmonocle.platform=Headless --add-exports=javafx.graphics/com.sun.glass=ALL-UNNAMED --enable-native-access=javafx.graphics"
)

if defined JAVAFX_LIB (
  java -Djava.library.path="%~dp0lib\javafx\bin" %PRISM_OPTS% %EXTRA_OPTS% --module-path "%JAVAFX_LIB%" --add-modules javafx.controls,javafx.fxml -cp "%~dp0bin;%MYSQL_JAR%" MainApp
) else (
  java %PRISM_OPTS% %EXTRA_OPTS% -cp "%~dp0bin;%MYSQL_JAR%" MainApp
)

endlocal
