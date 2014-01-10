
package ascelion.rest.bridge.client;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

class ValidationAction
extends Action
{

	static class Bean
	implements Serializable
	{

		@Valid
		final List<Object> parameters;

		Bean( List<Object> parameters )
		{
			this.parameters = parameters;
		}
	}

	static void validate( RestContext cx )
	{
		final ValidatorFactory vf = getValidator();

		if( vf != null ) {
			final Validator val = vf.getValidator();
			final Set vio = new LinkedHashSet<>();

			vio.addAll( val.validate( new Bean( cx.validate ) ) );

			if( vio.size() > 0 ) {
				throw new ConstraintViolationException( vio );
			}
		}
	}

	private static ValidatorFactory getValidator()
	{
		try {
			return Validation.buildDefaultValidatorFactory();
		}
		catch( final ValidationException e ) {
			return null;
		}
	}

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
