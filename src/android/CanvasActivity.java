//****************************************************************************************
//Module Name		: 	CanvasActivity
//Author Name		: 	Marcus Manvinder
//Date				: 	Sept-25-2012
//Purpose			: 	This activity is for invoking the SPen Canvas and  perform all the functionalities related to SCanvas.
//Table referred	: 	NA
//Table updated		: 	NA
//Most Important Related Files: org.apache.cordova.plugin.SCanvas.Java
//****************************************************************************************

package com.rub.spen;


import java.io.ByteArrayOutputStream;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

import com.samsung.spen.settings.SettingStrokeInfo;
import com.samsung.spensdk.SCanvasConstants;
import com.samsung.spensdk.SCanvasView;
import com.samsung.spensdk.applistener.HistoryUpdateListener;
import com.samsung.spensdk.applistener.SCanvasInitializeListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class CanvasActivity extends Activity {

	private final float STROKE_WIDTH = 2;
	
	private Context mContext;
	private RelativeLayout mCanvasContainer;
	private SCanvasView mSCanvas;
	
	private LinearLayout tool_menu;
	private ImageButton btnPencil;
	private ImageButton btnEraser;
	private ImageButton btnUndo;
	private ImageButton btnRedo;
	private Button btnClear;
	private ImageButton btnClose;
	
	private Rect	mSrcImageRect = null;
	private Bitmap 	canvasBackground = null;
	private Bitmap 	bmForeground = null;
	
	//private String base64Image = null;
	private String savePath = null;
	String backgroundImageUrl = null;
	boolean saveOnlyForegroundImage = false;
	String foregroundImageData = null;

	private final int    CANVAS_HEIGHT_MARGIN = 0; // Top,Bottom margin  
	private final int    CANVAS_WIDTH_MARGIN = 0; // Left,Right margin
	
	// ********************************************************************************************************************
	// Author Name 		: Marcus Manvinder		Date	:	Sept-25-2012
	// Input Parameters : savedInstanceState - The Bundle passed from the calling activity.
	// Purpose 			: The overridden function of the Activity Class.
	// ********************************************************************************************************************
	public void onCreate(Bundle savedInstanceState) {

		// permits to make a HttpURLConnection
		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}
		super.onCreate(savedInstanceState);
		
		//Restrict the Orientation to landscape only.
		//this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	   	
		setContentView(R.layout.canvas_view);

		Bundle bundle = this.getIntent().getExtras();

		if (bundle != null) {
			backgroundImageUrl = bundle.getString("backgroundImageUrl");
			saveOnlyForegroundImage = bundle.getBoolean("saveOnlyForegroundImage");
			foregroundImageData = bundle.getString("foregroundImageData");
		}
		tool_menu = (LinearLayout) findViewById(R.id.tool_menu);
		btnPencil = (ImageButton) findViewById(R.id.btnPen);
		btnPencil.setOnClickListener(new ImageButton.OnClickListener() {

			// ********************************************************************************************************************
			// Author Name 		: Marcus Manvinder 		Date 	:	Sept-25-2012
			// Input Parameters : view - the view clicked.
			// Purpose 			: The overridden function of the OnClickListener interface and sets the canvas mode to input pen.
			// ********************************************************************************************************************
			public void onClick(View view) {
				// Set the pen mode of SCanvas
				Log.d("S Pen", "Pen mode set");
				mSCanvas.setCanvasMode(SCanvasConstants.SCANVAS_MODE_INPUT_PEN);
			}
		});

		btnEraser = (ImageButton) findViewById(R.id.btnEraser);
		btnEraser.setOnClickListener(new ImageButton.OnClickListener() {
			// ********************************************************************************************************************
			// Author Name 		: Marcus Manvinder		Date 	:	Sept-25-2012
			// Input Parameters : view - the view clicked.
			// Purpose 			: The overridden function of the OnClickListener interface and sets the canvas mode to input eraser.
			// ********************************************************************************************************************
			public void onClick(View view) {
				// Set the eraser mode of SCanvas
				Log.d("S Pen", "Eraser Mode set");
				mSCanvas.setCanvasMode(SCanvasConstants.SCANVAS_MODE_INPUT_ERASER);
			}
		});

		btnUndo = (ImageButton) findViewById(R.id.btnUndo);
		btnUndo.setEnabled(false);

		btnUndo.setOnClickListener(new ImageButton.OnClickListener() {
			// ********************************************************************************************************************
			// Author Name 		: Marcus Manvinder 		Date 	: 	Sept-25-2012
			// Input Parameters : view - the view clicked.
			// Purpose 			: The overridden function of the OnClickListener interface and performs the undo action on the canvas.
			// ********************************************************************************************************************
			public void onClick(View view) {
				// perform undo action
				mSCanvas.undo();
			}
		});

		btnRedo = (ImageButton) findViewById(R.id.btnRedo);
		btnRedo.setEnabled(false);
		btnRedo.setOnClickListener(new ImageButton.OnClickListener() {
			// ********************************************************************************************************************
			// Author Name 		: Marcus Manvinder 		Date 	: 	Sept-25-2012
			// Input Parameters : view - the view clicked.
			// Purpose 			: The overridden function of the OnClickListener interface and performs the redo action on the canvas.
			// ********************************************************************************************************************
			public void onClick(View view) {
				// perform Redo action
				mSCanvas.redo();
			}

		});
		
		btnClear = (Button) findViewById(R.id.btnClear);
		btnClear.setOnClickListener(new ImageButton.OnClickListener() {
			// ********************************************************************************************************************
			// Author Name 		: Marcus Manvinder 		Date	: 	Sept-25-2012
			// Input Parameters : view - the view clicked.
			// Purpose 			: The overridden function of the OnClickListener interface, close the canvas.
			// ********************************************************************************************************************
			public void onClick(View view) {
				// Close the canvas and send the image to server
				Log.d("S Pen", "clear set");
				mSCanvas.setClearImageBitmap(null);
				
//				boolean isImageSaved = saveCanvasImage(getSaveOnlyForegroundImage());
//
//				if (isImageSaved == true) {// if image saved successfully
//					mSCanvas.closeSCanvasView();
//
//					Intent returnIntent = new Intent();
//					returnIntent.putExtra("savedImage", savePath);
//					//returnIntent.putExtra("base64Image", base64Image);
//					setResult(RESULT_OK, returnIntent);
//					CanvasActivity.this.finish();
//				} 
//				else {
//					AlertDialog.Builder builder = new AlertDialog.Builder(CanvasActivity.this);
//
//					builder.setMessage(R.string.canvas_image_save_error_message);
//					builder.setTitle(R.string.error);
//				}
			}
		});		
		
		btnClose = (ImageButton) findViewById(R.id.btnClose);
		btnClose.setOnClickListener(new ImageButton.OnClickListener() {
			// ********************************************************************************************************************
			// Author Name 		: Marcus Manvinder 		Date	: 	Sept-25-2012
			// Input Parameters : view - the view clicked.
			// Purpose 			: The overridden function of the OnClickListener interface, close the canvas.
			// ********************************************************************************************************************
			public void onClick(View view) {
				// Close the canvas and send the image to server
				boolean isImageSaved = saveCanvasImage(getSaveOnlyForegroundImage());

				if (isImageSaved == true) {// if image saved successfully
					mSCanvas.closeSCanvasView();

					Intent returnIntent = new Intent();
					returnIntent.putExtra("savedImage", savePath);
					//returnIntent.putExtra("base64Image", base64Image);
					setResult(RESULT_OK, returnIntent);
					CanvasActivity.this.finish();
				} 
				else {
					AlertDialog.Builder builder = new AlertDialog.Builder(CanvasActivity.this);

					builder.setMessage(R.string.canvas_image_save_error_message);
					builder.setTitle(R.string.error);
				}
			}
		});
		mContext = this;
		mCanvasContainer = (RelativeLayout) findViewById(R.id.canvas_container);
		mSCanvas = new SCanvasView(mContext);
		//mSCanvas = (SCanvasView) findViewById(R.id.canvas_view);

		// History Listener for in SCanvasView.
		mSCanvas.setHistoryUpdateListener(new HistoryUpdateListener() {
			// ********************************************************************************************************************
			// Author Name 		: Marcus Manvinder 		Date	:	Sept-25-2012
			// Input Parameters : undoable - Boolean undoable defines if the Canvas activities are undoable.
			//					  redoable - Boolean redoable defines if the Canvas activities are redoable.
			// Purpose 			: The overridden function of the HistoryUpdateListener interface, enable and disable the redo , undo buttons.
			// ********************************************************************************************************************
			public void onHistoryChanged(boolean undoable, boolean redoable) {
				// enable or disable undo button
				btnUndo.setEnabled(undoable);
				// enable or disable redo button
				btnRedo.setEnabled(redoable);
			}
		});
		
		
		// Defining the layout parameters of the TextView
		// Set the listener to set the background image on SCanvas initialization
		mSCanvas.setSCanvasInitializeListener(mSCanvasInitializeListener);
		
		if(getBackgroundImageUrl() != null) {
			canvasBackground = getBitmapFromURL(backgroundImageUrl);
			
			if(canvasBackground != null){
				mSCanvas.createSCanvasView(canvasBackground.getWidth(),canvasBackground.getHeight());
				mSrcImageRect = new Rect(0, 0, canvasBackground.getWidth(), canvasBackground.getHeight());
			}
		}
		
		//mSCanvas.setCanvasMinZoomScale((float) 0.00001);
		
		mCanvasContainer.addView(mSCanvas);
		setSCanvasViewLayout(getResources().getConfiguration());
		
		
	}

	SCanvasInitializeListener mSCanvasInitializeListener = new SCanvasInitializeListener() {
		// ********************************************************************************************************************
		// Author Name 		: Eric Vikas 		Date	:	Oct-03-2012
		// Input Parameters : view - the view clicked.
		// Purpose 			: The overridden function of the SCanvasInitializeListener interface and sets the background image of SCanvas from URL.
		// ********************************************************************************************************************
		public void onInitialized() {
			// Bitmap canvasBackground;
			// Get the bitmap image from url
			// Set Background Image
			if(!mSCanvas.setBGImage(canvasBackground)){
				Toast.makeText(mContext, "Fail to set Background Image Bitmap.", Toast.LENGTH_LONG).show();
			}
			//mSCanvas.setBGColor(android.graphics.Color.WHITE);
			//mSCanvas.createSCanvasView(canvasBackground.getWidth(),canvasBackground.getHeight());
			// if foreground image is defined set it on canvas.
			if (foregroundImageData != null) {
				//if(foregroundImageData != "null"){
					bmForeground = getBitmapFromURL(foregroundImageData);//BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
				//}
				if(!mSCanvas.setCanvasBitmap(bmForeground)){
					Toast.makeText(mContext, "Fail to set Foregroun Image Bitmap.", Toast.LENGTH_LONG).show();
				}

			}
			
			
			
			// set the Stroke Width
			SettingStrokeInfo strokeInfo = new SettingStrokeInfo();
			strokeInfo.setStrokeWidth(STROKE_WIDTH);
			mSCanvas.setSettingStrokeInfo(strokeInfo);
			
			// Enable HistoricalOperationSupport
			mSCanvas.setHistoricalOperationSupport(true);
		}
	};
	// ********************************************************************************************************************
	// Author Name 		: Marcus Manvinder 		Date	:	Oct-03-2012
	// Input Parameters : NA
	// Purpose 			: This is the getter function for background image URL.
	// ********************************************************************************************************************
	private String getBackgroundImageUrl() {
		return this.backgroundImageUrl;
	}

	// ********************************************************************************************************************
	// Author Name 		: Marcus Manvinder 		Date	: 	Oct-03-2012
	// Input Parameters : NA
	// Purpose 			: This is the getter function for boolean saveOnlyForegroundImage.
	// ********************************************************************************************************************
	private boolean getSaveOnlyForegroundImage() {
		return this.saveOnlyForegroundImage;
	}

	// ********************************************************************************************************************
	// Author Name 		: Eric Vikas 		Date	:	Oct-03-2012
	// Input Parameters : imageURL - URL of the background image.
	// Purpose 			: Function to create an http connection and decode the bitmap from url.
	// ********************************************************************************************************************
	public Bitmap getBitmapFromURL(String imageURL) {
		try {
			URL url = new URL(imageURL);
			// Create an http connection
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoInput(true);
			connection.connect();
			InputStream input = connection.getInputStream();
			Bitmap myBitmap = BitmapFactory.decodeStream(input);
			input.close();// close the input Stream
			return myBitmap;
		} 
		catch (IOException e) {
			e.printStackTrace();
			Log.d("Exception", e.getMessage());
			return null;
		}
	}

	// ********************************************************************************************************************
	// Author Name 		: Marcus Manvinder 		Date	: 	Sept-28-2012
	// Input Parameters : bSaveOnlyForegroundImage - The boolean bSaveOnlyForegroundImage specifies if only foreground image need to saved.
	// Purpose 			: Function to save the SCanvas background image
	// ********************************************************************************************************************
	public boolean saveCanvasImage(boolean bSaveOnlyForegroundImage) {
		Bitmap bmCanvas = mSCanvas.getCanvasBitmap(false);//bSaveOnlyForegroundImage);
		
		//make bmCanvas of the original image dimension
		//if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
		//	bmCanvas = Bitmap.createScaledBitmap(bmCanvas,  (mSrcImageRect.right - mSrcImageRect.left), (mSrcImageRect.bottom - mSrcImageRect.top), true);
		//}
		
		if (saveBitmapJPEG(bmCanvas)) {
			//saveBitmapBase64(bmCanvas) && 
			Log.d("S Pen", "Saved image successfully");
			return true;
		} 
		else {
			Toast.makeText(mContext, "Error occured to save image.", Toast.LENGTH_SHORT).show();
			return true;
		}
	}

	// ********************************************************************************************************************
	// Author Name 		: Marcus Manvinder 		Date	: 	Oct-05-2012
	// Input Parameters : bmCanvas - The Bitmap data need to be saved as base64.
	// Purpose 			: Function to return the captured image as base64 string.
	// ********************************************************************************************************************
