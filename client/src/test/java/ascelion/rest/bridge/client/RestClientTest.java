
package ascelion.rest.bridge.client;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URI;
import java.time.LocalDate;
import java.util.function.Function;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;

import ascelion.rest.bridge.tests.api.util.RestClientTrace;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith( MockitoJUnitRunner.class )
public class RestClientTest
{

	@Rule
	public final WireMockRule rule = new WireMockRule();

	private Client client;
	private URI target;

	@Mock( lenient = true )
	private ParamConverter conv;
	private PCP prov;

	@Before
	public void setUp()
	{
		this.client = ClientBuilder.newClient();
		this.target = URI.create( this.rule.baseUrl() );

		this.client.register( RestClientTrace.class );

		when( this.conv.toString( any( String.class ) ) ).then( ic -> {
			return "X" + ic.getArgument( 0 );
		} );
		when( this.conv.toString( any() ) ).then( ic -> {
			return "X" + ic.getArgument( 0 );
		} );

		this.prov = new PCP( this.conv );

		this.client.register( this.prov );
	}

	@Provider
	@RequiredArgsConstructor
	static class PCP implements ParamConverterProvider
	{

		private final ParamConverter<?> conv;

		@Override
		public <T> ParamConverter<T> getConverter( Class<T> rawType, Type genericType, Annotation[] annotations )
		{
			return (ParamConverter<T>) this.conv;
		}
	}

	@Provider
	@Consumes( MediaType.TEXT_PLAIN )
	static class LocalDateBodyReader implements MessageBodyReader<LocalDate>
	{

		@Override
		public boolean isReadable( Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType )
		{
			return type == LocalDate.class;
		}

		@Override
		public LocalDate readFrom( Class<LocalDate> type, Type genericType, Annotation[] annotations, MediaType mt, MultivaluedMap<String, String> headers, InputStream is ) throws IOException, WebApplicationException
		{
			return LocalDate.parse( IOUtils.toString( is, "ISO-8859-1" ) );
		}
	}

	@Test
	public void create()
	{
		final RestClient rc = new RestClient( this.client, this.target );
		final Interface ct = rc.getInterface( Interface.class );

		assertThat( ct, notNullValue() );

		this.rule.stubFor( any( urlPathEqualTo( "/interface" ) )
			.willReturn(
				aResponse()
					.withBody( "HIHI" )
					.withHeader( "content-type", "text/plain" ) ) );

		assertThat( ct.get(), equalTo( "HIHI" ) );
	}

	@Test
	public void findConverter()
	{
		final ConvertersFactory cvsf = new ConvertersFactory( this.client.getConfiguration() );
		final Function<Object, String> cv = cvsf.getConverter( String.class, new Annotation[0] );

		assertThat( cv, notNullValue() );
		assertThat( "XHIHI", is( equalTo( this.conv.toString( "HIHI" ) ) ) );
	}

	@Test
	public void dateFormat() throws NoSuchMethodException, SecurityException, IllegalArgumentException, IllegalAccessException
	{
		final RestClient rc = new RestClient( this.client, this.target );
		final Interface ct = rc.getInterface( Interface.class );

		assertThat( ct, notNullValue() );

		final LocalDate now = LocalDate.now();

		stubFormat( now );

		assertThat( ct.format( now ), equalTo( now.toString() ) );

		verify( this.conv, times( 1 ) ).toString( any() );
		verify( this.conv, times( 0 ) ).toString( any( String.class ) );
	}

	@Test
	public void dateFormatDefault() throws NoSuchMethodException, SecurityException, IllegalArgumentException, IllegalAccessException
	{
		final RestClient rc = new RestClient( this.client, this.target );
		final Interface ct = rc.getInterface( Interface.class );

		assertThat( ct, notNullValue() );

		final LocalDate def = LocalDate.of( 1643, 1, 4 );

		stubFormat( def );

		assertThat( ct.format( null ), equalTo( def.toString() ) );

		verify( this.conv, times( 0 ) ).toString( any() );
		verify( this.conv, times( 0 ) ).toString( any( String.class ) );
	}

	@Test
	public void parseFormat() throws NoSuchMethodException, SecurityException, IllegalArgumentException, IllegalAccessException
	{
		this.client.register( LocalDateBodyReader.class );

		final RestClient rc = new RestClient( this.client, this.target );
		final Interface ct = rc.getInterface( Interface.class );

		assertThat( ct, notNullValue() );

		final LocalDate now = LocalDate.now();

		stubParse( now );

		assertThat( ct.parse( now.toString() ), equalTo( now ) );

		verify( this.conv, times( 0 ) ).toString( any() );
		verify( this.conv, times( 0 ) ).toString( any( String.class ) );
	}

	@Test
	public void parseFormatDefault() throws NoSuchMethodException, SecurityException, IllegalArgumentException, IllegalAccessException
	{
		this.client.register( LocalDateBodyReader.class );

		final RestClient rc = new RestClient( this.client, this.target );
		final Interface ct = rc.getInterface( Interface.class );

		assertThat( ct, notNullValue() );

		final LocalDate def = LocalDate.of( 1643, 1, 4 );

		stubParse( def );

		assertThat( ct.parse( null ), equalTo( def ) );

		verify( this.conv, times( 0 ) ).toString( any() );
		verify( this.conv, times( 0 ) ).toString( any( String.class ) );
	}

	private void stubParse( final LocalDate now )
	{
		this.rule.stubFor( any( urlPathEqualTo( "/interface/parse" ) )
			.willReturn(
				aResponse()
					.withBody( now.toString() )
					.withHeader( "content-type", "text/plain" ) ) );
	}

	private void stubFormat( final LocalDate now )
	{
		this.rule.stubFor( any( urlPathEqualTo( "/interface/format" ) )
			.willReturn(
				aResponse()
					.withBody( now.toString() )
					.withHeader( "content-type", "text/plain" ) ) );
	}
}
