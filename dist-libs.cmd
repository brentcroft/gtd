SETLOCAL

set root=.\

:: update cucumber resources - feedback to build tests
CALL :distributeLibs gui-driver-cucumber\src\test\resources\lib\

:: prepare remote and inspector packs
CALL :distributeLibsRemote dist\remote
CALL :distributeLibs dist\inspector

:: update demo froom cucumber
xcopy /S /Y %root%gui-driver-cucumber\src\test\resources dist\demo

if errorlevel 1 pause


:: force execution to quit at the end of the "main" logic
EXIT /B %ERRORLEVEL%


:: a function to distribute libs
:: from various places
:: to a target directory
:distributeLibs
xcopy /Y %root%gui-driver-cucumber\target\*.jar %1
xcopy /Y %root%gui-driver-adapter\target\*.jar %1
xcopy /Y %root%gui-driver-adapter-fx\target\*.jar %1
xcopy /Y %root%gui-driver-adapter-swt\target\*.jar %1
xcopy /Y %root%gui-driver-browser\target\*.jar %1
xcopy /Y %root%gui-driver-inspector\target\*.jar %1
xcopy /Y %root%swingset2\target\*.jar %1
xcopy /Y %root%swtset2\target\*.jar %1


:: a function to distribute libs
:: from various places
:: to a target directory
:distributeLibsRemote
xcopy /Y %root%gui-driver-driver\target\*.jar %1
xcopy /Y %root%gui-driver-adapter\target\*.jar %1
xcopy /Y %root%gui-driver-adapter-fx\target\*.jar %1
xcopy /Y %root%gui-driver-adapter-swt\target\*.jar %1
xcopy /Y %root%gui-driver-events\target\*.jar %1
xcopy /Y %root%gui-driver-utils\target\*.jar %1



EXIT /B 0