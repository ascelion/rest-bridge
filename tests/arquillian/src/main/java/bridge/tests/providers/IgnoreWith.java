
package bridge.tests.providers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import bridge.tests.ClientProvider;

@Target( { ElementType.METHOD, ElementType.TYPE } )
@Retention( RetentionPolicy.RUNTIME )
public @interface IgnoreWith
{

	Class<? extends ClientProvider>[] value() default {};
}
