<h1>Using Spring Oauth2 to secure REST</h1>
This project is part of a tutorial about Oauth2 authentication on Spring.
You can read the material <a href="http://wp.me/p7gH7l-4U">here</a>.

<h3>Tutorial's Summary</h3>
<p>How to create from scratch a REST service with Spring Boot. We'll secure it using the Oauth2 protocol, using JSON Web Tokens, or JWT. There are several interesting materials scattered on the web, however, after studying a lot of then, I believe that the theme could be examined a little further. Instead of simply showing how to configure the server, I'll try to briefly explain why such configuration is necessary.</p>

<h4>To Build and Run</h4>
Go to the cloned directory and run <code>mvn spring-boot:run</code> or build with your chosen IDE.

<h4>Curl Commands</h4>
You should install <a href="https://stedolan.github.io/jq/">./JQ</a> before running these Curl commands.
<div>
<strong>To get a new token</strong> <br/>
<code>
curl trusted-app:secret@localhost:8080/oauth/token -d "grant_type=password&username=user&password=password" | jq
</code>

<br/>
<strong>To get a refresh token</strong><br/>
<code>
curl trusted-app:secret@localhost:8080/oauth/token -d "grant_type=refresh_token&jti=[JTI]&refresh_token=[REFRESH_TOKEN]" | jq
</code>

<br/>
<strong>To access a protected resource</strong><br/>
<code>
curl -H "Authorization: Bearer [ACCESS_TOKEN]" localhost:8080/api/hello
</code>
</div>