@echo off
rem this file MUST be called twice - before build cpp project and then after compile java project
rem build plan:
rem 1. call this bat (ignore copy errors)
rem 2. rebuild cpp project both arch - x86 and x64
rem 3. rebuild java classes
rem 4. call this bat (must have no errors)

rem make java class for cpp constants
if exist SAPIImpl.cpp\Release\ExportConstants.exe SAPIImpl.cpp\Release\ExportConstants.exe > SAPIImpl.java\SAPIImplTest\src\org\luwrain\windows\speech\SAPIImpl_constants.java

rem make cpp header for javajni class
cd SAPIImpl.java\SAPIImplTest\bin
javah org.luwrain.extensions.mssapi.SAPIImpl
@copy org_luwrain_extensions_mssapi_SAPIImpl.h ..\..\..\SAPIImpl.cpp\SAPIImpl\jni_SAPIImpl.h
del org_luwrain_windows_speech_SAPIImpl.h
cd ..\..\..\exec

rem make copy for compiled dll's into exec
@copy ..\SAPIImpl.cpp\x64\Release\SAPIImpl.dll SAPIImpl.64.dll
@copy ..\SAPIImpl.cpp\Release\SAPIImpl.dll SAPIImpl.32.dll

rem make jar from compiled java classes
cd ..\SAPIImpl.java\SAPIImplTest\bin
jar cfe ..\..\..\exec\SAPIImplTest.jar org.luwrain.extensions.mssapi.SAPIImplTest .

cd ..\..\..
