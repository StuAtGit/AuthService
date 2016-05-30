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
