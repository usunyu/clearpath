:: echo on

set CLASSPATH=%CLASSPATH%;lib\rdf-psflite-core-SNAPSHOT.jar;lib\rdf-psflite-compiler-SNAPSHOT.jar;lib\dom4j-1.6.1.jar;lib\jaxen-1.1.1.jar;lib\log4j-1.2.16.jar;lib\mysql-connector-5.0.4.jar;lib\ojdbc6-11.2.0.2.0.jar;lib\postgresql-9.0-801.jdbc4.jar;lib\sqlite-jdbc-3.7.2.jar;lib\sqljdbc4-3.0.jar;lib\commons-io-2.4.jar

set HEAPSIZE=-Xmx1024M
if %PROCESSOR_ARCHITECTURE%==AMD64 set HEAPSIZE=-Xmx4096M

java %HEAPSIZE% com.navteq.rdf_psf_lite.xml_import.ImportXml %*
pause
