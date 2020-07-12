package server;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;

/**
 * Class represents information about an end point (IP Address, port).
 *
 */
public class EndpointInfo {

	private InetAddress address;
	private int port;
	
	public EndpointInfo(InetAddress address, int port) {
	    this.address = address;
	    this.port = port;
	}
	
	public EndpointInfo(String address, String port) {
		try {
			this.address = InetAddress.getByName(address);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	    
	    this.port = Integer.parseInt(port);
	}
	
	public InetAddress getAddress() {
	    return address;
	}
	
	public int getPort() {
	    return port;
	}
	
	@Override
	public boolean equals(Object o) {
	    if (this == o) return true;
	    if (o == null || getClass() != o.getClass()) return false;
	    EndpointInfo endpoint = (EndpointInfo) o;
	    return port == endpoint.port &&
	            address.equals(endpoint.address);
	}
	
	@Override
	public int hashCode() {
	    return Objects.hash(address, port);
	}
	
	@Override
	public String toString() {
	    return "Endpoint{" +
	            "address=" + address +
	            ", port=" + port +
	            '}';
	
	}

}
