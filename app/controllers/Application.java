package controllers;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import models.DBConnection;
import models.Queue;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.design;
import views.html.main;

public class Application extends Controller {
	public static DBConnection db = new DBConnection();

	
	/**
	 * @todo fill "ok" method
	 * 
	 */
    public static Result index() {
    	if(AuthController.check()) {   		
    		return ok(design.render(main.render(Application.db.selectAll(Command.class), Application.db.selectAll(ComplexCommand.class), Application.db.selectAll(Queue.class))));
    	}
    	else {
    		return redirect("/auth");
    	}
    }
    
    public static String sha1(String input) {
        MessageDigest mDigest = null;
		try {
			mDigest = MessageDigest.getInstance("SHA1");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        byte[] result = mDigest.digest(input.getBytes());
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < result.length; i++) {
            sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
        }
         
        return sb.toString();
    }
  
}
