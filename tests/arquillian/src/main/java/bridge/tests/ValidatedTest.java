
package bridge.tests;

import java.util.function.Function;

import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.ValidationException;
import javax.ws.rs.BadRequestException;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import bridge.tests.arquillian.IgnoreWith;
import bridge.tests.providers.RestBridgeProvider;
import bridge.tests.providers.ResteasyProvider;

import ascelion.rest.bridge.web.BeanData;
import ascelion.rest.bridge.web.Validated;

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
	@IgnoreWith( ResteasyProvider.class )
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
	@IgnoreWith( ResteasyProvider.class )
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
	@IgnoreWith( ResteasyProvider.class )
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
	@IgnoreWith( ResteasyProvider.class )
	public void beanValidNotNull_WithNull()
	{
		setUpValidationException();

		runTest( this.client::beanValidNotNull, null );
	}

	@Test
	public void beanValidNotNull_WithValid()
	{
		runTest( this.client::beanValidNotNull, new BeanData( "value" ) );
	}

	@Test
	public void notNullFormParam_WithNotNull()
	{
		runTest( this.client::notNullFormParam, "value" );
	}

	@Test
	@IgnoreWith( ResteasyProvider.class )
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
		Assert.assertEquals( data, func.apply( data ) );
	}

	private void setUpValidationException()
	{
		try {
			if( RestBridgeProvider.class.isInstance( AbstractTestCase.CLIENT_PROVIDER ) ) {
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
