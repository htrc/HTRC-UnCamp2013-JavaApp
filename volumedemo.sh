#!/bin/sh

OAUTH2_CLIENT_ID="YOUR_CLIENT_ID"
OAUTH2_CLIENT_SECRET="YOUR_CLIENT_SECRET"
SOLR_QUERY_STRING="publishDate:1884 AND author:Dickens"
OUTPUT_FILE="volume.zip"

java -classpath build/jar/htrc-uncamp-client-1.0.jar htrc.uncamp.UnCampVolumeDemo $OAUTH2_CLIENT_ID $OAUTH2_CLIENT_SECRET "$SOLR_QUERY_STRING" $OUTPUT_FILE
