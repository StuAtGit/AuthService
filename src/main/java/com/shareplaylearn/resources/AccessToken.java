package com.shareplaylearn.resources;

import org.apache.commons.codec.binary.Base64;
import spark.Request;
import spark.Response;

import java.nio.charset.StandardCharsets;

/**
 * Created by stu on 5/30/16.
 */
public class AccessToken {

    public static String handlePostAccessToken(Request req, Response res) {
        String credentialHeader = req.headers("Authorization");
        if( credentialHeader == null ) {
            res.body("No credentials provided.");
            res.status(400);
            return "Bad Request";
        }
        //TODO: fuzz this to see what happens with bad strings
        byte[] decodedCredentials = Base64.decodeBase64(credentialHeader.trim());
        if( decodedCredentials == null ) {
            res.body("Invalid credentials provided.");
            res.status(400);
            return "Bad Request";
        }
        String credentialsString = new String( decodedCredentials, StandardCharsets.UTF_8 );
        String[] credentials = credentialsString.split(":");
        if( credentials.length != 3 ) {
            res.body("Invalid credentials provided.");
            res.status(400);
            return "Bad Request";
        }
        String realm = credentials[0];
        String username = credentials[1];
        String password = credentials[2];

        res.status(200);
        res.body("dummmy_token");
        return "OK";
    }

    public static String getAccessToken(Request req, Response res) {
        res.status(500);
        res.body("Not Implemented.");
        return "Internal Server Error";
    }
}
