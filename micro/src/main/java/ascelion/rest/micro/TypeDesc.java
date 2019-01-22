
package ascelion.rest.micro;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

import javax.ws.rs.core.Context;

import static java.util.Arrays.stream;
import static org.apache.commons.lang3.reflect.MethodUtils.getMatchingAccessibleMethod;

final class TypeDesc<T>
{

	final Class<T> type;
	private final Collection<PropDesc<?>> props = new ArrayList<>();

	TypeDesc( Class<T> type )
	{
		this.type = type;
		final Class<?>[] parent = new Class[1];
		Class<?> walk = type;

		while( walk != Object.class ) {
			final Stream<PropDesc<?>> sm = stream( walk.getDeclaredMethods() )
				.filter( m -> m.isAnnotationPresent( Context.class ) )
				.filter( m -> parent[0] == null || ( parent[0] != null && getMatchingAccessibleMethod( parent[0], m.getName(), m.getParameterTypes() ) == null ) )
				.map( PropDesc::new );
			final Stream<PropDesc<?>> sf = stream( walk.getDeclaredFields() )
				.filter( f -> f.isAnnotationPresent( Context.class ) )
				.map( PropDesc::new );

			Stream.concat( sf, sm )
				.forEach( this.props::add );

			parent[0] = walk;
			walk = walk.getSuperclass();
		}
	}

	void inject( T target )
	{
		this.props.forEach( p -> p.setValue( target, ThreadLocalProxy.create( p.type ) ) );
	}
}
