package helper;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.view.View;
import android.widget.ProgressBar;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;

// url comes in the last parameter
public class WebRequest extends AsyncTask<String, Integer, String> {

	 URL url;
	 String response = "";
	 final String charset ="UTF-8";
	 String targetURL = null, endResult=null;
	 boolean verifiedCodeStat=false;
	 boolean downloadStat = false;
	 public Navigator webcall;
	 private ProgressBar mPDialog;
	 private final int  TIME_OUT_WAIT = 6500;

	 // private static final String URL_ROOT_API = "http://api.fgroupindonesia.com/fgimobile";

	 // for production purposes call below URL

	 
	 public static final int POST_METHOD = 1, GET_METHOD=2;
	 public static final int SERVER_ERROR = -1, SERVER_NO_RESULT = 0, SERVER_SUCCESS=1, SERVER_VERIFIED=2;
	
	 private String boundary;
	 private static final String LINE_FEED = "\r\n";
	 
	 private int pilihanMethod=0, statusCode=0;
	 private ArrayList<String> keys = new ArrayList();
	 private ArrayList<String> values = new ArrayList(); 

	 private ArrayList<String> keysFiles  = new ArrayList();
	 private ArrayList<FileInputStream> fileStreams = new ArrayList(); 
	 private ArrayList<String> fileNames = new ArrayList(); 

	 private Activity myContext;
	 private boolean multipartform = false;
	 private static boolean waitState = false;

	 public boolean hasData(){
	 	return keys.size()>0;
	 }

	public boolean isMultipartform() {
		return multipartform;
	}

	public void setMultipartform(boolean multipartform) {
		this.multipartform = multipartform;
		
		if(multipartform==true)
		boundary = "===" + System.currentTimeMillis() + "===";
		
	}

	public WebRequest(Activity contIn, Navigator webCallIn){
		myContext = contIn;
		pilihanMethod= GET_METHOD;
		webcall = webCallIn;
	}

	public WebRequest(){
		pilihanMethod= GET_METHOD;
	}

	public WebRequest(int pilihanNyaMethod){
		pilihanMethod = pilihanNyaMethod;
	}

	public void setRequestMethod(int pilihan){
		pilihanMethod = pilihan;
	}
	
	public void clearData(){
		keys = new ArrayList();
		values = new ArrayList();
	}
	
	public void clearDataMultipartForm(){
		keysFiles = new ArrayList();
		fileStreams = new ArrayList();
		fileNames = new ArrayList();
	}

	public void setProgressBar(ProgressBar progBar){
		mPDialog = progBar;
	}

	public static boolean isWaitState(){
		return waitState;
	}

	public void setWaitState(boolean b){
		waitState = b;
	}

	public void setDownloadState(boolean b){
		downloadStat = b;
	}

	public boolean isDownloadState(){
		return downloadStat;
	}

	public int getStatusCode(){
		return statusCode;
	}
	
	public void setStatusCode(int stat){
		statusCode = stat;
	}
	
	public String getEndResult(){
		return endResult;
	}
	
	private void setStatusVerifiedCode(boolean b){
			verifiedCodeStat=b;
	}
	
	public boolean getStatusVerifiedCode(){
		return verifiedCodeStat;
	}
	
	public void setTargetURL(String tujuanURL){
		targetURL = tujuanURL;
	}
	
	public String getAllDataPassed(){
		
		StringBuilder result = new StringBuilder();
		String overall=null;
		
		if(this.isMultipartform()!=true){
			
			// if this is normal instead of multipartform request
			// we do usual keyvalue pairings...
			// this method is to build the 
			// key=value& ' format along with the passing HTTP Request
			
			for (int index=0; index<keys.size(); index++) {
				result.append(keys.get(index));
				result.append("=");
				result.append(values.get(index));
				result.append("&");
			}
			
			// last character remove the &' from the end of bufferstring
			overall= result.toString();
			overall = overall.substring(0, overall.length()-1);
			
			
		}
		
		return overall;
	}
	
	public void addData(String keyIn, String valIn){
		try{
			keys.add(URLEncoder.encode(keyIn, charset));
			values.add(URLEncoder.encode(valIn, charset));
		}catch(Exception ex){
			endResult = "Error while adding data!";
		}
		
	}
	
