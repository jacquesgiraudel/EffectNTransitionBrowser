package com.barabao.effectntransitionbrowser;

/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * A two-dimensional square for use as a drawn object in OpenGL ES 2.0.
 */
public class Frame {

    private final String vertexShaderCode =
            // This matrix member variable provides a hook to manipulate
            // the coordinates of the objects that use this vertex shader
            "uniform mat4 uMVPMatrix;" +
            "attribute vec4 vPosition;" +
            "attribute vec2 a_TexCoordinate;" + 
            "varying vec2 v_TexCoordinate;" + 
            "void main() {" +
            // The matrix must be included as a modifier of gl_Position.
            // Note that the uMVPMatrix factor *must be first* in order
            // for the matrix multiplication product to be correct.
            "  gl_Position = uMVPMatrix * vPosition;" +
            "  v_TexCoordinate = a_TexCoordinate;" + 
            "}";
    
    private int vertexShader;
    
    //Note : 
    // - add the point to float constant
    // - add the precision for floating point
    private final String fragmentShaderCode =
    		"precision mediump float;" +
            "uniform vec4 vColor;" +
            "uniform sampler2D u_Texture;" + 
            "uniform float width;\n" +
            "uniform float height;\n" +
            "uniform float stepx;\n" +
            "uniform float stepy;\n" +
            "varying vec2 v_TexCoordinate;" + 		
            "void main() {" +
            "  gl_FragColor = texture2D(u_Texture, v_TexCoordinate);" +
            "}";//vColor;

    private int fragmentShader;
    
    private static final String BLUR_FRAGMENT_SHADER =
            "precision mediump float;\n" +
            "uniform sampler2D u_Texture;\n" +
            "varying vec2 v_TexCoordinate;\n" +
            "void main(void)\n" +
            "{\n" +
            "    float step = 0.004;\n" +
            "    vec3 c1 = texture2D(u_Texture, vec2(v_TexCoordinate.s - step, v_TexCoordinate.t - step)).bgr;\n" +
            "    vec3 c2 = texture2D(u_Texture, vec2(v_TexCoordinate.s + step, v_TexCoordinate.t + step)).bgr;\n" +
            "    vec3 c3 = texture2D(u_Texture, vec2(v_TexCoordinate.s - step, v_TexCoordinate.t + step)).bgr;\n" +
            "    vec3 c4 = texture2D(u_Texture, vec2(v_TexCoordinate.s + step, v_TexCoordinate.t - step)).bgr;\n" +
            "    gl_FragColor.a = 1.0;\n" +
            "    gl_FragColor.rgb = (c1 + c2 + c3 + c4) / 4.0;\n" +
            "}";
    
    private static final String EMBOSS_FRAGMENT_SHADER =
            "precision mediump float;\n" +
            "uniform sampler2D u_Texture;\n" +
            "varying vec2 v_TexCoordinate;\n" +
            "uniform float width;\n" +
            "uniform float height;\n" +
            "uniform float stepx;\n" +
            "uniform float stepy;\n" +
            "void main(void)\n" +
            "{\n" +
            "    vec3 t1 = texture2D(u_Texture, vec2(v_TexCoordinate.x - stepx, v_TexCoordinate.y - stepy)).bgr;\n" +
            "    vec3 t2 = texture2D(u_Texture, vec2(v_TexCoordinate.x, v_TexCoordinate.y - stepy)).bgr;\n" +
            "    vec3 t3 = texture2D(u_Texture, vec2(v_TexCoordinate.x + stepx, v_TexCoordinate.y - stepy)).bgr;\n" +
            "    vec3 t4 = texture2D(u_Texture, vec2(v_TexCoordinate.x - stepx, v_TexCoordinate.y)).bgr;\n" +
            "    vec3 t5 = texture2D(u_Texture, v_TexCoordinate).bgr;\n" +
            "    vec3 t6 = texture2D(u_Texture, vec2(v_TexCoordinate.x + stepx, v_TexCoordinate.y)).bgr;\n" +
            "    vec3 t7 = texture2D(u_Texture, vec2(v_TexCoordinate.x - stepx, v_TexCoordinate.y + stepy)).bgr;\n" +
            "    vec3 t8 = texture2D(u_Texture, vec2(v_TexCoordinate.x, v_TexCoordinate.y + stepy)).bgr;\n" +
            "    vec3 t9 = texture2D(u_Texture, vec2(v_TexCoordinate.x + stepx, v_TexCoordinate.y + stepy)).bgr;\n" +
            "    vec3 rr = -4.0 * t1 - 4.0 * t2 - 4.0 * t4 + 12.0 * t5;\n" +
            "    float y = (rr.r + rr.g + rr.b) / 3.0;\n" +
            "    gl_FragColor.a = 1.0;\n" +
            "    gl_FragColor.rgb = vec3(y, y, y) + 0.3;\n" +
            "}";
    
    private static final String GLOW_FRAGMENT_SHADER =
            "precision mediump float;\n" +
            "uniform sampler2D u_Texture;\n" +
            "varying vec2 v_TexCoordinate;\n" +
            "uniform float width;\n" +
            "uniform float height;\n" +
            "uniform float stepx;\n" +
            "uniform float stepy;\n" +
            "void main(void)\n" +
            "{\n" +
            "    vec3 t1 = texture2D(u_Texture, vec2(v_TexCoordinate.x - stepx, v_TexCoordinate.y - stepy)).bgr;\n" +
            "    vec3 t2 = texture2D(u_Texture, vec2(v_TexCoordinate.x, v_TexCoordinate.y - stepy)).bgr;\n" +
            "    vec3 t3 = texture2D(u_Texture, vec2(v_TexCoordinate.x + stepx, v_TexCoordinate.y - stepy)).bgr;\n" +
            "    vec3 t4 = texture2D(u_Texture, vec2(v_TexCoordinate.x - stepx, v_TexCoordinate.y)).bgr;\n" +
            "    vec3 t5 = texture2D(u_Texture, v_TexCoordinate).bgr;\n" +
            "    vec3 t6 = texture2D(u_Texture, vec2(v_TexCoordinate.x + stepx, v_TexCoordinate.y)).bgr;\n" +
            "    vec3 t7 = texture2D(u_Texture, vec2(v_TexCoordinate.x - stepx, v_TexCoordinate.y + stepy)).bgr;\n" +
            "    vec3 t8 = texture2D(u_Texture, vec2(v_TexCoordinate.x, v_TexCoordinate.y + stepy)).bgr;\n" +
            "    vec3 t9 = texture2D(u_Texture, vec2(v_TexCoordinate.x + stepx, v_TexCoordinate.y + stepy)).bgr;\n" +
            "    vec3 xx= t1 + 2.0*t2 + t3 - t7 - 2.0*t8 - t9;\n" +
            "    vec3 yy = t1 - t3 + 2.0*t4 - 2.0*t6 + t7 - t9;\n" +
            "    vec3 rr = sqrt(xx * xx + yy * yy);\n" +
            "    gl_FragColor.a = 1.0;\n" +
            "    gl_FragColor.rgb = rr * 2.0 * t5;\n" +
            "}";
    
    private static final String HALFTONE_FRAGMENT_SHADER =
            "precision mediump float;\n" +
            "uniform sampler2D u_Texture;\n" +
            "varying vec2 v_TexCoordinate;\n" +
            "uniform float width;\n" +
            "uniform float height;\n" +
            "uniform float stepx;\n" +
            "uniform float stepy;\n" +
            "const float strength = 6.0;\n" +
            "void main(void)\n" +
            "{\n" +
            "    float offx = floor(v_TexCoordinate.s  / (strength * stepx));\n" +
            "    float offy = floor(v_TexCoordinate.t  / (strength * stepy));\n" +
            "    vec3 res = texture2D(u_Texture, vec2(offx * strength * stepx , offy * strength * stepy)).bgr;\n" +
            "    vec2 prc = fract(v_TexCoordinate.st / vec2(strength * stepx, strength * stepy));\n" +
            "    vec2 pw = pow(abs(prc - 0.5), vec2(2.0));\n" +
            "    float  rs = pow(0.45, 2.0);\n" +
            "    float gr = smoothstep(rs - 0.1, rs + 0.1, pw.x + pw.y);\n" +
            "    float y = (res.r + res.g + res.b) / 3.0; \n" +
            "    vec3 ra = res / y;\n" +
            "    float ls = 0.3;\n" +
            "    float lb = ceil(y / ls);\n" +
            "    float lf = ls * lb + 0.3;\n" +
            "    res = lf * res;\n" +
            "    gl_FragColor.a = 1.0;\n" +
            "    gl_FragColor.rgb = mix(res, vec3(0.1, 0.1, 0.1), gr);\n" +
            "}";
    
