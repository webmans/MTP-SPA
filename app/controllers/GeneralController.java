package controllers;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import com.aldebaran.proxy.Variant;

import nao.Nao;

import exceptions.NaoCommandInvokeException;

import models.Command;
import models.Parameters;
import models.Queue;
import play.*;
import play.mvc.*;
import play.mvc.Http.Response;

import views.html.*;

public class GeneralController extends Controller {
	
	private static boolean stop = false;
	private static HashMap<String, ArrayList<String>> execResults 	= new HashMap<String, ArrayList<String>>();
	private static HashMap<String, byte[]> camPictures 				= new HashMap<String, byte[]>();
	
	/**
	 * Executes a command on the NAO.
	 * @author Christian
	 * @return "-1" if not logged in or Session expired, "0" if something went wrong and 1 if completed successfully.   
	 */
	public static Result exec(){
		if(AuthController.check()){
			int demoMode = Integer.parseInt(Parameters.get("demo"));
			Command cmd = new Command(Parameters.get("command"));
			String params = Parameters.get("params");
			String uniqueID = Parameters.get("uid");
			stop = false;
			String returnString = "";
			ArrayList returnValue = new ArrayList<String>();
			
			while(!stop){
				if(demoMode == 0){
					stop = true;
				}
				Command.getNao().checkConnection();
				
				try{
					returnValue.add(cmd.exec(params));
				}catch(NumberFormatException e){
					returnValue.add("0||Float is expected!");
				}catch(NaoCommandInvokeException e){
					returnValue.add("0||" + e.getMessage());
				}
			}
			execResults.put(uniqueID, returnValue);
			
			for(int i = 0; i < returnValue.size(); i++){
				returnString = returnString + log.render(((String) returnValue.get(i)).split("\\|")[0], ((String) returnValue.get(i)).split("\\|")[1]);
			}
			return ok("1||" + returnString);
		}
		return ok("-1||Authorization failed!");
	}
	/**
	 * Stops the command if it is executed in "demomode"
	 * @author Christian
	 * @return "1" if command was stopped successfully, "-1" if not logged in or session expired.
	 */
	public static Result stop(){
		if(AuthController.check()){
			stop = true;
			return ok("1|Command stopped");
		}
		return ok("-1|Authorization failed!");
	}
	
	/**
	 * Used for the videostream
	 * @author Christian
	 * @param id The id of the picture to be loaded next
	 * @return Returns the image to the NAO-RCC Panel
	 */
	public static Result getPicture(String id){
		if(!camPictures.containsKey(id)) {
			camPictures.put(id, Command.getNao().getPicture());
		}
		return ok(camPictures.get(id)).as("image/jpeg");
	}
	
	/**
	 * Used to get the status of a command.
	 * @author Christian
	 * @param uid The uniqueID of the command that was exectued earlier.
	 * @return The status of the command or "-1" if not logged in or session expired.
	 */
	public static Result getStatus(String uid){
		if(AuthController.check()){
			ArrayList<String> tempResult = execResults.get(uid);
			execResults.remove(uid);
			String status = tempResult.get(0);
			for(int i = 1; i < tempResult.size(); i++){
				status = status + "||" + tempResult.get(i);
			}
			return ok(status);
		}
		return ok("-1||Authorization failed!");
	}
	/**
	 * Saves a queue in the database.
	 * @author Christian
	 * @return "0" if something went wrong. "-1" if not logged in or session expired and "1" if queue was saved successfully.
	 */
	public static Result saveQueue(){
		if(AuthController.check()){
			String queueItems = Parameters.get("queue_items");
			String queueParams = Parameters.get("queue_params");
			String name = Parameters.get("queue_name");
			if(queueItems.trim().equals("")){
				return ok("0|No items in the queue!");
			}
			if(name.length() > 50){
				return ok("0|The name for the queue is too long!");
			}
			if(name.length() == 0){
				return ok("0|The queue needs a name!");
			}
			String[] items = queueItems.split("\\|");
			Command[] itemsCmd = new Command[items.length];
			for(int i = 0; i < items.length; i++){
				Command cmd = new Command(items[i]);
				itemsCmd[i] = cmd;
			}
			Queue q = new Queue();
			if(q.create(name, itemsCmd, queueParams)){
				return ok("1|Queue saved successfully|"+queue.render(q));
			}else{
				return ok("0|Queue already exists!");
			}
		}else{
			return ok("-1|Authorization failed!");
		}
	}
	
	/**
	 * Deletes a saved queue from the database.
	 * @author Christian
	 * @return "0" if something went wrong. "-1" if not logged in or session expired and "1" if queue was deleted successfully.
	 */
	public static Result deleteQueue(){
		if(AuthController.check()){
			String id = Parameters.get("queue_id");
			Logger.debug(id);
			Queue q = new Queue(id);
			if(q.delete()){
				return ok("1|Queue \"" + q.getName() + "\" was deleted");
			}else{
				return ok("0|Unable to delete queue!");
			}
		}else{
			return ok("-1|Authorization failed!");
		}
	}
	
	/**
	 * Loads the queue from the database.
	 * @author Christian
	 * @param id The id of the queue to be loaded.
	 * @return "0" if something went wrong. "-1" if not logged in or session expired and "1" if queue was loaded successfully.
	 */
	public static Result getQueueItems(String id){
		if(AuthController.check()){
			Queue q = new Queue(id);
			if(q.getItems().length == 0){
				return ok("0|Unable to load the queue!");
			}
			return ok("1|Loaded the queue succesfully|"+saved_queue.render(q));
		}else{
			return ok("-1|Authorization failed!");
		}
	}
}
