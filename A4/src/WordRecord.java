public class WordRecord {
	private String text;
	private  int x;
	private int y;
	private int maxY;
	private boolean dropped;
	
	private int fallingSpeed;
	private static int maxWait=1500;
	private static int minWait=100;

	public static WordDictionary dict;
	

	
	WordRecord() {
		text="";
		x=0;
		y=0;	
		maxY=300;
		dropped=false;
		fallingSpeed=(int)(Math.random() * (maxWait-minWait)+minWait); 
	}
	
	WordRecord(String text) {
		this();
		this.text=text;
	}

	/**
	 * Constructor for the WordRecord class
	 * @param text word
	 * @param x horizontal position
	 * @param maxY position of red rectangle
	 */
	WordRecord(String text,int x, int maxY) {
		this(text);
		this.x=x;
		this.maxY=maxY;
	}
	
// all getters and setters must be synchronized

	public synchronized  void setY(int y) {
		if (y>maxY) {
			y=maxY;
			dropped=true;
		}
		this.y=y;
	}
	
	public synchronized  void setX(int x) {
		this.x=x;
	}
	
	public synchronized  void setWord(String text) {
		this.text=text;
	}

	/**
	 *get the word as a string
	 * @return String This is the word
	 */
	public synchronized  String getWord() {
		return text;
	}
	
	public synchronized  int getX() {
		return x;
	}	
	
	public synchronized  int getY() {
		return y;
	}

	/**
	 * acquires the falling speed
	 * @return int is the fallingspeed
	 */
	public synchronized  int getSpeed() {
		return fallingSpeed;
	}

	public synchronized void setPos(int x, int y) {
		setY(y);
		setX(x);
	}
	public synchronized void resetPos() {
		setY(0);
	}

	/**
	 * removes the word, creates an new one and resets the position of where the old word used to be.
	 */
	public synchronized void resetWord() {
		resetPos();
		text=dict.getNewWord();
		dropped=false;
		fallingSpeed=(int)(Math.random() * (maxWait-minWait)+minWait); 
		//System.out.println(getWord() + " falling speed = " + getSpeed());

	}

	/**
	 * Checks if the word typed by user matches the word stored in this class
	 * @param typedText text from the user
	 * @return boolean true or false depending on whether the word matches
	 */
	public synchronized boolean matchWord(String typedText) {
		//System.out.println("Matching against: "+text);
		if (typedText.equals(this.text)) {
			resetWord();
			return true;
		}
		else
			return false;
	}

	/**
	 * lowers the word
	 * @param inc how far down the word should drop
	 */
	public synchronized  void drop(int inc) {
		setY(y+inc);
	}

	/**
	 * Checks whether the word is in the red zone
	 * @return boolean True or False
	 */
	public synchronized  boolean dropped() {
		return dropped;
	}

}
