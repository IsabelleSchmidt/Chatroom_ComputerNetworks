package server;

import java.util.HashMap;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ServerData {
	
	private Map<String, String> userPasswords;
	private Map<String, EndpointInfo> routingTable;
	public ObservableList<String> activeUser;
	
	public ServerData() {
		routingTable = new HashMap<>();
		userPasswords = new HashMap<>();
		activeUser = FXCollections.observableArrayList();
	}
	
	public void addRoutingInfo(String clientName, String address, String host) {
		EndpointInfo endpointInfo = new EndpointInfo(address, host);
		routingTable.put(clientName, endpointInfo);
	}
	
	public boolean login(String name, String passwort) {
		for (String n : userPasswords.keySet()) {
			if (n.equals(name)) {
				if (passwort.equals(userPasswords.get(n))) {
					return true;
				} else {
					return false;
				}
			}
		}
		return false;
	}

	public void loggoutUser(String name) {
		activeUser.remove(name);
	}

	public void registerUser(String name, String passwort) {
		userPasswords.put(name, passwort);
	}

	public boolean usernameAvailable(String name) {
		for (String n : userPasswords.keySet()) {
			if (n.equals(name)) {
				return false;
			}
		}
		return true;
	}
	
	public void addActiveUser(String userName) {
		activeUser.add(userName);
		System.out.println(activeUser);
	}
	
	public ObservableList<String> getActiveUser() {
		return activeUser;
	}

}
