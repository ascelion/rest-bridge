
package bridge.tests.arquillian;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import bridge.tests.TestClientProvider;
import bridge.tests.arquillian.IgnoreWithProvider.IgnoreWithProviders;

@Target( { ElementType.METHOD, ElementType.TYPE } )
@Retention( RetentionPolicy.RUNTIME )
@Repeatable( IgnoreWithProviders.class )
public @interface IgnoreWithProvider
{

	@Target( { ElementType.METHOD, ElementType.TYPE } )
	@Retention( RetentionPolicy.RUNTIME )
	public @interface IgnoreWithProviders
	{

		IgnoreWithProvider[] value();
	}

	Class<? extends TestClientProvider>[] value();

	String reason() default "";
}
