##Infrastructure Update/API refactor
- Test Oauth implementation 
    - basic redirect works
    - get simple UI To parse token, etc
    - use with e2e test of FileService, pinging auth service
- Get UI ported over
- Mock unit tests for File & Auth Service
- Redis cache of S3 objects
- Backup shareplaylearn.com server, including secrets file and home directory
- Figure out how much of a clean-slate I should start with for that server
- Re-configure nginx, update TLS settings (session, ciphers, etc), and routes to services
- Port UI (Note: Need to fix license on that and RaspberryPi2 daemon - should be GPLv3 AND EPL, exception won't work)
##Start on next set of features!
    - Redis for image caching
    - Local User Identity:
        - Signing and Verification lib, each service gets it's own signing key, so it can verify locally
        (This can be done with Google JWTs, not just our own local ones).
        - save and store bcrypt user passwords, user identities