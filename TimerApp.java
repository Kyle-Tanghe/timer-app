package timerapp;
import static java.lang.System.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
/**
 *
 * @author Kyle
 */
public class TimerApp {
    public static void main(String[] args) {
        
        TimerFrame frame = new TimerFrame();
        frame.addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e){
                int result = JOptionPane.showConfirmDialog(frame,
                        "Are you sure you want to exit the application?",
                        "Exit Application",
                        JOptionPane.YES_NO_OPTION);
                if(result == JOptionPane.YES_OPTION){
                    exit(0);
                }
            }
        });
    }
}
class TimerFrame extends JFrame
{
    private GridBagConstraints c = new GridBagConstraints();
    private JPanel northPanel = new JPanel(new GridBagLayout());
    private JLabel enterTextLabel = new JLabel("Enter number of minutes for timer");
    private JTextField enterText = new JTextField();
    private JButton enterButton = new JButton("Enter");

    private JPanel centerPanel = new JPanel(new GridBagLayout());
    private JLabel startStopLabel = new JLabel("Press start to begin timer");
    private JButton startButton = new JButton("Start");
    private JButton stopButton = new JButton("Stop");
    private JButton clearButton = new JButton("Clear");

    private JPanel southPanel = new JPanel(new GridBagLayout());
    private JLabel timerText = new JLabel("0:00");
    ProcessEvents processEvents = new ProcessEvents(enterText,enterButton,startButton,stopButton,clearButton,timerText,this);
    
    //initialize important parts of the
    //app without making cosmetic code messy
    {
        PlainDocument doc = (PlainDocument) enterText.getDocument();
        doc.setDocumentFilter(new FilterTextBox());
        enterButton.addActionListener(processEvents);
        startButton.addActionListener(processEvents);
        stopButton.addActionListener(processEvents);
        clearButton.addActionListener(processEvents);
        timerText.addPropertyChangeListener("text",new PropertyChangeListener(){
            @Override
            public void propertyChange(PropertyChangeEvent e){
                if(e.getNewValue().toString().equals("0:00") && processEvents.getChangeTimerText().isAlive()){
                    if(processEvents.getChangeTimerText() != null){//if the thread that handles changing timerText is alive
                        processEvents.getChangeTimerText().interrupt();
                        processEvents.resetTimer();
                        enterButton.setEnabled(true);
                        stopButton.setVisible(false);
                        startButton.setVisible(true);
                        enterText.setEnabled(true);
                        Thread beep = new Thread(new Runnable(){
                            @Override
                            public void run(){
                                while(true){
                                    try{
                                        Toolkit.getDefaultToolkit().beep();
                                        Thread.sleep(1000);
                                    }catch(InterruptedException ie){
                                        Thread.interrupted();
                                        return;
                                    }
                                }
                            }
                        });
                        beep.start();
                        if(JOptionPane.showConfirmDialog(null, "Timer finished", "", JOptionPane.OK_OPTION) == JOptionPane.OK_OPTION){
                            beep.interrupt();
                        }
                    }
                }
            }
        });
    }
    public TimerFrame()
    {
        setTitle("Timer");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        add(northPanel, BorderLayout.NORTH);
        northPanel.setBorder(new EmptyBorder(10,5,5,5));
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(0,0,5,0);
        enterTextLabel.setFont(new Font(Font.DIALOG,Font.BOLD,13));
        northPanel.add(enterTextLabel, c);
        c.gridx = 0;
        c.gridy = 1;
        c.insets = new Insets(0,0,0,0);
        enterText.setHorizontalAlignment(JTextField.RIGHT);
        northPanel.add(enterText, c);
        c.gridx = 1;
        c.gridy = 1;
        northPanel.add(enterButton, c);

        add(centerPanel, BorderLayout.CENTER);
        centerPanel.setBorder(new EmptyBorder(25,5,25,5));
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.NONE;
        startStopLabel.setFont(new Font(Font.DIALOG,Font.BOLD,13));
        centerPanel.add(startStopLabel, c);
        c.gridx = 0;
        c.gridy = 1;
        c.insets = new Insets(5,0,0,5);
        centerPanel.add(startButton, c);
        c.gridx = 0;
        c.gridy = 1;
        stopButton.setVisible(false);
        centerPanel.add(stopButton, c);
        c.gridx = 0;
        c.gridy = 2;
        centerPanel.add(clearButton, c);
        


        add(southPanel, BorderLayout.SOUTH);
        southPanel.setBorder(new EmptyBorder(0,5,10,5));
        c.gridx = 0;
        c.gridy = 0;
        c.ipadx = 100;
        c.ipady = 50;
        timerText.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 30));
        timerText.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        timerText.setHorizontalAlignment(JLabel.CENTER);
        southPanel.add(timerText, c);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
    public TimerFrame getFrame(){return this;}
}
class ProcessEvents implements ActionListener{
    private boolean startButtonClicked = false;
    private boolean stopButtonClicked = false;
    private boolean enterButtonClicked = false;
    private int timerAmountInMinutes;
    private int timerSeconds;
    private long timeToWaitUntilNextRun = 1000;
    private Thread changeTimerText;
    private TimerFrame frame;
    
