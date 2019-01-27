
package ascelion.utils.etc;

import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

import static java.security.AccessController.doPrivileged;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor( access = AccessLevel.NONE )
public final class Secured
{

	static public void runPrivileged( Runnable action )
	{
		runPrivileged( () -> {
			action.run();

			return null;
		} );
	}

	static public <T> T runPrivileged( Supplier<T> action )
	{
		return doPrivileged( (PrivilegedAction<T>) action::get );
	}

	static public void runPrivilegedWithException( ThrowableAction action ) throws PrivilegedActionException
	{
		runPrivilegedWithException( () -> {
			action.run();

			return null;
		} );
	}

	static public <T> T runPrivilegedWithException( Callable<T> action ) throws PrivilegedActionException
	{
		return doPrivileged( (PrivilegedExceptionAction<T>) () -> action.call() );
	}

}
