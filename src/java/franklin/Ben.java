/* ----------------------------------------------------------------------------
 * Ben (formerly SimpleOpenNI)
 * ----------------------------------------------------------------------------
 * Copyright (C) 2011 Max Rheiner / Interaction Design Zhdk
 * Copyright (C) 2012 Dan Lidral-Porter
 *
 * This file is part of Ben.
 *
 * Ben is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version (subject to the "Classpath" exception
 * as provided in the LICENSE.txt file that accompanied this code).
 *
 * Ben is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Ben.  If not, see <http://www.gnu.org/licenses/>.
 * ----------------------------------------------------------------------------
 */

package franklin;

import clojure.lang.IFn;
import processing.core.*;
import SimpleOpenNI.*;

public class Ben extends ContextWrapper implements SimpleOpenNIConstants
{
	static
        {   // load the nativ shared lib
            String sysStr = System.getProperty("os.name").toLowerCase();
            String libName = "SimpleOpenNI";
            String archStr = System.getProperty("os.arch").toLowerCase();

            // check which system + architecture
            if(sysStr.indexOf("win") >= 0)
            {   // windows
                if(archStr.indexOf("86") >= 0)
                    // 32bit
                    libName += "32";
                else if(archStr.indexOf("64") >= 0)
                    libName += "64";
             }
            else if(sysStr.indexOf("nix") >= 0 || sysStr.indexOf("linux") >=  0 )
            {   // unix
                if(archStr.indexOf("86") >= 0)
                    // 32bit
                    libName += "32";
                else if(archStr.indexOf("64") >= 0)
                {
                    System.out.println("----");
                    libName += "64";
                }
            }
            else if(sysStr.indexOf("mac") >= 0)
            {     // mac
            }

            try{
              //System.out.println("-- " + System.getProperty("user.dir"));
              System.loadLibrary(libName);
            }
            catch(UnsatisfiedLinkError e)
            {
              System.out.println("Can't find the SimpleOpenNI library (" +  libName  + ") : " + e);
            }
	}

    public static void start()
	{
	  if(_initFlag)
		return;

	  _initFlag = true;
	  initContext();
	}

	public static int deviceCount()
	{
	  start();
	  return ContextWrapper.deviceCount();
	}

    public static int deviceNames(StrVector nodeNames)
	{
	  start();
	  return ContextWrapper.deviceNames(nodeNames);
	}

	///////////////////////////////////////////////////////////////////////////
	// callback vars
	protected IFn _newUserFn;
	protected IFn _lostUserFn;

	protected IFn _startCalibrationFn;
	protected IFn _endCalibrationFn;

	protected IFn _startPoseFn;
	protected IFn _endPoseFn;

	// hands cb
	protected IFn _createHandsFn;
	protected IFn _updateHandsFn;
	protected IFn _destroyHandsFn;

	// gesture cb
	protected IFn _recognizeGestureFn;
	protected IFn _progressGestureFn;

	// nite session cb
	protected IFn _startSessionFn;
	protected IFn _endSessionFn;
	protected IFn _focusSessionFn;


	protected String 			_filename;
	//protected PApplet			_parent;

	protected PImage			_depthImage;
	protected int[]				_depthRaw;
	protected PVector[]			_depthMapRealWorld;
	protected XnPoint3D[] 		_depthMapRealWorldXn;
	//protected XnPoint3DArray	_depthMapRealWorldArray;

	protected PImage			_rgbImage;

	protected PImage			_irImage;

	protected PImage			_sceneImage;
  	protected int[]				_sceneRaw;

  	protected int[]				_userRaw;

	// update flags
	protected long				_depthMapTimeStamp;
	protected long				_depthImageTimeStamp;
	protected long				_depthRealWorldTimeStamp;

	protected long				_rgbTimeStamp;

	protected long				_irImageTimeStamp;

	protected long				_sceneMapTimeStamp;
	protected long				_sceneImageTimeStamp;

	static protected boolean	_initFlag = false;

	/**
	* Creates the OpenNI context ands inits the modules
	*
	* @param parent
	*          PApplet
	*/
	public Ben()
	{
		initVars();

		// load the initfile
		this.init();
	}

	/**
	* Creates the OpenNI context ands inits the modules
	*
	* @param deviceIndex
	*          int
	*/
	public Ben(int deviceIndex)
	{
		initVars();

		// load the initfile
		this.init(deviceIndex, RUN_MODE_SINGLE_THREADED);
	}

