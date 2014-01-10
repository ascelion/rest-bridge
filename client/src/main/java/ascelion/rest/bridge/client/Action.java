
package ascelion.rest.bridge.client;

abstract class Action
implements Comparable<Action>
{

	enum Priority
	{
		SET_VALUE,
		DEFAULT_VALUE,
		VALID_VALUE,
		ANY,
	}

	final int ix;

	final Action.Priority px;

	Action( int ix )
	{
		this.ix = ix;
		this.px = Priority.ANY;
	}

	Action( int ix, Action.Priority px )
	{
		this.ix = ix;
		this.px = px;
	}

	@Override
	public int compareTo( Action o )
	{
		if( this.ix != o.ix ) {
			return this.ix - o.ix;
		}

		return this.px.compareTo( o.px );
	}

	void evaluate( Object[] arguments )
	{
	}

	abstract void execute( RestContext cx );
}
