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
