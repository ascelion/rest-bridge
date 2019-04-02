# [Rest Bridge](https://github.com/ascelion/rest-bridge), a JAX-RS based proxy client

## Update 2019/04/02

- excluded duplicates from jar libs
- limit the logging of requests/response body
- dont close the JAX-RS Response when the return type is AutoClosable

## Update 2019/02/05

- added Spring Boot support

## Update 2019/02/01

- implemented Eclipse Microprofile Rest Client 1.2.0
- TCK tests passed - 100%

## Update 2019/01/18

- implemented Eclipse Microprofile Rest Client 1.2-m2
- TCK tests passed - 90%


## Modules

### Rest Client

This module provides a JAX-RS client proxy. To obtain a client proxy, one must first get an instance of **javax.ws.rs.client.Client**.
The client is then used to obtain a client proxy via RestClient class. Assuming an endpoint implementing the following interface

	@Path("hello")
	interface Hello
	{
		@GET
		String say(String name);
	}

... the proxy can be obtained with the following code

	URI target = URI.create( "http://api.example.com" );
	String appBase = "/rest/v1";
	Client client = ClientBuilder.build();

	// ... register client components
	client.register( MyFeature.class );

	RestClient restClient = RestClient.newRestClient( client, target, base );
	Hello hello = restClient.getInterface( Hello.class );
	
### Rest Client CDI extension

In a CDI environment the interface Hello can be simply injected:

	@Inject
	private Hello hello;

The **RestClient** must be provided by a CDI producer:

	@ApplicationScoped
	class RestClientFactory
	{
	
		@Produces
		RestClient create()
		{
			URI target = URI.create( "http://api.example.com" );
			String appBase = "/rest/v1";
			Client client = ClientBuilder.build();

			// ... register client components
			client.register( MyFeature.class );

			return RestClient.newRestClient( client, target, base );
		}
	}
	
### Rest Client Spring module

Similar to CDI, autowire the interface to your bean

	@Autowired
	private Hello hello;

... and create a factory for RestClient

	@Bean
	static RestClient createRestClient()
	{
			URI target = URI.create( "http://api.example.com" );
			String appBase = "/rest/v1";
			Client client = ClientBuilder.build();

			// ... register client components
			client.register( MyFeature.class );

			return RestClient.newRestClient( client, target, base );
	}

### Eclipse Microprofile

Rest Bridge also provides an implementation of Eclipse Microprofile Rest Client 1.2; to use the MP API add **rest-bridge-micro** as a dependency.

## Compatibility

Rest Bridge is built around JAX-RS and has been tested with the following implementations.
 - Apache CXF - http://cxf.apache.org
 - Eclipse Jersey - https://projects.eclipse.org/projects/ee4j.jersey
 - Resteasy - https://resteasy.github.io

## Examples

See https://github.com/ascelion/rest-bridge-demo for demos and examples (work in progress)

## Coordinates

Rest Bridge is published at JCenter under groupId **com.ascelion.rest-bridge**.

### Maven Usage

Add the following dependencies to your *pom.xml*.

	<dependency>
		<groupId>com.ascelion.rest-bridge</groupId>
		<artifactId>rest-bridge-client</artifactId>
		<version>[latest version]</version>
	</dependency>

To use the CDI extension, add

	<dependency>
		<groupId>com.ascelion.rest-bridge</groupId>
		<artifactId>rest-bridge-cdi</artifactId>
		<version>[latest version]</version>
	</dependency>

To use RestClient with SpringBoot (either 1 or 2), add

	<dependency>
		<groupId>com.ascelion.rest-bridge</groupId>
		<artifactId>rest-bridge-spring</artifactId>
		<version>[latest version]</version>
	</dependency>

To use the MP Rest Client, add

	<dependency>
		<groupId>com.ascelion.rest-bridge</groupId>
		<artifactId>rest-bridge-micro</artifactId>
		<version>[latest version]</version>
	</dependency>

### Gradle Usage

	repositories {
		jcenter()
	}
	
	dependencies {
		implementation 'com.ascelion.rest-bridge:rest-bridge-client:1.2.+'
		implementation 'com.ascelion.rest-bridge:rest-bridge-cdi:1.2.+'
		implementation 'com.ascelion.rest-bridge:rest-bridge-spring:1.2.+'
		implementation 'com.ascelion.rest-bridge:rest-bridge-micro:1.2.+'
	}
 
## Background

I started this project some years ago to facilitate the easy implementation of JAX-RS clients in a type-safe manner. Idea was to share
the interfaces between the client and the server and to build JAX-RS requests based on JAX-RS annotations.

At that time, no such framework existed, only RestEasy provided a proxy generator that used the endpoint interfaces to build the REST
invocations, but the implementation was incomplete and limited.

This is nowadays known as Eclipse Microprofile Rest Client; here is a JAX-RS based implementation.