    private static final String MIRROR_FRAGMENT_SHADER =
            "precision mediump float;\n" +
            "uniform sampler2D u_Texture;\n" +
            "varying vec2 v_TexCoordinate;\n" +
            "uniform float width;\n" +
            "uniform float height;\n" +
            "void main(void)\n" +
            "{\n" +
            "    vec2 off = vec2(0.0, 0.0);\n" +
            "    if (v_TexCoordinate.t > 0.5) {\n" +
            "        off.t = 1.0 - v_TexCoordinate.t;\n" +
            "        off.s = v_TexCoordinate.s;\n" +
            "    } else {\n" +
            "         off = v_TexCoordinate;\n" +
            "    }\n" +
            "    vec3 color = texture2D(u_Texture, vec2(off)).bgr;\n" +
            "    gl_FragColor.a = 1.0;\n" +
            "    gl_FragColor.rgb = color;\n" +
            "}";
    
    private static final String OUTLINE_FRAGMENT_SHADER =
            "precision mediump float;\n" +
            "uniform sampler2D u_Texture;\n" +
            "varying vec2 v_TexCoordinate;\n" +
            "uniform float width;\n" +
            "uniform float height;\n" +
            "uniform float stepx;\n" +
            "uniform float stepy;\n" +
            "void main(void)\n" +
            "{\n" +
            "    vec3 t1 = texture2D(u_Texture, vec2(v_TexCoordinate.x - stepx, v_TexCoordinate.y - stepy)).bgr;\n" +
            "    vec3 t2 = texture2D(u_Texture, vec2(v_TexCoordinate.x, v_TexCoordinate.y - stepy)).bgr;\n" +
            "    vec3 t3 = texture2D(u_Texture, vec2(v_TexCoordinate.x + stepx, v_TexCoordinate.y - stepy)).bgr;\n" +
            "    vec3 t4 = texture2D(u_Texture, vec2(v_TexCoordinate.x - stepx, v_TexCoordinate.y)).bgr;\n" +
            "    vec3 t5 = texture2D(u_Texture, v_TexCoordinate).bgr;\n" +
            "    vec3 t6 = texture2D(u_Texture, vec2(v_TexCoordinate.x + stepx, v_TexCoordinate.y)).bgr;\n" +
            "    vec3 t7 = texture2D(u_Texture, vec2(v_TexCoordinate.x - stepx, v_TexCoordinate.y + stepy)).bgr;\n" +
            "    vec3 t8 = texture2D(u_Texture, vec2(v_TexCoordinate.x, v_TexCoordinate.y + stepy)).bgr;\n" +
            "    vec3 t9 = texture2D(u_Texture, vec2(v_TexCoordinate.x + stepx, v_TexCoordinate.y + stepy)).bgr;\n" +
            "    vec3 xx= t1 + 2.0*t2 + t3 - t7 - 2.0*t8 - t9;\n" +
            "    vec3 yy = t1 - t3 + 2.0*t4 - 2.0*t6 + t7 - t9;\n" +
            "    vec3 rr = sqrt(xx * xx + yy * yy);\n" +
            "    float y = (rr.r + rr.g + rr.b) / 3.0;\n" +
            "    if (y > 0.2)\n" +
            "        rr = vec3(0.0, 0.0, 0.0);\n" +
            "    else\n" +
            "        rr = vec3(1.0, 1.0, 1.0);\n" +
            "    gl_FragColor.a = 1.0;\n" +
            "    gl_FragColor.rgb = rr;\n" +
            "}";
    
    private static final String PIXELATE_FRAGMENT_SHADER =
            "precision mediump float;\n" +
            "uniform sampler2D u_Texture;\n" +
            "varying vec2 v_TexCoordinate;\n" +
            "uniform float width;\n" +
            "uniform float height;\n" +
            "uniform float stepx;\n" +
            "uniform float stepy;\n" +
            "const float strength = 4.0;\n" +
            "void main(void)\n" +
            "{\n" +
            "	 float stepx = 1.0 / width; \n" +
            "	 float stepy = 1.0 / height; \n" + 
            "    float offx = floor(v_TexCoordinate.s  / (strength * stepx));\n" +
            "    float offy = floor(v_TexCoordinate.t  / (strength * stepy));\n" +
            "    vec3 res = texture2D(u_Texture, vec2(offx * strength * stepx , offy * strength * stepy)).bgr;\n" +
            "    gl_FragColor.a = 1.0;\n" +
            "    gl_FragColor.rgb = res;\n" +
            "}";
    
    private static final String POPART_FRAGMENT_SHADER =
            "precision mediump float;\n" +
            "uniform sampler2D u_Texture;\n" +
            "varying vec2 v_TexCoordinate;\n" +
            "void main(void)\n" +
            "{\n" +
            "    vec3 col = texture2D(u_Texture, v_TexCoordinate).bgr;\n" +
            "    float y = 0.3 *col.r + 0.59 * col.g + 0.11 * col.b;\n" +
            "    y = y < 0.3 ? 0.0 : (y < 0.6 ? 0.5 : 1.0);\n" +
            "    if (y == 0.5)\n" +
            "        col = vec3(0.8, 0.0, 0.0);\n" +
            "    else if (y == 1.0)\n" +
            "        col = vec3(0.9, 0.9, 0.0);\n" +
            "    else\n" +
            "        col = vec3(0.0, 0.0, 0.0);\n" +
            "    gl_FragColor.a = 1.0;\n" +
            "    gl_FragColor.rgb = col;\n" +
            "}";
    
    private static final String SCANLINES_FRAGMENT_SHADER =
            "precision mediump float;\n" +
            "uniform sampler2D u_Texture;\n" +
            "const float offset = 0.0;\n" +
            "float frequency = 83.0;\n" +
            "varying vec2 v_TexCoordinate;\n" +
            "void main(void)\n" +
            "{\n" +
            "    float global_pos = (v_TexCoordinate.y + offset) * frequency;\n" +
            "    float wave_pos = cos((fract(global_pos) - 0.5)*3.14);\n" +
            "    vec4 pel = texture2D(u_Texture, v_TexCoordinate);\n" +
            "    gl_FragColor = mix(vec4(0,0,0,0), pel, wave_pos);\n" +
            "}";
    
    private static final String SEPIA_FRAGMENT_SHADER = 
    		"precision mediump float;\n" +
            "uniform sampler2D u_Texture;\n" +
            "varying vec2 v_TexCoordinate;\n" +
            "const lowp mat4 colorMatrix = mat4(0.3588, 0.7044, 0.1368, 0, 0.2990, 0.5870, 0.1140, 0, 0.2392, 0.4696, 0.0912 ,0, 0,0,0,0);\n" +             
            "const lowp float intensity = 0.6;" +             
            "void main(void)\n" +
            "{\n" +            
            "   lowp vec4 textureColor = texture2D(u_Texture, v_TexCoordinate);\n" + 
            "	lowp vec4 outputColor = textureColor * colorMatrix;\n" +
            "	gl_FragColor = (intensity * outputColor) + ((1.0 - intensity) * textureColor);\n" + 
            "}";
    
    private static final String BLACKWHITE_FRAGMENT_SHADER = 
    		"precision highp float;\n" +
		    "varying vec2 v_TexCoordinate;\n" +		
		    "uniform sampler2D u_Texture;\n" +		
		    "const highp vec3 W = vec3(0.2125, 0.7154, 0.0721);\n" +		
		    "void main()\n" +
		    "{\n" +
		    "    float luminance = dot(texture2D(u_Texture, v_TexCoordinate).rgb, W);\n" +		
		    "    gl_FragColor = vec4(vec3(luminance), 1.0);\n" +
		    "}";        
    
