:: echo on

set CLASSPATH=lib\rdf-installer-SNAPSHOT.jar
set CLASSPATH=%CLASSPATH%;lib\rdf-psflite-core-SNAPSHOT.jar
set CLASSPATH=%CLASSPATH%;lib\rdf-psflite-compiler-SNAPSHOT.jar
set CLASSPATH=%CLASSPATH%;lib\dom4j-1.6.1.jar;lib\jaxen-1.1.1.jar
set CLASSPATH=%CLASSPATH%;lib\log4j-1.2.16.jar;lib\mysql-connector-5.0.4.jar
set CLASSPATH=%CLASSPATH%;lib\ojdbc6-11.2.0.2.0.jar;
set CLASSPATH=%CLASSPATH%;lib\postgresql-9.0-801.jdbc4.jar
set CLASSPATH=%CLASSPATH%;lib\sqlite-jdbc-3.7.2.jar;
set CLASSPATH=%CLASSPATH%;lib\sqljdbc4-3.0.jar
set CLASSPATH=%CLASSPATH%;lib\tar-2.5.jar;
set CLASSPATH=%CLASSPATH%;lib\commons-io-2.4.jar

set HEAPSIZE=-Xmx1024M
if %PROCESSOR_ARCHITECTURE%==AMD64 set HEAPSIZE=-Xmx4096M

java %HEAPSIZE% -Dswing.defaultlaf=com.sun.java.swing.plaf.windows.WindowsLookAndFeel com.nokia.rdf_installer.RDFInstaller %*
pause