	protected void initVars()
	{
		_depthMapTimeStamp			= -1;
		_depthImageTimeStamp		= -1;
		_depthRealWorldTimeStamp	= -1;

		_rgbTimeStamp				= -1;

		_irImageTimeStamp			= -1;

		_sceneMapTimeStamp			= -1;
		_sceneImageTimeStamp		= -1;

		_newUserFn			= null;
		_lostUserFn 		= null;

		_startCalibrationFn = null;
		_endCalibrationFn	= null;

		_startPoseFn 		= null;
		_endPoseFn			= null;

		_createHandsFn		= null;
		_updateHandsFn		= null;
		_destroyHandsFn		= null;
	}

	/**
	*
	*/
	public void dispose()
	{
		close();
	}

	public void finalize()
	{
		close();
	}

	private void setupDepth()
	{
		_depthImage 		= new PImage(depthWidth(), depthHeight(),PConstants.RGB);
		_depthRaw 			= new int[depthMapSize()];
		_depthMapRealWorld 	= new PVector[depthMapSize()];
		_depthMapRealWorldXn = new XnPoint3D[depthMapSize()];

		for(int i=0;i < depthMapSize();i++ )
		{
			_depthMapRealWorld[i] 	= new PVector();
			_depthMapRealWorldXn[i] = new XnPoint3D();
		}

		//_depthMapRealWorldArray	= new XnPoint3DArray(depthMapSize());
	}

	/**
	* Enable the depthMap data collection
	*/
	public boolean enableDepth()
	{
		if(super.enableDepth())
		{	// setup the var for depth calc
			setupDepth();
			return true;
		}
		else
			return false;
	}

	/**
	* Enable the depthMap data collection
	*
	* @param width
	*          int
	* @param height
	*          int
	* @param fps
	*          int
	* @return returns true if depthMap generation was succesfull
	*/
	public boolean enableDepth(int width,int height,int fps)
	{
		if(super.enableDepth(width,height,fps))
		{	// setup the var for depth calc
			setupDepth();
			return true;
		}
		else
			return false;
	}

	public PImage depthImage()
	{
		updateDepthImage();
		return _depthImage;
	}

	public int[] depthMap()
	{
		updateDepthRaw();
		return _depthRaw;
	}

	public PVector[] depthMapRealWorld()
	{
		updateDepthRealWorld();
		return _depthMapRealWorld;
	}

	private void setupRGB()
	{
		_rgbImage = new PImage(rgbWidth(), rgbHeight(),PConstants.RGB);
	}

	/**
	* Enable the camera image collection
	*/
	public boolean enableRGB()
	{
		if(super.enableRGB())
		{	// setup the var for depth calc
			setupRGB();
			return true;
		}
		else
			return false;
	}

	/**
	* Enable the camera image collection
	*
	* @param width
	*          int
	* @param height
	*          int
	* @param fps
	*          int
	* @return returns true if rgbMap generation was succesfull
	*/
	public boolean enableRGB(int width,int height,int fps)
	{
		if(super.enableRGB(width,height,fps))
		{	// setup the var for depth calc
			setupRGB();
			return true;
		}
		else
			return false;
	}

	public PImage rgbImage()
	{
		updateImage();
		return _rgbImage;
	}

	private void setupIR()
	{
		_irImage = new PImage(irWidth(), irHeight(),PConstants.RGB);
	}

	/**
	* Enable the irMap data collection
	* ir is only available if there is no rgbImage activated at the same time
	*/
	public boolean enableIR()
	{
		if(super.enableIR())
		{	// setup the var for depth calc
			setupIR();
			return true;
		}
		else
			return false;
	}

	/**
	* Enable the irMap data collection
	* ir is only available if there is no irImage activated at the same time
	*
	* @param width
	*          int
	* @param height
	*          int
	* @param fps
	*          int
	* @return returns true if irMap generation was succesfull
	*/
	public boolean enableIR(int width,int height,int fps)
	{
		if(super.enableIR(width,height,fps))
		{	// setup the var for depth calc
			setupIR();
			return true;
		}
		else
			return false;
	}

	public PImage irImage()
	{
		updateIrImage();
		return _irImage;
	}

