
package ascelion.rest.bridge.client;

import java.lang.reflect.Method;
import java.util.stream.Stream;

import javax.validation.ConstraintViolationException;
import javax.ws.rs.client.WebTarget;

import ascelion.rest.bridge.tests.api.BeanData;
import ascelion.rest.bridge.tests.api.Validated;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith( MockitoJUnitRunner.class )
public class ValidationActionTest
{

	static {
//		asList( System.getProperty( "java.class.path", "" ).split( ":" ) ).forEach( System.out::println );
	}

	static private final Object NULL = null;

	@Mock
	private WebTarget target;
	@Mock
	private Validated client;

	@Rule
	public ExpectedException ex = ExpectedException.none();

	public void bean_WithInvalid()
	{
		runTest( "bean", new BeanData() );
	}

	@Test
	public void bean_WithNull()
	{
		runTest( "bean", NULL );
	}

	@Test
	public void bean_WithValid()
	{
		runTest( "bean", new BeanData( "value" ) );
	}

	@Test
	public void beanNotNull_WithInvalid()
	{
		runTest( "beanNotNull", new BeanData() );
	}

	@Test
	public void beanNotNull_WithNull()
	{
		setUpValidationException();

		runTest( "beanNotNull", NULL );
	}

	@Test
	public void beanNotNull_WithValid()
	{
		runTest( "beanNotNull", new BeanData( "value" ) );
	}

	@Test
	public void beanValid_WithInvalid()
	{
		setUpValidationException();

		runTest( "beanValid", new BeanData() );
	}

	@Test
	public void beanValid_WithNull()
	{
		runTest( "beanValid", NULL );
	}

	@Test
	public void beanValid_WithValid()
	{
		runTest( "beanValid", new BeanData( "value" ) );
	}

	@Test
	public void beanValidNotNull_WithInvalid()
	{
		setUpValidationException();

		runTest( "beanValidNotNull", new BeanData() );
	}

	@Test
	public void beanValidNotNull_WithNull()
	{
		setUpValidationException();

		runTest( "beanValidNotNull", NULL );
	}

	@Test
	public void beanValidNotNull_WithValid()
	{
		runTest( "beanValidNotNull", new BeanData( "value" ) );
	}

	@Test
	public void notNullFormParam_WithNotNull()
	{
		runTest( "notNullFormParam", "value" );
	}

	@Test
	public void notNullFormParam_WithNull()
	{
		setUpValidationException();

		runTest( "notNullFormParam", NULL );
	}

	@Test
	public void notNullHeaderParam_WithNotNull()
	{
		runTest( "notNullHeaderParam", "value" );
	}

	@Test
	public void notNullHeaderParam_WithNull()
	{
		setUpValidationException();

		runTest( "notNullHeaderParam", NULL );
	}

	@Test
	public void notNullQueryParam_WithNotNull()
	{
		runTest( "notNullQueryParam", "value" );
	}

	@Test
	public void notNullQueryParam_WithNull()
	{
		setUpValidationException();

		runTest( "notNullQueryParam", NULL );
	}

	private void setUpValidationException()
	{
		this.ex.expect( ConstraintViolationException.class );
	}

	private void runTest( String methodName, Object... arguments )
	{
		final Method method = findMethod( methodName );
		final RestRequest req = new RestRequest( this.client, "GET", this.target, Object.class, arguments );
		final ValidationAction action = new ValidationAction( method );

		action.execute( req );
	}

	private Method findMethod( String methodName )
	{
		return Stream.of( Validated.class.getMethods() )
			.filter( m -> methodName.equals( m.getName() ) )
			.findFirst()
			.orElseThrow( () -> new NoSuchMethodError( methodName ) );
	}
}
