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

import com.shareplaylearn.AuthService;
import org.apache.commons.codec.binary.Base64;
import spark.Request;
import spark.Response;

import java.nio.charset.StandardCharsets;

/**
 * Created by stu on 5/30/16.
 */
public class AccessToken {

    /**
     * TODO: we're abandoning the idea of internal OAUTH flow (it 'twas a teensy shady), and
     * TODO: this will just be use for users that create their own accounts.
     * TODO: which means redis, JWT signing & validation utilities, user stores, bcrypt, etc.
     * @param req
     * @param res
     * @return
     */
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

        res.status(500);
        res.body("Not Implemented.");
        return "Internal Server Error";
    }

    public static String getTokenValidation(Request req, Response res) {
        String token = req.headers(AuthService.AUTHENTICATION_HEADER);
        if( token == null || token.trim().equals("") ) {
            token = req.params(":token");
        }
        res.status(500);
        res.body("Not Implemented.");
        return "Internal Server Error";
    }
}
