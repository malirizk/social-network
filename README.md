# social-network

## Posting
### URL : POST /posts
A user should be able to post a 140 character message.

## Wall
### URL : GET /users/{userId}/wall
A user should be able to see a list of the messages they've posted, in reverse chronological order.

## Following
### URL : POST /users/{userId}/follow
A user should be able to follow another user. Following doesn't have to be reciprocal: Alice can follow Bob without Bob having to follow Alice.

## Timeline
### URL : GET /users/{userId}/timeline
A user should be able to see a list of the messages posted by all the people they follow, in reverse chronological order.

## Start Application

To start the application, we need to run the following command :
```mvn clean package spring-boot:run```

## Documentation

**Documentation can be found in target\generated-docs**