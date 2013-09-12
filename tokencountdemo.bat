:: NOTE: For SOLR_QUERY_STRING, it should ONLY be the query string comes after "q="

set OAUTH2_CLIENT_ID=YOUR_CLIENT_ID
set OAUTH2_CLIENT_SECRET=YOUR_CLIENT_SECRET
set SOLR_QUERY_STRING=publishDate:1884 AND author:Dickens
set OUTPUT_FILE=token.zip

java -classpath build/jar/htrc-uncamp-client-1.0.jar htrc.uncamp.UnCampTokenCountDemo "%OAUTH2_CLIENT_ID%" "%OAUTH2_CLIENT_SECRET%" "%SOLR_QUERY_STRING%" "%OUTPUT_FILE%" 