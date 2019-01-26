//
//package ascelion.rest.bridge.client;
//
//import java.lang.reflect.Method;
//
//import static ascelion.rest.bridge.client.RestClientProperties.NO_ASYNC_INTERCEPTOR;
//import static ascelion.rest.bridge.client.RestClientProperties.NO_REQUEST_INTERCEPTOR;
//import static ascelion.rest.bridge.client.RestClientProperties.NO_RESPONSE_HANDLER;
//import static org.hamcrest.Matchers.containsString;
//import static org.hamcrest.Matchers.not;
//import static org.hamcrest.Matchers.sameInstance;
//import static org.junit.Assert.assertThat;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.junit.MockitoJUnitRunner;
//
//@RunWith( MockitoJUnitRunner.class )
//public class RestMethodTest
//{
//
//	private final MockClient mc = new MockClient();
//	private ConvertersFactoryOLD cvsf;
//
//	@Before
//	public void setUp()
//	{
//		this.cvsf = new ConvertersFactoryOLD( this.mc.client );
//	}
//
//	@Test
//	public void missingPaths() throws NoSuchMethodException, SecurityException
//	{
//		final RestClientData rcd = new RestClientData(	InterfaceWithMissingPath.class, this.mc.configuration,
//														this.cvsf, null, NO_REQUEST_INTERCEPTOR, NO_RESPONSE_HANDLER, null,
//														NO_ASYNC_INTERCEPTOR, () -> this.mc.methodTarget );
//
//		final Method met = InterfaceWithMissingPath.class.getMethod( "get", String.class );
//
//		try {
//			new RestMethod( rcd, met );
//		}
//		catch( final RestClientMethodException e ) {
//			System.out.println( e.getMessage() );
//
//			assertThat( e.getMethod(), sameInstance( met ) );
//			assertThat( e.getMessage(), containsString( "{path1}" ) );
//			assertThat( e.getMessage(), not( containsString( "{path2}" ) ) );
//			assertThat( e.getMessage(), containsString( "{path3}" ) );
//		}
//	}
//
//}