	public void addData(String keyIn, boolean valIn){
		
		int nilai = valIn? 1:0;
		
		try{
			keys.add(URLEncoder.encode(keyIn, charset));
			values.add(""+nilai);
		}catch(Exception ex){
			endResult = "Error while adding data!";
		}
		
	}
	
	public void addFile(String keyIn, File fileIn){
		
		try{
			keysFiles.add(URLEncoder.encode(keyIn, charset));
			FileInputStream inputStream = new FileInputStream(fileIn);
			fileStreams.add(inputStream);
			fileNames.add(fileIn.getName());
		}catch(Exception ex){
			endResult = "Error while adding file!";
		}
		
	}

	// just for temporarily data passed usually we put it at the front
	public String getFirstData(){
		return values.get(0);
	}

	protected String doInBackground(String... params) {
		 try {

		 	// if this is a GET mETHOD
			 // and has a data parameter
			 // so we combined into a full length of URL
		 	if(this.hasData() && pilihanMethod == GET_METHOD){
		 		if(!targetURL.contains("?")){
		 			targetURL += "?";
				}
		 		targetURL += getAllDataPassed();
			}

				//ShowDialog.message(myContext, "url going is " + targetURL);

			 url = new URL(targetURL);

			 HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		        conn.setReadTimeout(TIME_OUT_WAIT);
		        conn.setConnectTimeout(TIME_OUT_WAIT);
		        conn.setInstanceFollowRedirects(true);
		        
		        if(pilihanMethod==POST_METHOD){
		        	conn.setRequestMethod("POST");
					conn.setDoOutput(true);
		        }else{
		        	conn.setRequestMethod("GET");
					conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36");
					conn.setRequestProperty("Accept-Charset", charset);
		        }
		        
		        // we want to receive the input & output stream
		        conn.setUseCaches(false);
		        conn.setDoInput(true);

		        if(isMultipartform()){
		        	
		        	conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

		            //conn.setRequestProperty("User-Agent", "FGIMobile");
		        	
		        }

		        //conn.connect();


		        if(pilihanMethod == POST_METHOD) {

					OutputStream outputStream = conn.getOutputStream();
					BufferedWriter writer = new BufferedWriter(
							new OutputStreamWriter(outputStream, "UTF-8"));

					if (isMultipartform() == false && isDownloadState() == false) {
						// not for download nor post file

						writer.write(this.getAllDataPassed());
					} else if (isMultipartform() == true && isDownloadState() == false) {
						// we do writing for each data stored
						// for multipartform request

						// write each key-values
						for (int index = 0; index < keys.size(); index++) {
							writer.append("--" + boundary).append(LINE_FEED);
							writer.append("Content-Disposition: form-data; name=\"" + keys.get(index) + "\"")
									.append(LINE_FEED);
							writer.append("Content-Type: text/plain; charset=" + charset).append(
									LINE_FEED);
							writer.append(LINE_FEED);
							writer.append(decode(values.get(index))).append(LINE_FEED);
							writer.flush();

						}

						// write each file contents
						for (int index = 0; index < keysFiles.size(); index++) {
							String fileName = fileNames.get(index);
							writer.append("--" + boundary).append(LINE_FEED);
							writer.append(
									"Content-Disposition: form-data; name=\"" + keysFiles.get(index)
											+ "\"; filename=\"" + fileName + "\"")
									.append(LINE_FEED);
							writer.append(
									"Content-Type: "
											+ conn.guessContentTypeFromName(fileName))
									.append(LINE_FEED);
							writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
							writer.append(LINE_FEED);
							writer.flush();

							FileInputStream inputStream = fileStreams.get(index);
							byte[] buffer = new byte[4096];
							int bytesRead = -1;
							while ((bytesRead = inputStream.read(buffer)) != -1) {
								outputStream.write(buffer, 0, bytesRead);
							}
							outputStream.flush();
							inputStream.close();

							writer.append(LINE_FEED);
							writer.flush();
						}

						// enclosing
						writer.append(LINE_FEED).flush();
						writer.append("--" + boundary + "--").append(LINE_FEED);


					}


					writer.flush();
					writer.close();
					outputStream.close();

					//conn.connect();
				}
		        
		        int responseCode=conn.getResponseCode();

		        if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_BAD_REQUEST ) {

		        	// if this is a usual text reply mode no downloading
					// just casual text return we read
		        	if(isDownloadState()==false) {

						String line;
						BufferedReader br = null;

						if(responseCode == HttpURLConnection.HTTP_OK){
							br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
						}else if(responseCode == HttpURLConnection.HTTP_BAD_REQUEST) {
							br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
						}

						while ((line = br.readLine()) != null) {
							response += line;
						}

					} else {

		        		// this is for downloading progress
						// save to file locally

						int length = conn.getContentLength();

						// the file name expected is here in the first data
						String singleFile = getFirstData();
						String endPath = Environment.getExternalStorageDirectory()
								+ "/Android/data/"+ myContext.getApplicationContext().getPackageName() + "/files/" + singleFile;

						//String endPath = Environment.getExternalStorageDirectory() + File.separator + singleFile;
						//pd.setMax(length / (1000));

						InputStream is = conn.getInputStream();
						FileOutputStream os = new FileOutputStream(endPath);

						byte data[] = new byte[4096];
						long total = 0;
						int count;
						while ((count = is.read(data)) != -1) {
							if (isCancelled()) {
								is.close();
								return null;
							}
							total += count;

							if (length > 0) // only if total length is known
								publishProgress((int) (total * 100 / length));

							os.write(data, 0, count);
						}

						os.close();
						is.close();

						// the respond is actually the file path completely
						response = endPath;

					}
		        }
		        else {
		            response="error";
		        }

			 	endResult = response;
		        
		        // this is for debugging purposes
		        tryToGetStatusCode(endResult);
		        if(conn!=null) {
					conn.disconnect();
				}

		    } catch (Exception e) {
		 		ShowDialog.message(myContext, "error WebRequest " + e.getMessage());
		    	//ErrorLogger.write(e);
		    	onPostExecute(endResult);
		    }