    private static final String SWIRL_FRAGMENT_SHADER = 
    		"varying highp vec2 v_TexCoordinate;\n" +
		    "uniform sampler2D u_Texture;\n" +		
		    "const highp vec2 center = vec2(0.5, 0.5);\n" +
		    "const highp float radius = 0.5;\n" +
		    "const highp float angle = 1.0;\n" +		
		    "void main()\n" +
		    "{\n" +
		    "    highp vec2 textureCoordinateToUse = v_TexCoordinate;\n" +
		    "    highp float dist = distance(center, v_TexCoordinate);\n" +
		    "    textureCoordinateToUse -= center;\n" +
		    "    if (dist < radius)\n" +
		    "    {\n" +
		    "        highp float percent = (radius - dist) / radius;\n" +
		    "        highp float theta = percent * percent * angle * 8.0;\n" +
		    "        highp float s = sin(theta);\n" +
		    "        highp float c = cos(theta);\n" +
		    "        textureCoordinateToUse = vec2(dot(textureCoordinateToUse, vec2(c, -s)), dot(textureCoordinateToUse, vec2(s, c)));\n" +
		    "    }\n" +
		    "    textureCoordinateToUse += center;\n" +		
		    "    gl_FragColor = texture2D(u_Texture, textureCoordinateToUse );\n" +		
		    "}";
    
    /*private static final String SKETCH_FRAGMENT_SHADER = 
    		"varying highp vec2 v_TexCoordinate;\n" +	

		    "uniform sampler2D u_Texture;\n" +	
		
		    "const mediump float intensity = 5;\n" +	
		    "const mediump float imageWidthFactor = 2; \n" +	
		    "const mediump float imageHeightFactor = 2; \n" +	
		
		    "const mediump vec3 W = vec3(0.2125, 0.7154, 0.0721);\n" +	
		
		    "void main()\n" +	
		    "{\n" +	
		    "   mediump vec3 textureColor = texture2D(u_Texture, v_TexCoordinate).rgb;\n" +	
		
		    "   mediump vec2 stp0 = vec2(1.0 / imageWidthFactor, 0.0);\n" +	
		    "   mediump vec2 st0p = vec2(0.0, 1.0 / imageHeightFactor);\n" +	
		    "   mediump vec2 stpp = vec2(1.0 / imageWidthFactor, 1.0 / imageHeightFactor);\n" +	
		    "   mediump vec2 stpm = vec2(1.0 / imageWidthFactor, -1.0 / imageHeightFactor);\n" +	
		
		    "   mediump float i00   = dot( textureColor, W);\n" +	
		    "   mediump float im1m1 = dot( texture2D(u_Texture, v_TexCoordinate - stpp).rgb, W);\n" +	
		    "   mediump float ip1p1 = dot( texture2D(u_Texture, v_TexCoordinate + stpp).rgb, W);\n" +	
		    "   mediump float im1p1 = dot( texture2D(u_Texture, v_TexCoordinate - stpm).rgb, W);\n" +	
		    "   mediump float ip1m1 = dot( texture2D(u_Texture, v_TexCoordinate + stpm).rgb, W);\n" +	
		    "   mediump float im10 = dot( texture2D(u_Texture, v_TexCoordinate - stp0).rgb, W);\n" +	
		    "   mediump float ip10 = dot( texture2D(u_Texture, v_TexCoordinate + stp0).rgb, W);\n" +	
		    "   mediump float i0m1 = dot( texture2D(u_Texture, v_TexCoordinate - st0p).rgb, W);\n" +	
		    "   mediump float i0p1 = dot( texture2D(u_Texture, v_TexCoordinate + st0p).rgb, W);\n" +	
		    "   mediump float h = -im1p1 - 2.0 * i0p1 - ip1p1 + im1m1 + 2.0 * i0m1 + ip1m1;\n" +	
		    "   mediump float v = -im1m1 - 2.0 * im10 - im1p1 + ip1m1 + 2.0 * ip10 + ip1p1;\n" +	
		
		    "   mediump float mag = 1.0 - length(vec2(h, v));\n" +	
		    "   mediump vec3 target = vec3(mag);\n" +	
		
		    "   gl_FragColor = vec4(mix(textureColor, target, intensity), 1.0);\n" +	
		    "}";
    */
    
    private static final String SKETCH_FRAGMENT_SHADER =    
		    "precision mediump float;\n" +	
    		"uniform float width;\n" +
            "uniform float height;\n" +
		     
		    //"const vec2 resolution = vec2(width, height);\n" +	
		    "uniform sampler2D u_Texture;\n" +	
		     
		    "const vec4 luminance_vector = vec4(0.3, 0.59, 0.11, 0.0);\n" +	
		     
		    "void main() {\n" +	
		    "   vec2 resolution = vec2(width, height);\n" + 
		    "	vec2 uv = vec2(1.0) - (gl_FragCoord.xy / resolution.xy);\n" +	
		    "	vec2 n = 1.0/resolution.xy;\n" +	
		    "	vec4 CC = texture2D(u_Texture, uv);\n" +	
		    "	vec4 RD = texture2D(u_Texture, uv + vec2( n.x, -n.y));\n" +	
		    "	vec4 RC = texture2D(u_Texture, uv + vec2( n.x,  0.0));\n" +	
		    "	vec4 RU = texture2D(u_Texture, uv + n);\n" +	
		    "	vec4 LD = texture2D(u_Texture, uv - n);\n" +	
		    "	vec4 LC = texture2D(u_Texture, uv - vec2( n.x,  0.0));\n" +	
		    "	vec4 LU = texture2D(u_Texture, uv - vec2( n.x, -n.y));\n" +	
		    "	vec4 CD = texture2D(u_Texture, uv - vec2( 0.0,  n.y));\n" +	
		    "	vec4 CU = texture2D(u_Texture, uv + vec2( 0.0,  n.y));\n" +	
		     
		    "	gl_FragColor = vec4(2.0*abs(length(\n" +	
		    "		vec2(\n" +	
		    "			-abs(dot(luminance_vector, RD - LD))\n" +	
		    "			+4.0*abs(dot(luminance_vector, RC - LC))\n" +	
		    "			-abs(dot(luminance_vector, RU - LU)),\n" +	
		    "			-abs(dot(luminance_vector, LD - LU))\n" +	
		    "			+4.0*abs(dot(luminance_vector, CD - CU))\n" +	
		    "			-abs(dot(luminance_vector, RD - RU))\n" +	
		    "		)\n" +	
		    "	)-0.5));\n" +	
		    "}";
    private static final String OILPAINTING_FRAGMENT_SHADER = 
    		"precision mediump float;\n" +	
    
    		"varying highp vec2 v_TexCoordinate;\n" +	
		    "uniform sampler2D u_Texture;\n" +	
    		
		    "const int radius = 2;\n" +			    
			"uniform float width;\n" +
			"uniform float height;\n" +				    
		
		    "void main (void) \n" +	
		    "{\n" +	
		    "	vec2 src_size = vec2 (width, height);\n" +
		    "   vec2 uv = v_TexCoordinate;\n" +	
		    "   float n = float((radius + 1) * (radius + 1));\n" +	
		
		    "   vec3 m[4];\n" +	
		    "   vec3 s[4];\n" +	
		    "   for (int k = 0; k < 4; ++k) {\n" +	
		    "       m[k] = vec3(0.0);\n" +	
		    "       s[k] = vec3(0.0);\n" +	
		    "   }\n" +	
		
		   "   for (int j = -radius; j <= 0; ++j)  {\n" +	
		    "       for (int i = -radius; i <= 0; ++i)  {\n" +	
		    "           vec3 c = texture2D(u_Texture, uv + vec2(i,j) / src_size).rgb;\n" +	
		    "           m[0] += c;\n" +	
		    "           s[0] += c * c;\n" +	
		    "       }\n" +	
		    "   }\n" +	
		
		    "   for (int j = -radius; j <= 0; ++j)  {\n" +	
		    "       for (int i = 0; i <= radius; ++i)  {\n" +	
		    "           vec3 c = texture2D(u_Texture, uv + vec2(i,j) / src_size).rgb;\n" +	
		    "           m[1] += c;\n" +	
		    "           s[1] += c * c;\n" +	
		    "       }\n" +	
		    "   }\n" +	
		
