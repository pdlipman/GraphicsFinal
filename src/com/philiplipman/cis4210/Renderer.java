package com.philiplipman.cis4210;
/**
 * 
 */
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;

import com.jogamp.opengl.util.GLBuffers;

import objects.block.Block;
import objects.block.BlockType;
import utils.Color;

import java.awt.Component; 
import java.awt.Point;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
/**
 * @author	Ost
 */

public class Renderer implements GLEventListener, KeyListener, MouseListener, MouseWheelListener
{  
	private static final int BUFSIZE = 512;
	
	private float _rotateX = 0;
    private float _rotateY = 0;
    private float cameraZoom = 30;
    
    private int _worldX = 20;
    private int _worldY = 20;
    private int _worldZ = 20;
    
	private final int RENDER = 1, SELECT = 2;
	private int _cmd = RENDER;
	private int _selected = -1;
	
    private int _names = 0;
    
    private Point pickPoint = new Point();
    
    private Block[][][] world = new Block[_worldX][_worldY][_worldZ];

	
    public void display(GLAutoDrawable drawable) 
    {
        update();
        render(drawable);
        
    	_names = 0;
    	
    	GL2 gl = drawable.getGL().getGL2();
    	
    	update();
        
		
		if(_cmd == RENDER)
		{
	        render(drawable);

		}
		else if(_cmd == SELECT)
		{
			pickRects(drawable);
		}

    }

    public void dispose(GLAutoDrawable drawable) 
    {
    	
    }

    public void init(GLAutoDrawable drawable) 
    {
    	initWorld();
    	
    	GL2 gl = drawable.getGL().getGL2();
    	GLU glu = new GLU();
    	
        //float[] position = {0, 1, -1, 1};
        //float[] ambient = {0.2f, 0.0f, 0.0f, 1f};
        //float[] specular = {0.6f, 0.4f, 0.4f, 1f};   	    
        
    	gl.glClearColor(1.0f,1.0f,1.0f,0.0f);
    	gl.glColor3f(1.0f,0.0f,0.0f);
    	
    	
    	gl.glMatrixMode(GL2.GL_PROJECTION);
    	
    	((Component) drawable).addKeyListener(this);
    	((Component) drawable).addMouseListener(this);
    	((Component) drawable).addMouseWheelListener(this);
    	
    	gl.glLoadIdentity();
    	//gl.glOrtho(-3.0,3.0,-3.0,3.0,-3.0,3.0);
    	glu.gluPerspective(55, 1, 1, 64);
    	glu.gluLookAt(0, 0, cameraZoom, 0, 0, 0, 0, 1, 0);

    	        
    	gl.glEnable(GL2.GL_DEPTH_TEST);

    	/**
    	gl.glEnable(GL2.GL_LIGHTING);
    	gl.glEnable(GL2.GL_LIGHT0);
    	gl.glEnable(GL2.GL_LIGHT1);
    	
    	gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, position, 0);
    	gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_AMBIENT, ambient, 0);
        gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_SPECULAR, specular, 0);   	
        
        float[] red = {0.7f, 0.0f, 0.0f};
        float[] white = {0.6f, 0.4f, 0.4f};
        
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, red, 0);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, specular, 0);
        gl.glMaterialf(GL2.GL_FRONT_AND_BACK, GL2.GL_SHININESS, 32f);
        **/
    }

    public void initWorld()
    {
        for(int x = 0; x < 20; x++)
        {
        	for(int y = 0; y < 3; y++)
        	{
        		for(int z = 0; z < 20; z++)
        		{
					world[x][y][z] = new Block(BlockType.DIRT, x, y, z);
					//System.out.println("hello, world");
        			
        		}
        	}
        }
    }
    
