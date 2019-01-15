
package ascelion.rest.bridge.client;

import java.lang.reflect.Method;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import javax.validation.executable.ExecutableValidator;

class ValidationAction
extends Action
{

	private static ValidatorFactory getValidator()
	{
		try {
			return javax.enterprise.inject.spi.CDI.current().select( ValidatorFactory.class ).get();
		}
		catch( final NoClassDefFoundError e ) {
			;
		}
		catch( final IllegalStateException e ) {
			;
		}
		catch( final RuntimeException e ) {
			;
		}

		try {
			return Validation.buildDefaultValidatorFactory();
		}
		catch( final Throwable e ) {
			return null;
		}
	}

	private final Method method;

	ValidationAction( Method method )
	{
		super( new ActionParam( HEAD ) );

		this.method = method;
	}

	@Override
	public RestRequest<?> execute( RestRequest<?> req )
	{
		final ValidatorFactory vf = getValidator();

		if( vf != null ) {
			final ExecutableValidator val = vf.getValidator().forExecutables();
			final Set<ConstraintViolation<Object>> vio = val.validateParameters( req.proxy, this.method, req.arguments );

			if( vio.size() > 0 ) {
				throw new ConstraintViolationException( vio );
			}
		}

		return req;
	}
}