		return null;

	}

	@Override
	protected void onProgressUpdate(Integer... progress) {
		super.onProgressUpdate(progress);
		// if we get here, length is known, now set indeterminate to false

		if(mPDialog!=null)
		mPDialog.setProgress(progress[0]);

	}

	@Override
	protected void onPostExecute(String result) {

		if (endResult != null ) {
			// move to the next Activity
			if (isWaitState() != true) {
				webcall.nextActivity();
			}else{

				// progressbar removal if the work is done
				if(mPDialog!=null){
					mPDialog.setVisibility(View.INVISIBLE);
				}

				webcall.onSuccess(targetURL, endResult);
			}
		} else {
			ShowDialog.message(myContext, "WebRequest error, result is " + endResult);

		}

	}

	
	private void tryToGetStatusCode(String serverReply){
	
		// if we can convert into numbers there are several possibilities
		// 0 - no result
		// 1 - success
		// -1 - error
		// other number

		try{
			statusCode = Integer.parseInt(serverReply);
			
			if(statusCode>=1){
				// if we obtain an ID from here
				// so we said
				if(statusCode==SERVER_VERIFIED){
					setStatusVerifiedCode(true);
				}
				statusCode=SERVER_SUCCESS;
			}
			
		}catch(Exception ex){

			// we ensure this is a json
			if(serverReply!=null){
				if(serverReply.contains("{") && serverReply.contains("}")){
					statusCode = SERVER_SUCCESS;
				}else if(serverReply.length()==0){
					statusCode = SERVER_ERROR;
				}else if(serverReply.contains("error")!=true) {
					statusCode = SERVER_SUCCESS;
				}	
			}else{
				statusCode = SERVER_ERROR;
			}
			
			
		}
	
	
		
	}
	


	public static boolean checkConnection(Context context) {
		final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo activeNetworkInfo = connMgr.getActiveNetworkInfo();

		if (activeNetworkInfo != null) { // connected to the internet
			// Toast.makeText(context, activeNetworkInfo.getTypeName(), Toast.LENGTH_SHORT).show();

			if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
				// connected to wifi
				return true;
			} else if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
				// connected to the mobile provider's data plan
				return true;
			}
		}
		return false;
	}


	public String decode(String mess) throws Exception {
		String val = null;
		val = URLDecoder.decode(mess, "UTF-8");
		return val;
	}
	
}
