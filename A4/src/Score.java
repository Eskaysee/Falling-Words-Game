import java.util.concurrent.atomic.AtomicInteger;
public class Score {
	private AtomicInteger missedWords;
	private AtomicInteger caughtWords;
	private AtomicInteger gameScore;

    /**Score constructor contains atomic variables that serve as sore counters
     * made atomic for thread safety purpose
     */

	Score() {
		missedWords = new AtomicInteger(0);
		caughtWords = new AtomicInteger(0);
		gameScore = new AtomicInteger(0);
	}
		
	// all getters and setters must be synchronized

    /**
     * gets the total number of missed words
     * @return int number of missed words
     */
	public synchronized int getMissed() {
		return missedWords.get(); //put in a lock to avoid race conditions
	}

    /**
     * gets the total number of caught words
     * @return int number of caught words
     */
	public synchronized int getCaught() {
		return caughtWords.get();//put in a lock to avoid race conditions
	}

    /**
     * gets the sum of the total number of words caught and the total number of words missed
     * @return int returns the total number of words
     */
	public synchronized int getTotal() {//not including the lock could result in data races since I am READING the number
		return (missedWords.get()+caughtWords.get());//of missed words and the number of caught words and then WRITING
	}											//the sum of the two as a total. the process may get interrupted without the lock present

    /**
     * gets the Score for the game
     * @return int returns the number of points
     */
	public synchronized int getScore() {
		return gameScore.get();//put in a lock to avoid race conditions
	}
	
	public synchronized void missedWord() {
		missedWords.getAndIncrement();//placed in a lock to ensure the operation is not interrupted by another thread.
		// that could potentially lead to bad interleavings.
		// other threads only have access once lock is released
	}

    /**
     * increments the words caught and add to the score the length of the word caught
     * @param length number of letters in the string
     */
	public synchronized void caughtWord(int length) {
		caughtWords.getAndIncrement();//read and write of same memory location
		gameScore.set(gameScore.get()+length);//not including this in a lock may result in the wrong result for gameScore
	}									//if get() was also not included included in as any other thread would have access to it

	public synchronized void resetScore() {
		caughtWords.set(0);//all three of these are atomic so the process of setting these values wont be interrupted
		missedWords.set(0);//by another thread as the setting of the values will either be fully completed or not at all
		gameScore.set(0);//the three atomic processes are placed in a lock to ensure the full completion of the resetScore method
	}
}