		    "   for (int j = 0; j <= radius; ++j)  {\n" +	
		    "       for (int i = 0; i <= radius; ++i)  {\n" +	
		    "           vec3 c = texture2D(u_Texture, uv + vec2(i,j) / src_size).rgb;\n" +	
		    "           m[2] += c;\n" +	
		    "           s[2] += c * c;\n" +	
		    "       }\n" +	
		    "   }\n" +	
		
		    "   for (int j = 0; j <= radius; ++j)  {\n" +	
		    "       for (int i = -radius; i <= 0; ++i)  {\n" +	
		    "           vec3 c = texture2D(u_Texture, uv + vec2(i,j) / src_size).rgb;\n" +	
		    "           m[3] += c;\n" +	
		    "           s[3] += c * c;\n" +	
		    "       }\n" +	
		    "   }\n" +	
		
		
		    "   float min_sigma2 = 1e+2;\n" +	
		    "   for (int k = 0; k < 4; ++k) {\n" +	
		    "       m[k] /= n;\n" +	
		    "       s[k] = abs(s[k] / n - m[k] * m[k]);\n" +	
		
		    "       float sigma2 = s[k].r + s[k].g + s[k].b;\n" +	
		    "       if (sigma2 < min_sigma2) {\n" +	
		    "           min_sigma2 = sigma2;\n" +	
		    "           gl_FragColor = vec4(m[k], 1.0);\n" +	
		    "       }\n" +	
		    "   }\n" +	
		    //"  gl_FragColor = texture2D(u_Texture, v_TexCoordinate);" +
		    "}";
    
    public static final String POLKA_DOT_FILTER_FRAGMENT_SHADER =
    		"precision mediump float;" +
    
			"varying vec2 v_TexCoordinate;\n" +			    
		    "uniform sampler2D u_Texture;\n" +	
		    
		    "const float fractionalWidthOfPixel = 0.05;\n" +	
		    "const float aspectRatio = 1.0;\n" +	
		    "const float dotScaling = 0.9;\n" +	
		    
		    "void main()\n" +	
		    "{\n" +	
		    "    vec2 sampleDivisor = vec2(fractionalWidthOfPixel, fractionalWidthOfPixel / aspectRatio);\n" +	
		        
		    "    vec2 samplePos = v_TexCoordinate - mod(v_TexCoordinate, sampleDivisor) + 0.5 * sampleDivisor;\n" +	
		    "    vec2 textureCoordinateToUse = vec2(v_TexCoordinate.x, (v_TexCoordinate.y * aspectRatio + 0.5 - 0.5 * aspectRatio));\n" +	
		    "    vec2 adjustedSamplePos = vec2(samplePos.x, (samplePos.y * aspectRatio + 0.5 - 0.5 * aspectRatio));\n" +	
		    "    float distanceFromSamplePoint = distance(adjustedSamplePos, textureCoordinateToUse);\n" +	
		    "    float checkForPresenceWithinDot = step(distanceFromSamplePoint, (fractionalWidthOfPixel * 0.5) * dotScaling);\n" +	
		        
		    "    vec4 inputColor = texture2D(u_Texture, samplePos);\n" +	
		
		    "    gl_FragColor = vec4(inputColor.rgb * checkForPresenceWithinDot, inputColor.a);\n" +	
		    "}";
    
    private static final String TOON_FILTER_FRAGMENT_SHADER = 
			"varying vec2 v_TexCoordinate;\n" +	
		    "const vec2 leftTextureCoordinate = vec2(0,0.5);\n" +	
		    "const vec2 rightTextureCoordinate = vec2(1,0.5);\n" +	
		    
		    "const vec2 topTextureCoordinate = vec2(0.5,1);\n" +	
		    "const vec2 topLeftTextureCoordinate = vec2(0,1);\n" +		
		    "const vec2 topRightTextureCoordinate = vec2(1,1);\n" +	
		    
		    "const vec2 bottomTextureCoordinate = vec2(0.5,0);\n" +	
		    "const vec2 bottomLeftTextureCoordinate = vec2(0,0);\n" +		
		    "const vec2 bottomRightTextureCoordinate = vec2(1,0);\n" +		
		    
		    "uniform sampler2D u_Texture;\n" +	
		    
		    "const float intensity = 1.0;\n" +	
		    "const float threshold = 0.2;\n" +	
		    "const float quantizationLevels = 10.0;\n" +	
		    
		    "const vec3 W = vec3(0.2125, 0.7154, 0.0721);\n" +	
		    
		    "void main()\n" +	
		    "{\n" +	
		    "    vec4 textureColor = texture2D(u_Texture, v_TexCoordinate);\n" +	
		        
		    "    float bottomLeftIntensity = texture2D(u_Texture, bottomLeftTextureCoordinate).r;\n" +	
		    "    float topRightIntensity = texture2D(u_Texture, topRightTextureCoordinate).r;\n" +	
		    "    float topLeftIntensity = texture2D(u_Texture, topLeftTextureCoordinate).r;\n" +	
		    "    float bottomRightIntensity = texture2D(u_Texture, bottomRightTextureCoordinate).r;\n" +	
		    "    float leftIntensity = texture2D(u_Texture, leftTextureCoordinate).r;\n" +	
		    "    float rightIntensity = texture2D(u_Texture, rightTextureCoordinate).r;\n" +	
		    "    float bottomIntensity = texture2D(u_Texture, bottomTextureCoordinate).r;\n" +	
		    "    float topIntensity = texture2D(u_Texture, topTextureCoordinate).r;\n" +	
		    "    float h = -topLeftIntensity - 2.0 * topIntensity - topRightIntensity + bottomLeftIntensity + 2.0 * bottomIntensity + bottomRightIntensity;\n" +	
		    "    float v = -bottomLeftIntensity - 2.0 * leftIntensity - topLeftIntensity + bottomRightIntensity + 2.0 * rightIntensity + topRightIntensity;\n" +	
		        
		    "    float mag = length(vec2(h, v));\n" +	
		        
		    "    vec3 posterizedImageColor = floor((textureColor.rgb * quantizationLevels) + 0.5) / quantizationLevels;\n" +	
		        
		    "    float thresholdTest = 1.0 - step(threshold, mag);\n" +	
		        
		    "    gl_FragColor = vec4(posterizedImageColor * thresholdTest, textureColor.a);\n" +	
		    "}";
    
    private static final String POSTERIZE_FRAGMENT_SHADER =
    		"precision mediump float;" +
    
    		"varying vec2 v_TexCoordinate;\n" +	    
		    "uniform sampler2D u_Texture;\n" +	
    		
		    "const float colorLevels = 10.0;\n" +	
		    
		    "void main()\n" +	
		    "{\n" +	
		    "    vec4 textureColor = texture2D(u_Texture, v_TexCoordinate);\n" +	
		        
		    "    gl_FragColor = floor((textureColor * colorLevels) + vec4(0.5)) / colorLevels;\n" +	
		    "}";
    
    private static final String GLASS_SPHERE_FRAGMENT_SHADER = 
    		"precision mediump float;" +
    
		    "varying vec2 v_TexCoordinate;\n" +			    
		    "uniform sampler2D u_Texture;\n" +	
		    
		    "const vec2 center = vec2(0.5,0.5);\n" +	
		    "const float radius = 0.25;\n" +	
		    "const float aspectRatio = 1.0;\n" +	
		    "const float refractiveIndex = 0.71;\n" +	
		    // uniform vec3 lightPosition;
		    "const vec3 lightPosition = vec3(-0.5, 0.5, 1.0);\n" +	
		    "const vec3 ambientLightPosition = vec3(0.0, 0.0, 1.0);\n" +	
		    
		    "void main()\n" +	
		    "{\n" +	
		    "    vec2 textureCoordinateToUse = vec2(v_TexCoordinate.x, (v_TexCoordinate.y * aspectRatio + 0.5 - 0.5 * aspectRatio));\n" +	
		    "    float distanceFromCenter = distance(center, textureCoordinateToUse);\n" +	
		    "    float checkForPresenceWithinSphere = step(distanceFromCenter, radius);\n" +	
		        
		    "    distanceFromCenter = distanceFromCenter / radius;\n" +	
		        
