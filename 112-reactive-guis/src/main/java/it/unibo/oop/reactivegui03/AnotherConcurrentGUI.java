package it.unibo.oop.reactivegui03;

import java.io.Serial;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unibo.oop.JFrameUtil;

/**
 * Third experiment with reactive gui.
 */
public final class AnotherConcurrentGUI extends JFrame {

        @Serial
    private static final long serialVersionUID = 1L;
    private static final int MAX_TIME = 10000;
    private static final Logger LOGGER = LoggerFactory.getLogger(AnotherConcurrentGUI.class);
    private final JLabel counter = new JLabel();
    final JButton up = new JButton("up");
    final JButton down = new JButton("down");
    final JButton stop = new JButton("stop");
    final Agent agent = new Agent();

    public AnotherConcurrentGUI(){
        super();
        JFrameUtil.dimensionJFrame(this);
        final JPanel panel = new JPanel();
        counter.setText("0");
        panel.add(counter);
        panel.add(up);
        panel.add(down);
        panel.add(stop);
        this.setContentPane(panel);
        this.setVisible(true);
        stop.addActionListener(e -> this.stopCount());
        up.addActionListener(e -> agent.upCounting());
        down.addActionListener(e -> agent.downCounting());
        new Thread(agent).start();
        new Thread(() -> {
            try{
                Thread.sleep(MAX_TIME);
            }catch(Exception e){
                e.printStackTrace();
            }
            SwingUtilities.invokeLater(()-> this.stopCount());
        }).start();
    }

    private void stopCount() {
        agent.stopCounting();
        up.setEnabled(false);
        down.setEnabled(false);
        stop.setEnabled(false);
    }

    /*
     * The counter agent is implemented as a nested class. This makes it
     * invisible outside and encapsulated.
     */
    private final class Agent implements Runnable {
        /*
         * Stop is volatile to ensure visibility. Look at:
         *
         * http://archive.is/9PU5N - Sections 17.3 and 17.4
         *
         * For more details on how to use volatile:
         *
         * http://archive.is/4lsKW
         *
         */
        private volatile boolean stop;
        private volatile boolean down;
        private int counter;

        @Override
        public void run() {
            while (!this.stop) {
                try {
                    // The EDT doesn't access `counter` anymore, it doesn't need to be volatile
                    final var nextText = Integer.toString(this.counter);
                    SwingUtilities.invokeAndWait(() -> AnotherConcurrentGUI.this.counter.setText(nextText));
                    if (down) {
                        this.counter--;
                    } else {
                        this.counter++;
                    }
                    Thread.sleep(100);
                } catch (InvocationTargetException | InterruptedException ex) {
                    LOGGER.error(ex.getMessage(), ex);
                }
            }
        }

        /**
         * External command to stop counting.
         */
        public void stopCounting() {
            this.stop = true;
        }

        public void upCounting(){
            this.down = false;
        }

        public void downCounting(){
            this.down = true;
        }
    }
}