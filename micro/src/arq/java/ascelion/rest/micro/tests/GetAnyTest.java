
package ascelion.rest.micro.tests;

import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static java.util.Collections.singletonMap;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.eclipse.microprofile.rest.client.tck.WiremockArquillianTest;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.annotations.Test;

public class GetAnyTest extends WiremockArquillianTest
{

	@Path( "" )
	@RegisterRestClient
	public interface GetAny
	{

		@GET
		@Path( "map" )
		@Consumes( APPLICATION_JSON )
		Map<String, String> getMap();

		@GET
		@Path( "string" )
		String getString();
	}

	@Inject
	@RestClient
	private GetAny client;

	private final ObjectMapper om = new ObjectMapper();

	@Deployment
	public static WebArchive createDeployment()
	{
		final StringAsset mpConfig = new StringAsset( "*/mp-rest/url=" + getStringURL() );

		final JavaArchive jar = ShrinkWrap.create( JavaArchive.class, "GetMap.jar" )
			.addClasses( GetAnyTest.class, WiremockArquillianTest.class )
			.addAsManifestResource( mpConfig, "microprofile-config.properties" )
			.addAsManifestResource( EmptyAsset.INSTANCE, "beans.xml" );

		return ShrinkWrap.create( WebArchive.class, "GetMap.war" )
			.addAsLibraries( jar )
			.addAsWebInfResource( EmptyAsset.INSTANCE, "beans.xml" );
	}

	@Test
	public void getString()
	{
		final String body = "HIHIHIHI";

		stubFor( get( urlEqualTo( "/string" ) )
			.willReturn( aResponse()
				.withBody( body ) ) );

		assertThat( this.client.getString(), equalTo( body ) );
	}

	@Test
	public void getMap() throws JsonProcessingException
	{
		final Map<String, String> body = singletonMap( "key", "value" );

		stubFor( get( urlEqualTo( "/map" ) )
			.willReturn( aResponse()
				.withBody( this.om.writeValueAsString( body ) ) ) );

		final Map<String, String> map = this.client.getMap();

		assertThat( map, notNullValue() );
		assertThat( map.get( "key" ), equalTo( "value" ) );
	}
}