		    "    float normalizedDepth = radius * sqrt(1.0 - distanceFromCenter * distanceFromCenter);\n" +	
		    "    vec3 sphereNormal = normalize(vec3(textureCoordinateToUse - center, normalizedDepth));\n" +	
		        
		    "    vec3 refractedVector = 2.0 * refract(vec3(0.0, 0.0, -1.0), sphereNormal, refractiveIndex);\n" +	
		    "    refractedVector.xy = -refractedVector.xy;\n" +	
		        
		    "    vec3 finalSphereColor = texture2D(u_Texture, (refractedVector.xy + 1.0) * 0.5).rgb;\n" +	
		        
		        // Grazing angle lighting
		    "    float lightingIntensity = 2.5 * (1.0 - pow(clamp(dot(ambientLightPosition, sphereNormal), 0.0, 1.0), 0.25));\n" +	
		    "    finalSphereColor += lightingIntensity;\n" +	
		        
		        // Specular lighting
		    "    lightingIntensity  = clamp(dot(normalize(lightPosition), sphereNormal), 0.0, 1.0);\n" +	
		    "    lightingIntensity  = pow(lightingIntensity, 15.0);\n" +	
		    "    finalSphereColor += vec3(0.8, 0.8, 0.8) * lightingIntensity;\n" +	
		        
		    "    gl_FragColor = vec4(finalSphereColor, 1.0) * checkForPresenceWithinSphere;\n" +	
		    "}";
    
    private static final String VIGNETTE_FRAGMENT_SHADER =    		
		"precision mediump float;" +
        "uniform sampler2D u_Texture;" + 
        "varying vec2 v_TexCoordinate;" + 		
		 
		"const vec2 vignetteCenter = vec2(0.5,0.5);\n" +	
		 "const vec3 vignetteColor = vec3(0.0,0.0,0.0);\n" +	
		 "const float vignetteStart = 0.3;\n" +	
		 "const float vignetteEnd = 0.75;\n" +	
		 
		 "void main()\n" +	
		 "{\n" +	
		 "    vec4 sourceImageColor = texture2D(u_Texture, v_TexCoordinate);\n" +	
		 "    float d = distance(v_TexCoordinate, vec2(vignetteCenter.x, vignetteCenter.y));\n" +	
		 "    float percent = smoothstep(vignetteStart, vignetteEnd, d);\n" +	
		 "    gl_FragColor = vec4(mix(sourceImageColor.rgb, vignetteColor, percent), sourceImageColor.a);\n" +	
		 "}";
    
    private static final String BULGE_FRAGMENT_SHADER = 
    	"precision mediump float;" +
	    "varying vec2 v_TexCoordinate;\n" +		    
	    "uniform sampler2D u_Texture;\n" +	
	    
	    "const float aspectRatio = 1.0;\n" +	
	    "const vec2 center = vec2(0.5,0.5);\n" +	
	    "const float radius = 0.25;\n" +	
	    "const float scale = 0.5;\n" +	
	    
	    "void main()\n" +	
	    "{\n" +	
	    "   vec2 textureCoordinateToUse = vec2(v_TexCoordinate.x, (v_TexCoordinate.y * aspectRatio + 0.5 - 0.5 * aspectRatio));\n" +	
	    "   float dist = distance(center, textureCoordinateToUse);\n" +	
	    "   textureCoordinateToUse = v_TexCoordinate;\n" +	
	       
	    "   if (dist < radius)\n" +	
	    "   {\n" +	
	    "       textureCoordinateToUse -= center;\n" +	
	    "       float percent = 1.0 - ((radius - dist) / radius) * scale;\n" +	
	    "       percent = percent * percent;\n" +	
	           
	    "       textureCoordinateToUse = textureCoordinateToUse * percent;\n" +	
	    "       textureCoordinateToUse += center;\n" +	
	    "   }\n" +	
	       
	    "   gl_FragColor = texture2D(u_Texture, textureCoordinateToUse );\n" +	
	    "}";
    
    private static final String PINCH_FRAGMENT_SHADER =
		"precision mediump float;" +
	    "varying vec2 v_TexCoordinate;\n" +		    
	    "uniform sampler2D u_Texture;\n" +	
	    
	    "const float aspectRatio = 1.0;\n" +	
	    "const vec2 center = vec2(0.5,0.5);\n" +	
	    "const float radius = 1.0;\n" +	
	    "const float scale = 1.0;\n" +	
	    
	    "void main()\n" +	
	    "{\n" +	
	    "    vec2 textureCoordinateToUse = vec2(v_TexCoordinate.x, (v_TexCoordinate.y * aspectRatio + 0.5 - 0.5 * aspectRatio));\n" +	
	    "    float dist = distance(center, textureCoordinateToUse);\n" +	
	    "    textureCoordinateToUse = v_TexCoordinate;\n" +	
	        
	    "    if (dist < radius)\n" +	
	    "    {\n" +	
	    "        textureCoordinateToUse -= center;\n" +	
	    "        float percent = 1.0 + ((0.5 - dist) / 0.5) * scale;\n" +	
	    "        textureCoordinateToUse = textureCoordinateToUse * percent;\n" +	
	    "        textureCoordinateToUse += center;\n" +	
	            
	    "        gl_FragColor = texture2D(u_Texture, textureCoordinateToUse );\n" +	
	    "    }\n" +	
	    "    else\n" +	
	    "    {\n" +	
	    "        gl_FragColor = texture2D(u_Texture, v_TexCoordinate );\n" +	
	    "    }\n" +	
	    "}";
    
    private static final String CROSS_PROCESS_FRAGMENT_SHADER =
            "precision mediump float;\n" +
            "uniform sampler2D u_Texture;\n" +
            "varying vec2 v_TexCoordinate;\n" +
            "void main() {\n" +
            "  vec4 color = texture2D(u_Texture, v_TexCoordinate);\n" +
            "  vec3 ncolor = vec3(0.0, 0.0, 0.0);\n" +
            "  float value;\n" +
            "  if (color.r < 0.5) {\n" +
            "    value = color.r;\n" +
            "  } else {\n" +
            "    value = 1.0 - color.r;\n" +
            "  }\n" +
            "  float red = 4.0 * value * value * value;\n" +
            "  if (color.r < 0.5) {\n" +
            "    ncolor.r = red;\n" +
            "  } else {\n" +
            "    ncolor.r = 1.0 - red;\n" +
            "  }\n" +
            "  if (color.g < 0.5) {\n" +
            "    value = color.g;\n" +
            "  } else {\n" +
            "    value = 1.0 - color.g;\n" +
            "  }\n" +
            "  float green = 2.0 * value * value;\n" +
            "  if (color.g < 0.5) {\n" +
            "    ncolor.g = green;\n" +
            "  } else {\n" +
            "    ncolor.g = 1.0 - green;\n" +
            "  }\n" +
            "  ncolor.b = color.b * 0.5 + 0.25;\n" +
            "  gl_FragColor = vec4(ncolor.rgb, color.a);\n" +
            "}\n";
    
    private static final String DOCUMENTARY_FRAGMENT_SHADER =
            "precision mediump float;\n" +
            "uniform sampler2D u_Texture;\n" +
            "const vec2 seed = vec2(1,50);\n" +
            "uniform float width;\n" +
            "uniform float height;\n" +
            "const float stepsize = 0.01;\n" +
            "uniform float inv_max_dist;\n" +
            "uniform vec2 vScale;\n" +
            "varying vec2 v_TexCoordinate;\n" +
            "float rand(vec2 loc) {\n" +
            "  float theta1 = dot(loc, vec2(0.9898, 0.233));\n" +
            "  float theta2 = dot(loc, vec2(12.0, 78.0));\n" +
            "  float value = cos(theta1) * sin(theta2) + sin(theta1) * cos(theta2);\n" +
            // keep value of part1 in range: (2^-14 to 2^14).
            "  float temp = mod(197.0 * value, 1.0) + value;\n" +
            "  float part1 = mod(220.0 * temp, 1.0) + temp;\n" +
            "  float part2 = value * 0.5453;\n" +
            "  float part3 = cos(theta1 + theta2) * 0.43758;\n" +
            "  return fract(part1 + part2 + part3);\n" +
            "}\n" +
            "void main() {\n" +
            
