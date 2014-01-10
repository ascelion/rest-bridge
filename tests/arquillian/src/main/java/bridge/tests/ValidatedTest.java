
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

import ascelion.rest.bridge.web.BeanValidData;
import ascelion.rest.bridge.web.Validated;

public class ValidatedTest
extends AbstractTestCase<Validated>
{

	@Rule
	public ExpectedException ex = ExpectedException.none();

	public void bean_WithInvalid()
	{
		runTest( new BeanValidData(), this.client::bean );
	}

	@Test
	@IgnoreWith( ResteasyProvider.class )
	public void bean_WithNull()
	{
		runTest( null, this.client::bean );
	}

	@Test
	public void bean_WithValid()
	{
		runTest( new BeanValidData( "value" ), this.client::bean );
	}

	@Test
	public void beanNotNull_WithInvalid()
	{
		runTest( new BeanValidData(), this.client::beanNotNull );
	}

	@Test
	@IgnoreWith( ResteasyProvider.class )
	public void beanNotNull_WithNull()
	{
		runTest( null, this.client::beanNotNull );
	}

	@Test
	public void beanNotNull_WithValid()
	{
		runTest( new BeanValidData( "value" ), this.client::beanNotNull );
	}

	@Test
	public void beanValid_WithInvalid()
	{
		setUpValidationException();

		runTest( new BeanValidData(), this.client::beanValid );
	}

	@Test
	@IgnoreWith( ResteasyProvider.class )
	public void beanValid_WithNull()
	{
		runTest( null, this.client::beanValid );
	}

	@Test
	public void beanValid_WithValid()
	{
		runTest( new BeanValidData( "value" ), this.client::beanValid );
	}

	@Test
	public void beanValidNotNull_WithInvalid()
	{
		setUpValidationException();

		runTest( new BeanValidData(), this.client::beanValidNotNull );
	}

	@Test
	@IgnoreWith( ResteasyProvider.class )
	public void beanValidNotNull_WithNull()
	{
		runTest( null, this.client::beanValidNotNull );
	}

	@Test
	public void beanValidNotNull_WithValid()
	{
		runTest( new BeanValidData( "value" ), this.client::beanValidNotNull );
	}

	@Test
	public void notNullFormParam_WithNotNull()
	{
		runTest( "value", this.client::notNullFormParam );
	}

	@Test
	@IgnoreWith( ResteasyProvider.class )
	public void notNullFormParam_WithNull()
	{
		setUpValidationException();

		runTest( null, this.client::notNullFormParam );
	}

	@Test
	public void notNullHeaderParam_WithNotNull()
	{
		runTest( "value", this.client::notNullHeaderParam );
	}

	@Test
	public void notNullHeaderParam_WithNull()
	{
		setUpValidationException();

		runTest( null, this.client::notNullHeaderParam );
	}

	@Test
	public void notNullQueryParam_WithNotNull()
	{
		runTest( "value", this.client::notNullQueryParam );
	}

	@Test
	public void notNullQueryParam_WithNull()
	{
		setUpValidationException();

		runTest( null, this.client::notNullQueryParam );
	}

	private <T> void runTest( T data, Function<T, T> func )
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
