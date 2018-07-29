SETLOCAL

set root=.\

CALL :distributeLibs dist\
CALL :distributeLibs gui-driver-cucumber\src\test\resources\lib\

if errorlevel 1 pause


:: force execution to quit at the end of the "main" logic
EXIT /B %ERRORLEVEL%


:: a function to distribute libs
:: from various places
:: to a target directory
:distributeLibs
xcopy /Y %root%gui-driver-cucumber\target\*.jar %1
xcopy /Y %root%gui-driver-adapter\target\*.jar %1
xcopy /Y %root%gui-driver-adapter-swt\target\*.jar %1
xcopy /Y %root%gui-driver-browser\target\*.jar %1
xcopy /Y %root%gui-driver-inspector\target\*.jar %1

xcopy /Y %root%swingset2\target\*.jar %1
xcopy /Y %root%swtset2\target\*.jar %1


EXIT /B 0