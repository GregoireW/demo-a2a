# A2A Client

This is the client application for the A2A (Application to Application) communication system. It allows users to connect to the A2A server and interact with other applications seamlessly.

## Test non-authenticated

You can use curl to test the client application. Here are some example commands:

Simple response test:

```bash
curl -X POST http://localhost:8081/chat -H "Content-Type: application/json" -d '{"message": "Hello, A2A!"}'
```

With tasks specifics  

```bash
curl -X POST http://localhost:8081/chat -H "Content-Type: application/json" -d '{"message": "Hello, A2A async !"}'
```

With slow response

```bash
curl -X POST http://localhost:8081/chat -H "Content-Type: application/json" -d '{"message": "Hello, A2A task !"}'
```
## Test with John Doe user

```bash
export token=eyJraWQiOiJiOWQ0OTBhYy1kMzA1LTQyYmUtOTlhZS1lMGI4MWYxYWUyOWEiLCJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJodHRwOi8vbG9jYWxob3N0Ojk5OTkiLCJzdWIiOiJKb2huIERvZSIsIm5iZiI6MTc2NzEyMTQ2MSwiZXhwIjo0MTAyNDQ0Nzk5LCJpYXQiOjE3NjcxMjE0NjEsImp0aSI6ImI0NWZhN2Y3LWMzMWItNDMzMS05NWI5LWU1NDI5M2VhMWE1NCJ9.NKNWvvWvZ9B9Hq3pOnuv7Xzimro3AbYocb6y_6LwFT9wcCJFoQN5O28LiPSgi7NnsKSKnyPlFM3QoCbbQIko5VQXytaCDjraYzS_CtAxLIH94-YFA-799iihYaEr_d3XBdPniBLuo5qyXM1DB_mA05EIHjkQWyLk7BEmFT0EDn6NwiEjAXR4GpDgevB_lGc4PTu036NqZTR1zVSCxEfUVUsgSWhBJAQ7NAEmNqPyALaAz_bVpJeXb6W5QAPyvDvHZYUzUP_X_pFvrUdq8LFIIukHaITQjzTMOyy654HTaut6PeDLe4wTmtdvA0szjfGREA_mhdEjGK3lRfE1AjG83w
curl -X POST -H "Authorization: Bearer $token" http://localhost:8081/chat -H "Content-Type: application/json" -d '{"message": "Hello, A2A!"}'
curl -X POST -H "Authorization: Bearer $token" http://localhost:8081/chat -H "Content-Type: application/json" -d '{"message": "Hello, A2A async !"}'
curl -X POST -H "Authorization: Bearer $token" http://localhost:8081/chat -H "Content-Type: application/json" -d '{"message": "Hello, A2A task !"}'
```

## Test with Jane Roe user

```bash
export token=eyJraWQiOiJiOWQ0OTBhYy1kMzA1LTQyYmUtOTlhZS1lMGI4MWYxYWUyOWEiLCJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJodHRwOi8vbG9jYWxob3N0Ojk5OTkiLCJzdWIiOiJKYW5lIFJvZSIsIm5iZiI6MTc2NzEyMTQ2MSwiZXhwIjo0MTAyNDQ0Nzk5LCJpYXQiOjE3NjcxMjE0NjEsImp0aSI6IjQ0NGU5MzI4LWUxZTEtNGI2MC04ZGEyLTJmMzViYjQ1MGM1MSJ9.eFtvxF1Q5mDfFoU9ZJ-jKppF3Xde4w56u1KCQpbFPGjdrrtDY2E2ypcuvqEC4bFiNO_q0kUQRh26yeIJi8khtSpxQ-cvLGQra0DVMR-f3uv4OjV7HKrdMA3QqNRfKE5HZfg3Ax4qUSnu4ynGncXXfcVwrAy1cg6f_ypdbn8Al4zwQ5d4e9f9utYGY3Qk6HY3sxB4Sqw4DD5PKz1x4qH71uWZ-TRhb0zw6ymnhPxPVkx0gisPUaU-s8YJraENMSZzuxJroahrBGZBI9sszjxSfZUBgoCQUwxqRXAW9zno6TvZbASZ3cuqZGujJF7D_1dc3mbxQY11KHsugPce6ZoBUQ
curl -X POST -H "Authorization: Bearer $token" http://localhost:8081/chat -H "Content-Type: application/json" -d '{"message": "Hello, A2A!"}'
curl -X POST -H "Authorization: Bearer $token" http://localhost:8081/chat -H "Content-Type: application/json" -d '{"message": "Hello, A2A async !"}'
curl -X POST -H "Authorization: Bearer $token" http://localhost:8081/chat -H "Content-Type: application/json" -d '{"message": "Hello, A2A task !"}'
```
