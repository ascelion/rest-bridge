
package ascelion.rest.bridge.client;

import java.lang.reflect.Method;

import javax.validation.ConstraintViolationException;

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
		runTest( "bean", null );
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

		runTest( "beanNotNull", null );
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
		runTest( "beanValid", null );
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

		runTest( "beanValidNotNull", null );
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

		runTest( "notNullFormParam", null );
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

		runTest( "notNullHeaderParam", null );
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

		runTest( "notNullQueryParam", null );
	}

	private Method findMethod( String methodName )
	{
		for( final Method m : Validated.class.getMethods() ) {
			if( m.getName().equals( methodName ) ) {
				return m;
			}
		}

		throw new NoSuchMethodError( methodName );
	}

	private void runTest( String methodName, Object object )
	{
		final Object[] arguments = new Object[] { object };
		final Method method = findMethod( methodName );
		final RestContext cx = new RestContext( this.client, method, arguments, null, null, null, null, null, null );
		final ValidationAction action = new ValidationAction( 0 );

		action.evaluate( arguments );
		action.execute( cx );
	}

	private void setUpValidationException()
	{
		this.ex.expect( ConstraintViolationException.class );
	}
}
