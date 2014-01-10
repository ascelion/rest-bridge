
package bridge.tests.arquillian;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import bridge.tests.TestClientProvider;

@Target( { ElementType.METHOD, ElementType.TYPE } )
@Retention( RetentionPolicy.RUNTIME )
public @interface IgnoreWith
{

	Class<? extends TestClientProvider>[] value() default {};
}
