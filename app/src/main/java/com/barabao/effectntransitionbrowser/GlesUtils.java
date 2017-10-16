package com.barabao.effectntransitionbrowser;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

public class GlesUtils {
	private static final String TAG = "GlesUtils";
	/**
     * Utility method for debugging OpenGL calls. Provide the name of the call
     * just after making it:
     *
     * <pre>
     * mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
     * MyGLRenderer.checkGlError("glGetUniformLocation");</pre>
     *
     * If the operation is not successful, the check throws an error.
     *
     * @param glOperation - Name of the OpenGL call to check.
     */
     public static void checkGlError(String glOperation) {
         int error;
         while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
             Log.e(TAG, glOperation + ": glError " + error);
             throw new RuntimeException(glOperation + ": glError " + error);
         }
     }
     
     public static void loadTexture(Bitmap bitmap, int[] textureHandles, int position) {
    	 
         GLES20.glGenTextures(1, textureHandles, position);
         checkGlError("glGenTextures");

         if (textureHandles[position] != 0)
         {
	         // Bind the texture to the name
	         GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandles[position]);
	         checkGlError("glBindTexture");
	
	         // Set the texture properties
	         GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);//GL_LINEAR
	         checkGlError("glTexParameteri");
	         GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);//GL_LINEAR
	         checkGlError("glTexParameteri");
	         GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
	         checkGlError("glTexParameteri");
	         GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
	         checkGlError("glTexParameteri");
	
	         // Load the texture
	         GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
	         GLES20.glFlush();
	         bitmap.recycle();
	         if (!GLES20.glIsTexture(textureHandles[position])) {
	             Log.e(TAG, "Failed to load a valid texture");
	             return;
	         }
         }
         else
         {
             throw new RuntimeException("Error loading texture.");
         }

     }
     
	 /**
      * Utility method for compiling a OpenGL shader.
      *
      * <p><strong>Note:</strong> When developing shaders, use the checkGlError()
      * method to debug shader coding errors.</p>
      *
      * @param type - Vertex or fragment shader type.
      * @param shaderCode - String containing the shader code.
      * @return - Returns an id for the shader.
      */
     public static int loadShader(int type, String shaderCode){

         // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
         // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
         int shader = GLES20.glCreateShader(type);
         GlesUtils.checkGlError("glCreateShader");

         // add the source code to the shader and compile it
         GLES20.glShaderSource(shader, shaderCode);
         GlesUtils.checkGlError("glShaderSource");
         GLES20.glCompileShader(shader);
         GlesUtils.checkGlError("glCompileShader");

         return shader;
     } 
}
