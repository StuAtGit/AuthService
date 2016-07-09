/**
 * Copyright 2015-2016 Stuart Smith
 *
 * This file is part of an implementation of the Share,Play,Learn API
 *
 * The Share,Play,Learn API implementation is free software: you can redistribute it and/or modify
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

package com.shareplaylearn.resources;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.shareplaylearn.AuthService;
import com.shareplaylearn.TokenValidator;
import com.shareplaylearn.exceptions.Exceptions;
import com.shareplaylearn.models.TokenInfo;
import com.shareplaylearn.services.SecretsService;
import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static com.shareplaylearn.AuthService.*;
import static org.eclipse.jetty.http.HttpStatus.Code.*;

/**
 * Provides the callbacks and validation necessary for handling oauth tokens
 * Created by stu on 6/26/16.
 */
public class OAuth {

    private static final String ACCESS_TOKEN_FIELD = "access_token";
    private static final String ID_TOKEN_FIELD = "id_token";
    private static final String TOKEN_EXPIRY_FIELD = "expires_in";

    private static final Logger log = LoggerFactory.getLogger(OAuth.class);

    /**
     * Callback for Google Oauth. Google calls this endpoint with the information
     * needed to make yet another callback to google which returns the user token.
     * When that information is returned, this resource then redirects back to the
     * configured OAUTH_REDIRECT_LOCATION (set in the AuthService class)
     *
     * Sample returned URL:
     * http://www.shareplaylearn.com/SharePlayLearn2/api/oauth2callback?
     * state=insecure_test_token
     * &code=4/1Oxqgx2PRd8y4YxC7ByfJOLNiN-2.4hTyImQWEVMREnp6UAPFm0EEMmr5kAI
     * &authuser=0&num_sessions=1
     * &prompt=consent
     * &session_state=3dd372aa714b1b2313a838f8c4a4145b928da51f..8b83
     * @return an instance of java.lang.String
     */
    public static String GoogleOauthCallback(Request req, Response res) {
        String clientState = req.queryParams("state");
        String authCode = req.queryParams("code");
        String sessionState = req.queryParams("session_state");
        HttpPost tokenPost = new HttpPost(GOOGLE_TOKEN_ENDPOINT);
        List<NameValuePair> authArgs = new ArrayList<>();
        authArgs.add( new BasicNameValuePair("code",authCode) );
        authArgs.add( new BasicNameValuePair("client_id", SecretsService.googleClientId) );
        authArgs.add( new BasicNameValuePair("client_secret", SecretsService.googleClientSecret) );
        authArgs.add( new BasicNameValuePair("redirect_uri",
                OAUTH_CALLBACK_URL) );
        authArgs.add( new BasicNameValuePair("grant_type","authorization_code") );
        UrlEncodedFormEntity tokenRequestEntity = new UrlEncodedFormEntity(authArgs, Consts.UTF_8);
        tokenPost.setEntity(tokenRequestEntity);

        try( CloseableHttpResponse response = AuthService.httpClient.execute(tokenPost) ) {
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if( statusCode != 200 )
            {
                String loginStatus = "Login failed, google called us back with state token " + clientState;
                loginStatus += "and auth code " + authCode;
                loginStatus += ", but then we made a token request and got: " +
                        response.getStatusLine().getReasonPhrase() + "/" + response.getStatusLine().getStatusCode();
                res.status(500);
                res.body(loginStatus);
                return INTERNAL_SERVER_ERROR.toString();
            }
            String authJson = EntityUtils.toString(response.getEntity());
            /**
             * What google returns (as of 01/05/2014)
             * https://developers.google.com/accounts/docs/OAuth2Login
             * { "access_token" : "ya29.lQAIu_8j0WfvQrOT3ZCExMddengITNLFoBsioB63QN1zNiLMvcQ7wslG",
             *   "token_type" : "Bearer",
             *   "expires_in" : 3597,
             *   "id_token" : "eyJhbGciOiJSUzI1NiIsImtpZCI6IjljNjMxNDFjMzAzNjkyY2E3Y2Q4MDAxZTUxNmNhNDVhZDdlNTJiZTIifQ.eyJpc3MiOiJhY2NvdW50cy5nb29nbGUuY29tIiwic3ViIjoiMTEwODMxNjM0MzU1MjI2MzY0OTQwIiwiYXpwIjoiNzI2ODM3ODY1MzU3LXRxczIwdTZsdXFjOW9hdjFicDN2YjhuZGdhdmpucmtmLmFwcHMuZ29vZ2xldXNlcmNvbnRlbnQuY29tIiwiZW1haWwiOiJzdHUyNmNvZGVAZ21haWwuY29tIiwiYXRfaGFzaCI6Im12WnMzaXgtR2NHSVVETThuTW9TaWciLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiYXVkIjoiNzI2ODM3ODY1MzU3LXRxczIwdTZsdXFjOW9hdjFicDN2YjhuZGdhdmpucmtmLmFwcHMuZ29vZ2xldXNlcmNvbnRlbnQuY29tIiwiaWF0IjoxNDEyNTM3NjE0LCJleHAiOjE0MTI1NDE1MTR9.Zc6zsQPkj5an-1XFJLCdOvMDw_rDAqIe3YzA8g2DzhbGqBbd6XiqjxeRl3XDfC1aqp0Vx15fGn5R1e9RIh8Nmp6xWPvCHrA0c4eY8SaIazJ6FBQyK-n3k1sxQNJpuYhVtctAKsmlxFZbilwL2OTqIf0RDx0BpcIgmnk_7gupGxs"
             * }
             *
             * TODO: parse JWT for token and store in DB ?
             *       or just do it in JS? JS will need to confirm anyways..
             */
            JsonParser jsonParser = new JsonParser();
            JsonElement authInfo = jsonParser.parse(authJson);
            JsonObject authObject = authInfo.getAsJsonObject();
            String accessToken = authObject.get(ACCESS_TOKEN_FIELD).getAsString();
            String accessExpires = authObject.get(TOKEN_EXPIRY_FIELD).getAsString();
            String accessId = authObject.get(ID_TOKEN_FIELD).getAsString();
            String loggedInEndpoint = OAUTH_REDIRECT_LOCATION + "?client_state=" + clientState
                    + "&" + ACCESS_TOKEN_FIELD + "=" + accessToken + "&" + TOKEN_EXPIRY_FIELD +"=" + accessExpires + ""
                    + "&" + ID_TOKEN_FIELD + "=" + accessId;
            /**
             * ID token is a jws signed object.. we should verify this in the servlet, since we can't verify the client state "secret"
             * Note that this secret is not the same secret used in the jws
             */
            res.redirect(URI.create(loggedInEndpoint).toString(),303);
            res.body(authJson);
            return SEE_OTHER.toString();
        } catch( Exception e ) {
            res.body(e.getMessage());
            res.status(500);
            return INTERNAL_SERVER_ERROR.toString();
        }
    }

