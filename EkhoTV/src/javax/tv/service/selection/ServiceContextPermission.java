/*
 * @(#)ServiceContextPermission.java	1.16 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.tv.service.selection;

import java.security.BasicPermission;
import java.security.Permission;

/**
 * <code>ServiceContextPermission</code> represents permission to control a
 * <code>ServiceContext</code>. A <code>ServiceContextPermission</code> contains
 * a name (also referred to as a "target name") and an actions string.
 * 
 * <p>
 * The target name is the name of the service context permission (see the table
 * below). Each permission identifies a method. A wildcard match is signified by
 * an asterisk, i.e., "*".
 * 
 * <p>
 * <a name="actions"></a> The actions string is either "own" or "*". From a
 * security standpoint, a caller is said to "own" a <code>ServiceContext</code>
 * instance if it was acquired through
 * {@link ServiceContextFactory#createServiceContext} or
 * {@link ServiceContextFactory#getServiceContext}. The string "own" means the
 * permission applies to your own service contexts; the string "*" implies
 * permission to these, plus permission for service contexts obtained from all
 * other sources.
 * 
 * <p>
 * The following table lists all the possible
 * <code>ServiceContextPermission</code> target names, and describes what the
 * permission allows for each.
 * <p>
 * 
 * <table border=1 cellpadding=5>
 * <tr>
 * <th>Permission Target Name</th>
 * <th>What the Permission Allows</th>
 * </tr>
 * 
 * <tr>
 * <td>access</td>
 * <td>Access to a <code>ServiceContext</code>, via
 * <code>ServiceContextFactory.getServiceContexts()</code></td>
 * </tr>
 * 
 * <tr>
 * <td>create</td>
 * <td>Creation of a <code>ServiceContext</code>.</td>
 * </tr>
 * 
 * <tr>
 * <td>destroy</td>
 * <td>Destruction of a <code>ServiceContext</code>.</td>
 * </tr>
 * 
 * <tr>
 * <td>getServiceContentHandlers</td>
 * <td>Obtaining the service content handlers from a <code>ServiceContext</code>
 * .</td>
 * </tr>
 * 
 * <tr>
 * <td>stop</td>
 * <td>Stopping a <code>ServiceContext</code>.</td>
 * </tr>
 * 
 * </table>
 * 
 * <p>
 * The permission ServiceContextPermission("access", "*") is intended to be
 * granted only to special monitoring applications and not to general broadcast
 * applications.
 * <p>
 * 
 * Note that undefined target and actions strings may be provided to the
 * constructors of this class, but subsequent calls to
 * <code>SecurityManager.checkPermission()</code> with the resulting
 * <code>SelectPermission</code> object will fail.
 * 
 * @see java.security.BasicPermission
 * @see java.security.Permission
 * @see ServiceContext
 * @see ServiceContextFactory
 * 
 * @version 1.23, 10/09/00
 * @author Bill Foote
 */

public final class ServiceContextPermission extends BasicPermission {

    /**
     * @serial the actions string
     */
    private String actions;

    /**
     * Creates a new ServiceContextPermission object with the specified name.
     * The name is the symbolic name of the permission, such as "create". An
     * asterisk may be used to signify a wildcard match.
     * 
     * @param name
     *            The name of the <code>ServiceContextPermission</code>
     * 
     * @param actions
     *            The actions string, <a href="#actions">as detailed in the
     *            class description</a>.
     */
    public ServiceContextPermission(String name, String actions) {
	super(name);
	this.actions = actions;
	if (actions == null) {
	    throw new NullPointerException();
	}
    }

    /**
     * Checks if the specified permission is "implied" by this object.
     * <p>
     * 
     * More specifically, this method returns true if:
     * <p>
     * <ul>
     * <li><i>p</i> is an instance of ServiceContextPermission, and
     * <li><i>p</i>'s action string matches this object's, or this object has
     * "*" as an action string, and
     * <li><i>p</i>'s locator's external form matches this object's locator
     * string, or this object's locator string is "*".
     * </ul>
     * 
     * @param p
     *            The permission against which to test.
     * 
     * @return <code>true</code> if the specified permission is equal to or
     *         implied by this permission; <code>false</code> otherwise.
     * 
     */
    public boolean implies(Permission p) {
	if (p == null)
	    throw new NullPointerException();

	if (!(p instanceof ServiceContextPermission))
	    return false;

	ServiceContextPermission scp = (ServiceContextPermission) p;
	// TBD: impl dependent on organization of locator
	// Use locator.equals() in the future? ^M
	boolean isName = ((getName().equals(scp.getName())) || (getName()
		.equals("*")));
	boolean isAction = ((getActions().equals(scp.getActions())) || (getActions()
		.equals("*")));
	return (isName && isAction);
    }

    /**
     * Tests two <code>ServiceContextPermission</code> objects for equality.
     * Returns <code>true</code> if and only if <code>obj</code>'s class is the
     * same as the class of this object, and <code>obj</code> has the same name
     * and actions string as this object.
     * 
     * @param obj
     *            The object to test for equality.
     * 
     * @return <code>true</code> if the two permissions are equal;
     *         <code>false</code> otherwise.
     */
    public boolean equals(Object obj) {

	if (this == obj)
	    return true;

	if (!(obj instanceof ServiceContextPermission))
	    return false;

	ServiceContextPermission other = (ServiceContextPermission) obj;
	return hashCode() == other.hashCode();
    }

    /**
     * Provides the hash code value of this object. Two
     * <code>ServiceContextPermission</code> objects that are equal will return
     * the same hash code.
     * 
     * @return The hash code value of this object.
     */
    public int hashCode() {
	return actions.hashCode() ^ getName().hashCode();
    }

    /**
     * Returns the canonical representation of the actions string.
     * 
     * @return The actions string of this permission.
     */
    public String getActions() {
	return actions;
    }
}
