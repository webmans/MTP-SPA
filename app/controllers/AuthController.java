package controllers;

import models.Parameters;
import models.Session;
import models.User;
import play.mvc.*;
import play.mvc.Http.Cookie;

import views.html.*;

public class AuthController extends Controller {
	
	private static Session s;
	
	public static Session getSession(){
		return AuthController.s;
	}
	/**
	 * Checks if the session is still valid.
	 * @author Christian
	 * @return true if session still valid, false otherwise.
	 */
	public static boolean check(){
		Cookie sessKey = request().cookies().get("sesskey");
		Cookie sessId = request().cookies().get("sessid");
		if(sessKey != null){
			AuthController.s = new Session(sessId.value());
			Long time = System.currentTimeMillis();
			if(sessKey.value().equals(Application.sha1(AuthController.s.getKey())) && time-AuthController.s.getUpdate()<1000*60*20){
				AuthController.s.setUpdate(System.currentTimeMillis());
				Application.db.update(AuthController.s, "update" , time.toString());
				return true;
			}
		}
		return false;
	}
	/**
	 * Logs the user into the system.
	 * @author Christian
	 * @return "1" if login was successful, "0" if Login failed.
	 */
	public static Result login(){
		if(!AuthController.check()){
			User u = new User();
			u.init(Parameters.get("email"));
			if(u.createSession(Parameters.get("passwd"))){
				return ok("1|Login succesful!");
			}
		}
		return ok("0|Login failed!");
	}
	
	/**
	 * Logs the user out of the system.
	 * @author Christian
	 * @return A redirect to the Login-Screen.
	 */
	public static Result logout(){
		if(AuthController.check()){
			AuthController.s = new Session(request().cookies().get("sessid").value());
			AuthController.s.getUser().setSession(null);
			Application.db.update(AuthController.s.getUser(), "session", "");
			Application.db.delete(AuthController.s);
			response().discardCookie("sessid");
			response().discardCookie("sesskey");
		}
		return redirect("/auth");
	}
	/**
	 * Redirects the user to the NAO-RCC Panel
	 * @author Christian
	 */
	public static Result start(){
		if(AuthController.check()) {
			return redirect("/");
		}
    	else {
    		return ok(design.render(login.render()));
    	}
	}
	
	
	
}
