#! /bin/bash

CLASSPATH=lib/rdf-installer-SNAPSHOT.jar:lib/rdf-psflite-core-SNAPSHOT.jar:lib/rdf-psflite-compiler-SNAPSHOT.jar:lib/dom4j-1.6.1.jar:lib/jaxen-1.1.1.jar:lib/log4j-1.2.16.jar:lib/mysql-connector-5.0.4.jar:lib/ojdbc6-11.2.0.2.0.jar:lib/postgresql-9.0-801.jdbc4.jar:lib/sqlite-jdbc-3.7.2.jar:lib/sqljdbc4-3.0.jar:lib/tar-2.5.jar:lib/commons-io-2.4.jar
HEAPSIZE=-Xmx1024M
if [ -n "uname -p | grep -- _64" ]; then 
  HEAPSIZE=-Xmx4096M
fi

java -cp $CLASSPATH $HEAPSIZE com.nokia.rdf_installer.RDFInstaller $*
