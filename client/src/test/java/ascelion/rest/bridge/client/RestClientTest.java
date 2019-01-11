
package ascelion.rest.bridge.client;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URI;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import javax.ws.rs.Consumes;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;

import ascelion.rest.bridge.etc.RestClientTrace;
import ascelion.rest.bridge.tests.api.SLF4JHandler;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
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

	static {
		SLF4JHandler.install();
	}

	@Rule
	public final WireMockRule rule = new WireMockRule();

	private Client client;
	private URI target;

	@SuppressWarnings( "rawtypes" )
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
			return "S" + ic.getArgument( 0 );
		} );
		when( this.conv.toString( any( LocalDate.class ) ) ).then( ic -> {
			return "D" + ic.getArgument( 0 );
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
	public void findStringConverter()
	{
		final ConvertersFactory cvsf = new ConvertersFactory( this.client );
		final Function<String, String> cv = cvsf.getConverter( String.class, new Annotation[0] );

		assertThat( cv, notNullValue() );
		assertThat( "SHIHI", is( equalTo( this.conv.toString( "HIHI" ) ) ) );
	}

	@Test
	public void findLocalDateConverter()
	{
		final ConvertersFactory cvsf = new ConvertersFactory( this.client );
		final Function<LocalDate, String> cv = cvsf.getConverter( LocalDate.class, new Annotation[0] );

		assertThat( cv, notNullValue() );
		assertThat( "SHIHI", is( equalTo( this.conv.toString( "HIHI" ) ) ) );
	}

	@Test
	public void dateFormat() throws Exception
	{
		final RestClient rc = new RestClient( this.client, this.target );
		final Interface ct = rc.getInterface( Interface.class );

		assertThat( ct, notNullValue() );

		final LocalDate now = LocalDate.now();

		stubFormat( now );

		assertThat( ct.format( now ), equalTo( now.toString() ) );

		verify( this.conv, times( 1 ) ).toString( any( LocalDate.class ) );
		verifyNoMoreInteractions( this.conv );
	}

	@Test
	public void dateFormatDefault() throws Exception
	{
		final RestClient rc = new RestClient( this.client, this.target );
		final Interface ct = rc.getInterface( Interface.class );

		assertThat( ct, notNullValue() );

		final LocalDate def = LocalDate.of( 1643, 1, 4 );

		stubFormat( def );

		assertThat( ct.format( null ), equalTo( def.toString() ) );

		verify( this.conv, times( 1 ) ).toString( any( String.class ) );
		verifyNoMoreInteractions( this.conv );
	}

	@Test
	public void parseDate() throws Exception
	{
		this.client.register( LocalDateBodyReader.class );

		final RestClient rc = new RestClient( this.client, this.target );
		final Interface ct = rc.getInterface( Interface.class );

		assertThat( ct, notNullValue() );

		final LocalDate now = LocalDate.now();

		stubParse( now );

		assertThat( ct.parse( now.toString() ), equalTo( now ) );

		verify( this.conv, times( 1 ) ).toString( any( String.class ) );
		verifyNoMoreInteractions( this.conv );
	}

	@Test
	public void parseDateDefault() throws Exception
	{
		this.client.register( LocalDateBodyReader.class );

		final RestClient rc = new RestClient( this.client, this.target );
		final Interface ct = rc.getInterface( Interface.class );

		assertThat( ct, notNullValue() );

		final LocalDate def = LocalDate.of( 1643, 1, 4 );

		stubParse( def );

		assertThat( ct.parse( null ), equalTo( def ) );

		verify( this.conv, times( 1 ) ).toString( any( String.class ) );
		verifyNoMoreInteractions( this.conv );
	}

	@Test
	public void changeURI()
	{
		final URI t1 = this.target.resolve( "t1" );
		final URI t2 = this.target.resolve( "t2" );
		final RestClient rc = new RestClient( this.client, t1 );

		this.rule.stubFor( any( urlPathEqualTo( "/t1/interface" ) )
			.willReturn(
				aResponse()
					.withBody( "t1" ) ) );
		this.rule.stubFor( any( urlPathEqualTo( "/t2/interface" ) )
			.willReturn(
				aResponse()
					.withBody( "t2" ) ) );

		final Interface api = rc.getInterface( Interface.class );

		assertThat( api.get(), equalTo( "t1" ) );

		rc.setTarget( t2 );

		assertThat( api.get(), equalTo( "t2" ) );
	}

	@Test( expected = ProcessingException.class )
	public void exception()
	{
		final IOException ex = new IOException( "thrown" );
		final ClientRequestFilter flt = cx -> {
			ex.fillInStackTrace();
			throw ex;
		};

		this.client.register( flt, Integer.MIN_VALUE );

		final RestClient rc = new RestClient( this.client, this.target );
		final Interface api = rc.getInterface( Interface.class );

		try {
			api.get();
		}
		catch( final ProcessingException e ) {
			assertThat( e.getCause(), sameInstance( ex ) );

			throw e;
		}
	}

	@Test( expected = RuntimeException.class )
	public void rtException()
	{
		final RuntimeException ex = new RuntimeException( "thrown" );
		final ClientRequestFilter flt = cx -> {
			ex.fillInStackTrace();
			throw ex;
		};

		this.client.register( flt, Integer.MIN_VALUE );

		final RestClient rc = new RestClient( this.client, this.target );
		final Interface api = rc.getInterface( Interface.class );

		try {
			api.get();
		}
		catch( final RuntimeException e ) {
			assertThat( e, sameInstance( ex ) );

			throw e;
		}
	}

	@Test( expected = ProcessingException.class )
	public void timeout()
	{
		final Client clt = ClientBuilder.newBuilder()
			.connectTimeout( 2, TimeUnit.SECONDS )
			.build();

		final RestClient rc = new RestClient( clt, URI.create( "http://ascelion.com:1234" ) );
		final Interface api = rc.getInterface( Interface.class );

		api.get();
	}

	@Test
	public void redirect()
	{
		final RestClient rc = new RestClient( this.client, this.target );
		final Interface ct = rc.getInterface( Interface.class );

		final String dest = UriBuilder.fromUri( this.target )
			.path( Interface.class )
			.path( "redirected" )
			.build()
			.toASCIIString();

		this.rule.stubFor( any( urlPathEqualTo( "/interface/redirect" ) )
			.willReturn(
				aResponse()
					.withStatus( 301 )
					.withHeader( "Location", dest ) ) );
		this.rule.stubFor( any( urlPathEqualTo( "/interface/redirected" ) )
			.willReturn(
				aResponse()
					.withBody( "redirected" )
					.withHeader( "content-type", "text/plain" ) ) );

		assertThat( ct.redirect(), equalTo( "redirected" ) );
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
