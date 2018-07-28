color 8f

@echo off

DEL *.log

title Gui Inspector


set root=
set cp=.;lib\*


set configFile=config.xml


set inspectorClass=com.brentcroft.gtd.inspector.InspectorApplication

set javaCommand=C:/Java/jdk1.8.0_161/bin/java

set vm=-Xmx2048m -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8001

set options=--add-opens=javafx.graphics/com.sun.javafx.tk.quantum=ALL-UNNAMED


@echo on

%javaCommand% %vm% -cp %cp% %inspectorClass% %configFile%

@echo off

if errorlevel 1 pause