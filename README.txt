Steps below show you how to run the demo applications against HTRC Data API and HTRC Solr Proxy. The applications have been developed on JDK 1.6.

1.	Get your client id and client secret from [URL]. Data API is protected by Oauth2 authetication. You need client id and client secret to access the Data API.

2.	Download the demo archive from [URL].

3.	Unzip the archive.

4.	Edit your client id and client secret in OAUTH2_CLIENT_ID and OAUTH2_CLIENT_SECRET defined in "volumedemo.sh" or "pagedemo.sh" or "tokencount.sh" (look for the .bat files if you are in Windows). A sample setting is as follows.
OAUTH2_CLIENT_ID="YOUR_CLIENT_ID"
OAUTH2_CLIENT_SECRET="YOUR_CLIENT_SECRET"

5.	Run the demos by typing following command. Assume your command line terminal is under folder "HTRC-UnCamp2013-JavaApp".
> ./volumedemo.sh 
This demo sends Solr query string "publishDate:1884 AND author:Dickens" to Solr proxy to get a list of volume id, sends the volume id list to Data API to download contents of the volumes and finally saves the contents in a zip file whose name is provided through command line. 

> ./pagedemo.sh
This demo sends Solr query string "publishDate:1884 AND author:Dickens" to Solr proxy to get a list of volume id, append pape number after each volume id, sends the volume id list to Data API to download contents of the volumes and finally saves the contents in a zip file whose name is provided through command line. 

> ./tokencountdemo.sh  
This demo sends Solr query string "publishDate:1884 AND author:Dickens" to Solr proxy to get a list of volume id, sends the volume id list to Data API to get token count of the volumes and finally saves the results in a zip file whose name is provided through command line.

6.	View the results. Each demo app should create one zip file. 
volumedemo.sh creates a zip file called "volume.zip". It includes a number of folders. Each folder uses a volume id as its name and contains all the page contents as separated files. 
pagedemo.sh creates a zip file called "page.zip". It includes a number of folders. Each folder uses a volume id as its name and contains all the page contents as separated files. 
tokencountdemo.sh creates a zip file called "tokencount.zip". It includes a number of text files. Each text file uses a volume id as its name and has the token count for that volume. 



