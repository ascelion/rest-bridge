
package ascelion.rest.bridge.web;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

@XmlRootElement
@XmlAccessorType( XmlAccessType.FIELD )
@JsonAutoDetect( fieldVisibility = ANY, creatorVisibility = NONE, getterVisibility = NONE, isGetterVisibility = NONE, setterVisibility = NONE )
public class BeanParamData
{

	@PathParam( "path1" )
	private String pathParam1;

	@HeaderParam( "header1" )
	@DefaultValue( "none" )
	private String headerParam1;

	@QueryParam( "query1" )
	private String queryParam1;

	@PathParam( "path2" )
	private String pathParam2;

	@HeaderParam( "header2" )
	private String headerParam2;

	@QueryParam( "query2" )
	private String queryParam2;

	public BeanParamData()
	{
	}

	public BeanParamData( String pathParam2, String headerParam2, String queryParam2 )
	{
		this.pathParam2 = pathParam2;
		this.headerParam2 = headerParam2;
		this.queryParam2 = queryParam2;
	}

	public String getHeaderParam1()
	{
		return this.headerParam1;
	}

	public String getHeaderParam2()
	{
		return this.headerParam2;
	}

	public String getPathParam1()
	{
		return this.pathParam1;
	}

	public String getPathParam2()
	{
		return this.pathParam2;
	}

	public String getQueryParam1()
	{
		return this.queryParam1;
	}

	public String getQueryParam2()
	{
		return this.queryParam2;
	}

	public void setHeaderParam1( String headerParam1 )
	{
		this.headerParam1 = headerParam1;
	}

	public void setHeaderParam2( String headerParam2 )
	{
		this.headerParam2 = headerParam2;
	}

	public void setPathParam1( String pathParam1 )
	{
		this.pathParam1 = pathParam1;
	}

	public void setPathParam2( String pathParam2 )
	{
		this.pathParam2 = pathParam2;
	}

	public void setQueryParam1( String queryParam1 )
	{
		this.queryParam1 = queryParam1;
	}

	public void setQueryParam2( String queryParam2 )
	{
		this.queryParam2 = queryParam2;
	}
}
