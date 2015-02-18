package models;

import java.util.Map;

import play.mvc.Controller;

public class Parameters {
	
	/**
	 * Gets the Parameters for a given key.
	 * @author Christian
	 * @param key The key of the part of the POST.
	 * @return The parameters of the key.
	 */
	public static String get(String key){
		if(Controller.request().body().asFormUrlEncoded() != null){
			Map<String,String[]> params = Controller.request().body().asFormUrlEncoded();
			if(params.containsKey(key)){
				return params.get(key)[0];
			}
		}
		return "";
	}
}
