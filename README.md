# Rest Bridge, a REST client framework

I started this project some years ago to facitilitate the implementation of REST clients.

The client uses the API interfaces that are defined by the REST endpoints implementation.

At that time, no such framework existed, only RestEasy provided a proxy generator that used the REST interfaces to build the REST invocations.

Rest Bridge used from the beginning the JAX-RS annotations to build the invocations; you probably recognise what is nowadays called Rest Client for MicroProfile.

Rest Bridge doesn't cover yet the whole Eclipse Microprofile specification, this is something that will be hopefully done in the near future.

