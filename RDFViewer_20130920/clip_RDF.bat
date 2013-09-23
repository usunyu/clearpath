:: echo on
:: Example:
:: clip_RDF.bat -rdf <PATH_TO_RDF> -countries DEU,AUT,CHE
::
:: If no arguments are given, a GUI comes up.
::
:: The -out path is detected automatically:
::   -rdf D:\DownloadedProducts\RDF_EU will default to -out D:\DownloadedProducts\RDF_DEU_AUT_CHE, unless you specify -out <path> on the command line.

set HEAPSIZE=-Xmx1024M
if %PROCESSOR_ARCHITECTURE%==AMD64 set HEAPSIZE=-Xmx4096M
set CLASSPATH=lib\RdfFileClipper-SNAPSHOT.jar
set CLASSPATH=%CLASSPATH%;lib\rdf-installer-SNAPSHOT.jar
set CLASSPATH=%CLASSPATH%;lib\dom4j-1.6.1.jar
set CLASSPATH=%CLASSPATH%;lib\jaxen-1.1.1.jar
set CLASSPATH=%CLASSPATH%;lib\log4j-1.2.16.jar
java %HEAPSIZE% -Dswing.defaultlaf=com.sun.java.swing.plaf.windows.WindowsLookAndFeel com.navteq.clipper.RdfClipper %*
pause