    private JTextField enterText;
    private JButton enterButton;
    private JButton startButton;
    private JButton stopButton;
    private JButton clearButton;
    private JLabel timerText;
    public ProcessEvents(JTextField enterText,JButton enterButton,JButton startButton,
            JButton stopButton,JButton clearButton,JLabel timerText,TimerFrame frame){
        this.enterText = enterText;
        this.enterButton = enterButton;
        this.startButton = startButton;
        this.stopButton = stopButton;
        this.clearButton = clearButton;
        this.timerText = timerText;
        this.frame = frame;
    }
    @Override
    public void actionPerformed(ActionEvent e){
        if(e.getSource().equals(startButton)){
            if(enterButtonClicked){//if timer hasn't started yet
                enterButton.setEnabled(false);
                startButton.setVisible(false);
                stopButton.setVisible(true);
                if(stopButtonClicked){//if timer has already been started before
                    startButtonClicked = true;
                    startTimer(currentTimeMillis());
                }else{
                    enterButtonClicked = false;
                    startButtonClicked = true;
                    enterText.setEnabled(false);
                    startTimer(currentTimeMillis());
                }
                //enterButton.setEnabled(false);
            }else{JOptionPane.showMessageDialog(frame,"Please enter new value","Error",JOptionPane.ERROR_MESSAGE);}
        }
        else if(e.getSource().equals(stopButton)){
            if(startButtonClicked){
                //startButtonClicked = false;
                startButton.setVisible(true);
                stopButton.setVisible(false);
                stopButtonClicked = true;
                stopTimer();
            }else{JOptionPane.showMessageDialog(frame,"Timer hasn't started yet", "Error", JOptionPane.ERROR_MESSAGE);}
        }
        else if(e.getSource().equals(enterButton)){
            if(!enterText.getText().isEmpty() && !enterText.getText().equals("0")){
                enterButtonClicked = true;
                initializeTimer();
            }else{JOptionPane.showMessageDialog(frame,"Please enter new value","Error",JOptionPane.ERROR_MESSAGE);}
        }
        else if(e.getSource().equals(clearButton)){
            if(startButtonClicked){
                stopTimer();
                startButton.setVisible(true);
                
                resetTimer();
            }
        }
    }
    public void startTimer(long startTime){
        changeTimerText = new Thread(new TimerTextChanger(startTime));
        changeTimerText.start();
    }
    public void stopTimer(){
        changeTimerText.interrupt();
    }
    public void initializeTimer(){
        timerAmountInMinutes = Integer.parseInt(enterText.getText());
        timerSeconds = 0;
        timerText.setText(timerAmountInMinutes + ":00");
    }
    public Thread getChangeTimerText(){
        return changeTimerText;
    }
    public void resetTimer(){
        stopButtonClicked = false;
        enterButtonClicked = false;
        enterText.setEnabled(true);
        enterButton.setEnabled(true);
        enterText.setFocusable(true);
        timerText.setText("0:00");
    }
    private class TimerTextChanger implements Runnable
    {
        private long startTime;
        private long currentTime;
        private long elapsedTime;
        public TimerTextChanger(long c){
            startTime = c;
        }
        @Override
        public void run(){
            while(true){
                try{
                    if(timerSeconds == 0) {
                        timerSeconds = 60;
                        timerAmountInMinutes--;
                    }
                    if(stopButtonClicked){
                        Thread.sleep(timeToWaitUntilNextRun);
                        timerText.setText(timerAmountInMinutes + ":" + String.format("%02d",--timerSeconds));
                        timeToWaitUntilNextRun = 1000;
                        startTime = currentTimeMillis();//reset start time so program correctly calculates elapsed time
                        stopButtonClicked = false;
                    }else{
                        Thread.sleep(timeToWaitUntilNextRun);
                        timerText.setText(timerAmountInMinutes + ":" + String.format("%02d",--timerSeconds));
                        currentTime = currentTimeMillis();
                        elapsedTime = currentTime-startTime;
                        timeToWaitUntilNextRun = 1000 - (elapsedTime % 1000);
                    }
                }catch(InterruptedException ie){
                    currentTime = currentTimeMillis();
                    elapsedTime = currentTime-startTime;
                    timeToWaitUntilNextRun = 1000 - (elapsedTime % 1000);
                    Thread.interrupted();
                    return;
                }
            }
        }
    }
}










class FilterTextBox extends DocumentFilter{
    @Override
    public void insertString(FilterBypass fb, int offset, String string,
         AttributeSet attr) throws BadLocationException {

      Document doc = fb.getDocument();
      StringBuilder sb = new StringBuilder();
      sb.append(doc.getText(0, doc.getLength()));
      sb.insert(offset, string);

      if (test(sb.toString())) {
         super.insertString(fb, offset, string, attr);
      } else {
         // warn the user and don't allow the insert
      }
   }

   private boolean test(String text) {
      try {
         Integer.parseInt(text);
         return true;
      } catch (NumberFormatException e) {
         return false;
      }
   }

   @Override
   public void replace(FilterBypass fb, int offset, int length, String text,
         AttributeSet attrs) throws BadLocationException {

      Document doc = fb.getDocument();
      StringBuilder sb = new StringBuilder();
      sb.append(doc.getText(0, doc.getLength()));
      sb.replace(offset, offset + length, text);

      if (test(sb.toString())) {
         super.replace(fb, offset, length, text, attrs);
      } else {
         // warn the user and don't allow the insert
      }

   }

   @Override
   public void remove(FilterBypass fb, int offset, int length)
         throws BadLocationException {
      Document doc = fb.getDocument();
      StringBuilder sb = new StringBuilder();
      sb.append(doc.getText(0, doc.getLength()));
      sb.delete(offset, offset + length);

      if(sb.toString().length() == 0)
      {
		  super.replace(fb, offset, length, "", null);
      }
      else
      {
		  if (test(sb.toString()))
		  {
			  super.remove(fb, offset, length);
		  }
		  else
		  {
			  // warn the user and don't allow the insert
		  }
	  }
   }
}