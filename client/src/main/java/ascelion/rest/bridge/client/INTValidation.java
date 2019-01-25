
package ascelion.rest.bridge.client;

import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import javax.validation.executable.ExecutableValidator;

abstract class INTValidation extends INTBase
{

	final ExecutableValidator xv;

	static ValidatorFactory getValidator()
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

	INTValidation()
	{
		final ValidatorFactory vf = getValidator();

		this.xv = vf != null ? vf.getValidator().forExecutables() : null;
	}
}
