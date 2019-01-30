
package ascelion.rest.bridge.client;

import static java.lang.String.format;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class Prioritised<T> implements Comparable<Prioritised<T>>
{

	private final long priority;
	private final T instance;

	@Override
	public final int compareTo( Prioritised<T> that )
	{
		return Long.compare( this.priority, that.priority );
	}

	@Override
	public String toString()
	{
		return format( "%16X:%s", this.priority,
			this.instance != null ? this.instance.getClass().getSimpleName() : "" );
	}
}
