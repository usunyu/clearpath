::
:: Launch the command line compiler of the RDFViewer.
::
:: Parameters:
:: -jdbcurl <database connection url>
:: -dbuser <username>
:: -dbpass <password>
:: -ldm                 Optional, default = RDF
::
:: Example:
:: compile_PSFLite.bat -jdbcurl jdbc:oracle:thin:@YOUR_HOST.YOUR_DOMAIN.com:1521:YOUR_SID -dbuser YOUR_DB_USER -dbpass YOUR_DB_PASSWORD
::
::

set exedir=%0%
set exedir=%exedir:"=%
cd /d %exedir:run.bat=%
set dbfilepath=%1%
if "%dbfilepath%"=="" set dbfilepath=null
set CLASSPATH=rdfviewer-main-SNAPSHOT.jar;lib\*
set HEAPSIZE=-Xmx1024M
if %PROCESSOR_ARCHITECTURE%==AMD64 set HEAPSIZE=-Xmx4096M
set LIBPATH=-Djava.library.path=lib\
if %PROCESSOR_ARCHITECTURE%==AMD64 set LIBPATH=-Djava.library.path=libwin64\
set DEBUG=
rem set DEBUG=-Xdebug -Xrunjdwp:transport=dt_socket,address=8174,server=y,suspend=n
java %DEBUG% %HEAPSIZE% %LIBPATH% -Dswing.defaultlaf=com.sun.java.swing.plaf.windows.WindowsLookAndFeel com.navteq.rdf_psf_lite_compiler.Compiler %*
pause
