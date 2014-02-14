set exedir=%0%
set exedir=%exedir:"=%
cd /d %exedir:run.bat=%

rem filepath to database if sqlite f.e. dbfilepath=RDF_EU_Berlin_2011Q3_DICI.db - %1% uses the database if the file gets dragged on run.bat
set dbfilepath=%1%
rem f.e. rdfShowThisPlugin="Natural Guidance"
set rdfShowThisPlugin=""
rem use same format than rdf viewer toolbar f.e. rdfShowThisPosition="52.52506 / 13.36951"
set rdfShowThisPosition=""

set JAVA_EXEC=java
if defined RDFVIEWER_JAVA_PATH set JAVA_EXEC="%RDFVIEWER_JAVA_PATH%\java"

if "%dbfilepath%"=="" set dbfilepath=null
set CLASSPATH=rdfviewer-main-SNAPSHOT.jar
set HEAPSIZE=-Xmx1024M
if %PROCESSOR_ARCHITECTURE%==AMD64 set HEAPSIZE=-Xmx6000M
set LIBPATH=-Djava.library.path=lib\
if %PROCESSOR_ARCHITECTURE%==AMD64 set LIBPATH=-Djava.library.path=libwin64\
set DEBUG=
rem set DEBUG=-Xdebug -Xrunjdwp:transport=dt_socket,address=8174,server=y,suspend=n
%JAVA_EXEC% %DEBUG% %HEAPSIZE% %LIBPATH% -Dswing.defaultlaf=com.sun.java.swing.plaf.windows.WindowsLookAndFeel com.navteq.RDF_Viewer.RDFViewer -d %dbfilepath% -spl %rdfShowThisPlugin% -spo %rdfShowThisPosition%
pause
