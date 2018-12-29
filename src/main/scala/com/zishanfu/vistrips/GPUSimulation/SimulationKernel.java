package com.zishanfu.vistrips.GPUSimulation;

import java.util.Random;

import com.aparapi.Kernel;
import com.aparapi.Range;
import com.jogamp.opengl.GL2;

public class SimulationKernel extends Kernel{

    final Range range;

    final float[] xy; 
    Random rand = new Random();
    
    public float randFloat(float min, float max) {

        return rand.nextFloat() * (max - min) + min;

    }

    public SimulationKernel(Range _range) {
       range = _range;

       xy = new float[range.getGlobalSize(0) * 2];

       for (int body = 0; body < (range.getGlobalSize(0) * 2); body += 2) {

          xy[body + 0] = randFloat(0, 10.0f);
          xy[body + 1] = randFloat(0, 10.0f);
          
       }
       setExplicit(true);
    }

    /** 
     * Here is the kernel entrypoint. Here is where we calculate the position of each body
     */
    @Override public void run() {
       final int body = getGlobalId();
       final int count = getGlobalSize(0) * 2;
       final int globalId = body * 2;
       
       System.out.println(xy[globalId + 0] + ", " + xy[globalId + 1]);

       for (int i = 0; i < count; i += 2) {
          final float dx = xy[i + 0] + randFloat(0, 1f);
          final float dy = xy[i + 1] + randFloat(0, 1f);
          xy[globalId + 0] += dx;
          xy[globalId + 1] += dy;
       }
    }

    /**
     * Render all particles to the OpenGL context
     * @param gl
     */

    protected void render(GL2 gl) {
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
