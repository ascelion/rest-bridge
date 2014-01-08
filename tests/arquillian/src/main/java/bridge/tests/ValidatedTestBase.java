
package bridge.tests;

import javax.ws.rs.BadRequestException;

import org.junit.Test;

import ascelion.rest.bridge.web.BeanValidData;
import ascelion.rest.bridge.web.Validated;

public class ValidatedTestBase<P extends ClientProvider>
extends AbstractTestCase<Validated, P>
{

	@Test( expected = BadRequestException.class )
	public void beanNotNull()
	{
		try {
			this.client.beanNotNull( null );
		}
		catch( final Exception e ) {
			e.printStackTrace();

			throw e;
		}
	}

	@Test( expected = BadRequestException.class )
	public void beanValid()
	{
		try {
			this.client.beanValid( new BeanValidData() );
		}
		catch( final Exception e ) {
			e.printStackTrace();

			throw e;
		}
	}

	@Test( expected = BadRequestException.class )
	public void beanValidNotNull()
	{
		try {
			this.client.beanValidNotNull( new BeanValidData() );
		}
		catch( final Exception e ) {
			e.printStackTrace();

			throw e;
		}
	}

	@Test( expected = BadRequestException.class )
	public void notNull()
	{
		try {
			this.client.notNullQueryParam( null );
		}
		catch( final Exception e ) {
			e.printStackTrace();

			throw e;
		}
	}

}
