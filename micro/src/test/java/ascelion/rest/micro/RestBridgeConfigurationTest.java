
package ascelion.rest.micro;

import java.io.IOException;
import java.net.URI;
import java.util.Formatter;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.WriterInterceptor;

import ascelion.rest.bridge.client.ConfigurationEx;
import ascelion.rest.bridge.client.Prioritised;
import ascelion.rest.bridge.tests.api.API;
import ascelion.rest.bridge.tests.api.SLF4JHandler;
import ascelion.utils.etc.Log;
import ascelion.utils.jaxrs.RestClientTrace;

import static org.apache.commons.lang3.reflect.FieldUtils.readField;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.testng.Assert.assertEquals;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import lombok.SneakyThrows;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;
import org.eclipse.microprofile.rest.client.tck.interfaces.InterfaceWithProvidersDefined;
import org.eclipse.microprofile.rest.client.tck.providers.TestMessageBodyReader;
import org.eclipse.microprofile.rest.client.tck.providers.TestReaderInterceptor;
import org.eclipse.microprofile.rest.client.tck.providers.TestWriterInterceptor;
import org.junit.Rule;
import org.junit.Test;

public class RestBridgeConfigurationTest
{

	static class LocalFilter implements ClientRequestFilter
	{

		static Configuration cf;

		@Override
		public void filter( ClientRequestContext rc ) throws IOException
		{
			cf = rc.getConfiguration();
			rc.abortWith( Response.ok().build() );
		}
	}

	static class LocalRXM implements ResponseExceptionMapper<WebApplicationException>
	{

		@Override
		public WebApplicationException toThrowable( Response response )
		{
			return null;
		}
	}

	static {
		SLF4JHandler.install();
	}

	static private final Log L = Log.get();

	static List<Prioritised<MessageBodyReader>> getReaders( String tx, Configuration cf )
	{
		final List<Prioritised<MessageBodyReader>> rds = ConfigurationEx.providers( cf, MessageBodyReader.class );
		try( final Formatter fmt = new Formatter() ) {
			fmt.format( "\n%s\n", tx );

			rds.forEach( t -> fmt.format( "    %s\n", t ) );

			L.info( fmt.toString() );
		}

		return rds;
	}

	@Rule
	public final WireMockRule rule = new WireMockRule( API.reservePort() );

	@Test
	public void clientRegistration()
	{
		final RestClientBuilder rbb = RestClientBuilder.newBuilder();

		rbb.baseUri( URI.create( this.rule.baseUrl() ) );

		rbb.register( RestClientTrace.class, 8000 );
		rbb.register( new LocalFilter(), Integer.MAX_VALUE );

		final InterfaceWithProvidersDefined clt = rbb.build( InterfaceWithProvidersDefined.class );

		clt.executePost( "" );

		final List<Prioritised<MessageBodyReader>> rd1 = getReaders( "RBB", rbb.getConfiguration() );
		final List<Prioritised<MessageBodyReader>> rd2 = getReaders( "RD2", LocalFilter.cf );

		L.info( "RD2" );
		rd2.forEach( t -> L.info( "%s", t ) );

		assertEquals( rd1.size() + 1, rd2.size() );
	}

	@Test
	public void keepOrderOnClone()
	{
		final RestBridgeConfiguration cf1 = new RestBridgeConfiguration();

		new DefaultProviders().configure( new RestBridgeFeatureContext( cf1 ) );

		cf1.register( RestClientTrace.class, 8000 );
		cf1.register( new LocalFilter(), Integer.MAX_VALUE );
		cf1.register( new LocalRXM() );
		cf1.register( TestMessageBodyReader.class );

		assertThat( cf1.getClasses(), hasSize( 0 ) );
		assertThat( cf1.getInstances(), hasSize( 15 ) );

		final RestBridgeConfiguration cf2 = cf1.clone( false );
		final RestBridgeConfiguration cf3 = cf2.clone( true );

		assertThat( cf2.isRegistered( MBRWInterceptor.class ), is( true ) );
		assertThat( cf3.isRegistered( MBRWInterceptor.class ), is( true ) );

		assertThat( cf2.getClasses(), hasSize( 0 ) );
		assertThat( cf2.getInstances(), hasSize( cf1.getInstances().size() ) );

		assertThat( cf3.getClasses(), hasSize( 0 ) );
		assertThat( cf3.getInstances(), hasSize( cf1.getInstances().size() - 1 ) );

		final List<Prioritised<MessageBodyReader>> rds1 = getReaders( "RDS1", cf1 );
		final List<Prioritised<MessageBodyReader>> rds2 = getReaders( "RDS2", cf2 );
		final List<Prioritised<MessageBodyReader>> rds3 = getReaders( "RDS3", cf3 );

		assertThat( rds2, hasSize( rds1.size() ) );
		assertThat( rds3, hasSize( rds2.size() ) );

		for( int k = 0, z = rds1.size(); k < z; k++ ) {
			assertThat( rds2.get( k ).getInstance(), sameInstance( rds1.get( k ).getInstance() ) );
			assertThat( rds3.get( k ).getInstance(), sameInstance( rds2.get( k ).getInstance() ) );
		}
	}

	@Test
	public void lastIsMBRW()
	{
		final RestBridgeConfiguration rbc = new RestBridgeConfiguration();

		rbc.register( TestReaderInterceptor.class, Integer.MAX_VALUE );
		rbc.register( TestWriterInterceptor.class, Integer.MAX_VALUE );

		checkMBRW( rbc );
		checkMBRW( rbc.clone( false ) );
		checkMBRW( rbc.clone( true ) );
	}

	@SneakyThrows
	private void checkMBRW( RestBridgeConfiguration rbc )
	{
		final List<Prioritised<ReaderInterceptor>> readers = rbc.providers( ReaderInterceptor.class );

		assertThat( readers, hasSize( 2 ) );
		assertThat( readers.get( 0 ).getInstance(), instanceOf( TestReaderInterceptor.class ) );
		assertThat( readers.get( 1 ).getInstance(), instanceOf( MBRWInterceptor.class ) );

		final List<Prioritised<WriterInterceptor>> writers = rbc.providers( WriterInterceptor.class );

		assertThat( writers, hasSize( 2 ) );
		assertThat( writers.get( 0 ).getInstance(), instanceOf( TestWriterInterceptor.class ) );
		assertThat( writers.get( 1 ).getInstance(), instanceOf( MBRWInterceptor.class ) );

		final Object thatRBC = readField( writers.get( 1 ).getInstance(), "rbc", true );

		assertThat( thatRBC, sameInstance( rbc ) );
	}
}