            // black white
            "  vec4 color = texture2D(u_Texture, v_TexCoordinate);\n" +
            "  float dither = rand(v_TexCoordinate + seed);\n" +
            "  vec3 xform = clamp(2.0 * color.rgb, 0.0, 1.0);\n" +
            "  vec3 temp = clamp(2.0 * (color.rgb + stepsize), 0.0, 1.0);\n" +
            "  vec3 new_color = clamp(xform + (temp - xform) * (dither - 0.5), 0.0, 1.0);\n" +
            // grayscale
            "  float gray = dot(new_color, vec3(0.299, 0.587, 0.114));\n" +
            "  new_color = vec3(gray, gray, gray);\n" +
            // vignette
            "  vec2 coord = v_TexCoordinate - vec2(0.5, 0.5);\n" +
            "  float dist = length(coord * vScale);\n" +
            "  float lumen = 0.85 / (1.0 + exp((dist * inv_max_dist - 0.83) * 20.0)) + 0.15;\n" +
            "  gl_FragColor = vec4(new_color * lumen, color.a);\n" +
            "}\n";
    
    private static final String DUOTONE_FRAGMENT_SHADER =
            "precision mediump float;\n" +
            "uniform sampler2D u_Texture;\n" +
            "const vec3 first = vec3(0.6,0.2,0.2);\n" +
            "const vec3 second = vec3(0.2,0.6,0.2);\n" +
            "varying vec2 v_TexCoordinate;\n" +
            "void main() {\n" +
            "  vec4 color = texture2D(u_Texture, v_TexCoordinate);\n" +
            "  float energy = (color.r + color.g + color.b) * 0.3333;\n" +
            "  vec3 new_color = (1.0 - energy) * first + energy * second;\n" +
            "  gl_FragColor = vec4(new_color.rgb, color.a);\n" +
            "}\n";
    
    private static final String FILLIGHT_FRAGMENT_SHADER =
            "precision mediump float;\n" +
            "uniform sampler2D u_Texture;\n" +
            "const float mult = 2.0;\n" +
            "const float igamma = 0.5;\n" +
            "varying vec2 v_TexCoordinate;\n" +
            "void main()\n" +
            "{\n" +
            "  const vec3 color_weights = vec3(0.25, 0.5, 0.25);\n" +
            "  vec4 color = texture2D(u_Texture, v_TexCoordinate);\n" +
            "  float lightmask = dot(color.rgb, color_weights);\n" +
            "  float backmask = (1.0 - lightmask);\n" +
            "  vec3 ones = vec3(1.0, 1.0, 1.0);\n" +
            "  vec3 diff = pow(mult * color.rgb, igamma * ones) - color.rgb;\n" +
            "  diff = min(diff, 1.0);\n" +
            "  vec3 new_color = min(color.rgb + diff * backmask, 1.0);\n" +
            "  gl_FragColor = vec4(new_color, color.a);\n" +
            "}\n";
    
    private static final String FISHEYE_FRAGMENT_SHADER =
            "precision mediump float;\n" +
            "uniform sampler2D u_Texture;\n" +
            "uniform vec2 vScale;\n" +
            "const float alpha = float(4.0 * 2.0 + 0.75);\n" +
            "varying vec2 v_TexCoordinate;\n" +
            "void main() {\n" +
            "  float bound2 = 0.25 * (vScale.x * vScale.x + vScale.y * vScale.y);\n" + 
            "  float bound = sqrt(bound2);\n" +
            "  float radius = 1.15 * bound;\n" +
            "  float radius2 = radius * radius;\n" +
            "  float max_radian = 0.5 * 3.14159265 - atan(alpha / bound * sqrt(radius2 - bound2));\n" + 
            "  float factor = bound / max_radian;\n" + 
            "  float m_pi_2 = 1.570963;\n" +
            "  vec2 coord = v_TexCoordinate - vec2(0.5, 0.5);\n" +
            "  float dist = length(coord * vScale);\n" +
            "  float radian = m_pi_2 - atan(alpha * sqrt(radius2 - dist * dist), dist);\n" +
            "  float scalar = radian * factor / dist;\n" +
            "  vec2 new_coord = coord * scalar + vec2(0.5, 0.5);\n" +
            "  gl_FragColor = texture2D(u_Texture, new_coord);\n" +
            "}\n";
    
    private final static String SHARPEN_FRAGMENT_SHADER =
            "precision mediump float;\n" +
            "uniform sampler2D u_Texture;\n" +
            "const float scale = 1.0;\n" +
            "uniform float width;\n" +
            "uniform float height;\n" +
            "uniform float stepx;\n" +
            "uniform float stepy;\n" +
            "varying vec2 v_TexCoordinate;\n" +
            "void main() {\n" +
            "  vec3 nbr_color = vec3(0.0, 0.0, 0.0);\n" +
            "  vec2 coord;\n" +
            "  vec4 color = texture2D(u_Texture, v_TexCoordinate);\n" +
            "  coord.x = v_TexCoordinate.x - 0.5 * stepx;\n" +
            "  coord.y = v_TexCoordinate.y - stepy;\n" +
            "  nbr_color += texture2D(u_Texture, coord).rgb - color.rgb;\n" +
            "  coord.x = v_TexCoordinate.x - stepx;\n" +
            "  coord.y = v_TexCoordinate.y + 0.5 * stepy;\n" +
            "  nbr_color += texture2D(u_Texture, coord).rgb - color.rgb;\n" +
            "  coord.x = v_TexCoordinate.x + stepx;\n" +
            "  coord.y = v_TexCoordinate.y - 0.5 * stepy;\n" +
            "  nbr_color += texture2D(u_Texture, coord).rgb - color.rgb;\n" +
            "  coord.x = v_TexCoordinate.x + stepx;\n" +
            "  coord.y = v_TexCoordinate.y + 0.5 * stepy;\n" +
            "  nbr_color += texture2D(u_Texture, coord).rgb - color.rgb;\n" +
            "  gl_FragColor = vec4(color.rgb - 2.0 * scale * nbr_color, color.a);\n" +
            "}\n";
    
    private final static String TINTS_FRAGMENT_SHADER =
    		"precision mediump float;\n" +
            "uniform sampler2D u_Texture;\n" +
            "const vec3 tint = vec3(0,0,255);\n" +
            "const vec3 color_ratio = vec3(0.21, 0.71, 0.07);\n" +
            "varying vec2 v_TexCoordinate;\n" +
            "void main() {\n" +
            "  vec4 color = texture2D(u_Texture, v_TexCoordinate);\n" +
            "  float avg_color = dot(color_ratio, color.rgb);\n" +
            "  vec3 new_color = min(0.8 * avg_color + 0.2 * tint, 1.0);\n" +
            "  gl_FragColor = vec4(new_color.rgb, color.a);\n" +
            "}\n";
    
