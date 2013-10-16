//****************************************************************************************
//Module Name		: 	SCanvas
//Author Name		: 	Marcus Manvinder
//Date				: 	Sept-25-2012
//Purpose			: 	Plugin class to handle the javascript function call.
//Table referred	: 	NA
//Table updated		: 	NA
//Most Important Related Files: com.ith.spen.plugin.SCanvasActivity,java
//****************************************************************************************

package org.apache.cordova.plugin;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * This class invokes SCanvas called from JavaScript.
 */
public class SCanvas extends CordovaPlugin {
	  
	private static final String BACKGROUND_IMAGE_URL = "backgroundImageUrl";
	private static final String SAVEONLYFOREGROUND_IMAGE = "saveOnlyForegroundImage";
	private static final String FOREGROUND_IMAGE_DATA = "foregroundImageData"; 
	
	public static final String SCANVAS_LAUNCH = "com.rub.spen.SCANVAS_LAUNCH";
	
	public static final int REQUEST_CODE = 1;
	
	public CallbackContext callback;
	
	String savedImagePath = null;
	String base64Image = null;
	String foregroundImageData = null;
	
	// ********************************************************************************************************************
	// Author Name		:	Marcus Manvinder		Date	: 	Sept-25-2012
	// Input Parameters	: 	action       - The action to execute.
    //					 	args         - JSONArry of arguments for the plugin.
    //					 	callbackId   - The callback id used when calling back into JavaScript.
	// Purpose			: 	Executes the request and returns PluginResult.
	// ********************************************************************************************************************
	
   public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException  {
    	this.callback = callbackContext;
    	//Log.d("S Pen Plugin", "CallbackId: " + callbackId);
    	
    	String backgroundImageUrl = null;
		boolean saveOnlyForegroundImage = false;
		
        if (action.equals("showCanvas")) {
        	JSONObject obj = args.optJSONObject(0);
            if(obj != null) {
            	backgroundImageUrl = obj.optString(BACKGROUND_IMAGE_URL);//return empty if not defined
            		
            	saveOnlyForegroundImage = obj.optBoolean(SAVEONLYFOREGROUND_IMAGE);//returns false if not defined
            	
            	foregroundImageData = obj.optString(FOREGROUND_IMAGE_DATA);// 
            	
            	if(backgroundImageUrl.isEmpty()) {
            		//default file path
            		backgroundImageUrl = null;
            	}
            	
            	if(foregroundImageData.isEmpty()) {
            		foregroundImageData = null;
            	}
            }
            	
            try {
				Intent intent = new Intent(SCANVAS_LAUNCH);
				intent.addCategory(Intent.CATEGORY_DEFAULT);
			
				Bundle bundle = new Bundle();
				bundle.putString("backgroundImageUrl", backgroundImageUrl);
				bundle.putBoolean("saveOnlyForegroundImage", saveOnlyForegroundImage);
				bundle.putString("foregroundImageData", foregroundImageData);
				
				intent.putExtras(bundle);
				
				this.cordova.startActivityForResult((CordovaPlugin) this, intent, REQUEST_CODE);
	           	Log.d("S Pen", "open SCanvas Called.");
			}
			catch (Exception ex) {
				Log.d("S Pen", "Canvas Launching error.");
			}
        }
        else {
           	Log.d("S Pen", "Invalid Action called");
           	return false;
        }
      
        //return pluginResult;
        //PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
        //pluginResult.setKeepCallback(true);
        return true;
    }
    
	// ********************************************************************************************************************
	// Author Name		:	Marcus Manvinder          Date	: 	Sept-25-2012
	// Input Parameters	: 	requestCode - The request code originally supplied to startActivityForResult(), allowing you to identify from where this result came.
   	//					 	resultCode  - The integer result code returned by the child activity through its setResult().
   	//					 	intent    	- An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
	// Purpose			: 	Called when the activity returns the result.
	// ********************************************************************************************************************

	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		Log.d("SPen", "Returned from Canvas Activity");
		JSONObject obj = new JSONObject();
		if (requestCode == REQUEST_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				
				try {
					
					obj.put("imageData", intent.getStringExtra("base64Image"));
					obj.put("imageUri", intent.getStringExtra("savedImage"));
					
					savedImagePath = intent.getStringExtra("savedImage");
					base64Image = intent.getStringExtra("base64Image");
					Log.d("S Pen", "Setting Success callback");
					this.callback.success(obj);
					
				} 
				catch (Exception e) {
					// Log.d(LOG_TAG, "This should never happen");
				}
				Log.d("SPen", savedImagePath);
			} 
			else if (resultCode == Activity.RESULT_CANCELED) {
				//JSONObject obj = new JSONObject();
				try {
					obj.put("imageData", intent.getStringExtra("base64Image"));
					obj.put("imageUri", intent.getStringExtra("savedImage"));
					
					savedImagePath = null;
					base64Image = null;
				} 
				catch (Exception e) {
					// Log.d(LOG_TAG, "This should never happen");
				}
				this.callback.success();
			} 
			else {
				this.callback.error("error");
			}
		}
	}
}
