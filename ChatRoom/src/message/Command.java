package message;

public enum Command {
	
	// Client-Nachrichten
	REGISTER, 
	LOGIN,
	LOGOUT,
	
	SEND_REQUEST,			// Anfrage von einem Client an den anderen
	DECLINE_REQUEST,		// Client bestaetigt die Anfrage
	ACCEPT_REQUEST,			// Client lehnt die Anfrage ab
	
	SEND_TEXT_MESSAGE,		// Text-Nachricht von einem Client an den anderen
	CHUNK_RECEIVED,		// Empfaenger-Nachricht, wenn Message angekommen ist
	
	// Server-Nachrichten
	REGISTER_DECLINED,
	REGISTER_ACCEPTED,
	LOGIN_DECLINED,
	LOGIN_ACCEPTED,
	LOGEDOUT,
	
	REQUEST_DECLINED,		// Server gibt die Ablehnung an den Client weiter
	REQUEST_ACCEPTED,		// Server gibt die Bestaetigung an den Client weiter
	
	CHUNK_SAVED,			// Server-Bestaetigung, dass Message auf dem Server angekommen ist
	NEW_REQUEST,		// Server gibt die Chat-Request an den Empfaenger weiter
	NEW_TEXT_MESSAGE,		// Server gibt die Nachricht an den Empfaenger weiter
	
	ILLEGAL_COMMAND
}
