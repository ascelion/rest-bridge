# [Rest Bridge](https://github.com/pa314159/rest-bridge), a JAX-RS based proxy client

## Update 2019/02/01

- implemented Eclipse Microprofile Rest Client 1.2.0
- TCK tests passed - 100%

## Update 2019/01/18

- implemented Eclipse Microprofile Rest Client 1.2-m2
- TCK tests passed - 90%

## About

I started this project some years ago to facilitate the easy implementation of JAX-RS clients. Idea was to share
the interfaces between the client and the server and to build JAX-RS requests based on JAX-RS annotations.

At that time, no such framework existed, only RestEasy provided a proxy generator that used the endpoint interfaces to build the REST invocations, but the implementation was incomplete and limited.

## Compatibility

Rest Bridge is built around JAX-RS and has been tested with the following implementations.
 - Apache CXF - http://cxf.apache.org
 - Eclipse Jersey - https://projects.eclipse.org/projects/ee4j.jersey
 - Resteasy - https://resteasy.github.io


