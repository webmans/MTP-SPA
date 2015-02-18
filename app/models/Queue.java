package models;

import java.util.ArrayList;

import controllers.Application;
import controllers.AuthController;

public class Queue {
	private String id;
	private String name;
	private String params;
	private User user;
	private long time;
	
	public Queue(){
	}
	
	/**
	 * Creates a new Queue object and fills it automatically with the given attributes provided in the database.
	 * @author Christian
	 * @param id The id of the Queue, that is going to be initialized.
	 */
	public Queue(String id){
		this.id = id;
		ArrayList<String> filling = Application.db.select(this, true);
		if(filling != null){
			this.name = filling.get(1);
			this.params = filling.get(3);
			this.user = new User(filling.get(4));
			this.time = Long.parseLong(filling.get(5));
			if(!filling.get(2).equals("")){
				String[] sArray = filling.get(2).split("\\|");
				this.items = new Command[sArray.length];
				for(int j = 0; j < sArray.length; j++){
					Command cmd = new Command(sArray[j]);
					this.items[j]=cmd;
				}	
			}else{
				this.items = null;
			}
		}
	}
	
	/**
	 * Creates a new queue.
	 * @author Christian
	 * @param name The name of the queue.
	 * @param items Commanditems contained by the queue.
	 * @param params Parameters for the Commands.
	 * @return
	 */
	public boolean create(String name, Command[] items, String params){
		this.name = name;
		this.items = items;
		this.setParams(params);
		this.user = AuthController.getSession().getUser();
		if(Application.db.select(this, false) != null){
			return false;
		}
		return Application.db.add(this);
	}
	
	public boolean delete(){
		return Application.db.delete(this);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Command[] getItems() {
		return items;
	}

	public void setItems(Command[] items) {
		this.items = items;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}
	
}
