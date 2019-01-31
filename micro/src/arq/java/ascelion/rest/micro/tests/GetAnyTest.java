
package ascelion.rest.micro.tests;

import java.io.IOException;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.Response;

import static java.util.Collections.singletonMap;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

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
	@RegisterRestClient( baseUri = "http://localhost/" )
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

	static public class AbortResponse implements ClientRequestFilter
	{

		static Object body;

		@Override
		public void filter( ClientRequestContext ctx ) throws IOException
		{
			assertThat( body, notNullValue() );

			ctx.abortWith( Response.ok( body ).build() );
		}
	}

	@Inject
	@RestClient
	private GetAny client;

	@Deployment
	public static WebArchive createDeployment()
	{
		final StringAsset mpConfig = new StringAsset( GetAny.class.getName() + "/mp-rest/providers=" + AbortResponse.class.getName() );

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
		AbortResponse.body = "HIHIHIHI";

		final String body = this.client.getString();

		assertThat( body, equalTo( AbortResponse.body ) );
	}

	@Test
	public void getMap()
	{
		AbortResponse.body = singletonMap( "key", "value" );

		final Map<String, String> map = this.client.getMap();

		assertThat( map, notNullValue() );
		assertThat( map.get( "key" ), equalTo( "value" ) );
	}
}
