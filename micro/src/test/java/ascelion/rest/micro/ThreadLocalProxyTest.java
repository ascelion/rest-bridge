
package ascelion.rest.micro;

import java.util.List;

import javax.ws.rs.core.HttpHeaders;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;

public class ThreadLocalProxyTest
{

	@Test
	public void run()
	{
		final HttpHeaders h = mock( HttpHeaders.class );
		final HttpHeaders p1 = ThreadLocalProxy.create( HttpHeaders.class ).get();

		assertThat( p1, instanceOf( ThreadLocalValue.class ) );

		( (ThreadLocalValue) p1 ).set( h );

		p1.getAcceptableLanguages();
		p1.getAcceptableMediaTypes();
		p1.getCookies();
		p1.getDate();
		p1.getHeaderString( "" );
		p1.getLanguage();
		p1.getLength();
		p1.getMediaType();
		p1.getRequestHeader( "" );

		verify( h, times( 1 ) ).getAcceptableLanguages();
		verify( h, times( 1 ) ).getAcceptableMediaTypes();
		verify( h, times( 1 ) ).getCookies();
		verify( h, times( 1 ) ).getDate();
		verify( h, times( 1 ) ).getHeaderString( eq( "" ) );
		verify( h, times( 1 ) ).getLanguage();
		verify( h, times( 1 ) ).getLength();
		verify( h, times( 1 ) ).getMediaType();
		verify( h, times( 1 ) ).getRequestHeader( eq( "" ) );

		final HttpHeaders p2 = ThreadLocalProxy.create( HttpHeaders.class ).get();

		when( h.getRequestHeader( eq( "H" ) ) ).thenReturn( asList( "V" ) );

		final List<String> rh = p2.getRequestHeader( "H" );

		assertThat( rh, hasSize( 1 ) );
		assertThat( rh, contains( "V" ) );

		verify( h, times( 1 ) ).getRequestHeader( eq( "H" ) );
	}

}
