
package ascelion.rest.bridge.client;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

final class INTResponseValidation extends INTValidation
{

	@Override
	protected void after( RestRequestContext rc, Object result, Exception exception )
	{
		if( this.xv == null || exception != null ) {
			return;
		}

		final RestMethodInfo mi = rc.getMethodInfo();
		final Set<ConstraintViolation<Object>> cv = this.xv.validateReturnValue( rc.getService(), mi.getJavaMethod(), result );

		if( cv.size() > 0 ) {
			throw new ConstraintViolationException( cv );
		}
	}

}
