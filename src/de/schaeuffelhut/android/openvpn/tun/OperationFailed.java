package de.schaeuffelhut.android.openvpn.tun;

public class OperationFailed extends Exception
{
	private static final long serialVersionUID = -6006562450492167220L;

	public OperationFailed() {
		super();
	}

	public OperationFailed(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public OperationFailed(String detailMessage) {
		super(detailMessage);
	}

	public OperationFailed(Throwable throwable) {
		super(throwable);
	}

}
