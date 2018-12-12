package com.zishanfu.vistrips;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
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
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.fixedfunc.GLLightingFunc;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.gl2.GLUT;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

public class NBody{

	   public static class NBodyKernel extends Kernel{
	      protected final float delT = .005f;

	      protected final float espSqr = 1.0f;

	      protected final float mass = 5f;

	      public final Range range;

	      public final float[] xy; // positions xy and z of bodies

	      private final float[] vxy; // velocity component of x,y and z of bodies 

	      public NBodyKernel(Range _range) {
	         range = _range;
	         // range = Range.create(bodies);
	         xy = new float[range.getGlobalSize(0) * 2];
	         vxy = new float[range.getGlobalSize(0) * 2];
	         final float maxDist = 20f;
	         for (int body = 0; body < (range.getGlobalSize(0) * 2); body += 2) {

	            final float theta = (float) (Math.random() * Math.PI * 2);
	            final float phi = (float) (Math.random() * Math.PI * 2);
	            final float radius = (float) (Math.random() * maxDist);

	            // get the 3D dimensional coordinates
	            xy[body + 0] = (float) (radius * Math.cos(theta) * Math.sin(phi));
	            xy[body + 1] = (float) (radius * Math.sin(theta) * Math.sin(phi));
	            //xy[body + 2] = (float) (radius * Math.cos(phi));

	            // divide into two 'spheres of bodies' by adjusting x 

	            if ((body % 2) == 0) {
	               xy[body + 0] += maxDist * 1.5;
	            } else {
	               xy[body + 0] -= maxDist * 1.5;
	            }
	         }
	         setExplicit(true);
	      }

	      /** 
	       * Here is the kernel entrypoint. Here is where we calculate the position of each body
	       */
	      @Override public void run() {
	         final int body = getGlobalId();
	         final int count = getGlobalSize(0) * 3;
	         final int globalId = body * 3;

	         float accx = 0.f;
	         float accy = 0.f;
	         float accz = 0.f;

	         final float myPosx = xy[globalId + 0];
	         final float myPosy = xy[globalId + 1];
	         //final float myPosz = xy[globalId + 2];
	         for (int i = 0; i < count; i += 2) {
	            final float dx = xy[i + 0] - myPosx;
	            final float dy = xy[i + 1] - myPosy;
	            //final float dz = xy[i + 2] - myPosz;
	            //final float invDist = rsqrt((dx * dx) + (dy * dy) + (dz * dz) + espSqr);
	            final float invDist = rsqrt((dx * dx) + (dy * dy) + espSqr);
	            final float s = mass * invDist * invDist * invDist;
	            accx = accx + (s * dx);
	            accy = accy + (s * dy);
	            //accz = accz + (s * dz);
	         }
	         accx = accx * delT;
	         accy = accy * delT;
	         accz = accz * delT;
	         xy[globalId + 0] = myPosx + (vxy[globalId + 0] * delT) + (accx * .5f * delT);
	         xy[globalId + 1] = myPosy + (vxy[globalId + 1] * delT) + (accy * .5f * delT);
	         //xy[globalId + 2] = myPosz + (vxy[globalId + 2] * delT) + (accz * .5f * delT);

	         vxy[globalId + 0] = vxy[globalId + 0] + accx;
	         vxy[globalId + 1] = vxy[globalId + 1] + accy;
	         //vxy[globalId + 2] = vxy[globalId + 2] + accz;
	      }

	      /**
	       * Render all particles to the OpenGL context
	       * @param gl
	       */

	      public void render(GL2 gl) {
	         gl.glBegin(GL2.GL_QUADS);

	         //gl.glBegin(GL2.GL_POINT);

	         for (int i = 0; i < (range.getGlobalSize(0) * 2); i += 2) {
	            gl.glTexCoord2f(0, 1);
	            gl.glVertex3f(xy[i + 0], xy[i + 1] + 1, 0);
	            gl.glTexCoord2f(0, 0);
	            gl.glVertex3f(xy[i + 0], xy[i + 1], 0);
	            gl.glTexCoord2f(1, 0);
	            gl.glVertex3f(xy[i + 0] + 1, xy[i + 1], 0);
	            gl.glTexCoord2f(1, 1);
	            gl.glVertex3f(xy[i + 0] + 1, xy[i + 1] + 1, 0);
	        	 //gl.glVertex2d(xy[i + 0], xy[i + 1]);
	         }
	         gl.glEnd();
	      }

	   }

	   public static int width;

	   public static int height;

	   public static boolean running;

	   //public static Texture texture;

	   public static void main(String _args[]) {

	      final NBodyKernel kernel = new NBodyKernel(Range.create(Integer.getInteger("bodies", 10000)));
	      kernel.setExecutionMode(Kernel.EXECUTION_MODE.JTP);
	      final JFrame frame = new JFrame("NBody");

	      final JPanel panel = new JPanel(new BorderLayout());
	      final JPanel controlPanel = new JPanel(new FlowLayout());
	      panel.add(controlPanel, BorderLayout.SOUTH);

	      final JButton startButton = new JButton("Start");

	      startButton.addActionListener(new ActionListener(){
	         public void actionPerformed(ActionEvent e) {
	            running = true;
	            startButton.setEnabled(false);
	         }
	      });
	      controlPanel.add(startButton);
	      
	      kernel.setExecutionMode(Kernel.EXECUTION_MODE.GPU);


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

	         public void dispose(GLAutoDrawable drawable) {

	         }

	         public void display(GLAutoDrawable drawable) {

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

	         public void init(GLAutoDrawable drawable) {
	            final GL2 gl = drawable.getGL().getGL2();

	            gl.glShadeModel(GLLightingFunc.GL_SMOOTH);
	            gl.glEnable(GL.GL_BLEND);
	            gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE);
	         }

	         public void reshape(GLAutoDrawable drawable, int x, int y, int _width, int _height) {
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