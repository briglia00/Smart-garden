package gardenservice;

import java.awt.GridLayout;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GardenService extends JFrame {
    
    private static final long serialVersionUID = -6218820567019985015L;
    private SerialCommChannel channel;
    JLabel textgeneraltemp;
    JLabel textgeneralbright;
    

    public GardenService(int size, SerialCommChannel scc) {
    	
    	Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                //clearAll();
            }
        }, "Shutdown-thread"));
    	
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.channel = scc;
        this.setTitle("Garden Service");
        this.setSize(100*size, 70*size);
        
        
        JPanel panel = new JPanel(new GridLayout(size-1,size));
        JLabel l1 = new JLabel("Temperature:");
        JLabel l2 = new JLabel("Brightness:");
        
        JButton jupdate = new JButton("Update data");
        
        textgeneraltemp = new JLabel("");
        textgeneralbright = new JLabel("");
        JTextField texttemp = new JTextField("0");
        JTextField textbright = new JTextField("0");
        
        
        this.getContentPane().add(panel);
        this.setVisible(true);
        panel.add(l1);
        panel.add(texttemp);
        panel.add(l2);
        panel.add(textbright);
        panel.add(textgeneraltemp);
        panel.add(textgeneralbright);
        panel.add(jupdate);
        
        
        /*ActionListener updateData = e -> {
        	try {
        		int temp = Integer.parseInt(texttemp.getText());
        		int bright = Integer.parseInt(textbright.getText());
        		texttemp.setBackground(Color.white);
        		textbright.setBackground(Color.white);
        		
        		//channel.sendMsg("REFCOF-" + temp);
        	} catch (NumberFormatException e1) {
        		texttemp.setBackground(Color.red);
        		textbright.setBackground(Color.red);
        	}
        };
        
        ActionListener getStatus = e -> {
        	try {
        		channel.sendMsg("GETSTS");
        		String msg = channel.receiveMsg();
        		
        		if(msg == "0") {
        			textstat.setText("Inizializing");
        		} else if (msg.equals("1")) {
        			textstat.setText("Maintenance");
        		} else if (msg.equals("2")) {
        			textstat.setText("Ready");
        		} else if (msg.equals("3")) {
        			textstat.setText("Making Products");
        		} else if (msg.equals("4")) {
        			textstat.setText("Product Done");
        		} else if (msg.equals("5")) {
        			textstat.setText("Sleeping");
        		} else if (msg.equals("6")) {
        			textstat.setText("Testing");
        		} else if (msg.equals("7")) {
        			textstat.setText("Broken");
        		}
        	} catch (Exception e1) {
        		
        	}
        };
        
        ActionListener getCof = e -> {
        	try {
        		channel.sendMsg("GETCOF");
        		String msg = channel.receiveMsg();
        		textgcof.setText(msg);
        	} catch (Exception e1) {
        		
        	}
        };
        
        ActionListener getTea = e -> {
        	try {
        		channel.sendMsg("GETTEA");
        		String msg = channel.receiveMsg();
        		textgtea.setText(msg);
        	} catch (Exception e1) {
        		
        	}
        };
        
        ActionListener getChoc = e -> {
        	try {
        		channel.sendMsg("GETCHO");
        		String msg = channel.receiveMsg();
        		textgchoc.setText(msg);
        	} catch (Exception e1) {
        		
        	}
        };
        
        ActionListener getSelfTest = e -> {
        	try {
        		channel.sendMsg("GETSLF");
        		String msg = channel.receiveMsg();
        		textgself.setText(msg);
        	} catch (Exception e1) {
        		
        	}
        };
        
        ActionListener recoverMachine = e -> {
        	try {
        		channel.sendMsg("RECOVR");
        	} catch (Exception e1) {
        		
        	}
        };
        
        jself.addActionListener(getSelfTest);
        jcof.addActionListener(refCoffee);
        jtea.addActionListener(refTea);
        jchoc.addActionListener(refChoc);
        jgcof.addActionListener(getCof);
        jgtea.addActionListener(getTea);
        jgchoc.addActionListener(getChoc);
        jstat.addActionListener(getStatus);
        jrecover.addActionListener(recoverMachine);*/
    }
    
    public void setTemp(String temp) {
    	this.textgeneraltemp.setText(temp);
    }
    
    public static void main(String[] args) throws Exception {
    	SerialCommChannel channel = new SerialCommChannel("/dev/ttyACM0",9600);
    	DataCollector collector = new DataCollector(channel);
    	collector.start();
    	//new GardenService(5, channel);
    }
}