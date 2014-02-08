
package ascelion.rest.bridge.web;

import java.util.Objects;

import javax.validation.constraints.NotNull;

public class BeanData
{

	@NotNull
	private String value;

	public BeanData()
	{
	}

	public BeanData( String value )
	{
		this.value = value;
	}

	@Override
	public boolean equals( Object obj )
	{
		if( this == obj ) {
			return true;
		}
		if( obj == null ) {
			return false;
		}
		if( getClass() != obj.getClass() ) {
			return false;
		}

		final BeanData that = (BeanData) obj;

		return Objects.equals( this.value, that.value );
	}

	public String getNotNull()
	{
		return this.value;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash( this.value );
	}

	public void setNotNull( String value )
	{
		this.value = value;
	}
}
