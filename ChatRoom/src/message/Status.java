package message;

public enum Status {
    SENT, ACKED, RECEIVED, LOST, RESENT;
	
}

//Client 
//SENT - an Server gesendet
//ACKED - vom Server Bestaetigung erhalten
//RESENT - nochmal an Server gesendet

//Server
//RECEIVED - vom Client erhalten
//LOST - als verloren markiert (random)
//ACKED - Bestaetigung an Client abgeschickt
