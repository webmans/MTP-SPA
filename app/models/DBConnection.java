package models;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import controllers.Application;

import play.Logger;

public class DBConnection {
	
	/*
	 * MySQL-Connection information
	 */
	private String host 	= "webmans.de";
	private int    port 	= 3306;
	private String path 	= "data_nao";
	private String user 	= "user_nao";
	private String passwd	= "K3sOOrlMO+jMW";
	private String Driver = "com.mysql.jdbc.Driver";
	private Connection connection;
	
	/**
	 * Connects to the database.
	 * @author Christian
	 * @return true if successfully connected, false otherwise.
	 */
	public boolean connect(){
		try {
			
			if(this.connection != null && this.connection.isValid(0)){
				return true;
			}
			Class.forName(this.Driver);
			this.connection = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.path, this.user, this.passwd);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Disconnect for the database.
	 * @author Christian
	 * @return True if disconnected successfully, false otherwise.
	 */
	public boolean disconnect(){
		if (this.connection != null){
			try {
				this.connection.close();
				return true;
			}catch (Exception e){
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Selects all rows for a given id of an Object.
	 * @author Christian
	 * @param o Object of either User, Session, Queue or Command.
	 * @param mode Only necessary for User and Queue. Enables you to get Users by their email if mode=false, and Queue by their name for mode=false.
	 * @return A list of strings filled with the rows.
	 */
	public ArrayList<String> select(Object o, boolean mode){
		ArrayList<String> re = new ArrayList<String>();
		ArrayList<String> args = new ArrayList<String>();
		
		String query = "";
		
		if(this.connect()){
			if(o instanceof User){
				query = "SELECT * FROM `user` WHERE `%s` = '%s'";
				if(mode == false){
					args.add("email");
					args.add(((User) o).getEmail());
				}
				else {
					args.add("id");
					args.add(((User) o).getId());
				}
			}else if (o instanceof Session){
				query = "SELECT * FROM `session` WHERE `id` = '%s'";
				args.add(((Session) o).getId());
			}else if (o instanceof Queue){
				query = "SELECT * FROM `queue` WHERE `%s` = '%s'";
				if(mode){
					args.add("id");
					args.add(((Queue) o).getId());
				}else{
					args.add("name");
					args.add(((Queue) o).getName());
				}
				
			}else if (o instanceof Command){
				query = "SELECT * FROM `command` WHERE `id` = '%s'";
				args.add(((Command) o).getId());
			}
			ResultSet rs = this.exec(query, args, true);
			try {
				if(rs.first()){
					for(int i = 1; i <= rs.getMetaData().getColumnCount(); i++){
						re.add(rs.getString(i));
					}
				}else{
					return null;
				}
				
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return re;
	}
	
	/**
	 * Selects all rows and columns of the give class.
	 * @author Christian
	 * @param The class of either Command, ComplexCommand or Queue.
	 * @return A List of objects that were stored in the database by either Command, ComplexCommand or Queue.
	 */
	@SuppressWarnings("rawtypes")//For compatibility with Scala Template Engine.
	public ArrayList selectAll(Class c){
		ArrayList<Object> reObj = new ArrayList<Object>();
		if(this.connect()){
			String query = "";
			if(c.equals(Command.class)){
				query = "SELECT * FROM `command` WHERE `type` = 'simple' ORDER BY `name`";
			}else if(c.equals(ComplexCommand.class)){
				query = "SELECT * FROM `command` WHERE `type` = 'complex' ORDER BY `name`";
			}else if(c.equals(Queue.class)){
				query = "SELECT * FROM `queue` ORDER BY `time`";
			}
			ResultSet rs = this.exec(query, null, true);
			try {
				while(rs.next()){
					if(c.equals(Command.class)||c.equals(ComplexCommand.class)){
						Command cmd = new Command();
						cmd.setId(rs.getString("id"));
						cmd.setName(rs.getString("name"));
						cmd.setParams(rs.getString("params"));
						reObj.add(cmd);
					}else if(c.equals(Queue.class)){
						Queue q = new Queue();
						q.setId(rs.getString("id"));
						q.setName(rs.getString("name"));
						q.setTime(rs.getLong("time"));
						q.setUser(new User(rs.getString("user")));
						reObj.add(q);
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return reObj;
	}
	
	/**
	 * Adds an Object to the database.
	 * @author Christian
	 * @param o Object of either User,Queue or Session.
	 * @return True if stored successfully, false otherwise.
	 */
	public boolean add(Object o){
		if(this.connect()){
			
			String query="";
			ArrayList<String> args = new ArrayList<String>();
			if(o instanceof User){
				((User) o).setId(Application.sha1(""+System.currentTimeMillis()).substring(0, 8));
				query = "INSERT INTO `user` (`id`, `name`, `email`, `passwd`, `time`, `session`) VALUES ( '%s', '%s', '%s', '%s', '%s', '%s')";
				args.add(((User) o).getId());
				args.add(((User) o).getName());
				args.add(((User) o).getEmail());
				args.add(((User) o).getPasswd());
				args.add(""+((User) o).getTime());
				args.add(((User) o).getSession().getId());
			}else if(o instanceof Queue){
				((Queue) o).setId(Application.sha1(""+System.currentTimeMillis()).substring(0, 8));
				query = "INSERT INTO `queue` (`id`, `name`, `items`, `params`, `user`, `time`) VALUES ('%s', '%s', '%s', '%s', '%s', '%s')";
				args.add(((Queue) o).getId());
				args.add(((Queue) o).getName());
				Command[] cmd = ((Queue) o).getItems();
				String s="";
				for(int i = 0; i < cmd.length; i++){
					if(i == 0){
						s = cmd[i].getId();
					}else{
						s=s + "|" + cmd[i].getId();
					}
				}
				args.add(s);
				args.add(((Queue) o).getParams());
				args.add(((Queue) o).getUser().getId());
				args.add(""+((Queue) o).getTime());
			}else if(o instanceof Session){
				((Session) o).setId(Application.sha1(""+System.currentTimeMillis()).substring(0, 8));
				query = "INSERT INTO `session` (`id`, `user`, `key`, `time`, `update`) VALUES ('%s', '%s', '%s', '%s', '%s')";
				args.add(((Session) o).getId());
				args.add(((Session) o).getUser().getId());
				args.add(((Session) o).getKey());
				args.add(""+((Session) o).getTime());
				args.add(""+((Session) o).getUpdate());
			}
			this.exec(query, args, false);
			return true;
		}
		return false;
	}
	
	/**
	 * Delete a gived Object from the database.
	 * @author Christian
	 * @param o Object of either Queue or Session.
	 * @return True if deleted successfully, false otherwise.
	 */
	public boolean delete(Object o){
		if(this.connect()){
			String query = "DELETE FROM `%s` WHERE `id` = '%s'";
			ArrayList<String> args = new ArrayList<String>();
			if(o instanceof Queue){
				args.add("queue");
				args.add(((Queue) o).getId());
			}
			if(o instanceof Session){
				args.add("session");
				args.add(((Session) o).getId());
			}
			this.exec(query, args, false);
			return true;
		}
		return false;	
	}
	
	/**
	 * Updates the a User or Session stored in the Database.
	 * @author Christian
	 * @param o Either a User or a Session Object.
	 * @param prop The property to be updated.
	 * @param value The value for the property.
	 * @return True if updated successfully, false otherwise.
	 */
	public boolean update(Object o, String prop, String value){
		if(this.connect()){
			String query = "UPDATE `%s` SET `%s`='%s' WHERE `id`='%s'";
			ArrayList<String> args = new ArrayList<String>();
			if(o instanceof Session){
				args.add("session");
				args.add(prop);
				args.add(value);
				args.add(((Session) o).getId());
			}
			if(o instanceof User){
				args.add("user");
				args.add(prop);
				args.add(value);
				args.add(((User) o).getId());
			}
			this.exec(query, args, false);
			return true;
		}
		return false;
	}
	
	private String secure(String o) {
		return o;
	}
	/**
	 * Exectues a given query on the database.
	 * @author Christian
	 * @param query The query to be executed.
	 * @param args A list of arguments that replace the place holders in the queryString.
	 * @param read Read something from the database with read = true. Write something with read = false.
	 * @return
	 */
	public ResultSet exec(String query, ArrayList<String> args, boolean read) {
		String secureQuery;
		if(args != null){
			int i = 0;
			while(i < args.size()) {
				args.set(i, this.secure(args.get(i)));
				i++;
			}
			secureQuery = String.format(query, args.toArray());
		}else{
			secureQuery = query;
		}
		Logger.debug(secureQuery);
		try {
			Statement stmt = this.connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);

			if(read){
				ResultSet rs = stmt.executeQuery(secureQuery.toString());
				return rs;
			} else{
				stmt.execute(secureQuery.toString());
				return null;
			}

		} catch (Exception e) {
			System.err.println(e.toString());
			return null;
		}
	}
	
}
