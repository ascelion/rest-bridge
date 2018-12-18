
package ascelion.rest.bridge.client;

import java.util.concurrent.Callable;

import static java.lang.String.format;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
abstract class Action
implements Comparable<Action>
{

	static final int HEAD = -1000;
	static final int TAIL = +1000;

	final ActionParam param;

	@Override
	public int compareTo( Action that )
	{
		return Integer.compare( this.param.index, that.param.index );
	}

	@Override
	public final String toString()
	{
		return format( "%s[%s]", getClass().getSimpleName(), inspect() );
	}

	String inspect()
	{
		return format( "index = %s", this.param.index );
	}

	abstract Callable<?> execute( RestRequest cx );
}
