#! /bin/bash
#
# Launch the command line compiler of the RDFViewer
#
# Parameters:
# -jdbcurl <database connection url>
# -jdbcdriver <driver class> Optional, otherwise derived from jdbcurl
# -dbuser <username>
# -dbpass <password>
# -ldm                 Optional, default = RDF
#
# Example:
# ./compile_PSFLite.sh -jdbcurl jdbc:oracle:thin:@YOUR_HOST.YOUR_DOMAIN.com:1521:YOUR_SID -dbuser YOUR_DB_USER -dbpass YOUR_DB_PASSWORD
#

HEAPSIZE=-Xmx1024M
if [ -n "uname -p | grep -- _64" ]; then 
  HEAPSIZE=-Xmx4096M
fi

java -cp ./rdfviewer-main-SNAPSHOT.jar:./lib/* $HEAPSIZE com.navteq.rdf_psf_lite_compiler.Compiler $*
