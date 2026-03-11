@echo on
setlocal
REM Attempts several JavaFX startup modes to find one that works.
set "JAVAFX_LIB=%~dp0lib\javafx\lib"
set "MYSQL_JAR="
for %%F in ("%~dp0lib\mysql-connector*.jar" "%~dp0lib\mysql-connector-*.jar" "%~dp0lib\mysql-connector-j-*.jar" "%~dp0lib\mysql-*.jar") do (
  if exist %%~fF set "MYSQL_JAR=%%~fF" & goto :found_mysql
)
:found_mysql
if not defined MYSQL_JAR echo MySQL jar not found in lib && exit /b 1

set "BINCP=%~dp0bin;%MYSQL_JAR%"

echo JavaFX lib: %JAVAFX_LIB%
echo MySQL JAR: %MYSQL_JAR%

call :tryRun "Normal" ""
call :tryRun "Software Prism" "-Dprism.order=sw -Dprism.verbose=false"
call :tryRun "Software + NativeAccess" "-Dprism.order=sw -Dprism.verbose=false --enable-native-access=javafx.graphics --add-exports=javafx.graphics/com.sun.glass=ALL-UNNAMED"
call :tryRun "Monocle Headless" "-Dprism.order=sw -Dprism.verbose=false -Dglass.platform=Monocle -Dmonocle.platform=Headless --enable-native-access=javafx.graphics --add-exports=javafx.graphics/com.sun.glass=ALL-UNNAMED"

echo All attempts failed.
exit /b 1

:tryRun
setlocal
set "label=%~1"
set "opts=%~2"
echo --------------------------------------------------
echo Attempting %label% with opts: %opts%
echo --------------------------------------------------
java -Djava.library.path="%~dp0lib\javafx\bin" %opts% --module-path "%JAVAFX_LIB%" --add-modules javafx.controls,javafx.fxml -cp "%BINCP%" MainApp
if errorlevel 1 (
  echo Attempt %label% failed with exit %errorlevel%
  endlocal & goto :eof
)
echo Attempt %label% succeeded.
endlocal
exit /b 0
