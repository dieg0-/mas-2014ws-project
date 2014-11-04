package bookshelf;

import jade.core.Agent;

public class BookshelfAgent extends Agent {
	protected void setup(){
		System.out.println("Hello, I am "+getLocalName());
	}

}
