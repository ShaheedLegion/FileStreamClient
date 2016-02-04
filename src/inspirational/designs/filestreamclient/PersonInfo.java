package inspirational.designs.filestreamclient;

import inspirational.designs.filestreamclient.network.ClientSocketConnection.ServerPerson;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

public class PersonInfo {

	// The person details just has info about the users connected to this session.
	public class PersonDetails {
		private String name;
		private String photoUrl;
		private String email;
		private Bitmap photo;
		PersonImageLoaded loadedInterface;
		
		public PersonDetails(String name, String photo, String email, PersonImageLoaded loadedInterface) {
			this.loadedInterface = loadedInterface;
			this.name = name;
			this.photoUrl = photo;
			this.email = email;
			this.photo = null;
			new LoadProfileImage(this).execute(this.photoUrl);
		}
		
		public void setPersonBitmap(Bitmap photo) {
			this.photo = photo;
			
			if (this.loadedInterface != null)
				loadedInterface.onImageLoaded(this.name, this.photoUrl, this.email);
		}

		public String getName() {
			return name;
		}
		public Bitmap getPhoto() {
			return photo;
		}
		public String getPhotoUrl() {
			return photoUrl;
		}
		public String getEmail() {
			return email;
		}
	}	

	/**
	 * Background Async task to load user profile picture from url
	 * */
	private class LoadProfileImage extends AsyncTask<String, Void, Bitmap> {
		PersonDetails details;
	    public LoadProfileImage(PersonDetails details) {
	    	this.details = details;
	    }

	    protected Bitmap doInBackground(String... urls) {
	        String urldisplay = urls[0];
	        Bitmap mIcon11 = null;
	        try {
	            InputStream in = new java.net.URL(urldisplay).openStream();
	            mIcon11 = BitmapFactory.decodeStream(in);
	        } catch (Exception e) {
	            Log.d("FileStreamClient", e.getMessage());
	            e.printStackTrace();
	        }
	        return mIcon11;
	    }

	    protected void onPostExecute(Bitmap result) {
	    	this.details.setPersonBitmap(result);
	    }
	}
	
	private PersonDetails currentUser = null;
	private List<PersonDetails> users = new ArrayList<PersonDetails>();
	private static PersonInfo instance = null;
	private ArrayList<ServerPerson> people = new ArrayList<ServerPerson>();
	
	public static PersonInfo getInstance() {
		if (instance == null)
			instance = new PersonInfo();
		return instance;
	}
	
	private PersonInfo() {
		
	}
	
	public ArrayList<ServerPerson> getServerPeople() {
		return people;
	}
	
	public void setCurrentUser(String name, String photo, String email, PersonImageLoaded loadedInterface) {
		if (currentUser != null)
			return;

		currentUser = new PersonDetails(name, photo, email, loadedInterface);
	}
	
	public void addUser(String name, String photo, String email, PersonImageLoaded loadedInterface) {
		for (PersonDetails details : users) {
			if (details.name.equalsIgnoreCase(name) && details.photoUrl.equalsIgnoreCase(photo))
				return;
		}

		users.add(new PersonDetails(name, photo, email, loadedInterface));
	}
	
	public void removeUser(String name, String photo, String email, PersonImageLoaded loadedInterface) {
		// Do nothing while we test.
	}
	
	public PersonDetails findUser(String name, String photoUrl, String email) {
		for (PersonDetails details: users) {
			if (details.name.equalsIgnoreCase(name) && details.photoUrl.equalsIgnoreCase(photoUrl) && details.email.equalsIgnoreCase(email))
				return details;
		}
		
		if (currentUser != null && currentUser.name.equalsIgnoreCase(name) && currentUser.photoUrl.equalsIgnoreCase(photoUrl) && currentUser.email.equalsIgnoreCase(email))
			return currentUser;
	
		return null;
	}
	
	public PersonDetails getCurrentUser() {
		return currentUser;
	}
	
	public List<PersonDetails> getUserList() { return users; }
}
