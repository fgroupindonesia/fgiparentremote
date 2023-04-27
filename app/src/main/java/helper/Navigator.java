package helper;

// just an interface for WebRequest httpcall
public  interface Navigator {

    public void nextActivity();
    public void onSuccess(String urlTarget, String result);

}
