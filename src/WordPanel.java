import java.awt.Color;
import java.awt.Graphics;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.CountDownLatch;

import javax.swing.JButton;
import javax.swing.JPanel;

public class WordPanel extends JPanel implements Runnable {
    public static volatile boolean done;
    private WordRecord[] words;
    private int noWords;
    private int maxY;

    /**
     * Sets up the WordPanel for drawing the words
     * @param g uses graphics for drawing the words
     */
    public void paintComponent(Graphics g) {
        int width = getWidth();
        int height = getHeight();
        g.clearRect(0,0,width,height);
        g.setColor(Color.red);
        g.fillRect(0,maxY-10,width,height);

        g.setColor(Color.black);
        g.setFont(new Font("Helvetica", Font.PLAIN, 26));
       //draw the words
       //animation must be added
        for (int i=0;i<noWords;i++){
            //g.drawString(words[i].getWord(),words[i].getX(),words[i].getY());
            g.drawString(words[i].getWord(),words[i].getX(),words[i].getY()+20);  //y-offset for skeleton so that you can see the words
        }

      }

    /**
     * Constructor of the WordPanel class.
      * @param words Accepts an array of WordRecord class
     * @param maxY maximum vertical y value for the screen
     */

    WordPanel(WordRecord[] words, int maxY) {
        this.words=words; //will this work?
        noWords = words.length;
        done=false;
        this.maxY=maxY;
    }

    /**
     * Each thread represents a column (word on screen). Each Thread makes the word drop until the word is
     * either caught, missed, the total of words have been reached or game is ended by interrupting the thread.
     * Threads are created using start.
     */

    public void run() {
        WordRecord word = words[Integer.parseInt(Thread.currentThread().getName().substring(7))%noWords];//%noWords ensures the index is not out of bounds
        while (!done){
            synchronized (this){word.drop(maxY/((word.getSpeed()%maxY)+4));}// maxY/((word.getSpeed()%maxY)+4) was the formula I used to determine how many increments of y a word should fall
            try {
                Thread.sleep(62);
            } catch (InterruptedException e) {
                synchronized (this){word.resetWord();//synchronized so that only one thread is setting the word,
                WordApp.score.resetScore();//setting the scores and
                WordApp.counters();}//setting the score text at a time. all other threads must wait for the current thread to release the lock
                repaint();
                break;//so current thread completes run. Since run is complete, no other threads will be kept waiting and avoid deadlock
            }
            repaint();
            synchronized (this){
                if (word.dropped()) {//including word.dropped() in the lock avoids race conditions
                    word.resetWord();//removes the word and a new one falls from the top
                    WordApp.score.missedWord();//increments the missedWords score atomically so that no other thread my interrupt its execution
                    WordApp.counters();//since set methods are being used I put a lock on all set methods to avoid bad interleavings
                }
            }
            synchronized (this){
                if (word.matchWord(WordApp.text)){//including word.matchWord results in reentry locks for better security
                    WordApp.score.caughtWord(WordApp.text.length());//increments caught words and sets the score value
                    WordApp.counters();//sets the score totals text in the window after every caught word
                }
            }
            if (WordApp.totalWords == WordApp.score.getTotal()){//if the sum of words missed and words caught equals the total words
                done = true;//thread finishes run by making the volatile boolean true. will return the most recent write by any thread (in this case true)
                WordApp.finished();//open result window
            }
        }
    }
}