    private void pickRects(GLAutoDrawable drawable)
    {
    	
    	GL2 gl = drawable.getGL().getGL2();
    	GLU glu = new GLU();

    	int viewport[] = new int[4];
    	
    	IntBuffer buffer = GLBuffers.newDirectIntBuffer(BUFSIZE);
    	
    	gl.glGetIntegerv(GL.GL_VIEWPORT, viewport, 0);

    	gl.glSelectBuffer(BUFSIZE, buffer);
    	gl.glRenderMode(GL2.GL_SELECT);

    	gl.glInitNames();

        
    	// draw a triangle filling the window
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        
        gl.glPushMatrix();
        
        gl.glLoadIdentity();
                       
        gl.glRotatef(_rotateX, 1.0f, 0.0f, 0.0f);
        gl.glRotatef(_rotateY, 0.0f, 1.0f, 0.0f);

        gl.glTranslatef(-(_worldX * Block.BLOCKSIZE)/2, -(_worldY * Block.BLOCKSIZE)/2, -(_worldZ * Block.BLOCKSIZE)/2);
        
        gl.glMatrixMode(GL2.GL_PROJECTION);
    	gl.glPushMatrix();
    	gl.glLoadIdentity();
    	/* create 5x5 pixel picking region near cursor location */
    	glu.gluPickMatrix((double) pickPoint.x,
    			(double) (viewport[3] - pickPoint.y), //
    			0.01, 0.01, viewport, 0);
    	
    	glu.gluPerspective(55, 1, 1, 64);
    	glu.gluLookAt(0, 0, cameraZoom, 0, 0, 0, 0, 1, 0);
    	
		gl.glMatrixMode(gl.GL_MODELVIEW);

    	gl.glPushName(999);
    	
    	blockRender(drawable);
        
        gl.glPopName();
		gl.glMatrixMode(gl.GL_PROJECTION);
    	
    	gl.glPopMatrix();
    	
		gl.glMatrixMode(gl.GL_MODELVIEW);
    	
		gl.glPopMatrix();
	
    	int hits = gl.glRenderMode(gl.GL_RENDER);

    	processHits(hits, buffer);
    	
		_cmd = RENDER;

    }
 
    private void processHits(int hits, IntBuffer buffer)
    {
    	int names, ptr = 0;
    	Integer lowest = Integer.MAX_VALUE;
    	Integer z;
    	System.out.println("hits = " + hits);
    	// ptr = (GLuint *) buffer;
    	for (int i = 0; i < hits; i++)
    	{ /* for each hit */
    		names = buffer.get(ptr);
    		//System.out.println(" number of names for hit = " + names);
    		ptr++;
    		z = buffer.get(ptr);
    		ptr++;
    		//System.out.println(" z2 is " + buffer.get(ptr));
    		ptr++;
    		
    		//System.out.print("\n   the name is ");
    		
    		if(z < lowest)
    		{
    			lowest = z;
    			_selected = buffer.get(ptr);
    		}
    		ptr++;
    		
    		//for (int j = 0; j < names; j++)
    		//{ /* for each name */
    		//	System.out.println("" + buffer.get(ptr));
    		//	ptr++;
    		//}
    		
    	}
    	
		//_selected = buffer.get(ptr);
   	
    }
    