//	public boolean saveBitmapBase64(Bitmap bmCanvas) {
//		Log.d("S Pen", "Saved Image height: " + bmCanvas.getHeight() + " , Width:  " + bmCanvas.getWidth());
//
//		try {
//			ByteArrayOutputStream baos = new ByteArrayOutputStream();
//			bmCanvas.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//
//			byte[] byteArray = baos.toByteArray();
//
//			base64Image = Base64.encodeToString(byteArray, Base64.DEFAULT);
//
//			return true;
//		} 
//		catch (Exception ex) {
//			ex.printStackTrace();
//		}
//
//		return false;
//	}

	// ********************************************************************************************************************
	// Author Name 		: Marcus Manvinder 		Date	: 	Oct-01-2012
	// Input Parameters : bmCanvas - The Bitmap data need to be saved.
	// Purpose 			: Function to save PNG image.
	// ********************************************************************************************************************
	private boolean saveBitmapJPEG(Bitmap bmCanvas) {
		UUID uuid= UUID.randomUUID();
		String FILENAME = uuid.toString()+".jpg";
		try {

			FileOutputStream out = openFileOutput(FILENAME, Context.MODE_PRIVATE);

			savePath = this.getFileStreamPath(FILENAME).toString();

			bmCanvas.compress(Bitmap.CompressFormat.JPEG, 100, out);
			out.flush();
			out.close();
			return true;
		} 
		catch (Exception ex) {
			ex.printStackTrace();
		}

		return false;
	}
	
	// ********************************************************************************************************************
	// Author Name 		: Marcus Manvinder 		Date	: 	Nov-15-2012
	// Input Parameters : newConfig - New changed configuration.
	// Purpose 			: Overridden function to handle the change of configuration.
	// ********************************************************************************************************************
	
