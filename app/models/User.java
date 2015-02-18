package models;

import java.util.ArrayList;

import play.Logger;
import play.mvc.Controller;

import controllers.Application;


public class User {
	
	private String id;
	private String name;
	private String email;
	private String passwd;
	private long time;
	private Session session;
	
	public User(){
	}
	/**
	 * Instantiates a User object and fills it automatically with the given attributes provided in the database.
	 * @author Christian
	 * @param id The id of the user
	 */
	public User(String id){
		this.id = id;
		ArrayList<String> filling = Application.db.select(this, true);
		if(filling != null){
			this.name = filling.get(1);
			this.email = filling.get(2);
			this.passwd = filling.get(3);
			this.time = Long.parseLong(filling.get(4));
			if(Session.o != null){
				this.session = Session.o;
			}else{
				this.session = new Session(filling.get(5));
			}
		}
		
		
	}
	
	/**
	 * Fills the attributes of a user Object by providing an email address.
	 * @author Christian
	 * @param email The email of the user.
	 * @return True if the filling process was successful, false otherwise.
	 */
	public boolean init(String email){
		this.setEmail(email);
		ArrayList<String> filling = Application.db.select(this, false);
		if(filling != null){
			this.id = filling.get(0);
			this.name = filling.get(1);
			this.email = filling.get(2);
			this.passwd = filling.get(3);
			this.time = Long.parseLong(filling.get(0));
			return true;
		}else{
			return false;
		}
		
	}
	/**
	 * Creates a Session that belongs to the User and adds it to the database.
	 * @author Christian
	 * @param passwd The password of the User.
	 * @return True if the password provided by the user matches the password of the User Objecet. False otherwise.
	 */
	public boolean createSession(String passwd){
		String password = Application.sha1(passwd);
		Logger.debug(password);
		Logger.debug(passwd);
		Logger.debug(this.passwd);
		if(this.passwd != null && passwd != null && this.passwd.equals(password)){
			this.session = new Session();
			this.session.setKey(Application.sha1("askejdknr"+System.currentTimeMillis()+""+Math.random()*400));
			this.session.setTime(System.currentTimeMillis());
			this.session.setUpdate(System.currentTimeMillis());
			this.session.setUser(this);
			Application.db.add(this.session);
			Controller.response().setCookie("sessid", this.session.getId());
			Controller.response().setCookie("sesskey", Application.sha1(this.session.getKey()));
			return true;
		}
		return false;
	}

	public String getId() {
		return id;
	}
	
	public void setId(String id){
		this.id = id;
	}
	
	public void setEmail(String email){
		this.email = email;
	}

	public String getName() {
		return name;
	}

	public String getPasswd() {
		return passwd;
	}

	public String getEmail() {
		return email;
	}

	public long getTime() {
		return time;
	}

	public Session getSession() {
		return session;
	}
	
	public void setSession(Session session){
		this.session = session;
	}

}
