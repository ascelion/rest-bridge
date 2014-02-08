
package ascelion.rest.bridge.client;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.enterprise.inject.spi.CDI;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

class ValidationAction
extends Action
{

	static void validate( RestContext cx )
	{
		final ValidatorFactory vf = getValidator();

		if( vf != null ) {
			final Validator val = vf.getValidator();
			final Set vio = new LinkedHashSet<>();

			cx.validate.forEach( o -> vio.addAll( val.validate( o ) ) );

			if( vio.size() > 0 ) {
				throw new ConstraintViolationException( vio );
			}
		}
	}

	private static ValidatorFactory cdiValidator()
	{
		return CDI.current().select( ValidatorFactory.class ).get();
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
		catch( final ValidationException e ) {
			return null;
		}
	}

	private static boolean isCDI()
	{
		try {
			Class.forName( "javax.enterprise.inject.spi.CDI", false, Thread.currentThread().getContextClassLoader() );

			return true;
		}
		catch( final ClassNotFoundException e ) {
			return false;
		}

	}

	static final boolean isCDI = isCDI();

	ValidationAction( int ix )
	{
		super( ix, Priority.VALID_VALUE );
	}

	@Override
	public void execute( RestContext cx )
	{
		while( cx.validate.size() < this.ix ) {
			cx.validate.add( null );
		}

		cx.validate.add( cx.parameterValue );
	}
}
