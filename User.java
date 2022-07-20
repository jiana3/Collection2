//user class to store user's information
public class User {
	
    private String username;  //user's name
    private int count;  //the number of guess

    public User(String username) {  //create a user with user name
		super();
		this.username = username;
		this.count = 0;
	}

	public String getUsername() {
        return username;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
