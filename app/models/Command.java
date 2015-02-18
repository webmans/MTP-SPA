package models;

import java.lang.reflect.Method;
import java.util.ArrayList;

import play.Logger;

import nao.Nao;

import controllers.Application;
import exceptions.NaoCommandInvokeException;

public class Command {
	private static Nao nao = null;
	private String id;
	private String name;
	private String params;
	private String type;
	
	public Command(){
	}
	
	/**
	 * Fills the new command object's attributes with the data stored in the database.
	 * @author Christian
	 * @param id ID of the command.
	 */
	public Command(String id){
		this.id = id;
		ArrayList<String> filling = Application.db.select(this, true);
		
		if(filling != null){
			this.name = filling.get(1);
			this.params = filling.get(2);
			this.type = filling.get(3);
		}
	}
	
	/**
	 * Executes a command with given parameters on the Nao
	 * @author Christian
	 * @param params The parameters given as a String in the form of "param1|param2|.."
	 * @return The status which is given by the Nao.
	 * @throws NumberFormatException If Float is expected but wasn't passed over.
	 * @throws NaoCommandInvokeException if failed to invoke a command on the Nao.
	 */
	public String exec(String params) throws NumberFormatException, NaoCommandInvokeException{
		String returnString;
		String methodName = this.name.substring(0, 1).toLowerCase() + this.name.substring(1).replace(" ", "");
		String[] paramsSplit = params.split("\\|");
		Object[] paramsObjects = new Object[paramsSplit.length];
		Method[] naoMethods = Nao.class.getMethods();
		Class[] paramsType = null;
		for(int i = 0; i < naoMethods.length; i++){
			if(naoMethods[i].getName().equals(methodName)){
				paramsType = naoMethods[i].getParameterTypes();
			}
		}
		for(int i = 0; i < paramsType.length; i++){
			if(paramsType[i].getSimpleName().equals("float")){
				paramsObjects[i] = Float.parseFloat(paramsSplit[i]);
			}else{
				paramsObjects[i] = paramsSplit[i];
			}
		}
		try{
			
			Method m = Nao.class.getMethod(methodName, paramsType);
			if(paramsType.length > 0) {
				returnString = m.invoke(Command.getNao(), paramsObjects).toString();
			}
			else {
				returnString = m.invoke(Command.getNao()).toString();
			}
			
		}catch(Exception e){
			Logger.debug(e.getMessage());
			throw new NaoCommandInvokeException();
		}
		return returnString;
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

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}
	
	public static Nao getNao() {
		if(Command.nao == null) {
			Command.nao = new Nao();
		}
		
		return Command.nao;
	}
}
