
package ascelion.rest.bridge.web;

import java.util.Objects;

import javax.validation.constraints.NotNull;

public class BeanValidData
{

	@NotNull
	private String value;

	public BeanValidData()
	{
	}

	public BeanValidData( String value )
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

		final BeanValidData that = (BeanValidData) obj;

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
