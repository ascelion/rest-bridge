
package ascelion.rest.bridge.client;

import java.lang.reflect.Method;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidatorFactory;
import javax.validation.executable.ExecutableValidator;

class ValidationAction
extends Action
{

	private final Method method;

	ValidationAction( Method method )
	{
		super( new ActionParam( HEAD ) );

		this.method = method;
	}

	@Override
	public RestRequest<?> execute( RestRequest<?> req )
	{
		final ValidatorFactory vf = INTValidation.getValidator();

		if( vf != null ) {
			final ExecutableValidator val = vf.getValidator().forExecutables();
			final Set<ConstraintViolation<Object>> vio = val.validateParameters( req.rc.proxy, this.method, req.rc.getArguments() );

			if( vio.size() > 0 ) {
				throw new ConstraintViolationException( vio );
			}
		}

		return req;
	}
}
