:: NOTE: For SOLR_QUERY_STRING, it should ONLY be the query string comes after "q="

set OAUTH2_CLIENT_ID=YOUR_CLIENT_ID
set OAUTH2_CLIENT_SECRET=YOUR_CLIENT_SECRET
set SOLR_QUERY_STRING=title:war AND author:Bill
set OUTPUT_FILE=page.zip

java -classpath build/jar/htrc-uncamp-client-1.0.jar htrc.uncamp.UnCampPageDemo "%OAUTH2_CLIENT_ID%" "%OAUTH2_CLIENT_SECRET%" "%SOLR_QUERY_STRING%" "%OUTPUT_FILE%" 