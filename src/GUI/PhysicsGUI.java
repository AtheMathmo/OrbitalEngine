package GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import objects.PlanetBody;
import objects.Illustrator;
import physicsEngine.PhysicsCalc;

/*The bulk of the work here is handled by the class: PhysicsPanel. This is where the objects are drawn.
 * We use separate classes for the objects which are being drawn and a button panel which allows user interaction.
 */

public class PhysicsGUI  {
	// Current program ver.
	public static final double version = 0.1;
	

    public static void main(String[] args) {
    	// Make it look nicer.
    	try {
    		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
    	
    	
    	
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
    
    private static void createAndShowGUI() {
        System.out.println("Created GUI on EDT? "+
                SwingUtilities.isEventDispatchThread());
        
        JFrame f = new JFrame("Physics GUI");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Add buttons and rendering panel to frame.
        PhysicsPanel physicsPanel = new PhysicsPanel();
        f.add(physicsPanel, BorderLayout.CENTER);
        f.add(new ButtonPanel(physicsPanel), BorderLayout.EAST);
        f.pack();
        f.setVisible(true);
    }
}

class PhysicsPanel extends JPanel {
	// For constructing and moving the balls and arrows (showing initial velocity).
	private boolean shiftHeld = false;
	private boolean controlHeld = false;
	
	// Holds all bodies in the panel.
	ArrayList<PlanetBody> bodyList = new ArrayList<PlanetBody>();
	private PlanetBody holdingBody = null;
	
	private Timer timer;
	
	public boolean isShiftHeld() {
		return shiftHeld;
	}

	public void setShiftHeld(boolean shiftHeld) {
		this.shiftHeld = shiftHeld;
	}

	public boolean isControlHeld() {
		return controlHeld;
	}

	public void setControlHeld(boolean controlHeld) {
		this.controlHeld = controlHeld;
	}

	public PhysicsPanel() {
		setBorder(BorderFactory.createLineBorder(Color.black));
		
		// Required to measure keypresses.
		this.setFocusable(true);
		this.requestFocusInWindow();
		this.addKeyListener(new ModeHandler(this));
		
		initComponents();	
		
		// Listens for clicks on a body.
        addMouseListener(new MouseAdapter(){
            public void mousePressed(MouseEvent e){
                for (PlanetBody body : bodyList) {
                	if (body.checkInBoundary(e.getX(), e.getY())) {
                		if (!shiftHeld && !controlHeld) {
                			body.setVelocity(new double[] {0,0});
                			moveBody(body, e.getX(), e.getY());
                		}
                		holdingBody = body;
                	}
                			
                }
            }
            public void mouseReleased(MouseEvent e){
                holdingBody = null;
            }
        });

        // Listens for dragging mouse, for moving, resizing and setting initial velocity.
        addMouseMotionListener(new MouseAdapter(){
            public void mouseDragged(MouseEvent e){
            	for (PlanetBody body : bodyList) {
                	if (holdingBody == body)
                		if (!shiftHeld && !controlHeld)
                			moveBody(body, e.getX(), e.getY());
                		else if (shiftHeld) {
                			/* here we calculate the new size of the ball, and repaint about it.
                			 */
                			repaint((int)(body.getCenter()[0]-body.getRadius())-1,(int)(body.getCenter()[1]-body.getRadius())-1,2*body.getRadius()+3,2*body.getRadius()+3);
                			body.setRadius((int)Math.sqrt(Math.pow((body.getCenter()[0]-e.getX()),2)+Math.pow((body.getCenter()[1]-e.getY()),2)));
                			repaint((int)(body.getCenter()[0]-body.getRadius())-1,(int)(body.getCenter()[1]-body.getRadius())-1,2*body.getRadius()+3,2*body.getRadius()+3);
                		}
                		else if (controlHeld) {
                			/* Here we calculate how to draw the arrow and the repainting area.
                			 * Each body gets given an arrow, to track the painting. Probably a better way but couldn't find it.
                			 */
                			if (!body.isWithArrow()) {
                				Illustrator arrow = new Illustrator();
                				arrow.setStartPoint(new int[] {(int)body.getCenter()[0], (int)body.getCenter()[1]});
                    			arrow.setEndPoint(new int[] {e.getX(),e.getY()});
                				body.setArrow(arrow);
                				body.setWithArrow(true);
                				repaint(arrow.getStartPoint()[0],arrow.getStartPoint()[1],e.getX()-arrow.getStartPoint()[0],e.getY()-arrow.getStartPoint()[1]);
                			}
                			else {
                				Illustrator arrow = body.getArrow();
                				//Calculate area about the ball where the arrow may be, then paint over it.
                				int[] repaintArea = arrow.arrowRepaintArea();
                				repaint(repaintArea[0],repaintArea[1],repaintArea[2],repaintArea[3]);
                				
                				arrow.setStartPoint(new int[] {(int)body.getCenter()[0], (int)body.getCenter()[1]});
                				arrow.setEndPoint(new int[] {e.getX(), e.getY()});
                				
                				repaintArea = arrow.arrowRepaintArea();
                				repaint(repaintArea[0],repaintArea[1],repaintArea[2],repaintArea[3]);
                			}
                			body.setVelocity(new double[] {e.getX()-body.getCenter()[0],e.getY()-body.getCenter()[1]});
                		}
                }
            }        
        });
        
        // Timer to control the movement of the bodies.
        timer = new Timer(PhysicsCalc.TIME_FRAME, new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		PhysicsCalc.CalculateForces(bodyList);
        		for (PlanetBody body: bodyList) {
        			if (body.isWithArrow()) {
        				int[] arrowRepaintArea = body.getArrow().arrowRepaintArea();
        				repaint(arrowRepaintArea[0],arrowRepaintArea[1],arrowRepaintArea[2],arrowRepaintArea[3]);
        				body.setArrow(null);
            			body.setWithArrow(false);
        			}
        			
        			repaint((int)(body.getCenter()[0]-body.getRadius())-1,(int)(body.getCenter()[1]-body.getRadius())-1,2*body.getRadius()+3,2*body.getRadius()+3);
        			body.updateMovement();
        			repaint((int)(body.getCenter()[0]-body.getRadius())-1,(int)(body.getCenter()[1]-body.getRadius())-1,2*body.getRadius()+3,2*body.getRadius()+3);
        		}
        		
        	}
        });
	}
	
