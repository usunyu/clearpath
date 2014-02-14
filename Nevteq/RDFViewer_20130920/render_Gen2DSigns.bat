:: Renders the 2D Generalized Signs and Generalized Junction Views into PNG files.

set CLASSPATH=lib\*
set HEAPSIZE=-Xmx1024M
set LIBPATH=-Djava.library.path=lib\
if %PROCESSOR_ARCHITECTURE%==AMD64 set LIBPATH=-Djava.library.path=libwin64\
set DEBUG=
rem set DEBUG=-Xdebug -Xrunjdwp:transport=dt_socket,address=8174,server=y,suspend=n
java %DEBUG% %HEAPSIZE% %LIBPATH% com.navteq.rdf_psf_lite.svg_renderer.GenericSvgSignRenderer %*