	private void setupScene()
	{
		_sceneImage = new PImage(sceneWidth(), sceneHeight(),PConstants.RGB);
		_sceneRaw = new int[sceneWidth() * sceneHeight()];
	}

	/**
	* Enable the scene data collection
	*/
	public boolean enableScene()
	{
		if(super.enableScene())
		{	// setup the var for depth calc
			setupScene();
			return true;
		}
		else
			return false;
	}

	/**
	* Enable the scene data collection
	*
	* @param width
	*          int
	* @param height
	*          int
	* @param fps
	*          int
	* @return returns true if sceneMap generation was succesfull
	*/
	public boolean enableScene(int width,int height,int fps)
	{
		if(super.enableScene(width,height,fps))
		{	// setup the var for depth calc
			setupScene();
			return true;
		}
		else
			return false;
	}

	public PImage sceneImage()
	{
		updateSceneImage();
		return _sceneImage;
	}

	public int[] sceneMap()
	{
		updateSceneRaw();
		return _sceneRaw;
	}


	public void getSceneFloor(PVector point,PVector normal)
	{
		XnVector3D p = new XnVector3D();
		XnVector3D n = new XnVector3D();

		super.getSceneFloor(p, n);
		point.set(p.getX(),p.getY(),p.getZ());
		normal.set(n.getX(),n.getY(),n.getZ());
	}


  public void onNewUser(IFn newUserFn) {
    _newUserFn = newUserFn;
  }

  public void onLostUser(IFn lostUserFn) {
		_lostUserFn = lostUserFn;
  }

  public void onStartCalibration(IFn startCalibrationFn) {
		_startCalibrationFn = startCalibrationFn;
  }

  public void onEndCalibration(IFn endCalibrationFn) {
		_endCalibrationFn = endCalibrationFn;
  }

  public void onStartPose(IFn startPoseFn) {
		_startPoseFn = startPoseFn;
  }

  public void onEndPose(IFn endPoseFn) {
		_endPoseFn = endPoseFn;
	}

	/**
	* Enable user
	*/
	public boolean enableUser(int flags)
	{
		if(super.enableUser(flags))
		{
      _userRaw = new int[userWidth() * userHeight()];
			return true;
		}
		else
			return false;
	}

	public int[] getUsersPixels(int user)
	{
		int size = userWidth() * userHeight();
		if(size == 0)
			return _userRaw;

		if(_userRaw.length != userWidth() * userHeight())
		{	// resize the array
			_userRaw = new int[userWidth() * userHeight()];
		}

		super.getUserPixels(user,_userRaw);
		return _userRaw;
	}

	public boolean getCoM(int user,PVector com)
	{
		boolean ret;
		XnPoint3D com1 = new XnPoint3D();
		ret = super.getCoM(user,com1);
		com.set(com1.getX(),
				com1.getY(),
				com1.getZ());

		return ret;
	}

  public void onCreateHands(IFn createHandsFn) {
		_createHandsFn = createHandsFn;
  }

  public void onUpdateHands(IFn updateHandsFn) {
		_updateHandsFn = updateHandsFn;
  }

  public void onDestroyHands(IFn destroyHandsFn) {
		_destroyHandsFn = destroyHandsFn;
	}

	/**
	* Enable hands
	*/
	public boolean enableHands() {
		return super.enableHands();
	}

	public void	startTrackingHands(PVector pos)
	{
		XnVector3D vec = new XnVector3D();
		vec.setX(pos.x);
		vec.setY(pos.y);
		vec.setZ(pos.z);
		super.startTrackingHands(vec);
	}


  public void onRecognizeGesture(IFn recognizeGestureFn) {
		_recognizeGestureFn = recognizeGestureFn;
   }

  public void onProgressGesture(IFn progressGestureFn) {
		_progressGestureFn = progressGestureFn;
	}

	/**
	* Enable gesture
	*/
	public boolean enableGesture()
	{
		return super.enableGesture();
	}

	protected void updateDepthRaw()
	{
		if((nodes() & NODE_DEPTH) == 0)
			return;
		if(_depthMapTimeStamp ==  updateTimeStamp())
			return;

		depthMap(_depthRaw);
		_depthMapTimeStamp = updateTimeStamp();
	}

