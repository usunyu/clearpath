#! /bin/bash
#
# Example:
# clip_RDF.sh -rdf <PATH_TO_RDF> -countries DEU,AUT,CHE
#
# If no arguments are given, a GUI comes up.
#
# The -out path is detected automatically:
#  -rdf D:\DownloadedProducts\RDF_EU will default to -out D:\DownloadedProducts\RDF_DEU_AUT_CHE, unless you specify -out <path> on the command line.


HEAPSIZE=-Xmx1024M
if [ -n "uname -p | grep -- _64" ]; then 
  HEAPSIZE=-Xmx4096M
fi

CLASSPATH=lib/RdfFileClipper-SNAPSHOT.jar:lib/rdf-installer-SNAPSHOT.jar:lib/dom4j-1.6.1.jar:lib/jaxen-1.1.1.jar:lib/log4j-1.2.16.jar
java -cp $CLASSPATH $HEAPSIZE com.navteq.clipper.RdfClipper $*