	public Dimension getPreferredSize() {
		return new Dimension(800,600);
	}
	
	private void initComponents() {
		PlanetBody body = new PlanetBody(20, new double[] {400,300});
		bodyList.add(body);
	}
	
	public void startTimer() {
		timer.start();
	}
	
	public void stopTimer() {
		timer.stop();
	}
	
	public void moveBody(PlanetBody body, int X, int Y) {
		// Updates position of body and specifies repaint area.
		double[] oldCenter = body.getCenter();
		int radius = body.getRadius();
		
		repaint((int)(oldCenter[0]-radius), (int)(oldCenter[1]-radius), 2*radius+1, 2*radius+1);
		
		body.setCenter(new double[] {X,Y});
		if (body.isWithArrow()) {
			int[] repaintArea = body.getArrow().arrowRepaintArea();
			repaint(repaintArea[0],repaintArea[1],repaintArea[2],repaintArea[3]);
			body.getArrow().moveArrow(X, Y);
			repaintArea = body.getArrow().arrowRepaintArea();
			repaint(repaintArea[0],repaintArea[1],repaintArea[2],repaintArea[3]);
		}
		
		repaint((int)(body.getCenter()[0]-radius), (int)(body.getCenter()[1]-radius), 2*radius+1, 2*radius+1);

	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);	
		
		//Draw Text
		g.drawString("Orbital Engine v"+PhysicsGUI.version, 10, 20);
		g.drawString("Author: James Lucas",10,40);
		
		// Iterate through bodies painting them.
		for (PlanetBody body : bodyList) {
			body.paintBody(g);
			if (body.isWithArrow())
				body.getArrow().drawArrow(g);
		}		
	}
}

class ButtonPanel extends JPanel implements ActionListener{
	
	private PhysicsPanel physicsPanel;
	private JButton createButton;
	private JButton startButton;
	private JButton stopButton;
	private JButton restartButton;
	private JSlider gravitySlider;
	private JLabel gravityLabel;
	private JSlider coeffRestSlider;
	private JLabel coeffRestLabel;
	
	public ButtonPanel(PhysicsPanel physicsPanel) {
		this.physicsPanel = physicsPanel;
		
		setBorder(BorderFactory.createLineBorder(Color.black));
		setLayout(new GridLayout(8,1,0,10));
			
		initComponents();
	}
	
