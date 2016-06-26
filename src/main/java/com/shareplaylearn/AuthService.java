/**
 * Copyright 2016 Stuart Smith
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.shareplaylearn;

import com.shareplaylearn.resources.AccessToken;
import com.shareplaylearn.resources.OAuth;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import static spark.Spark.get;
import static spark.Spark.post;

/**
 * Hello world!
 *
 */
public class AuthService
{
    public static final CloseableHttpClient httpClient;
    //this is the location we return our user to, after receiving
    //the user id information from google
    public static final String OAUTH_REDIRECT_LOCATION;
    //This is the location of the callback that google invokes with the necessary information
    //However, it's just used to send to google
    public static final String OAUTH_CALLBACK_URL;
    //THis is the endpoint to query for the final user information, after google invokes our callback
    public static final String GOOGLE_TOKEN_ENDPOINT;

    static {
        //eventually, this should be read in via some sort of config file or service.
        int maxConnections = 10000;
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(maxConnections);
        connectionManager.setDefaultMaxPerRoute(maxConnections);
        httpClient = HttpClientBuilder.create().setConnectionManager(connectionManager).build();
        OAUTH_REDIRECT_LOCATION = "https://www.shareplaylearn.com/#/login_callback";
        OAUTH_CALLBACK_URL = "https://www.shareplaylearn.com/api/oauth2callback";
        GOOGLE_TOKEN_ENDPOINT = "https://accounts.google.com/o/oauth2/token";
    }

    public static void main( String[] args )
    {
        post( "/auth_api/access_token", (req, res) -> AccessToken.handlePostAccessToken(req, res) );
        get( "/auth_api/token_validation", (req, res) -> AccessToken.getTokenValidation(req, res) );
        get( "/auth_api/oauth2callback", (req,res) -> OAuth.GoogleOauthCallback(req,res) );
        get( "/auth_api/oauthToken", (req,res) -> OAuth.validateToken(req,res));
    }
}
