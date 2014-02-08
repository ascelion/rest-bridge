
package ascelion.rest.bridge.client;

import java.lang.reflect.Method;

import javax.enterprise.inject.spi.CDI;
import javax.validation.Validation;
import javax.validation.ValidationException;
import javax.validation.ValidatorFactory;
import javax.validation.executable.ExecutableValidator;

class ValidationAction
extends Action
{

	//	static void validate( RestContext cx )
	//	{
	//		final ValidatorFactory vf = getValidator();
	//
	//		if( vf != null ) {
	//			final Validator val = vf.getValidator();
	//			final Set vio = new LinkedHashSet<>();
	//
	//			cx.validate.forEach( o -> vio.addAll( val.validate( o ) ) );
	//
	//			if( vio.size() > 0 ) {
	//				throw new ConstraintViolationException( vio );
	//			}
	//		}
	//	}

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

	static private final boolean isCDI = isCDI();

	private final Method method;

	ValidationAction( Method method )
	{
		super( -1 );

		this.method = method;
	}

	@Override
	public void execute( RestContext cx )
	{
		final ValidatorFactory vf = getValidator();

		if( vf != null ) {
			final ExecutableValidator val = vf.getValidator().forExecutables();

			val.validateParameters( cx.proxy, this.method, cx.arguments );
		}
	}
}
