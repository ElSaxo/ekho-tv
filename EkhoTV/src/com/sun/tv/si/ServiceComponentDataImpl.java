/*
 * @(#)ServiceComponentImpl.java	1.12 05/11/23
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 */
package com.sun.tv.si;

import java.util.Date;

import javax.tv.service.Service;
import javax.tv.service.ServiceInformationType;
import javax.tv.service.guide.ProgramEvent;
import javax.tv.service.navigation.StreamType;

/**
 * ServiceComponentData (service-bound and service-unbound xlets) in the SI
 * database.
 */
public class ServiceComponentDataImpl extends ServiceComponentImpl {

    boolean isServiceUnbound;
    String[] xletPaths;
    String[] xletArgs;

    public ServiceComponentDataImpl(String name, String language,
	    StreamType streamType, Service service, ProgramEvent program,
	    ServiceInformationType siType, boolean autorun, Date updatedTime,
	    String[] xletPaths, String[] xletArgs, boolean isServiceUnbound) {

	super(name, language, streamType, service, program, siType, autorun,
		updatedTime);

	this.xletPaths = xletPaths;
	this.xletArgs = xletArgs;
	this.isServiceUnbound = isServiceUnbound;
    }

    public boolean getIsServiceUnbound() {
	return isServiceUnbound;
    }

    public String[] getXletPaths() {
	return xletPaths;
    }

    public String[] getXletArgs() {
	return xletArgs;
    }
}
