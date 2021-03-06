#!/bin/bash

#
# Biobank version 3.8.0
#
# This script calls the Java tool to delete a study.
#
# Modify DBHOST, DBNAME, DBUSER, and DBPASSWD as needed.
#
# Ensure the path stored in the JAVA variable points to a version 6 JRE.
#

JAVA=/usr/lib/jvm/java-6-oracle/bin/java
JAR=BiobankCli.jar
DFLT_DBHOST=192.168.2.43
DFLT_DBNAME=biobank_v3100

if [[ ! -f "$JAVA" ]]; then
   echo "Error: Java JRE not found at $JAVA"
   exit 1
fi

if [[ ! -f "$JAR" ]]; then
   echo "Error: file $JAR does not exist"
   exit 1
fi

read -p "Enter host name for MySQL server: [$DFLT_DBHOST] " DBHOST
DBHOST=${DBHOST:-$DFLT_DBHOST}

read -p "Enter database name for the Biobank application: [$DFLT_DBNAME] " DBNAME
DBNAME=${DBNAME:-$DFLT_DBNAME}

read -p "Enter user name for MySQL server: " DBUSER

read -s -p "Enter user's password: " DBPWD
echo ""

read -p "Enter command: " COMMAND

eval ARGS=($COMMAND)

$JAVA -Ddatabase.host=$DBHOST -Ddatabase.name=$DBNAME -Ddatabase.user=$DBUSER -Ddatabase.password=$DBPWD -jar $JAR "${ARGS[0]}" "${ARGS[1]}"
