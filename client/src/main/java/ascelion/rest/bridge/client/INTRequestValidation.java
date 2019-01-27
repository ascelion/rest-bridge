
package ascelion.rest.bridge.client;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

final class INTRequestValidation extends INTValidation
{

	@Override
	protected void before( RestRequestContext rc )
	{
		if( this.xv == null ) {
			return;
		}

		final Set<ConstraintViolation<Object>> cv = this.xv.validateParameters( rc.getImplementation(), rc.getJavaMethod(), rc.getArguments() );

		if( cv.size() > 0 ) {
			throw new ConstraintViolationException( cv );
		}
	}
}
