
package ascelion.rest.bridge.tests;

import java.util.function.Function;

import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.ValidationException;
import javax.ws.rs.BadRequestException;

import ascelion.rest.bridge.tests.api.BeanData;
import ascelion.rest.bridge.tests.api.Validated;
import ascelion.rest.bridge.tests.arquillian.IgnoreWithProvider;
import ascelion.rest.bridge.tests.providers.JerseyProxyProvider;
import ascelion.rest.bridge.tests.providers.ResteasyProxyProvider;

import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ValidatedTest
extends AbstractTestCase<Validated>
{

	@Rule
	public ExpectedException ex = ExpectedException.none();

	public void bean_WithInvalid()
	{
		runTest( this.client::bean, new BeanData() );
	}

	@Test
	@IgnoreWithProvider( ResteasyProxyProvider.class )
	public void bean_WithNull()
	{
		runTest( this.client::bean, null );
	}

	@Test
	public void bean_WithValid()
	{
		runTest( this.client::bean, new BeanData( "value" ) );
	}

	@Test
	public void beanNotNull_WithInvalid()
	{
		runTest( this.client::beanNotNull, new BeanData() );
	}

	@Test
	@IgnoreWithProvider( ResteasyProxyProvider.class )
	public void beanNotNull_WithNull()
	{
		setUpValidationException();

		runTest( this.client::beanNotNull, null );
	}

	@Test
	public void beanNotNull_WithValid()
	{
		runTest( this.client::beanNotNull, new BeanData( "value" ) );
	}

	@Test
	public void beanValid_WithInvalid()
	{
		setUpValidationException();

		runTest( this.client::beanValid, new BeanData() );
	}

	@Test
	@IgnoreWithProvider( ResteasyProxyProvider.class )
	public void beanValid_WithNull()
	{
		runTest( this.client::beanValid, null );
	}

	@Test
	public void beanValid_WithValid()
	{
		runTest( this.client::beanValid, new BeanData( "value" ) );
	}

	@Test
	public void beanValidNotNull_WithInvalid()
	{
		setUpValidationException();

		runTest( this.client::beanValidNotNull, new BeanData() );
	}

	@Test
	@IgnoreWithProvider( ResteasyProxyProvider.class )
	public void beanValidNotNull_WithNull()
	{
		setUpValidationException();

		runTest( this.client::beanValidNotNull, null );
	}

	@Test
	public void beanValidNotNull_WithValid()
	{
		runTest( this.client::beanValidNotNull, new BeanData( "field-value" ) );
	}

	@Test
	public void notNullFormParam_WithNotNull()
	{
		runTest( this.client::notNullFormParam, "param-value" );
	}

	@Test
	@IgnoreWithProvider( value = JerseyProxyProvider.class, reason = "unable to get content-type" )
	@IgnoreWithProvider( value = ResteasyProxyProvider.class, reason = "unable to get content-type" )
	public void notNullFormParam_WithNull()
	{
		setUpValidationException();

		runTest( this.client::notNullFormParam, null );
	}

	@Test
	public void notNullHeaderParam_WithNotNull()
	{
		runTest( this.client::notNullHeaderParam, "value" );
	}

	@Test
	public void notNullHeaderParam_WithNull()
	{
		setUpValidationException();

		runTest( this.client::notNullHeaderParam, null );
	}

	@Test
	public void notNullQueryParam_WithNotNull()
	{
		runTest( this.client::notNullQueryParam, "value" );
	}

	@Test
	public void notNullQueryParam_WithNull()
	{
		setUpValidationException();

		runTest( this.client::notNullQueryParam, null );
	}

	private <T> void runTest( Function<T, T> func, T data )
	{
		assertEquals( data, func.apply( data ) );
	}

	private void setUpValidationException()
	{
		try {
			if( TestClientProvider.getInstance().hasClientValidation() ) {
				Validation.buildDefaultValidatorFactory().getValidator();

				this.ex.expect( ConstraintViolationException.class );

				return;
			}
		}
		catch( final ValidationException e ) {
		}

		this.ex.expect( BadRequestException.class );
	}
}
