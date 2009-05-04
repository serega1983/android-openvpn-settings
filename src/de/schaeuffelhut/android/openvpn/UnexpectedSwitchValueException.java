package de.schaeuffelhut.android.openvpn;

public class UnexpectedSwitchValueException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	UnexpectedSwitchValueException(int v)
	{
		super( Integer.toString( v ) );
	}
}