//	public void onConfigurationChanged(Configuration newConfig) {
//		//String temp= mSCanvas.saveSAMMData();
//		if(!mSCanvas.setCanvasBaseZoomScale((float)  newConfig.screenWidthDp/canvasBackground.getWidth())){
//			Log.d("S Pen", "basezoom fail");
//		}
//		//mCanvasContainer.removeView(mSCanvas);
//		super.onConfigurationChanged(newConfig);
//		//mCanvasContainer.addView(mSCanvas);
//		if(!mSCanvas.setCanvasBaseZoomScale((float)  newConfig.screenWidthDp/canvasBackground.getWidth())){
//			Log.d("S Pen", "basezoom fail");
//		}
//		
//		setSCanvasViewLayout(newConfig);
//	}
	
	// ********************************************************************************************************************
	// Author Name 		: Marcus Manvinder 		Date	: 	Nov-15-2012
	// Input Parameters : NA
	// Purpose 			: Function to resize the mSCanvas container.
	// ********************************************************************************************************************
	void setSCanvasViewLayout(Configuration newConfig) {
		//mSCanvas.setCanvasBaseZoomScale((float)1.0);
		//DisplayMetrics displayMetrics = new DisplayMetrics();
		
		//WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
		//wm.getDefaultDisplay().getMetrics(displayMetrics);
		int nScreenWidth = 0;
		int nScreenHeight = 0;
	    // Checks the orientation of the screen
	    if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
	    	nScreenWidth = newConfig.screenWidthDp;
			nScreenHeight = newConfig.screenHeightDp;
	    } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
	    	nScreenWidth = newConfig.screenWidthDp;
			nScreenHeight = newConfig.screenHeightDp;
	    }
	    Log.d("S Pen", "Orientation cahgne h" +nScreenHeight+" ");
	    Log.d("S Pen", "Orientation cahgne w" +nScreenWidth);

		int nImageWidth = canvasBackground.getWidth();
		int nImageHeight = canvasBackground.getHeight();
		Log.d("S Pen", "Orientation i h" +nImageHeight);
	    Log.d("S Pen", "Orientation i w" +nImageWidth);
	    Log.d("S Pen", "getzoom" +mSCanvas.getCanvasZoomScale());
		float fResizeWidth = (float)  nScreenWidth/nImageWidth ;
		float fResizeHeight = (float) nScreenHeight / nImageHeight;
		//mSCanvas.setCanvasMinZoomScale((float) 0.0001);
		// Fit to Height
		//if (fResizeWidth > fResizeHeight) {
			//mSCanvas.setCanvasZoomScale(fResizeHeight,false);
		//}
		// Fit to Width
		//else {
			//mSCanvas.setCanvasZoomScale(fResizeWidth,false);
		//}	
		//mSCanvas.setBaseScale(scale)
		//mSCanvas.setBaseScale(fResizeWidth);//.setCanvasSize(nImageWidth, nImageHeight);
		mSCanvas.setCanvasMinZoomScale(fResizeWidth);
		mSCanvas.setCanvasMaxZoomScale(fResizeHeight);
		if(!mSCanvas.setCanvasZoomScale(1,true)){
			Log.d("S Pen", "zoom fail");
		}
		Log.d("S Pen", "shouldzoom" +fResizeWidth);
		Log.d("S Pen", "getzoom" +mSCanvas.getCanvasZoomScale());
		//mSCanvas.setCanvasZoomScale(arg0, arg1)
		//Rect rectCanvas = getMaximumCanvasRect(mSrcImageRect, CANVAS_WIDTH_MARGIN, CANVAS_HEIGHT_MARGIN);
		//int nCurWidth = rectCanvas.right - rectCanvas.left;
		//int nCurHeight = rectCanvas.bottom - rectCanvas.top;

		// Place SCanvasView In the Center
		//FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mCanvasContainer.getLayoutParams();
		//layoutParams.width = nCurWidth;
		//layoutParams.height = nCurHeight;
		//layoutParams.gravity = Gravity.CENTER;
		
		//mCanvasContainer.setLayoutParams(layoutParams);
	}

	// ********************************************************************************************************************
	// Author Name 		: Marcus Manvinder 		Date	: 	Nov-15-2012
	// Input Parameters : rectImage - Background image rect.
	//					: nMarginWidth - Margin for the Left and Right side.
	//					: nMarginHeight - Margin for the top and bottom.
	// Purpose 			: Function to get the minimum image scaled rect which is fit to current screen.
	// ********************************************************************************************************************
	Rect getMaximumCanvasRect(Rect rectImage, int nMarginWidth, int nMarginHeight) {
			Rect menubounds = new Rect();
			tool_menu.getDrawingRect(menubounds);
			int menuheight=menubounds.bottom - menubounds.top;
		//if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			DisplayMetrics displayMetrics = new DisplayMetrics();
			
			WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
			wm.getDefaultDisplay().getMetrics(displayMetrics);
			
			int nScreenWidth = displayMetrics.widthPixels - nMarginWidth;
			int nScreenHeight = displayMetrics.heightPixels - nMarginHeight - menuheight-100;
	
			int nImageWidth = rectImage.right - rectImage.left;
			int nImageHeight = rectImage.bottom - rectImage.top;
	
			float fResizeWidth = (float) nScreenWidth / nImageWidth;
			float fResizeHeight = (float) nScreenHeight / nImageHeight;
			float fResizeRatio;
			int nResizeWidth;
			int nResizeHeight;
			// Fit to Height
			if (fResizeWidth > fResizeHeight) {
				nResizeHeight= (int) (nImageHeight * fResizeHeight);
				nResizeWidth = (int) (0.5 + (nResizeHeight * nImageWidth) / (float) nImageHeight);
			}
			// Fit to Width
			else {
				nResizeWidth = (int) (nImageWidth * fResizeWidth);
				nResizeHeight = (int) (0.5 + (nResizeWidth * nImageHeight) / (float) nImageWidth);
			}
			
			return new Rect(0, 0, nResizeWidth, nResizeHeight);
		//}
		//else {
		//	return rectImage;
		//}
	}
}
