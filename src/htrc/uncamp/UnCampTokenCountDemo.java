/*
#
# Copyright 2013 The Trustees of Indiana University
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either expressed or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
*/

package htrc.uncamp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

public class UnCampTokenCountDemo {	
	
	// service end points 
	private static final String DATA_API_ENDPOINT = "https://sandbox.htrc.illinois.edu:25443/data-api";
	private static final String SOLR_PROXY_URL = "http://sandbox.htrc.illinois.edu:9994/solr/meta/select?q=";
	private static final String OAUTH2_ENDPOINT = "https://sandbox.htrc.illinois.edu:9443/oauth2endpoints/token?grant_type=client_credentials";
	
	// data api request url
	private static final String TOKENCOUNT_URL = "/tokencount";	
	
	// https connection and response stream
	private static HttpsURLConnection httpsURLConnection = null;
    private static InputStream inputStream = null;
	
	/**
	 * It sends a request to Solr Proxy and parse response into a volume id list.
	 * @param solrRequestStr a solr request string
	 * @return a list of volume id
	 * @throws Exception
	 */
	private static List<String> getVolumeIdFromSolrProxy(String solrRequestStr) throws Exception {
		int numFound = 0;
		List<String> volumeList = new ArrayList<String>();
		HttpURLConnection urlConnSolr = null;
		try {
			// send request to Solr
			String encodedSolrStr = URLEncoder.encode(solrRequestStr, "UTF-8");
			URL url = new URL(SOLR_PROXY_URL + encodedSolrStr);
			System.out.println("Sending request to Solr Proxy " + SOLR_PROXY_URL + encodedSolrStr);
			
			urlConnSolr = (HttpURLConnection)url.openConnection();
			if (urlConnSolr.getResponseCode() == 200) {
				
				// parse volume id from response
				XMLInputFactory inputFactory = XMLInputFactory.newInstance();
				InputStream inputStream = urlConnSolr.getInputStream();
				XMLStreamReader streamReader = inputFactory.createXMLStreamReader(new InputStreamReader(inputStream));
				while (streamReader.hasNext()) {
					streamReader.next();
				    if(streamReader.getEventType() == XMLStreamReader.START_ELEMENT){
				    	if (streamReader.getLocalName().equals("result")) {
				    		for (int i = 0; i < streamReader.getAttributeCount(); i++) {
				    			if (streamReader.getAttributeName(i).getLocalPart().equals("numFound")) {
				    				numFound = Integer.valueOf(streamReader.getAttributeValue(i));
				    				break;
				    			}
				    		}
				    	} else if (streamReader.getLocalName().equals("str")) {				    		
				    		for (int i = 0; i < streamReader.getAttributeCount(); i++) {
				    			if (streamReader.getAttributeName(i).getLocalPart().equals("name")) {
				    				if (streamReader.getAttributeValue(i).equals("id")) {
				    					streamReader.next();				    					
				    					volumeList.add(streamReader.getText());
					    				break;
				    				}
				    			}
				    		}
				    	}
				    }
				}
			} else {
				System.out.println("Response code: " + urlConnSolr.getResponseCode());
			}
		} finally {
			if (urlConnSolr != null) urlConnSolr.disconnect();
		}
		
		return volumeList;
	}
	
	
	/**
	 * It sends a request to Data API and returns a zip stream.
	 * @param dataapiEPR Data API end point
	 * @param requestUrl url request, e.g., /volumes,
	 * @param volumeIDs a list of volume id
	 * @param token OAuth2 token
	 * @param concat a boolean flag to indicate whether the pages will be concatenated or not
	 * @return
	 * @throws Exception
	 */
	private static InputStream getDataFromDataAPI(String dataapiEPR, String requestUrl, 
		List<String> volumeIDs, String token) throws Exception {
		URL url = null;
        URLConnection urlConnection = null;
        
        // building the request URL with query string, which is URL encoded to ensure correct passing of special characters.
        // volumeIDs are joined together with the | character
        StringBuilder urlStringBuilder = new StringBuilder(dataapiEPR);
        urlStringBuilder.append(requestUrl);
        
        StringBuilder bodyBuilder = new StringBuilder();
        bodyBuilder.append("volumeIDs=").append(URLEncoder.encode(volumeIDs.get(0), "UTF-8"));
        for (int i = 1; i < volumeIDs.size(); i++) {
            bodyBuilder.append(URLEncoder.encode("|", "UTF-8")).append(URLEncoder.encode(volumeIDs.get(i), "UTF-8"));
        }    
        
        ///////////////////////////////////////////////////////////////////////////////////
        //	You can add other data api parameters here by appending then to bodyBuilder	 //
        ///////////////////////////////////////////////////////////////////////////////////
        
        // instantiate a URL object from the URL string
        url = new URL(urlStringBuilder.toString());
        
        // open a connection to the URL and obtains a URLConnection object
        urlConnection = url.openConnection();
        
        // sanity check: to make sure the URLConnection object is an HttpsURLConnection object (communication via HTTPS)
        assert(urlConnection instanceof HttpsURLConnection);
        
        // typecast the generic URLConnection object to HttpsURLConnection
        httpsURLConnection = (HttpsURLConnection)urlConnection;

        // IMPORTANT : attached the obtained OAuth2 token as an HTTP request header "Authorization".  Per OAuth2
        // specification, the token must be prepended with "Bearer " (note the last space)
        httpsURLConnection.addRequestProperty("Authorization", "Bearer " + token);
        httpsURLConnection.addRequestProperty("Content-type", "application/x-www-form-urlencoded");
        
        // Request to Data API must be "POST"
        httpsURLConnection.setRequestMethod("POST");

        // must set DoOutput to true in order to write request body contents to output stream
        httpsURLConnection.setDoOutput(true);

        // write body content to output stream
        OutputStream outputStream = httpsURLConnection.getOutputStream();
        PrintWriter printWriter = new PrintWriter(outputStream);
        printWriter.write(bodyBuilder.toString());
        printWriter.flush();
        printWriter.close();
        
        int responseCode = httpsURLConnection.getResponseCode();
        
        // if the response status code is 200, which means OK, get the input stream and return it
        if (responseCode == 200) {
            inputStream = httpsURLConnection.getInputStream();
            
        } else {
            
            // for other status code, the response is in the error stream
            StringBuilder respBuilder = new StringBuilder();
            try {
                InputStream eStream = httpsURLConnection.getErrorStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(eStream));

                String line = null;
                do {
                    line = reader.readLine();
                    if (line != null) {
                        respBuilder.append(line);
                    }
                } while (line != null);
                reader.close();
            } catch (IOException e) {
                System.err.println("Unable to read response body");
            }
                
            System.out.println("Unable to get volumes");
            System.out.println("Response Code: " + responseCode);
            System.out.println("Response: " + respBuilder.toString());
            
            // raise exception on status other than 200
            throw new Exception("Failed to retrieve volume. Response code: " + responseCode + " Response body: " + respBuilder.toString());
        }
        