	protected void updateDepthImage()
	{
		if((nodes() & NODE_DEPTH) == 0)
			return;
		if(_depthImageTimeStamp ==  updateTimeStamp())
			return;

		_depthImage.loadPixels();
			depthImage(_depthImage.pixels);
		_depthImage.updatePixels();
		_depthImageTimeStamp = updateTimeStamp();
	}

	protected void updateDepthRealWorld()
	{
		if((nodes() & NODE_DEPTH) == 0)
			return;
		if(_depthRealWorldTimeStamp ==  updateTimeStamp())
			return;

		depthMapRealWorld(_depthMapRealWorldXn);

		XnPoint3D vec;
		for(int i=0;i < _depthMapRealWorldXn.length;i++)
		{
			vec = _depthMapRealWorldXn[i];
			_depthMapRealWorld[i].set(vec.getX(),
									  vec.getY(),
								      vec.getZ());
		}

		_depthRealWorldTimeStamp = updateTimeStamp();
	}

	protected void updateImage()
	{
		if((nodes() & NODE_IMAGE) == 0)
			return;
		if(_rgbTimeStamp ==  updateTimeStamp())
			return;

		// copy the rgb map
		_rgbImage.loadPixels();
			rgbImage(_rgbImage.pixels);
		_rgbImage.updatePixels();

		_rgbTimeStamp = updateTimeStamp();
	}

	protected void updateIrImage()
	{
		if((nodes() & NODE_IR) == 0)
			return;
		if(_irImageTimeStamp ==  updateTimeStamp())
			return;

		_irImage.loadPixels();
			irImage(_irImage.pixels);
		_irImage.updatePixels();

		_irImageTimeStamp = updateTimeStamp();
	}

	protected void updateSceneRaw()
	{
		if((nodes() & NODE_SCENE) == 0)
			return;
		if(_sceneMapTimeStamp ==  updateTimeStamp())
			return;

		sceneMap(_sceneRaw);

		_sceneMapTimeStamp = updateTimeStamp();
	}

	protected void updateSceneImage()
	{
		if((nodes() & NODE_SCENE) == 0)
			return;
		if(_sceneImageTimeStamp ==  updateTimeStamp())
			return;

		// copy the scene map
		_sceneImage.loadPixels();
			sceneImage(_sceneImage.pixels);
		_sceneImage.updatePixels();
		_sceneImageTimeStamp = updateTimeStamp();
	}

	/**
	* Enable the user data collection
	*/
	public void update()
	{
		super.update();
	}

	/**
	* Draws a limb from joint1 to joint2
	*
	* @param userId
	*          int
	* @param joint1
	*          int
	* @param joint2
	*          int
	*/
	public void drawLimb(PApplet applet, int userId, int joint1, int  joint2)
	{
		if (!isCalibratedSkeleton(userId))
			return;
		if (!isTrackingSkeleton(userId))
			return;

		XnSkeletonJointPosition joint1Pos = new XnSkeletonJointPosition();
		XnSkeletonJointPosition joint2Pos = new XnSkeletonJointPosition();

		getJointPositionSkeleton(userId, joint1, joint1Pos);
		getJointPositionSkeleton(userId, joint2, joint2Pos);

		if (joint1Pos.getFConfidence() < 0.5 || joint2Pos.getFConfidence() < 0.5)
			return;

		// calc the 3d coordinate to screen coordinates
		XnVector3D pt1 = new XnVector3D();
		XnVector3D pt2 = new XnVector3D();

		convertRealWorldToProjective(joint1Pos.getPosition(), pt1);
		convertRealWorldToProjective(joint2Pos.getPosition(), pt2);

		applet.line(pt1.getX(), pt1.getY(),
					 pt2.getX(), pt2.getY());

	}

	/**
	* gets the coordinates of a joint
	*
	* @param userId
	*          int
	* @param joint
	*          int
	* @param jointPos
	*          PVector
	* @return The confidence of this joint
	*          float
	*/
	public float getJointPositionSkeleton(int userId,int joint,PVector jointPos)
	{
		if (!isCalibratedSkeleton(userId))
			return 0.0f;
		if (!isTrackingSkeleton(userId))
			return 0.0f;

		XnSkeletonJointPosition jointPos1 = new XnSkeletonJointPosition();

		getJointPositionSkeleton(userId, joint, jointPos1);
		jointPos.set(jointPos1.getPosition().getX(),
					 jointPos1.getPosition().getY(),
					 jointPos1.getPosition().getZ());

		return jointPos1.getFConfidence();
	}

