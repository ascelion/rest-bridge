
package ascelion.rest.micro;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import lombok.SneakyThrows;

final class PropDesc<T>
{

	interface Setter
	{

		void apply( Object o, Object v ) throws Exception;
	}

	private final Setter op;
	final Class<T> type;

	PropDesc( Field field )
	{
		this.type = (Class<T>) field.getType();
		this.op = ( o, v ) -> field.set( o, v );

		field.setAccessible( true );
	}

	PropDesc( Method method )
	{
		this.type = (Class<T>) method.getParameterTypes()[0];
		this.op = ( o, v ) -> method.invoke( o, v );

		method.setAccessible( true );
	}

	@SneakyThrows
	void setValue( Object o, Object v )
	{
		this.op.apply( o, v );
	}
}
