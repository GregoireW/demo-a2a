# Small Spring Boot Application using A2A 

## lib

This lib is used to integrate A2A with Spring Boot.

## agent-without-auth

Demo of the lib to demonstrate the creation of a simple A2A server.

## agent-with-auth

Demo of the lib to demonstrate the creation of a simple A2A server with authentication.

By default, authentication is done with the oidc mock server included. Launch `wiremock-dev.sh` to start it.

Here are the JWT tokens to use for testing:

| User   |         JWT Token          |
|--------|----------------------------|
| John Doe | eyJraWQiOiJiOWQ0OTBhYy1kMzA1LTQyYmUtOTlhZS1lMGI4MWYxYWUyOWEiLCJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJodHRwOi8vbG9jYWxob3N0Ojk5OTkiLCJzdWIiOiJKb2huIERvZSIsIm5iZiI6MTc2NzEyMTQ2MSwiZXhwIjo0MTAyNDQ0Nzk5LCJpYXQiOjE3NjcxMjE0NjEsImp0aSI6ImI0NWZhN2Y3LWMzMWItNDMzMS05NWI5LWU1NDI5M2VhMWE1NCJ9.NKNWvvWvZ9B9Hq3pOnuv7Xzimro3AbYocb6y_6LwFT9wcCJFoQN5O28LiPSgi7NnsKSKnyPlFM3QoCbbQIko5VQXytaCDjraYzS_CtAxLIH94-YFA-799iihYaEr_d3XBdPniBLuo5qyXM1DB_mA05EIHjkQWyLk7BEmFT0EDn6NwiEjAXR4GpDgevB_lGc4PTu036NqZTR1zVSCxEfUVUsgSWhBJAQ7NAEmNqPyALaAz_bVpJeXb6W5QAPyvDvHZYUzUP_X_pFvrUdq8LFIIukHaITQjzTMOyy654HTaut6PeDLe4wTmtdvA0szjfGREA_mhdEjGK3lRfE1AjG83w |
| Jane Roe | eyJraWQiOiJiOWQ0OTBhYy1kMzA1LTQyYmUtOTlhZS1lMGI4MWYxYWUyOWEiLCJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJodHRwOi8vbG9jYWxob3N0Ojk5OTkiLCJzdWIiOiJKYW5lIFJvZSIsIm5iZiI6MTc2NzEyMTQ2MSwiZXhwIjo0MTAyNDQ0Nzk5LCJpYXQiOjE3NjcxMjE0NjEsImp0aSI6IjQ0NGU5MzI4LWUxZTEtNGI2MC04ZGEyLTJmMzViYjQ1MGM1MSJ9.eFtvxF1Q5mDfFoU9ZJ-jKppF3Xde4w56u1KCQpbFPGjdrrtDY2E2ypcuvqEC4bFiNO_q0kUQRh26yeIJi8khtSpxQ-cvLGQra0DVMR-f3uv4OjV7HKrdMA3QqNRfKE5HZfg3Ax4qUSnu4ynGncXXfcVwrAy1cg6f_ypdbn8Al4zwQ5d4e9f9utYGY3Qk6HY3sxB4Sqw4DD5PKz1x4qH71uWZ-TRhb0zw6ymnhPxPVkx0gisPUaU-s8YJraENMSZzuxJroahrBGZBI9sszjxSfZUBgoCQUwxqRXAW9zno6TvZbASZ3cuqZGujJF7D_1dc3mbxQY11KHsugPce6ZoBUQ |

## Result

With a2a-inspector, you can get a visual representation of the interactions between services and for instance with the authenticated A2A server:
![A2A Inspector Screenshot](doc/img/demo.png)


## Note

### Client application

The client java application in the "client" folder is usable, but it is not entirely satisfying yet.

### History

There is a 'v0' tag showing the state of the project when the only option was to have a single agent.

Since then, the `@Agent` annotation was introduced to allow multiple agents in the same application (the agent class will be included in the Spring context automatically, but you have to implements the `AgentDefinition` interface to make it work).
With this version, you have to:
- Specify the base URL of the agent in the configuration (`a2a.agent.base-url` key) (default to `http://localhost:8080` if not set)
- If you set a `well-known-agent-name`, and if there is an agent with the same name ( `@Agent("your-agent-name")`)
  - the agent card will be served in the default a2a location: `/.well-known/agent-card.json`
  - the agent itself will be deployed on the `/agent` endpoint.
- All other agents will:
  - have their public card served at `/.well-known/agents/{agentName}.json` location 
  - will be served on the `/agents/{agentName}` endpoint.

Example:

```java
@Agent("hello-world")
public class HelloWorldAgent implements AgentDefinition {
    // implementation
}
```


```yaml
a2a:
  agent:
    base-url: http://localhost:8080
    well-known-agent-name: hello-world

```