	/**
	* gets the orientation of a joint
	*
	* @param userId
	*          int
	* @param joint
	*          int
	* @param jointOrientation
	*          PMatrix3D
	* @return The confidence of this joint
	*          float
	*/
	public float getJointOrientationSkeleton(int userId,int joint,PMatrix3D jointOrientation)
	{
		if (!isCalibratedSkeleton(userId))
			return 0.0f;
		if (!isTrackingSkeleton(userId))
			return 0.0f;

		XnSkeletonJointOrientation jointOrientation1 = new XnSkeletonJointOrientation();

		getJointOrientationSkeleton(userId, joint, jointOrientation1);

		// set the matrix by hand, openNI matrix is only 3*3(only rotation, no translation)
		float[] mat = jointOrientation1.getOrientation().getElements();
		jointOrientation.set(mat[0], mat[1], mat[2], 0,
							 mat[3], mat[4], mat[5], 0,
							 mat[6], mat[7], mat[8], 0,
							 0,		 0,		 0, 	 1);

		return jointOrientation1.getFConfidence();
	}



	public void convertRealWorldToProjective(PVector world,PVector proj)
	{
		XnVector3D w = new XnVector3D();
		XnVector3D p = new XnVector3D();

		w.setX(world.x);
		w.setY(world.y);
		w.setZ(world.z);
		convertRealWorldToProjective(w,p);
		proj.set(p.getX(),
				 p.getY(),
				 p.getZ());
	}

	public void convertProjectiveToRealWorld(PVector proj, PVector world)
	{
		XnVector3D p = new XnVector3D();
		XnVector3D w = new XnVector3D();

		p.setX( proj.x);
		p.setY( proj.y);
		p.setZ( proj.z);
		convertProjectiveToRealWorld(p,w);
		world.set(w.getX(),
				  w.getY(),
				  w.getZ());
	}

	///////////////////////////////////////////////////////////////////////////
	// helper methods
	public void drawCamFrustum(PApplet applet)
	{
		applet.g.pushStyle();

			// draw cam case
			applet.stroke(200,200,0);
			applet.noFill();
			applet.g.beginShape();
				applet.g.vertex(270 * .5f,40 * .5f,0.0f);
				applet.g.vertex(-270 * .5f,40 * .5f,0.0f);
				applet.g.vertex(-270 * .5f,-40 * .5f,0.0f);
				applet.g.vertex(270 * .5f,-40 * .5f,0.0f);
			applet.g.endShape(PConstants.CLOSE);

			applet.g.beginShape();
				applet.g.vertex(220 * .5f,40 * .5f,-50.0f);
				applet.g.vertex(-220 * .5f,40 * .5f,-50.0f);
				applet.g.vertex(-220 * .5f,-40 * .5f,-50.0f);
				applet.g.vertex(220 * .5f,-40 * .5f,-50.0f);
			applet.g.endShape(PConstants.CLOSE);

			applet.g.beginShape(PConstants.LINES);
				applet.g.vertex(270 * .5f,40 * .5f,0.0f);
				applet.g.vertex(220 * .5f,40 * .5f,-50.0f);

				applet.g.vertex(-270 * .5f,40 * .5f,0.0f);
				applet.g.vertex(-220 * .5f,40 * .5f,-50.0f);

				applet.g.vertex(-270 * .5f,-40 * .5f,0.0f);
				applet.g.vertex(-220 * .5f,-40 * .5f,-50.0f);

				applet.g.vertex(270 * .5f,-40 * .5f,0.0f);
				applet.g.vertex(220 * .5f,-40 * .5f,-50.0f);
			applet.g.endShape();

			// draw cam opening angles
			applet.stroke(200,200,0,50);
			applet.g.line(0.0f,0.0f,0.0f,
						   0.0f,0.0f,1000.0f);

			// calculate the angles of the cam, values are in radians, radius is 10m
			float distDepth = 10000;

			float valueH = distDepth * applet.tan(hFieldOfView() * .5f);
			float valueV = distDepth * applet.tan(vFieldOfView() * .5f);

			applet.stroke(200,200,0,100);
			applet.g.line(0.0f,0.0f,0.0f,
						 valueH,valueV,distDepth);
			applet.g.line(0.0f,0.0f,0.0f,
						 -valueH,valueV,distDepth);
			applet.g.line(0.0f,0.0f,0.0f,
						 valueH,-valueV,distDepth);
			applet.g.line(0.0f,0.0f,0.0f,
						 -valueH,-valueV,distDepth);
			applet.g.beginShape();
				applet.g.vertex(valueH,valueV,distDepth);
				applet.g.vertex(-valueH,valueV,distDepth);
				applet.g.vertex(-valueH,-valueV,distDepth);
				applet.g.vertex(valueH,-valueV,distDepth);
			applet.g.endShape(PConstants.CLOSE);

		applet.g.popStyle();
	}

