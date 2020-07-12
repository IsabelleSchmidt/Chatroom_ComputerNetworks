package message;

public enum Command {
	
	// Client-Nachrichten
	// TCP
	REGISTER, 
	LOGIN,
	LOGOUT,
	
	SEND_REQUEST,			// Anfrage von einem Client an den anderen
	DECLINE_REQUEST,		// Client bestaetigt die Anfrage
	ACCEPT_REQUEST,			// Client lehnt die Anfrage ab
	
	// UDP
	SEND_TEXT_MESSAGE,		// Text-Nachricht von einem Client an den anderen
	CHUNK_RECEIVED,			// Empfaenger-Nachricht, wenn Message angekommen ist
	
	// Server-Nachrichten
	//TCP
	REGISTER_DECLINED,
	REGISTER_ACCEPTED,
	LOGIN_DECLINED,
	LOGIN_ACCEPTED,
	LOGEDOUT,
	
	NEW_REQUEST,			// Server gibt die Chat-Request an den Empfaenger weiter
	REQUEST_DECLINED,		// Server gibt die Ablehnung an den Client weiter
	REQUEST_ACCEPTED,		// Server gibt die Bestaetigung an den Client weiter
	
	USER_ONLINE,
	USER_OFFLINE,
	
	ILLEGAL_COMMAND
}
