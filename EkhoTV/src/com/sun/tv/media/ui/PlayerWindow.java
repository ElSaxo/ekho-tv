/*
 * @(#)PlayerWindow.java	1.5 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media.ui;

import javax.media.CachingControl;
import javax.media.CachingControlEvent;
import javax.media.Controller;
import javax.media.ControllerClosedEvent;
import javax.media.ControllerErrorEvent;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.EndOfMediaEvent;
import javax.media.Player;
import javax.media.PrefetchCompleteEvent;
import javax.media.RealizeCompleteEvent;
import javax.media.Time;

import nl.ekholabs.ekhotv.awt.Dimension;
import android.content.Context;
import android.graphics.Canvas;
import android.view.View;
import android.widget.RelativeLayout;

import com.sun.tv.media.SizeChangeEvent;

public class PlayerWindow extends RelativeLayout implements ControllerListener {

    Player player;
    // Panel framePanel;
    // ComponentListener cl;
    // ComponentListener fcl;
    // WindowListener wl;
    // MouseListener ml;

    View controlComp;
    View visualComp;
    // Insets insets;
    // PopupMenu zoomMenu = null;
    boolean windowCreated;
    boolean newVideo = true;
    boolean panelResized;
    View progressBar;

    public PlayerWindow(Context context, Player player) {
	super(context);
	this.player = player;

	setVisibility(VISIBLE);

	// // Anonymous class
	// addWindowListener(wl = new WindowAdapter() {
	// public void windowClosing(WindowEvent we) {
	// killThePlayer();
	// }
	// });

	/*
	 * framePanel.addComponentListener( fcl = new ComponentAdapter() {
	 * public void componentResized(ComponentEvent ce) { panelResized =
	 * true; doResize(); } });
	 * 
	 * addComponentListener( fcl = new ComponentAdapter() { public void
	 * componentResized(ComponentEvent ce) { insets = getInsets(); Dimension
	 * dim = getSize(); framePanel.setSize(dim.width - insets.left -
	 * insets.right, dim.height - insets.top - insets.bottom); } });
	 */

	/*
	 * addComponentListener( cl = new ComponentAdapter() { public void
	 * componentResized(ComponentEvent ce) { framePanel.invalidate();
	 * doResize(); } });
	 */

	player.addControllerListener(this);
	player.realize();
	// player.start();
    }

    void sleep(long time) {
	try {
	    Thread.currentThread().sleep(time);
	} catch (Exception e) {
	}
    }

    @Override
    protected void onDraw(Canvas canvas) {
	super.onDraw(canvas);
	windowCreated = true;
    }

    public synchronized void killThePlayer() {
	/*
	 * player.stop(); player.deallocate();
	 */
	// in order to avoid deadlock problems, remove visual and
	// control component if they are present, before closing the
	// player
	/*
	 * if (visualComp != null) framePanel.remove(visualComp); if
	 * (controlComp != null) framePanel.remove(controlComp);
	 */
	if (visualComp != null) {
	    removeView(visualComp);
	}
	if (controlComp != null) {
	    removeView(controlComp);
	}

	player.close();
    }

    public synchronized void controllerUpdate(ControllerEvent ce) {
	if (ce instanceof RealizeCompleteEvent) {
	    int width = 320;
	    int height = 0;
	    // insets = getInsets();

	    /*
	     * if (progressBar != null) framePanel.remove(progressBar);
	     */

	    if ((visualComp = player.getVisualComponent()) != null) {
		width = visualComp.getWidth();
		height = visualComp.getHeight();
		// framePanel.add(visualComp);
		addView(visualComp);
		visualComp.setMinimumWidth(width);
		visualComp.setMinimumHeight(height);
		// addPopupMenu(visualComp);
	    }

	    if ((controlComp = player.getControlPanelComponent()) != null) {
		int prefHeight = controlComp.getHeight();
		// framePanel.add(controlComp);
		// controlComp.setBounds(0, height, width, prefHeight);
		height += prefHeight;
	    }
	    player.prefetch();

	} else if (ce instanceof PrefetchCompleteEvent) {
	    /*
	     * if (newVideo) { if (visualComp != null) { Dimension vSize =
	     * visualComp.getPreferredSize(); if (controlComp != null)
	     * vSize.height += controlComp.getPreferredSize().height;
	     * panelResized = false; setSize(vSize.width + insets.left +
	     * insets.right, vSize.height + insets.top + insets.bottom); int
	     * waited = 0; while (panelResized == false && waited < 2000) { try
	     * { waited += 50; Thread.currentThread().sleep(50);
	     * Thread.currentThread().yield(); } catch (Exception e) {} } } else
	     * { int height = 1; if (controlComp != null) height =
	     * controlComp.getPreferredSize().height; setSize(320+insets.left +
	     * insets.right, height + insets.top + insets.bottom); } newVideo =
	     * false; }
	     */
	    if (player.getTargetState() != Controller.Started) {
		player.start();
	    }
	} else if (ce instanceof EndOfMediaEvent) {
	    player.setMediaTime(new Time(0));
	    if (player.getMediaNanoseconds() == 0)
		player.start();
	    else
		System.err
			.println("Failed to loop back: the player is not seekable.");
	} else if (ce instanceof ControllerErrorEvent) {
	    System.err.println("Received controller error");
	    killThePlayer();
	    // dispose();
	    player.close();
	} else if (ce instanceof SizeChangeEvent) {
	    // The video size has changed, resize the panel
	    /*
	     * if (framePanel != null) { SizeChangeEvent sce = (SizeChangeEvent)
	     * ce; int nooWidth = sce.getWidth(); int nooHeight =
	     * sce.getHeight(); // Add the height of the default control
	     * component if (controlComp != null) nooHeight +=
	     * controlComp.getPreferredSize().height; if (
	     * framePanel.getSize().width != nooWidth ||
	     * framePanel.getSize().height != nooHeight) { setSize(nooWidth +
	     * insets.left + insets.right, nooHeight + insets.top +
	     * insets.bottom); //validate(); } else doResize();
	     * 
	     * if (controlComp != null) controlComp.invalidate(); }
	     */
	} else if (ce instanceof ControllerClosedEvent) {
	    /*
	     * player.removeControllerListener(this); player.close();
	     */
	    /*
	     * if (framePanel != null) framePanel.removeAll();
	     */
	    removeAllViews();

	    player = null;
	    visualComp = null;
	    controlComp = null;
	    sleep(200);
	    // dispose();
	} else if (ce instanceof CachingControlEvent) {
	    CachingControl cc = ((CachingControlEvent) ce).getCachingControl();
	    if (cc != null && progressBar == null) {
		progressBar = cc.getControlComponent();
		if (progressBar == null)
		    progressBar = cc.getProgressBarComponent();
		if (progressBar != null) {
		    // framePanel.add(progressBar);
		    Dimension prefSize = new Dimension(progressBar.getWidth(),
			    progressBar.getHeight());
		    setMinimumWidth(this.getLeft() + this.getRight()
			    + prefSize.width);
		    setMinimumHeight(this.getTop() + this.getBottom()
			    + prefSize.height);
		}
	    }
	}
    }

    public void zoomTo(float z) {
	if (visualComp != null) {
	    Dimension d = new Dimension(visualComp.getWidth(),
		    visualComp.getHeight());
	    d.width = (int) (d.width * z);
	    d.height = (int) (d.height * z);
	    if (controlComp != null)
		d.height += controlComp.getHeight();

	    setMinimumWidth(this.getLeft() + this.getRight() + d.width);
	    setMinimumHeight(this.getTop() + this.getBottom() + d.height);
	}
    }

    /*
     * private void addPopupMenu(Component visual) { MenuItem mi; ActionListener
     * zoomSelect;
     * 
     * zoomMenu = new PopupMenu("Zoom");
     * 
     * zoomSelect = new ActionListener() { public void
     * actionPerformed(ActionEvent ae) { String action = ae.getActionCommand();
     * if (action.indexOf("1:2") >= 0) zoomTo(0.5f); else if
     * (action.indexOf("1:1") >= 0) zoomTo(1.0f); else if (action.indexOf("2:1")
     * >= 0) zoomTo(2.0f); else if (action.indexOf("4:1") >= 0) zoomTo(4.0f); }
     * }; visual.add(zoomMenu); mi = new MenuItem("Scale 1:2");
     * zoomMenu.add(mi); mi.addActionListener(zoomSelect); mi = new
     * MenuItem("Scale 1:1"); zoomMenu.add(mi);
     * mi.addActionListener(zoomSelect); mi = new MenuItem("Scale 2:1");
     * zoomMenu.add(mi); mi.addActionListener(zoomSelect); mi = new
     * MenuItem("Scale 4:1"); zoomMenu.add(mi);
     * mi.addActionListener(zoomSelect);
     * 
     * visual.addMouseListener( ml = new MouseAdapter() { public void
     * mousePressed(MouseEvent me) { if (me.isPopupTrigger())
     * zoomMenu.show(visualComp, me.getX(), me.getY()); }
     * 
     * public void mouseReleased(MouseEvent me) { if (me.isPopupTrigger())
     * zoomMenu.show(visualComp, me.getX(), me.getY()); }
     * 
     * public void mouseClicked(MouseEvent me) { if (me.isPopupTrigger())
     * zoomMenu.show(visualComp, me.getX(), me.getY()); } } ); }
     */
}
