package com.shareplaylearn;

import com.shareplaylearn.resources.AccessToken;

import static spark.Spark.get;
import static spark.Spark.post;

/**
 * Hello world!
 *
 */
public class AuthService
{
    public static void main( String[] args )
    {
        post( "/api/access_token", (req, res) -> AccessToken.handlePostAccessToken(req, res) );
        get( "/api/token_validation", (req, res) -> AccessToken.getAccessToken(req, res) );
    }
}
