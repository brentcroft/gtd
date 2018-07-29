

@echo off

title Browser Gui Inspector


set cp=.;target\test-classes;target\classes;src\test\lib\*


set vm=%vm% -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8001

set inspectorClass=com.brentcroft.gtd.inspector.InspectorApplication

set javaCommand=java

set configFile=src\test\resources\swingset2\config-go4schools.xml


@echo on

%javaCommand% %vm% -cp %cp% %inspectorClass% %configFile%

@echo off

if errorlevel 1 pause
