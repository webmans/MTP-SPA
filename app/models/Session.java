package models;

import java.util.ArrayList;
import java.util.Date;

import controllers.Application;

public class Session {
	public static Session o=null;
	private String id;
	private User user;
	private String key;
	private long time;
	private long update;
	
	public Session(){
	}
	
	/**
	 * Instantiates a new Session object and fills it automatically with the given attributes provided in the database.
	 * @author Christian
	 * @param id The id of the queue. 
	 */
	public Session(String id){
		Session.o = this;
		this.id = id;
		ArrayList<String> filling = Application.db.select(this, true);
		if(filling != null){
			this.user = new User(filling.get(1));
			this.key = filling.get(2);
			this.time = Long.parseLong(filling.get(3));
			this.update = Long.parseLong(filling.get(4));
		}
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public User getUser() {
		return user;
	}
	
	public void setUser(User user) {
		this.user = user;
	}

	public String getKey() {
		return key;
	}
	
	public void setKey(String key) {
		this.key = key;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public long getUpdate() {
		return update;
	}
	
	public void setUpdate(long update) {
		this.update = update;
	}
	


}
