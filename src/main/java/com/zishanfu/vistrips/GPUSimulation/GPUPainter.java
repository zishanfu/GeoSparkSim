package com.zishanfu.vistrips.GPUSimulation;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import com.aparapi.Kernel;
import com.aparapi.ProfileInfo;
import com.aparapi.Range;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.fixedfunc.GLLightingFunc;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.gl2.GLUT;

public class GPUPainter {

	public static int width;

	   public static int height;

	   public static boolean running;

	   //public static Texture texture;

	   public static void main(String _args[]) {

	      final SimulationKernel kernel = new SimulationKernel(Range.create(10000));
	      
	      kernel.setExecutionMode(Kernel.EXECUTION_MODE.GPU);
	      
	      final JFrame frame = new JFrame("NBody");

	      final JPanel panel = new JPanel(new BorderLayout());
	      final JPanel controlPanel = new JPanel(new FlowLayout());
	      panel.add(controlPanel, BorderLayout.SOUTH);

	      final JButton startButton = new JButton("Start");

	      startButton.addActionListener(new ActionListener(){
	         @Override public void actionPerformed(ActionEvent e) {
	            running = true;
	            startButton.setEnabled(false);
	         }
	      });
	      controlPanel.add(startButton);
	      
	     

	      controlPanel.add(new JLabel("            " + kernel.range.getGlobalSize(0) + " Particles"));

	      final GLCapabilities caps = new GLCapabilities(null);
	      final GLProfile profile = caps.getGLProfile();
	      caps.setDoubleBuffered(true);
	      caps.setHardwareAccelerated(true);
	      final GLCanvas canvas = new GLCanvas(caps);

	      final GLUT glut = new GLUT();

	      final Dimension dimension = new Dimension(Integer.getInteger("width", 1024 + 256),
	            Integer.getInteger("height", 768 - 64 - 32));
	      canvas.setPreferredSize(dimension);

	      canvas.addGLEventListener(new GLEventListener(){
	         private double ratio;

	         private final float xeye = 0f;

	         private final float yeye = 0f;

	         private final float zeye = 100f;

	         private final float xat = 0f;

	         private final float yat = 0f;

	         private final float zat = 0f;

	         public final float zoomFactor = 1.0f;

	         private int frames;

	         private long last = System.currentTimeMillis();

	         @Override public void dispose(GLAutoDrawable drawable) {

	         }

	         @Override public void display(GLAutoDrawable drawable) {

	            final GL2 gl = drawable.getGL().getGL2();
	            
	            gl.glLoadIdentity();
	            gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
	            gl.glColor3f(1f, 1f, 1f);

	            final GLU glu = new GLU();
	            glu.gluPerspective(45f, ratio, 1f, 1000f);

	            glu.gluLookAt(xeye, yeye, zeye * zoomFactor, xat, yat, zat, 0f, 1f, 0f);
	            if (running) {
	               kernel.execute(kernel.range);
	               if (kernel.isExplicit()) {
	                  kernel.get(kernel.xy);
	               }
	               final List<ProfileInfo> profileInfo = kernel.getProfileInfo();
	               if ((profileInfo != null) && (profileInfo.size() > 0)) {
	                  for (final ProfileInfo p : profileInfo) {
	                     System.out.print(" " + p.getType() + " " + p.getLabel() + ((p.getEnd() - p.getStart()) / 1000) + "us");
	                  }
	                  System.out.println();
	               }
	            }
	            kernel.render(gl);

	            final long now = System.currentTimeMillis();
	            final long time = now - last;
	            frames++;

	            if (running) {
	               final float framesPerSecond = (frames * 1000.0f) / time;

	               gl.glColor3f(.5f, .5f, .5f);
	               gl.glRasterPos2i(-40, 38);
	               glut.glutBitmapString(8, String.format("%5.2f fps", framesPerSecond));
	               gl.glFlush();
	            }
	            frames = 0;
	            last = now;

	         }

	         @Override public void init(GLAutoDrawable drawable) {
	            final GL2 gl = drawable.getGL().getGL2();

	            gl.glShadeModel(GLLightingFunc.GL_SMOOTH);
	            gl.glEnable(GL.GL_BLEND);
	            gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE);
	         }

	         @Override public void reshape(GLAutoDrawable drawable, int x, int y, int _width, int _height) {
	            width = _width;
	            height = _height;

	            final GL2 gl = drawable.getGL().getGL2();
	            gl.glViewport(0, 0, width, height);

	            ratio = (double) width / (double) height;

	         }

	      });

	      panel.add(canvas, BorderLayout.CENTER);
	      frame.getContentPane().add(panel, BorderLayout.CENTER);
	      final FPSAnimator animator = new FPSAnimator(canvas, 100);

	      frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	      frame.pack();
	      frame.setVisible(true);

	      animator.start();

	   }
}