    public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) 
    {
    	
    }

    private void update() 
    {

    }

    private void render(GLAutoDrawable drawable) 
    {
        GL2 gl = drawable.getGL().getGL2();
    	GLU glu = new GLU();
    	
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT + GL2.GL_DEPTH_BUFFER_BIT);

        // draw a triangle filling the window
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
        
        gl.glRotatef(_rotateX, 1.0f, 0.0f, 0.0f);
        gl.glRotatef(_rotateY, 0.0f, 1.0f, 0.0f);
        
        gl.glTranslatef(-(_worldX * Block.BLOCKSIZE)/2, -(_worldY * Block.BLOCKSIZE)/2, -(_worldZ * Block.BLOCKSIZE)/2);
    	
        //glu.gluPerspective(55, 1, 1, 64);
    	//glu.gluLookAt(0, 0, cameraZoom, 0, 0, 0, 0, 1, 0);       
        
        gl.glPushMatrix();
               
        //gl.glCullFace(GL2.GL_BACK);
        //gl.glEnable(GL2.GL_CULL_FACE);
        
 
        //int[][][] test = new int[3][0][0];
        
        blockRender(drawable);
		/**
        int indexX = 0;
		int indexY = 0;
		int indexZ = 0;
		Float[] color = new Float[]{1.0f, 0.0f, 0.0f};
		squares(drawable, indexX, indexY, indexZ, color);
        **/
        
        /**
        _point2 = Arrays.asList(new Float[]{-1.5f, 1.5f, 1.5f});
        _point1 = Arrays.asList(new Float[]{1.5f, 1.5f, 1.5f});
        _point4 = Arrays.asList(new Float[]{1.5f, -1.5f, 1.5f});
        _point3 = Arrays.asList(new Float[]{-1.5f, -1.5f, 1.5f});      
        
        _color = Arrays.asList(new Float[]{1.0f, 0.0f, 0.0f});
        
        drawSquares(drawable, _color, _point1, _point2, _point3, _point4);      
        **/
        
        gl.glPopMatrix();
        
    }

    public void blockRender(GLAutoDrawable drawable)
    {
        for(int x = 0; x < 20; x++)
        {
        	for(int y = 0; y < 3; y++)
        	{
        		for(int z = 0; z < 20; z++)
        		{
					Block block = world[x][y][z];
					squares(drawable, block);
					//System.out.println("hello, world");
        			
        		}
        	}
        }

    }
    
    public void squares(GLAutoDrawable drawable, Block block)
    {

    	int indexX = block.x;
    	int indexY = block.y;
    	int indexZ = block.z;
    	
    	Color color = block.getColor();
    	
        float size = Block.BLOCKSIZE;

        List<Float> point1 = new ArrayList<Float>();
        List<Float> point2 = new ArrayList<Float>();
        List<Float> point3 = new ArrayList<Float>();
        List<Float> point4 = new ArrayList<Float>();
        
        //Front
        point1 = Arrays.asList(new Float[]{indexX * size + size, indexY * size, indexZ * size});
        point2 = Arrays.asList(new Float[]{indexX * size, indexY * size, indexZ * size});
        point3 = Arrays.asList(new Float[]{indexX * size, indexY * size - size, indexZ * size});                 
        point4 = Arrays.asList(new Float[]{indexX * size + size, indexY * size - size, indexZ * size});    
                
        block.frontName = drawSquare(drawable, color, point1, point2, point3, point4);
        
        //back
        point1 = Arrays.asList(new Float[]{indexX * size + size, indexY * size, indexZ * size - size});
        point2 = Arrays.asList(new Float[]{indexX * size, indexY * size, indexZ * size - size});
        point3 = Arrays.asList(new Float[]{indexX * size, indexY * size - size, indexZ * size - size});              
        point4 = Arrays.asList(new Float[]{indexX * size + size, indexY * size - size, indexZ * size - size});
               
        block.backName = drawSquare(drawable, color, point1, point2, point3, point4);
        
        //left
        point1 = Arrays.asList(new Float[]{indexX * size, indexY * size, indexZ * size - size});
        point2 = Arrays.asList(new Float[]{indexX * size, indexY * size, indexZ * size});
        point3 = Arrays.asList(new Float[]{indexX * size, indexY * size - size, indexZ * size});              
        point4 = Arrays.asList(new Float[]{indexX * size, indexY * size - size, indexZ * size - size});              
        
		block.leftName = drawSquare(drawable, color, point1, point2, point3, point4);
    
        //right
        point1 = Arrays.asList(new Float[]{indexX * size + size, indexY * size, indexZ * size});
        point2 = Arrays.asList(new Float[]{indexX * size + size, indexY * size, indexZ * size - size});
        point3 = Arrays.asList(new Float[]{indexX * size + size, indexY * size - size, indexZ * size - size});
        point4 = Arrays.asList(new Float[]{indexX * size + size, indexY * size - size, indexZ * size});

        block.rightName = drawSquare(drawable, color, point1, point2, point3, point4);
        

        //top
        point1 = Arrays.asList(new Float[]{indexX * size + size, indexY * size, indexZ * size});
        point2 = Arrays.asList(new Float[]{indexX * size, indexY * size, indexZ * size});
        point3 = Arrays.asList(new Float[]{indexX * size, indexY * size, indexZ * size - size});
        point4 = Arrays.asList(new Float[]{indexX * size + size, indexY * size, indexZ * size - size});
                
        block.topName = drawSquare(drawable, color, point1, point2, point3, point4);


        //bottom
        point1 = Arrays.asList(new Float[]{indexX * size + size, indexY * size - size, indexZ * size});
        point2 = Arrays.asList(new Float[]{indexX * size, indexY * size - size, indexZ * size});
        point3 = Arrays.asList(new Float[]{indexX * size, indexY * size - size, indexZ * size - size});
        point4 = Arrays.asList(new Float[]{indexX * size + size, indexY * size - size, indexZ * size - size});
        
        block.bottomName = drawSquare(drawable, color, point1, point2, point3, point4);

        
    }
    
    public int drawSquare(GLAutoDrawable drawable, Color color, List<Float> point1, List<Float> point2, List<Float> point3, List<Float> point4)
    {
        GL2 gl = drawable.getGL().getGL2();

        gl.glLoadName(_names);

        int name = _names;

        gl.glPolygonMode(GL2.GL_FRONT, GL2.GL_FILL);
        
        if(_names == _selected)
        {
            //gl.glColor3f(color.get(0) + 0.7f, color.get(1) + 0.7f, color.get(2) + 0.7f);
            gl.glColor4f(color.R() + 0.7f, color.G() + 0.7f, color.B() + 0.7f, color.Alpha());
            //System.out.println("selected: " + _selected);
        	//System.out.println("names: " + _names);
        }
        else
        {
            //gl.glColor3f(color.get(0), color.get(1), color.get(2));
            gl.glColor4f(color.R(), color.G(), color.B(), color.Alpha());
        }

        gl.glBegin(GL2.GL_POLYGON);
            gl.glVertex3f(point1.get(0), point1.get(1), point1.get(2));
            gl.glVertex3f(point2.get(0), point2.get(1), point2.get(2));
            gl.glVertex3f(point3.get(0), point3.get(1), point3.get(2));
            gl.glVertex3f(point4.get(0), point4.get(1), point4.get(2));
        gl.glEnd();
        
        
        gl.glColor4f(0.0f, 0.0f, 0.0f, 1.0f);
        gl.glLineWidth(5.0f);
        gl.glBegin(GL2.GL_LINES);
        	gl.glVertex3f(point1.get(0), point1.get(1), point1.get(2));
            gl.glVertex3f(point2.get(0), point2.get(1), point2.get(2));
                
            gl.glVertex3f(point2.get(0), point2.get(1), point2.get(2));
            gl.glVertex3f(point3.get(0), point3.get(1), point3.get(2));
                
            gl.glVertex3f(point3.get(0), point3.get(1), point3.get(2));
            gl.glVertex3f(point4.get(0), point4.get(1), point4.get(2));
                
            gl.glVertex3f(point4.get(0), point4.get(1), point4.get(2));
            gl.glVertex3f(point1.get(0), point1.get(1), point1.get(2));
        gl.glEnd();
    	
        _names++;
        return name;
    }

	public void keyPressed(KeyEvent key)
	{
		// TODO Auto-generated method stub
		switch(key.getKeyChar())
		{
    		case 'w':
    		{
    			_rotateX += 5.0f;
    			break;
    		}
    		case 's':
    		{
    			_rotateX -= 5.0f;
    			break;
    		}
    		
    		case 'a':
    		{
    			_rotateY -= 5.0f;
    			break;
    		}
    		case 'd':
    		{
    			_rotateY += 5.0f;
    			break;
    		}
		}
		//System.out.println("Pressed: " + key.getKeyChar());
		
	}

	public void keyReleased(KeyEvent arg0)
	{
		// TODO Auto-generated method stub
		
	}

	public void keyTyped(KeyEvent arg0)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseClicked(MouseEvent mouse)
	{
		// TODO Auto-generated method stub
		//System.out.println("x: " + mouse.getPoint().x + " y: " + mouse.getPoint().y);
	}

	@Override
	public void mouseEntered(MouseEvent mouse)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent mouse)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent mouse)
	{
		// TODO Auto-generated method stub
		System.out.println("button: " + mouse.getButton());
		pickPoint.x = mouse.getPoint().x;
		pickPoint.y = mouse.getPoint().y;
		_cmd = SELECT;		

		
	}

	@Override
	public void mouseReleased(MouseEvent arg0)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent event)
	{
		// TODO Auto-generated method stub
		//System.out.println(cameraZoom);
		//cameraZoom += event.getWheelRotation();
	}
		
}
