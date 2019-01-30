
package ascelion.rest.micro.tests;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import javax.inject.Inject;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.json.bind.JsonbException;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static java.lang.String.format;
import static org.apache.commons.lang3.reflect.FieldUtils.writeDeclaredField;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.eclipse.microprofile.rest.client.tck.WiremockArquillianTest;
import org.eclipse.microprofile.rest.client.tck.interfaces.JsonBClient;
import org.eclipse.microprofile.rest.client.tck.interfaces.MyJsonBObject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.SkipException;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class JsonBProviderTest extends WiremockArquillianTest
{

	static private final String DATE_FMT = "%1$tY-%1$tm-%1$td";

	@Deployment
	public static WebArchive createDeployment()
	{
		final StringAsset mpConfig = new StringAsset( JsonBClient.class.getName() + "/mp-rest/uri=" + getStringURL() );
		return ShrinkWrap.create( WebArchive.class, JsonBProviderTest.class.getSimpleName() + ".war" )
			.addClasses( JsonBClient.class, WiremockArquillianTest.class, MyJsonBObject.class, JsonBProviderTest.class )
			.addAsWebInfResource( EmptyAsset.INSTANCE, "beans.xml" )
			.addAsWebInfResource( mpConfig, "classes/META-INF/microprofile-config.properties" );
	}

	@RestClient
	@Inject
	private JsonBClient jsonBClient;

	@BeforeTest
	public void checkJsonB()
	{
		try {
			JsonbBuilder.create();
		}
		catch( final JsonbException t ) {
			throw new SkipException( "Skipping since JSON-B APIs were not found." );
		}
	}

	@Test
	public void cdiJsonBClient() throws IllegalAccessException
	{
		runTest();
	}

	@Test
	public void builtJsonBClient() throws IllegalAccessException
	{
		this.jsonBClient = RestClientBuilder.newBuilder()
			.baseUri( getServerURI() )
			.build( JsonBClient.class );

		runTest();
	}

	private void runTest() throws IllegalAccessException
	{
		final MyJsonBObject o1 = new MyJsonBObject();

		o1.setQty( 10 );

		final Instant now = LocalDate.now().atStartOfDay( ZoneId.of( "UTC" ) ).toInstant();

		writeDeclaredField( o1, "date", Date.from( now ), true );
		writeDeclaredField( o1, "name", "name", true );

		final JsonbConfig cf = new JsonbConfig();

		cf.withNullValues( true );
		cf.withFormatting( true );

		final String s = JsonbBuilder.create( cf ).toJson( o1 );

		stubFor( get( urlEqualTo( "/myObject" ) )
			.willReturn( aResponse()
				.withHeader( "Content-Type", "application/json" )
				.withBody( s ) ) );

		final MyJsonBObject o2 = this.jsonBClient.get( "myObject" );

		assertThat( o2.getName(), equalTo( o1.getName() ) );
		assertThat( o2.getQty(), equalTo( o1.getQty() ) );
		assertThat( format( DATE_FMT, o2.getDate() ), equalTo( format( DATE_FMT, o1.getDate() ) ) );
	}
}
