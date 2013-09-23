#! /bin/bash
# $Id: run.sh 8229 2012-05-10 08:57:57Z wbecke $
HEAPSIZE=-Xmx1024M
if [ -n "uname -p | grep -- _64" ]; then 
  HEAPSIZE=-Xmx4096M
fi

java -cp ./rdfviewer-main-SNAPSHOT.jar:./lib $HEAPSIZE com.navteq.RDF_Viewer.RDFViewer
