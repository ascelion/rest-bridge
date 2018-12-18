
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

	static private final boolean isCDI = isCDI();

	private static ValidatorFactory cdiValidator()
	{
		return javax.enterprise.inject.spi.CDI.current().select( ValidatorFactory.class ).get();
	}

	private static ValidatorFactory getValidator()
	{
		if( isCDI ) {
			try {
				return cdiValidator();
			}
			catch( final Exception e ) {
				;
			}
		}

		try {
			return Validation.buildDefaultValidatorFactory();
		}
		catch( final Throwable e ) {
			return null;
		}
	}

	private static boolean isCDI()
	{
		try {
			Class.forName( "javax.enterprise.inject.spi.CDI" );

			return true;
		}
		catch( final ClassNotFoundException e ) {
			return false;
		}

	}

	private final Method method;

	ValidationAction( Method method )
	{
		super( new ActionParam( HEAD ) );

		this.method = method;
	}

	@Override
	public RestRequest execute( RestRequest req )
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
