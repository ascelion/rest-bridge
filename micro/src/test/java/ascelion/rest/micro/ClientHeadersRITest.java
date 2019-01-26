
package ascelion.rest.micro;

import java.lang.reflect.Method;
import java.util.stream.Stream;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import ascelion.rest.bridge.client.RestRequestContext;

import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.tck.interfaces.ClientHeaderParamClient;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

@RunWith( Parameterized.class )
public class ClientHeadersRITest
{

	@Parameterized.Parameters( name = "{1}" )
	static public Object data()
	{
		return Stream.of( ClientHeaderParamClient.class.getMethods() )
			.filter( m -> !m.isDefault() )
			.map( m -> new Object[] { m, m.getName() } )
			.toArray();
	}

	@Rule
	public MockitoRule rule = MockitoJUnit.rule();
	public Method method;

	private final MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();

	@Mock( answer = Answers.CALLS_REAL_METHODS )
	private ClientHeaderParamClient client;
	@Mock
	private RestRequestContext rc;
	private ClientHeadersRI chri;

	public ClientHeadersRITest( Method method, String name )
	{
		this.method = method;
	}

	@Before
	public void setUp()
	{
		when( this.rc.getJavaMethod() ).thenReturn( this.method );
		when( this.rc.getHeaders() ).thenReturn( this.headers );
		when( this.rc.getImplementation() ).thenReturn( this.client );
		when( this.rc.getInterfaceType() ).thenReturn( (Class) ClientHeaderParamClient.class );

		this.chri = new ClientHeadersRI( this.rc.getInterfaceType(), this.rc.getJavaMethod() );
	}

	@Test
	public void run()
	{
		try {
			this.chri.before( this.rc );

			// verify class headers
			Stream.of( ClientHeaderParamClient.class.getAnnotationsByType( ClientHeaderParam.class ) )
				.forEach( a -> {
					if( a.required() ) {
						assertThat( this.headers, hasKey( a.name() ) );
					}
				} );
			;
		}
		catch( final Exception e ) {

		}
	}

}