	private void initComponents() {
		
		createButton = new JButton("Create Planet");
		createButton.setActionCommand("create");
		createButton.addActionListener(this);
		
		startButton = new JButton("Start");
		startButton.setActionCommand("start");
		startButton.addActionListener(this);
		
		stopButton = new JButton("Stop");
		stopButton.setActionCommand("stop");
		stopButton.addActionListener(this);
		
		restartButton = new JButton("Restart");
		restartButton.setActionCommand("restart");
		restartButton.addActionListener(this);
		
		// Create panel to hold gravity slider and label
		JPanel gravSliderHolder = new JPanel(new GridLayout(2,1));
		gravitySlider = new JSlider(JSlider.HORIZONTAL,PhysicsCalc.GRAV_MIN,PhysicsCalc.GRAV_MAX,PhysicsCalc.gScale);
		gravityLabel = new JLabel("gravity: "+PhysicsCalc.gScale,JSlider.CENTER);
		gravityLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		gravitySlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				PhysicsCalc.gScale = source.getValue();
				gravityLabel.setText("gravity: "+PhysicsCalc.gScale);
			}
		});
		
		// Create panel to hold bounciness slider and label
		JPanel coeffRestSliderHolder = new JPanel(new GridLayout(2,1));
		coeffRestSlider = new JSlider(JSlider.HORIZONTAL,1,12,10);
		coeffRestLabel = new JLabel("bounciness: "+PhysicsCalc.coeffRestitution,JSlider.CENTER);
		coeffRestLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		coeffRestSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				PhysicsCalc.coeffRestitution = 1.0*source.getValue()/10;
				coeffRestLabel.setText("bounciness: "+PhysicsCalc.coeffRestitution);
			}
		});
		
		this.add(createButton);
		this.add(startButton);
		this.add(stopButton);
		this.add(restartButton);
		
		gravSliderHolder.add(gravityLabel);
		gravSliderHolder.add(gravitySlider);
		this.add(gravSliderHolder);
		
		coeffRestSliderHolder.add(coeffRestLabel);
		coeffRestSliderHolder.add(coeffRestSlider);
		this.add(coeffRestSliderHolder);
	}
	
	public void actionPerformed(ActionEvent e) {
		if ("create".equals(e.getActionCommand())) {
			// Place new body at default position, update forces (incase we start without moving).
			PlanetBody newBody = new PlanetBody(20, new double[] {200,300});
			physicsPanel.bodyList.add(newBody);
			PhysicsCalc.CalculateForces(physicsPanel.bodyList);
			physicsPanel.requestFocusInWindow();
			physicsPanel.repaint();
		}
		
		if ("start".equals(e.getActionCommand())) {
			// Set of timer, remove all illustrations.
			physicsPanel.requestFocusInWindow();
			for (PlanetBody body : physicsPanel.bodyList) {
				body.setArrow(null);
				body.setWithArrow(false);
				physicsPanel.repaint();
			}
			physicsPanel.startTimer();
		}
		
		if ("stop".equals(e.getActionCommand())) {
			// Stop timer
			physicsPanel.requestFocusInWindow();
			physicsPanel.stopTimer();
		}
		
		if ("restart".equals(e.getActionCommand())) {
			// Return to default state, just arbitrary choices.
			physicsPanel.requestFocusInWindow();
			physicsPanel.stopTimer();
			
			physicsPanel.bodyList.clear();
			PlanetBody body = new PlanetBody(20, new double[] {400,300});
			physicsPanel.bodyList.add(body);
			
			physicsPanel.repaint();
		}
	}
	
}

class ModeHandler implements KeyListener {
	// Keeps knowledge of keys being held, for resizing/settint velocity of bodies.

	private PhysicsPanel physicsPanel;
	
	public ModeHandler(PhysicsPanel physicsPanel) {
		this.physicsPanel = physicsPanel;
	}
	
	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_SHIFT && !physicsPanel.isControlHeld()) {
			physicsPanel.setShiftHeld(true);
		}
		if (e.getKeyCode() == KeyEvent.VK_CONTROL && !physicsPanel.isShiftHeld()) {
			physicsPanel.setControlHeld(true);
		}
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
			physicsPanel.setShiftHeld(false);
		}
		if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
			physicsPanel.setControlHeld(false);
		}
	}
}