    private static final String LOMOISH_FRAGMENT_SHADER = 
    		"precision mediump float;\n" +
            "uniform sampler2D u_Texture;\n" +
            "const vec2 seed = vec2(50, 500);\n" +
            "uniform float width;\n" +
            "uniform float height;\n" +
            "uniform float stepx;\n" +
            "uniform float stepy;\n" +
            "const float stepsize = 0.00392;\n" +
            "uniform vec2 vScale;\n" +
            "uniform float inv_max_dist;\n" +
            "varying vec2 v_TexCoordinate;\n" +
            "float rand(vec2 loc) {\n" +
            "  float theta1 = dot(loc, vec2(0.9898, 0.233));\n" +
            "  float theta2 = dot(loc, vec2(12.0, 78.0));\n" +
            "  float value = cos(theta1) * sin(theta2) + sin(theta1) * cos(theta2);\n" +
            // keep value of part1 in range: (2^-14 to 2^14).
            "  float temp = mod(197.0 * value, 1.0) + value;\n" +
            "  float part1 = mod(220.0 * temp, 1.0) + temp;\n" +
            "  float part2 = value * 0.5453;\n" +
            "  float part3 = cos(theta1 + theta2) * 0.43758;\n" +
            "  return fract(part1 + part2 + part3);\n" +
            "}\n" +
            "void main() {\n" +
            // sharpen
            "  vec3 nbr_color = vec3(0.0, 0.0, 0.0);\n" +
            "  vec2 coord;\n" +
            "  vec4 color = texture2D(u_Texture, v_TexCoordinate);\n" +
            "  coord.x = v_TexCoordinate.x - 0.5 * stepx;\n" +
            "  coord.y = v_TexCoordinate.y - stepy;\n" +
            "  nbr_color += texture2D(u_Texture, coord).rgb - color.rgb;\n" +
            "  coord.x = v_TexCoordinate.x - stepx;\n" +
            "  coord.y = v_TexCoordinate.y + 0.5 * stepy;\n" +
            "  nbr_color += texture2D(u_Texture, coord).rgb - color.rgb;\n" +
            "  coord.x = v_TexCoordinate.x + stepx;\n" +
            "  coord.y = v_TexCoordinate.y - 0.5 * stepy;\n" +
            "  nbr_color += texture2D(u_Texture, coord).rgb - color.rgb;\n" +
            "  coord.x = v_TexCoordinate.x + stepx;\n" +
            "  coord.y = v_TexCoordinate.y + 0.5 * stepy;\n" +
            "  nbr_color += texture2D(u_Texture, coord).rgb - color.rgb;\n" +
            "  vec3 s_color = vec3(color.rgb + 0.3 * nbr_color);\n" +
            // cross process
            "  vec3 c_color = vec3(0.0, 0.0, 0.0);\n" +
            "  float value;\n" +
            "  if (s_color.r < 0.5) {\n" +
            "    value = s_color.r;\n" +
            "  } else {\n" +
            "    value = 1.0 - s_color.r;\n" +
            "  }\n" +
            "  float red = 4.0 * value * value * value;\n" +
            "  if (s_color.r < 0.5) {\n" +
            "    c_color.r = red;\n" +
            "  } else {\n" +
            "    c_color.r = 1.0 - red;\n" +
            "  }\n" +
            "  if (s_color.g < 0.5) {\n" +
            "    value = s_color.g;\n" +
            "  } else {\n" +
            "    value = 1.0 - s_color.g;\n" +
            "  }\n" +
            "  float green = 2.0 * value * value;\n" +
            "  if (s_color.g < 0.5) {\n" +
            "    c_color.g = green;\n" +
            "  } else {\n" +
            "    c_color.g = 1.0 - green;\n" +
            "  }\n" +
            "  c_color.b = s_color.b * 0.5 + 0.25;\n" +
            // blackwhite
            "  float dither = rand(v_TexCoordinate + seed);\n" +
            "  vec3 xform = clamp((c_color.rgb - 0.15) * 1.53846, 0.0, 1.0);\n" +
            "  vec3 temp = clamp((color.rgb + stepsize - 0.15) * 1.53846, 0.0, 1.0);\n" +
            "  vec3 bw_color = clamp(xform + (temp - xform) * (dither - 0.5), 0.0, 1.0);\n" +
            // vignette
            "  coord = v_TexCoordinate - vec2(0.5, 0.5);\n" +
            "  float dist = length(coord * vScale);\n" +
            "  float lumen = 0.85 / (1.0 + exp((dist * inv_max_dist - 0.73) * 20.0)) + 0.15;\n" +
            "  gl_FragColor = vec4(bw_color * lumen, color.a);\n" +
            "}\n";
    
    private static final String NEGATIVE_FRAGMENT_SHADER = 
    		"precision mediump float;\n" +
            "uniform sampler2D u_Texture;\n" +
            "varying vec2 v_TexCoordinate;\n" +
            "void main() {\n" +
            "  vec4 color = texture2D(u_Texture, v_TexCoordinate);\n" +
            "  gl_FragColor = vec4(1.0 - color.rgb, color.a);\n" +
            "}\n";
    
    private FloatBuffer vertexBuffer;
    private ShortBuffer drawListBuffer;
    private FloatBuffer mSquareTextureCoordinates;
    private int mProgram= -1;
    private int mPositionHandle;
    private int mColorHandle;
    private int mWidthHandle;
    private int mHeightHandle;
    private int mStepXHandle;
    private int mStepYHandle;
    private int mInvMaxDistHandle;
    private int mScaleHandle;
    private int mMVPMatrixHandle;
    /** This will be used to pass in the texture. */
    private int mTextureUniformHandle;     
    /** This will be used to pass in model texture coordinate information. */
    private int mTextureCoordinateHandle;
    /** This is a handle to our texture data. */
    private int mTextureDataHandle;
    

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    public float[] squareCoords;

    private final short drawOrder[] = { 0, 1, 2, 0, 2, 3 }; // order to draw vertices

    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    float color[] = { 0.2f, 0.709803922f, 0.898039216f, 1.0f };

    private Map<String, String> effects = new HashMap<String, String>();
    private Context ctx;
    private SharedPreferences pref;
    private int texturePosition;
    
    private float width;
    private float height;
    
    private float inv_max_dist;
    private float scale[];
    
    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     */
    public Frame(Context ctx, int texturePosition, float z, GLSurfaceView surface) {
    	this.ctx = ctx;
    	this.texturePosition= texturePosition;
    	
    	squareCoords = new float[]{
                -1.0f,  1.0f, z,   // top left
                -1.0f, -1.0f, z,   // bottom left
                 1.0f, -1.0f, z,   // bottom right
                 1.0f, 1.0f, z}; // top right
    	
    	pref = PreferenceManager.getDefaultSharedPreferences(ctx);
    	
    	effects.put("null", fragmentShaderCode);
    	effects.put("emboss", EMBOSS_FRAGMENT_SHADER);
    	effects.put("blur", BLUR_FRAGMENT_SHADER);
    	effects.put("glow", GLOW_FRAGMENT_SHADER);
    	effects.put("halftone", HALFTONE_FRAGMENT_SHADER);
    	effects.put("mirror", MIRROR_FRAGMENT_SHADER);
    	effects.put("outline", OUTLINE_FRAGMENT_SHADER);
    	effects.put("pixelate", PIXELATE_FRAGMENT_SHADER);
    	effects.put("popart", POPART_FRAGMENT_SHADER);
    	effects.put("scanlines", SCANLINES_FRAGMENT_SHADER);
    	effects.put("sepia", SEPIA_FRAGMENT_SHADER);
    	effects.put("blackwhite", BLACKWHITE_FRAGMENT_SHADER);
    	//effects.put("fisheye", FISHEYE_FRAGMENT_SHADER);
    	effects.put("swirl", SWIRL_FRAGMENT_SHADER);
    	effects.put("sketch", SKETCH_FRAGMENT_SHADER);
    	effects.put("oilpainting", OILPAINTING_FRAGMENT_SHADER);
    	effects.put("polka", POLKA_DOT_FILTER_FRAGMENT_SHADER);
    	//effects.put("toon", TOON_FILTER_FRAGMENT_SHADER);
    	effects.put("posterize", POSTERIZE_FRAGMENT_SHADER);
    	effects.put("glasssphere", GLASS_SPHERE_FRAGMENT_SHADER);
    	effects.put("vignette", VIGNETTE_FRAGMENT_SHADER);
    	effects.put("bulge", BULGE_FRAGMENT_SHADER);
    	effects.put("pinch", PINCH_FRAGMENT_SHADER);
    	effects.put("crossprocess", CROSS_PROCESS_FRAGMENT_SHADER);
    	effects.put("documentary", DOCUMENTARY_FRAGMENT_SHADER);
    	effects.put("duotone", DUOTONE_FRAGMENT_SHADER);
    	effects.put("fillight", FILLIGHT_FRAGMENT_SHADER);
    	effects.put("fisheye", FISHEYE_FRAGMENT_SHADER);
    	effects.put("sharpen", SHARPEN_FRAGMENT_SHADER);
    	effects.put("tints", TINTS_FRAGMENT_SHADER);
//    	effects.put("grain", GRAIN_FRAGMENT_FILTER);
    	effects.put("lomoish", LOMOISH_FRAGMENT_SHADER);
    	effects.put("negative", NEGATIVE_FRAGMENT_SHADER);
    	
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
        // (# of coordinate values * 4 bytes per float)
                squareCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(squareCoords);
        vertexBuffer.position(0);

     // S, T (or X, Y)
     // Texture coordinate data.
     // Because images have a Y axis pointing downward (values increase as you move down the image) while
     // OpenGL has a Y axis pointing upward, we adjust for that here by flipping the Y axis.
        final float[] squareTextureCoordinateData =
            {                                                                                                
        		1.0f, 0.0f,                                 
                1.0f, 1.0f,
                0.0f, 1.0f,
                0.0f, 0.0f
            };
        mSquareTextureCoordinates = ByteBuffer.allocateDirect(squareTextureCoordinateData.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mSquareTextureCoordinates.put(squareTextureCoordinateData).position(0);
        
        
        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 2 bytes per short)
                drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);

