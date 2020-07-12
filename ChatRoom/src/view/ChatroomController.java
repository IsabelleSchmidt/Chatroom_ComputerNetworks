package view;

import java.io.IOException;

import client.Client;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;
import message.ChatMessage;
import server.Server;

/**
 * TODO:
 * GUI Loesung fuer Chat-Anfragen implementieren. Gerade wird die Anfrage automatisch angenommen.
 *
 */


public class ChatroomController {

	@FXML
	private AnchorPane chatPane;

	@FXML
	private ListView<String> activeUserListView;

	@FXML
	private ListView<String> chatRoomListView;

	@FXML
	private TextArea typeMessageField;

	@FXML
	private Button sendButton;
	
	@FXML
	private Button anfrageButton;

	@FXML
	private Label chattingWithLabel;

	@FXML
	private AnchorPane startPane;

	@FXML
	private PasswordField password;

	@FXML
	private TextField username;

	@FXML
	private Button registerButton;

	@FXML
	private Button loginButton;

	@FXML
	private Button loggout;

	@FXML
	private Label informationLabel;
	
	private Client client;

	boolean isStarted = false;
	int clientUDPPort;
	boolean selected = false;
	
	public ChatroomController() {

	}

	@FXML
	void SetOnActionLoginButton(ActionEvent event) {
		String name = username.getText();
		String passwort = password.getText();
		
		System.out.println("ChatRoomController: " + name + ", " + passwort);
		
		// Client UDP Port wird random generiert
		clientUDPPort = (int) (Math.random() * 2123) + 1201; 
		System.out.println("VC: UDP port: " + clientUDPPort);
		
		// new client
		this.client = new Client(name, clientUDPPort);
		
		// listeners for ListViews
		initListeners();
		
		// When client logged in -> scrollUp
		client.loggedInProperty.addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldValue, Boolean newValue) {
				if (newValue.booleanValue()) {
					scrollUp();	
				} else {
					informationLabel.setText("Bitte pruefe nochmal deine Daten.");
				}
			}
		});
		
		try {
			client.startTCP();
			client.login(name, passwort);
		} catch (IOException e) {
			informationLabel.setText("Bitte erstmal den Server starten");
			e.printStackTrace();
		}

	}

	@FXML
	void SetOnActionRegisterButton(ActionEvent event) {
		String name = username.getText();
		String passwort = password.getText();
		System.out.println("ChatRoomController: " + name + ", " + passwort);

		// Client UDP Port wird random generiert
		clientUDPPort = (int) (Math.random() * 2123) + 1201;
		System.out.println("VC: UDP port: " + clientUDPPort);
		
		// new client
		this.client = new Client(name, clientUDPPort);
		
		// listeners for ListViews
		initListeners();
		
		// When client registered -> scrollUp
		client.registeredProperty.addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldValue, Boolean newValue) {
				if (newValue.booleanValue()) {
					scrollUp();	
				} else {
					informationLabel.setText("Bitte pruefe nochmal deine Daten.");
				}
			}
		});
		
		try {
			client.startTCP();
			client.registrier(name, passwort);
		} catch (IOException e) {
			informationLabel.setText("Bitte erstmal den Server starten");
			e.printStackTrace();
		}	
	}
	
	
	public void initListeners() {
		// active user listener
		client.getActiveUser().addListener(new ListChangeListener<String>() {
			@Override
			public void onChanged(Change<? extends String> arg0) {
				System.out.println("VC: change activeUserList - welcome " + arg0);
				Platform.runLater(() -> {
					activeUserListView.getItems().setAll(arg0.getList());
				});
			}
		});
		
		// switch between chats
		activeUserListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
			
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (activeUserListView.getSelectionModel().isEmpty()) {
					chatRoomListView.getItems().remove(0, chatRoomListView.getItems().size());
				} else {
					if (client.getChatPartners().contains(newValue)) {
						anfrageButton.setDisable(true);
						sendButton.setDisable(false);
						
						ObservableList<String> messages = FXCollections.observableArrayList();
						client.getChatData(newValue).getMessages().stream().forEach(e -> messages.add(e.toString()));
						
						chatRoomListView.getItems().remove(0, chatRoomListView.getItems().size());
						chatRoomListView.setItems(messages);
					} else {
						//TODO: show request button
						anfrageButton.setDisable(false);
						sendButton.setDisable(true);
					}
				}
			}	
		});
		
		// if new chat partner, listen for new messages
		client.getChatPartners().addListener(new ListChangeListener<String>() {
			
			@Override
			public void onChanged(Change<? extends String> c) {
				
				while (c.next()) {
					if (c.wasAdded()) { //TODO: if was Delete
						// get new user name
						String newChatPartner = c.getAddedSubList().get(c.getFrom());
						System.out.println("VC: listen for new messages from " + newChatPartner);

						// listen for new messages from new user
						client.getChatData(newChatPartner).getMessages().addListener(new ListChangeListener<ChatMessage>() {

							@Override
							public void onChanged(Change<? extends ChatMessage> c1) {
								while (c1.next()) {
									if (c1.wasAdded()) {
										
										if (!activeUserListView.getSelectionModel().isEmpty()) {
											if (activeUserListView.getSelectionModel().getSelectedItem().equals(newChatPartner)) {
												Platform.runLater(() -> {
													ObservableList<String> messages = FXCollections.observableArrayList();
													client.getChatData(newChatPartner).getMessages().stream().forEach(e -> messages.add(e.toString()));
													
													chatRoomListView.getItems().remove(0, chatRoomListView.getItems().size());
													chatRoomListView.setItems(messages);
												});
												
											} else {
												//TODO: notification about new message
											}
										}
									}
								}
							}
						});
						
						
						if (!activeUserListView.getSelectionModel().isEmpty()) {
							if (activeUserListView.getSelectionModel().getSelectedItem().equals(newChatPartner)) {
								Platform.runLater(() -> {
									sendButton.setDisable(false);
									anfrageButton.setDisable(true);
								});
							}
						}
						
					} /*else if (c.wasRemoved()) {
						String newChatPartner = c.getAddedSubList().get(c.getFrom());
						System.out.println("VC: clear chatRoomListView " + newChatPartner);
						
						if (!activeUserListView.getSelectionModel().isEmpty()) {
							if (activeUserListView.getSelectionModel().getSelectedItem().equals(newChatPartner)) {
								
							}
						}
					}*/
				}
			}
		});
	}

	
	@FXML
	void SetOnActionAnfrageButton(ActionEvent event) {
		String user = activeUserListView.getSelectionModel().getSelectedItem();
		client.sendChatRequest(user);
	}

	@FXML
	void SetOnActionSendButton(ActionEvent event) {
		if (typeMessageField.getText() != null) {
			String user = activeUserListView.getSelectionModel().getSelectedItem();
			client.sendTextMessage(user, "\"" + typeMessageField.getText() + "\"");
			typeMessageField.clear();
			
		}
	}

	@FXML
	void SetOnActionLogoutButton(ActionEvent event) {
		String name = username.getText();
		client.logout(name);
		
		client.loggedOutProperty.addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldValue, Boolean newValue) {
				if (newValue.booleanValue()) {
					scrollDown();
				}
			}
		});
		
	}

	void scrollUp() {
		TranslateTransition tr1 = new TranslateTransition();
		tr1.setDuration(Duration.millis(600));
		tr1.setToX(0);
		tr1.setToY(-1000);
		tr1.setNode(startPane);
		TranslateTransition tr2 = new TranslateTransition();
		tr2.setDuration(Duration.millis(600));
		tr2.setFromX(0);
		tr2.setFromY(1000);
		tr2.setToX(0);
		tr2.setToY(0);
		tr2.setNode(chatPane);
		ParallelTransition pt = new ParallelTransition(tr1, tr2);
		pt.play();
	}

	void scrollDown() {
		TranslateTransition tr1 = new TranslateTransition();
		tr1.setDuration(Duration.millis(600));
		tr1.setToX(0);
		tr1.setToY(1000);
		tr1.setNode(chatPane);
		TranslateTransition tr2 = new TranslateTransition();
		tr2.setDuration(Duration.millis(600));
		tr2.setFromX(0);
		tr2.setFromY(-1000);
		tr2.setToX(0);
		tr2.setToY(0);
		tr2.setNode(startPane);
		ParallelTransition pt = new ParallelTransition(tr1, tr2);
		pt.play();

	}

}
