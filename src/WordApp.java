import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.io.IOException;


import java.util.Scanner;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
//model is separate from the view.

public class WordApp {
//shared variables
	static int noWords=4;
	static int totalWords;

   	static int frameX=1000;
	static int frameY=600;
	static int yLimit=480;

	static WordDictionary dict = new WordDictionary(); //use default dictionary, to read from file eventually

	static WordRecord[] words;
	static volatile boolean done;  //must be volatile
	static 	Score score = new Score();

	static WordPanel w;

	static String text = "";
	static JLabel caught, missed, scr;

	public static void setupGUI(int frameX,int frameY,int yLimit) {
		// Frame init and dimensions
    	JFrame frame = new JFrame("WordGame"); 
    	frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    	frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				super.windowClosing(e);
				confirmation();
			}
		});
    	frame.setSize(frameX, frameY);
    	
      	JPanel g = new JPanel();
        g.setLayout(new BoxLayout(g, BoxLayout.PAGE_AXIS)); 
      	g.setSize(frameX,frameY);


		w = new WordPanel(words,yLimit);
		w.setSize(frameX,yLimit+100);
	    g.add(w);
	    
	    
	    JPanel txt = new JPanel();
	    txt.setLayout(new BoxLayout(txt, BoxLayout.LINE_AXIS)); 
	    caught =new JLabel("Caught: " + score.getCaught() + "    ");
	    missed =new JLabel("Missed:" + score.getMissed()+ "    ");
	    scr =new JLabel("Score:" + score.getScore()+ "    ");
	    txt.add(caught);
	    txt.add(missed);
	    txt.add(scr);
    
	    Thread[] threads = new Thread[noWords];
  
	    final JTextField textEntry = new JTextField("",20);
	   textEntry.addActionListener(new ActionListener()
	    {
	      public void actionPerformed(ActionEvent evt) {
	          text = textEntry.getText();
	          textEntry.setText("");
	          textEntry.requestFocus();
	      }
	    });
	   
	   txt.add(textEntry);
	   txt.setMaximumSize( txt.getPreferredSize() );
	   g.add(txt);
	    
	    JPanel b = new JPanel();
        b.setLayout(new BoxLayout(b, BoxLayout.LINE_AXIS)); 
	   	JButton startB = new JButton("Start");;
		
			// add the listener to the jbutton to handle the "pressed" event
        /**
         * creates and starts multiple threads of the WordPanel class and stores them in an array.
         * Words on screen begin falling after the start button is pressed
         */
			startB.addActionListener(new ActionListener()
		    {
		      public void actionPerformed(ActionEvent e)
		      {
		    	  text = "";
				  for (int i = 0; i < noWords; i++) {
					  threads[i] = new Thread(w);
					  threads[i].start();
				  }
				  textEntry.requestFocus();  //return focus to the text entry field
		      }
		    });

		JButton endB = new JButton("End");;
			
				// add the listener to the jbutton to handle the "pressed" event
				endB.addActionListener(new ActionListener()
			    {/**
                 * Interrupts the threads stored in the array. This will safely complete the threads by
                 * breaking the loop in the run method of the WordPanel class and then setting the array
                 * positions to zero. This happens when the end button is pressed
                 */
			      public void actionPerformed(ActionEvent e)
			      {
					  for (int i=0;i<noWords;i++){
					  	threads[i].interrupt();
					  	threads[i]=null;
					  }
				  }
			    });

		JButton quitB = new JButton("Quit");;

		// add the listener to the jbutton to handle the "pressed" event
		quitB.addActionListener(new ActionListener()
		{
            /**
             * Quits the game, but first confirms with the user first
             */
			public void actionPerformed(ActionEvent e)
			{
				if (threads[0]!=null) {
					endB.doClick();
					try {
						Thread.currentThread().sleep(2000);
					} catch (InterruptedException ex) {
						ex.printStackTrace();
					}
				}
				confirmation();
			}
		});

		b.add(startB);
		b.add(endB);
		b.add(quitB);

		g.add(b);

      	frame.setLocationRelativeTo(null);  // Center window on screen.
      	frame.add(g); //add contents to window
        frame.setContentPane(g);
        //frame.pack();  // don't do this - packs it into small space
        frame.setVisible(true);
	}

    /**
     * updates the counters on screen as th falling words are caught or dropped
     */
	public synchronized static void counters(){
		caught.setText("Caught: " + score.getCaught() + "    ");
		missed.setText("Missed:" + score.getMissed()+ "    ");
		scr.setText("Score:" + score.getScore()+ "    ");
	}

	private static void confirmation(){
		///**Confirm whether or not the user wants to exit the game**/
		JFrame peace = new JFrame("Confirm Exit");
		peace.setSize(300, 200);
		peace.setLayout(new BorderLayout());
		JLabel confirm = new JLabel("Are you sure you want to quit WordGame?");
		peace.add(confirm, BorderLayout.CENTER);
		JPanel buttonsDisplay = new JPanel();
		buttonsDisplay.setLayout(new FlowLayout());
		JButton exitButton = new JButton("Yes");
		exitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		buttonsDisplay.add(exitButton);

		JButton cancelButton = new JButton("No");
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				peace.dispose();
			}
		});
		buttonsDisplay.add(cancelButton);

		//Add the panel of buttons
		peace.add(buttonsDisplay, BorderLayout.SOUTH);
		peace.setLocationRelativeTo(null);
		peace.setVisible(true);
	}

    /**
     *takes words from a file and adds them to an array
     * @param filename name of textFile
     * @return String[] string array of words
     */
	public static String[] getDictFromFile(String filename) {
		String [] dictStr = null;
		try {
			Scanner dictReader = new Scanner(new FileInputStream(filename));
			int dictLength = dictReader.nextInt();
			//System.out.println("read '" + dictLength+"'");

			dictStr=new String[dictLength];
			for (int i=0;i<dictLength;i++) {
				dictStr[i]=new String(dictReader.next());
				//System.out.println(i+ " read '" + dictStr[i]+"'"); //for checking
			}
			dictReader.close();
		} catch (IOException e) {
	        System.err.println("Problem reading file " + filename + " default dictionary will be used");
	    }
		return dictStr;

	}

    /**
     * opens a window displaying the players results as well as a comment on their performance
     */
	public static void finished(){
		JFrame results = new JFrame("Results");
		results.setLayout(new BorderLayout());
		results.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		if (score.getCaught() == totalWords){
			results.setSize(250,250);
			JLabel congrats = new JLabel("Congratulations! You beat the game.");
			results.add(congrats, BorderLayout.CENTER);
		}
		else{
			results.setSize(450,350);
			JLabel over = new JLabel("GAME OVER");
			over.setFont(new Font("Comic Sans", Font.PLAIN, 25));
			double percentage = score.getCaught()/(double)totalWords*100;
			JLabel cght = new JLabel("You caught "+score.getCaught()+" words.");
			JLabel mssd = new JLabel("You missed "+score.getMissed()+" words.");
			JLabel ratio = new JLabel("You got "+percentage+"% out of the total "+totalWords+" correct");
			JLabel points = new JLabel("Your total score is "+score.getScore()+".");
			JLabel message = new JLabel("Better luck next time!");
			JPanel cntr = new JPanel();
			cntr.setLayout(new GridLayout(4,1));
			results.add(over, BorderLayout.NORTH);
			results.add(cntr, BorderLayout.CENTER);
			cntr.add(cght);
			cntr.add(mssd);
			cntr.add(ratio);
			cntr.add(points);
			results.add(message, BorderLayout.SOUTH);
		}
		results.setLocationRelativeTo(null);
		results.setVisible(true);
	}

	public static void main(String[] args) {
		//deal with command line arguments
		totalWords=Integer.parseInt(args[0]);  //total words to fall
		noWords=Integer.parseInt(args[1]); // total words falling at any point
		assert(totalWords>=noWords); // this could be done more neatly
		String[] tmpDict=getDictFromFile(args[2]); //file of words
		if (tmpDict!=null)
			dict= new WordDictionary(tmpDict);
		
		WordRecord.dict=dict; //set the class dictionary for the words.
		
		words = new WordRecord[noWords];  //shared array of current words

		setupGUI(frameX, frameY, yLimit);
    	//Start WordPanel thread - for redrawing animation

		int x_inc=(int)frameX/noWords;
	  	//initialize shared array of current words

		for (int i=0;i<noWords;i++) {
			words[i]=new WordRecord(dict.getNewWord(),i*x_inc,yLimit);
		}
	}

}