        //dont reload shaders and program
        if (mProgram == -1){
        
	        // prepare shaders and OpenGL program
	        vertexShader = GlesUtils.loadShader(
	                GLES20.GL_VERTEX_SHADER,
	                vertexShaderCode);
	        fragmentShader = GlesUtils.loadShader(
	                GLES20.GL_FRAGMENT_SHADER,
	                effects.get(pref.getString("selectedEffect", "null")));
	
	        mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
	        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
	        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
	        GLES20.glLinkProgram(mProgram);   
	        
	        // Add program to OpenGL environment
	        GLES20.glUseProgram(mProgram);// create OpenGL program executables
        }
                
        /* Color */
        // get handle to fragment shader's vColor member
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

        // Set color for drawing the triangle
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);
        
        /* Dimensions */
        width = surface.getWidth();
        height = surface.getHeight();
        Log.d("Frame", "width:" + surface.getWidth() + ", height:" + surface.getHeight());
        mWidthHandle = GLES20.glGetUniformLocation(mProgram, "width");
        GlesUtils.checkGlError("glGetUniformLocation");
        GLES20.glUniform1f(mWidthHandle, width);
        GlesUtils.checkGlError("glUniform1f");
        
        mHeightHandle = GLES20.glGetUniformLocation(mProgram, "height");
        GlesUtils.checkGlError("glGetUniformLocation");
        GLES20.glUniform1f(mHeightHandle, height);
        GlesUtils.checkGlError("glUniform1f");
        
        mStepXHandle = GLES20.glGetUniformLocation(mProgram, "stepx");
        GlesUtils.checkGlError("glGetUniformLocation");
        GLES20.glUniform1f(mStepXHandle, 0.004f);
        GlesUtils.checkGlError("glUniform1f");
        
        mStepYHandle = GLES20.glGetUniformLocation(mProgram, "stepy");
        GlesUtils.checkGlError("glGetUniformLocation");
        GLES20.glUniform1f(mStepYHandle, 0.004f);
        GlesUtils.checkGlError("glUniform1f");
        
        scale = new float[2];
        if (surface.getWidth() > surface.getHeight()) {
            scale[0] = 1f;
            scale[1] = ((float) surface.getHeight()) / surface.getWidth();
        } else {
            scale[0] = ((float) surface.getWidth()) / surface.getHeight();
            scale[1] = 1f;
        }
        inv_max_dist = ((float) Math.sqrt(scale[0] * scale[0] + scale[1] * scale[1])) * 0.5f;
        mInvMaxDistHandle = GLES20.glGetUniformLocation(mProgram, "inv_max_dist");
        GlesUtils.checkGlError("glGetUniformLocation");
        GLES20.glUniform1f(mInvMaxDistHandle, inv_max_dist);
        GlesUtils.checkGlError("glUniform1f");
        mScaleHandle = GLES20.glGetUniformLocation(mProgram, "vScale");
        GlesUtils.checkGlError("glGetUniformLocation");
        GLES20.glUniform2fv(mScaleHandle, 1, scale, 0);
        GlesUtils.checkGlError("glUniform2fv");
        
        /* Texture */
        mTextureUniformHandle = GLES20.glGetUniformLocation(mProgram, "u_Texture");                
        GlesUtils.checkGlError("glGetUniformLocation");                
        
        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.        
        GLES20.glUniform1i(mTextureUniformHandle, texturePosition);
        GlesUtils.checkGlError("glUniform1i");
        
        mTextureCoordinateHandle = GLES20.glGetAttribLocation(mProgram, "a_TexCoordinate");
        GlesUtils.checkGlError("glGetAttribLocation");
        
        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);
        GlesUtils.checkGlError("glEnableVertexAttribArray");
        
        GLES20.glVertexAttribPointer(
        		mTextureCoordinateHandle, 2,
                GLES20.GL_FLOAT, false,
                2 * 4, mSquareTextureCoordinates);
        GlesUtils.checkGlError("glVertexAttribPointer");                
        
        /* Preparing vertices */
        
        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(
                mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);        

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        GlesUtils.checkGlError("glGetUniformLocation");
        
    }

    public void resetFragmentShader(){ 
    	Log.d("Frame", "reseting fragment shader for the effect : " + pref.getString("selectedEffect", "null"));
    	GLES20.glDetachShader(mProgram, fragmentShader);    
    	GlesUtils.checkGlError("glDetachShader");
    	
    	fragmentShader = GlesUtils.loadShader(
                GLES20.GL_FRAGMENT_SHADER,
                effects.get(pref.getString("selectedEffect", "null")));    	
    	
        GLES20.glAttachShader(mProgram, fragmentShader);
        GlesUtils.checkGlError("glAttachShader");
        GLES20.glLinkProgram(mProgram);      
        GlesUtils.checkGlError("glLinkProgram");
        
        GLES20.glUseProgram(mProgram);
        GlesUtils.checkGlError("glUseProgram");
                
        mWidthHandle = GLES20.glGetUniformLocation(mProgram, "width");
        GlesUtils.checkGlError("glGetUniformLocation");
        GLES20.glUniform1f(mWidthHandle, width);
        GlesUtils.checkGlError("glUniform1f");
        
        mHeightHandle = GLES20.glGetUniformLocation(mProgram, "height");
        GlesUtils.checkGlError("glGetUniformLocation");
        GLES20.glUniform1f(mHeightHandle, height);
        GlesUtils.checkGlError("glUniform1f");
        
        mStepXHandle = GLES20.glGetUniformLocation(mProgram, "stepx");
        GlesUtils.checkGlError("glGetUniformLocation");
        GLES20.glUniform1f(mStepXHandle, 1.0f / width);
        GlesUtils.checkGlError("glUniform1f");
        
        mStepYHandle = GLES20.glGetUniformLocation(mProgram, "stepy");
        GlesUtils.checkGlError("glGetUniformLocation");
        GLES20.glUniform1f(mStepYHandle, 1.0f / height);
        GlesUtils.checkGlError("glUniform1f");
        
        inv_max_dist = ((float) Math.sqrt(scale[0] * scale[0] + scale[1] * scale[1])) * 0.5f;
        mInvMaxDistHandle = GLES20.glGetUniformLocation(mProgram, "inv_max_dist");
        GlesUtils.checkGlError("glGetUniformLocation");
        GLES20.glUniform1f(mInvMaxDistHandle, inv_max_dist);
        GlesUtils.checkGlError("glUniform1f");
        
        mScaleHandle = GLES20.glGetUniformLocation(mProgram, "vScale");
        GlesUtils.checkGlError("glGetUniformLocation");
        GLES20.glUniform2fv(mScaleHandle, 1, scale, 0);
        GlesUtils.checkGlError("glUniform2fv");        
                                  

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        GlesUtils.checkGlError("glGetUniformLocation");
               
    } 
    
    public void resetBuffer(){
    	vertexBuffer.position(0);
    	mSquareTextureCoordinates.position(0);
    	drawListBuffer.position(0);    	
    }
    
    public void draw(float[] mvpMatrix, int activeTexture, int texture) {
   	    Log.d("Frame", "drawing frame");
    	
	   	GLES20.glDisable(GLES20.GL_BLEND);
	   	GlesUtils.checkGlError("glDisable");
   	    
        GLES20.glActiveTexture(activeTexture);
        GlesUtils.checkGlError("glActiveTexture");
        
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);
        GlesUtils.checkGlError("glBindTexture");
    	
    	// Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        GlesUtils.checkGlError("glUniformMatrix4fv");        
        
        // Draw the square
        GLES20.glDrawElements(
                GLES20.GL_TRIANGLES, drawOrder.length,
                GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
        GlesUtils.checkGlError("glDrawElements");

    }    
    
}