	///////////////////////////////////////////////////////////////////////////
	// callbacks
	protected void onNewUserCb(long userId)
	{
    if (_newUserFn != null) {
			_newUserFn.invoke(this, (int) userId);
		}
	}

	protected void onLostUserCb(long userId)
	{
    if (_lostUserFn != null) {
			_lostUserFn.invoke(this, (int) userId);
		}
	}

	protected void onStartCalibrationCb(long userId)
	{
    if (_startCalibrationFn != null) {
			_startCalibrationFn.invoke(this, (int) userId);
    }
	}

	protected void onEndCalibrationCb(long userId, boolean successFlag)
	{
    if (_endCalibrationFn != null) {
			_endCalibrationFn.invoke(this, (int) userId, successFlag);
    }
	}

	protected void onStartPoseCb(String strPose, long userId)
	{
    if (_startPoseFn != null) {
			_startPoseFn.invoke(this, strPose, (int) userId);
		}
	}

	protected void onEndPoseCb(String strPose, long userId)
	{
    if (_endPoseFn != null) {
			_endPoseFn.invoke(this, strPose, (int) userId);
		}
	}

	// hands
	protected void onCreateHandsCb(long nId, XnPoint3D pPosition, float fTime)
	{
    if (_createHandsFn != null) {
      PVector pos = new PVector(pPosition.getX(), pPosition.getY(), pPosition.getZ());
      _createHandsFn.invoke(this, (int) nId, pos, fTime);
    }
	}

	protected void onUpdateHandsCb(long nId, XnPoint3D pPosition, float fTime)
	{
    if (_updateHandsFn != null) {
      PVector pos = new PVector(pPosition.getX(), pPosition.getY(), pPosition.getZ());
      _updateHandsFn.invoke(this, (int) nId, pos, fTime);
    }
	}

	protected void onDestroyHandsCb(long nId, float fTime)
	{
    if (_destroyHandsFn != null) {
      _destroyHandsFn.invoke(this, (int) nId, fTime);
    }
	}

	protected void onRecognizeGestureCb(String strGesture, XnPoint3D pIdPosition, XnPoint3D pEndPosition)
	{
    if (_recognizeGestureFn != null) {
      PVector idPos = new PVector(pIdPosition.getX(), pIdPosition.getY(), pIdPosition.getZ());
      PVector endPos = new PVector(pEndPosition.getX(), pEndPosition.getY(), pEndPosition.getZ());
			_recognizeGestureFn.invoke(this, strGesture, idPos, endPos);
		}
	}

	protected void onProgressGestureCb(String strGesture, XnPoint3D pPosition, float fProgress)
	{
    if (_recognizeGestureFn != null) {
      PVector pos = new PVector(pPosition.getX(),pPosition.getY(),pPosition.getZ());
			_recognizeGestureFn.invoke(this, strGesture, pos, fProgress);
		}
	}

	// nite callbacks
	protected void onStartSessionCb(XnPoint3D ptPosition)
	{
    if (_startSessionFn != null) {
      PVector pt = new PVector(ptPosition.getX(),ptPosition.getY(),ptPosition.getZ());
			_startSessionFn.invoke(this, pt);
		}
	}

	protected void onEndSessionCb()
	{
    if (_endSessionFn != null) {
			_endSessionFn.invoke(this);
		}
  }

	protected void onFocusSessionCb(String strFocus, XnPoint3D ptPosition, float fProgress)
	{
    if (_focusSessionFn != null) {
      PVector pt = new PVector(ptPosition.getX(), ptPosition.getY(), ptPosition.getZ());
			_focusSessionFn.invoke(this, strFocus, pt, fProgress);
    }
	}
}