    public static String validateToken( Request req, Response res ) {
        String token = req.headers(AUTHENTICATION_HEADER);

        if( token == null || token.trim().equals("") ) {
            token = req.params(":token");
        }

        if( token == null ) {
            res.status(BAD_REQUEST.getCode());
            res.body("No access token found.");
            return BAD_REQUEST.toString();
        }
        token = token.trim();
        //allow for access tokens passed directly from header into this method
        //(that still have the Bearer prefix)
        if( !token.startsWith("Bearer ") ) {
            token = "Bearer " + token;
        }
        HttpGet tokenGet = new HttpGet("https://www.googleapis.com/plus/v1/people/me");
        tokenGet.addHeader("Authorization", token);
        try( CloseableHttpResponse response = httpClient.execute(tokenGet) ) {
            if( response.getStatusLine().getStatusCode() != 200 ) {
                log.info( "Access token: " + token + " failed: " + response.getStatusLine().getReasonPhrase() );
                String errorMessage = "";
                if( response.getEntity() != null ) {
                    errorMessage = EntityUtils.toString(response.getEntity());
                    log.info( errorMessage );
                }

                res.status(UNAUTHORIZED.getCode());
                res.body(errorMessage);
                return UNAUTHORIZED.toString();
            }
            res.status(200);
            if( response.getEntity() != null ) {
                return EntityUtils.toString(response.getEntity());
            } else {
                TokenInfo tokenInfo = new TokenInfo( token );
                Gson gson = new Gson();
                return gson.toJson(tokenInfo);
            }
        } catch (Exception e) {
            log.error(Exceptions.asString(e));
            res.status(INTERNAL_SERVER_ERROR.getCode());
            res.body(Exceptions.asString(e));
            return INTERNAL_SERVER_ERROR.toString() + " " + e.getMessage();
        }
    }
}
