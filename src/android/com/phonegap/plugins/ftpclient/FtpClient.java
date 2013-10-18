/*
* PhoneGap is available under *either* the terms of the modified BSD license *or* the
* MIT License (2008). See http://opensource.org/licenses/alphabetical for full text.
*
* Copyright (c) 2005-2010, Nitobi Software Inc.
* Copyright (c) 2010, IBM Corporation
*/

package com.phonegap.plugins.ftpclient;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.StringTokenizer;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.json.JSONArray;
import org.json.JSONException;

import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;

public class FtpClient extends CordovaPlugin {

    private static final String LOG_TAG = "FtpClient";

        /**
* Executes the request and returns PluginResult.
*
* @param action The action to execute.
* @param args JSONArry of arguments for the plugin.
* @param callbackId The callback id used when calling back into JavaScript.
* @return A PluginResult object with a status and message.
*/
        @Override
        public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
                PluginResult.Status status = PluginResult.Status.OK;
        JSONArray result = new JSONArray();

        try {
                String filename = args.getString(0);
                URL url = new URL(args.getString(1));
                
                
            if (action.equals("get")) {
				Log.d(LOG_TAG, "Start " + action);
                    get(filename, url);
				Log.d(LOG_TAG, "End " + action);
            }
            else if (action.equals("put")) {
				Log.d(LOG_TAG, "Start " + action);
                    put(filename, url);
				Log.d(LOG_TAG, "End " + action);
            }
            callbackContext.sendPluginResult(new PluginResult(status, result));
        } catch (JSONException e) {
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION));
        } catch (MalformedURLException e) {
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.MALFORMED_URL_EXCEPTION));
                } catch (IOException e) {
                 callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.IO_EXCEPTION));
                }
        return true;
        }

        /**
         * Uploads a file to a ftp server.
         * @param filename the name of the local file to send to the server
         * @param url the url of the server
         * @throws IOException
         */
        private void put(String filename, URL url) throws IOException {
                FTPClient f = setup(url);
                
                BufferedInputStream buffIn=null;
                buffIn=new BufferedInputStream(new FileInputStream(filename));
                f.storeFile(extractFileName(url), buffIn);
                buffIn.close();
                
                teardown(f);
        }

        /**
         * Downloads a file from a ftp server.
         * @param filename the name to store the file locally
         * @param url the url of the server
         * @throws IOException
         */
        private void get(String filename, URL url) throws IOException {
                FTPClient f = setup(url);  
                BufferedOutputStream buffOut=null;
                buffOut=new BufferedOutputStream(new FileOutputStream(filename));
                f.retrieveFile(extractFileName(url), buffOut);
                buffOut.flush();
                buffOut.close();           
                teardown(f);
        }

        /**
         * Tears down the FTP connection
         * @param f the FTPClient
         * @throws IOException
         */
        private void teardown(FTPClient f) throws IOException {
                f.logout();
                f.disconnect();
        }

        /**
         * Creates, connects and logs into a FTP server
         * @param url of the FTP server
         * @return an instance of FTPClient
         * @throws IOException
         */
        private FTPClient setup(URL url) throws IOException {
         FTPClient f = new FTPClient();
                f.connect(url.getHost(), extractPort(url));
                
                StringTokenizer tok = new StringTokenizer(url.getUserInfo(), ":");
                f.login(tok.nextToken(), tok.nextToken());
                
                f.enterLocalPassiveMode();
                f.setFileType(FTP.BINARY_FILE_TYPE);
                
                return f;
        }

        /**
         * Extracts the port of the FTP server. Returns 21 by default.
         * @param url
         * @return
         */
        private int extractPort(URL url) {
         if (url.getPort() == -1) {
                        return url.getDefaultPort();
                }
                else {
                        return url.getPort();
                }
        }

        /**
         * Extracts the file name from the URL.
         * @param url of the ftp server, includes the file to upload/download
         * @return the filename to upload/download
         */
        private String extractFileName(URL url) {
                String filename = url.getFile();
                if (filename.endsWith(";type=i") || filename.endsWith(";type=a")) {
                        filename = filename.substring(0, filename.length() - 7);                        
                }
                return filename;
        }

}