        // return the inputstream
        return inputStream;
	}
	
	/**
	 * It takes client id, client secret, and a Solr query string as input. Then it makes 
	 * request to Solor Proxy, extracts a list of volume id from response, authorizes with 
	 * OAuth2 server, obtains an OAuth2 token, requests for token count for the volumes from 
	 * Data API, and saves the token count result to a ZIP file.
	 */
	public static void main(String[] args) throws Exception {
		if (args.length < 4) {
			System.out.println("UnCampVolumeDemo <client id> <client secret> <solr query string> <zip file name>");
			System.exit(-1);
		}
		
		String clientId = args[0];
		String clientSecret = args[1];
		String solrRequestStr = args[2];
		String outfilename = args[3];
		
		// get volume id list from Solr Proxy
		List<String> volumeList = getVolumeIdFromSolrProxy(solrRequestStr);
		System.out.println("Got volume id list: " + volumeList.toString());
		if (volumeList.size() == 0) {
			System.out.println("No volume id is returned from Solr Proxy. Change your Solr query string.");
			System.exit(-1);
		}
		
		// get token from OAuth2 authetication
		String token = HTRCUtils.getToken(OAUTH2_ENDPOINT, clientId, clientSecret);
		System.out.println("Obtained token: " + token);
		
		// send data api request
		try {
			System.out.println("Sending request to Data API");
			InputStream responseStream = getDataFromDataAPI(DATA_API_ENDPOINT, TOKENCOUNT_URL, volumeList, token);
			
			// write response stream to zip file
			System.out.println("Writing response to zip file " + outfilename);
			HTRCUtils.writeZipFile(outfilename, responseStream);
		} finally {
			// close stream and connection
			if (inputStream != null) inputStream.close();
			if (httpsURLConnection != null) httpsURLConnection.disconnect();
		}
	}
}
