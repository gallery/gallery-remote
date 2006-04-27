/*
* @(#)AbstractListModel.java	1.30 01/12/03
*
* Copyright 2002 Sun Microsystems, Inc. All rights reserved.
* SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
*/

package com.gallery.GalleryRemote;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.TreePath;
import java.io.Serializable;
import java.util.EventListener;

/**
 * The abstract definition for the data model that provides
 * a <code>List</code> with its contents.
 * <p/>
 * <strong>Warning:</strong>
 * Serialized objects of this class will not be compatible with
 * future Swing releases. The current serialization support is
 * appropriate for short term storage or RMI between applications running
 * the same version of Swing.  As of 1.4, support for long term storage
 * of all JavaBeans<sup><font size="-2">TM</font></sup>
 * has been added to the <code>java.beans</code> package.
 * Please see {@link java.beans.XMLEncoder}.
 * 
 * @author Hans Muller
 * @version 1.30 12/03/01
 */
public abstract class GalleryAbstractListModel implements ListModel, Serializable {
	transient protected EventListenerList listenerList = new EventListenerList();


	/**
	 * Adds a transferListener to the list that's notified each time a change
	 * to the data model occurs.
	 * 
	 * @param l the <code>ListDataListener</code> to be added
	 */
	public void addListDataListener(ListDataListener l) {
		if (listenerList == null) listenerList = new EventListenerList();
		listenerList.add(ListDataListener.class, l);
	}


	/**
	 * Removes a transferListener from the list that's notified each time a
	 * change to the data model occurs.
	 * 
	 * @param l the <code>ListDataListener</code> to be removed
	 */
	public void removeListDataListener(ListDataListener l) {
		if (listenerList == null) listenerList = new EventListenerList();
		listenerList.remove(ListDataListener.class, l);
	}


	/**
	 * Returns an array of all the list data listeners
	 * registered on this <code>AbstractListModel</code>.
	 * 
	 * @return all of this model's <code>ListDataListener</code>s,
	 *         or an empty array if no list data listeners
	 *         are currently registered
	 * @see #addListDataListener
	 * @see #removeListDataListener
	 * @since 1.4
	 */
	public ListDataListener[] getListDataListeners() {
		if (listenerList == null) listenerList = new EventListenerList();
		return (ListDataListener[]) listenerList.getListeners(
				ListDataListener.class);
	}


	/**
	 * <code>AbstractListModel</code> subclasses must call this method
	 * <b>after</b>
	 * one or more elements of the list change.  The changed elements
	 * are specified by the closed interval index0, index1 -- the endpoints
	 * are included.  Note that
	 * index0 need not be less than or equal to index1.
	 * 
	 * @param source the <code>ListModel</code> that changed, typically "this"
	 * @param index0 one end of the new interval
	 * @param index1 the other end of the new interval
	 * @see EventListenerList
	 * @see DefaultListModel
	 */
	public void fireContentsChanged(Object source, int index0, int index1) {
		if (listenerList == null) listenerList = new EventListenerList();
		Object[] listeners = listenerList.getListenerList();
		ListDataEvent e = null;

		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ListDataListener.class) {
				if (e == null) {
					e = new ListDataEvent(source, ListDataEvent.CONTENTS_CHANGED, index0, index1);
				}
				((ListDataListener) listeners[i + 1]).contentsChanged(e);
			}
		}
	}


	/**
	 * <code>AbstractListModel</code> subclasses must call this method
	 * <b>after</b>
	 * one or more elements are added to the model.  The new elements
	 * are specified by a closed interval index0, index1 -- the enpoints
	 * are included.  Note that
	 * index0 need not be less than or equal to index1.
	 * 
	 * @param source the <code>ListModel</code> that changed, typically "this"
	 * @param index0 one end of the new interval
	 * @param index1 the other end of the new interval
	 * @see EventListenerList
	 * @see DefaultListModel
	 */
	public void fireIntervalAdded(Object source, int index0, int index1) {
		if (listenerList == null) listenerList = new EventListenerList();
		Object[] listeners = listenerList.getListenerList();
		ListDataEvent e = null;

		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ListDataListener.class) {
				if (e == null) {
					e = new ListDataEvent(source, ListDataEvent.INTERVAL_ADDED, index0, index1);
				}
				((ListDataListener) listeners[i + 1]).intervalAdded(e);
			}
		}
	}


	/**
	 * <code>AbstractListModel</code> subclasses must call this method
	 * <b>after</b> one or more elements are removed from the model.
	 * The new elements
	 * are specified by a closed interval index0, index1, i.e. the
	 * range that includes both index0 and index1.  Note that
	 * index0 need not be less than or equal to index1.
	 * 
	 * @param source the ListModel that changed, typically "this"
	 * @param index0 one end of the new interval
	 * @param index1 the other end of the new interval
	 * @see EventListenerList
	 * @see DefaultListModel
	 */
	public void fireIntervalRemoved(Object source, int index0, int index1) {
		if (listenerList == null) listenerList = new EventListenerList();
		Object[] listeners = listenerList.getListenerList();
		ListDataEvent e = null;

		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ListDataListener.class) {
				if (e == null) {
					e = new ListDataEvent(source, ListDataEvent.INTERVAL_REMOVED, index0, index1);
				}
				((ListDataListener) listeners[i + 1]).intervalRemoved(e);
			}
		}
	}

	/**
	 * Returns an array of all the objects currently registered as
	 * <code><em>Foo</em>Listener</code>s
	 * upon this model.
	 * <code><em>Foo</em>Listener</code>s
	 * are registered using the <code>add<em>Foo</em>Listener</code> method.
	 * <p/>
	 * You can specify the <code>listenerType</code> argument
	 * with a class literal, such as <code><em>Foo</em>Listener.class</code>.
	 * For example, you can query a list model
	 * <code>m</code>
	 * for its list data listeners
	 * with the following code:
	 * <p/>
	 * <pre>ListDataListener[] ldls = (ListDataListener[])(m.getListeners(ListDataListener.class));</pre>
	 * <p/>
	 * If no such listeners exist,
	 * this method returns an empty array.
	 * 
	 * @param listenerType the type of listeners requested;
	 *                     this parameter should specify an interface
	 *                     that descends from <code>java.util.EventListener</code>
	 * @return an array of all objects registered as
	 *         <code><em>Foo</em>Listener</code>s
	 *         on this model,
	 *         or an empty array if no such
	 *         listeners have been added
	 * @throws ClassCastException if <code>listenerType</code> doesn't
	 *                            specify a class or interface that implements
	 *                            <code>java.util.EventListener</code>
	 * @see #getListDataListeners
	 * @since 1.3
	 */
	public EventListener[] getListeners(Class listenerType) {
		if (listenerList == null) listenerList = new EventListenerList();
		return listenerList.getListeners(listenerType);
	}


	/****** from DefaultTreeModel *****/
	/**
	 * Invoked this to insert newChild at location index in parents children.
	 * This will then message nodesWereInserted to create the appropriate
	 * event. This is the preferred way to add children as it will create
	 * the appropriate event.
	 */

	/**
	 * Adds a transferListener for the TreeModelEvent posted after the tree changes.
	 * 
	 * @param l the transferListener to add
	 * @see #removeTreeModelListener
	 */
	public void addTreeModelListener(TreeModelListener l) {
		listenerList.add(TreeModelListener.class, l);
	}

	/**
	 * Removes a transferListener previously added with <B>addTreeModelListener()</B>.
	 * 
	 * @param l the transferListener to remove
	 * @see #addTreeModelListener
	 */
	public void removeTreeModelListener(TreeModelListener l) {
		listenerList.remove(TreeModelListener.class, l);
	}

	/**
	 * Returns an array of all the tree model listeners
	 * registered on this model.
	 * 
	 * @return all of this model's <code>TreeModelListener</code>s
	 *         or an empty
	 *         array if no tree model listeners are currently registered
	 * @see #addTreeModelListener
	 * @see #removeTreeModelListener
	 * @since 1.4
	 */
	public TreeModelListener[] getTreeModelListeners() {
		return (TreeModelListener[]) listenerList.getListeners(
				TreeModelListener.class);
	}

	/**
	 * Notifies all listeners that have registered interest for
	 * notification on this event type.  The event instance
	 * is lazily created using the parameters passed into
	 * the fire method.
	 * 
	 * @param source       the node being changed
	 * @param path         the path to the root node
	 * @param childIndices the indices of the changed elements
	 * @param children     the changed elements
	 * @see javax.swing.event.EventListenerList
	 */
	public void fireTreeNodesChanged(Object source, Object[] path,
										int[] childIndices,
										Object[] children) {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		TreeModelEvent e = null;
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == TreeModelListener.class) {
				// Lazily create the event:
				if (e == null)
					e = new TreeModelEvent(source, path,
							childIndices, children);
				((TreeModelListener) listeners[i + 1]).treeNodesChanged(e);
			}
		}
	}

	/**
	 * Notifies all listeners that have registered interest for
	 * notification on this event type.  The event instance
	 * is lazily created using the parameters passed into
	 * the fire method.
	 * 
	 * @param source       the node where new elements are being inserted
	 * @param path         the path to the root node
	 * @param childIndices the indices of the new elements
	 * @param children     the new elements
	 * @see javax.swing.event.EventListenerList
	 */
	public void fireTreeNodesInserted(Object source, Object[] path,
										 int[] childIndices,
										 Object[] children) {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		TreeModelEvent e = null;
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == TreeModelListener.class) {
				// Lazily create the event:
				if (e == null)
					e = new TreeModelEvent(source, path,
							childIndices, children);
				((TreeModelListener) listeners[i + 1]).treeNodesInserted(e);
			}
		}
	}

	/**
	 * Notifies all listeners that have registered interest for
	 * notification on this event type.  The event instance
	 * is lazily created using the parameters passed into
	 * the fire method.
	 * 
	 * @param source       the node where elements are being removed
	 * @param path         the path to the root node
	 * @param childIndices the indices of the removed elements
	 * @param children     the removed elements
	 * @see javax.swing.event.EventListenerList
	 */
	public void fireTreeNodesRemoved(Object source, Object[] path,
										int[] childIndices,
										Object[] children) {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		TreeModelEvent e = null;
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == TreeModelListener.class) {
				// Lazily create the event:
				if (e == null)
					e = new TreeModelEvent(source, path,
							childIndices, children);
				((TreeModelListener) listeners[i + 1]).treeNodesRemoved(e);
			}
		}
	}

	/**
	 * Notifies all listeners that have registered interest for
	 * notification on this event type.  The event instance
	 * is lazily created using the parameters passed into
	 * the fire method.
	 * 
	 * @param source       the node where the tree model has changed
	 * @param path         the path to the root node
	 * @param childIndices the indices of the affected elements
	 * @param children     the affected elements
	 * @see javax.swing.event.EventListenerList
	 */
	public void fireTreeStructureChanged(Object source, Object[] path,
											int[] childIndices,
											Object[] children) {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		TreeModelEvent e = null;
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == TreeModelListener.class) {
				// Lazily create the event:
				if (e == null)
					e = new TreeModelEvent(source, path,
							childIndices, children);
				((TreeModelListener) listeners[i + 1]).treeStructureChanged(e);
			}
		}
	}

	/*
	 * Notifies all listeners that have registered interest for
	 * notification on this event type.  The event instance
	 * is lazily created using the parameters passed into
	 * the fire method.
	 *
	 * @param source the node where the tree model has changed
	 * @param path the path to the root node
	 * @see EventListenerList
	 */
	public void fireTreeStructureChanged(Object source, TreePath path) {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		TreeModelEvent e = null;
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == TreeModelListener.class) {
				// Lazily create the event:
				if (e == null)
					e = new TreeModelEvent(source, path);
				((TreeModelListener) listeners[i + 1]).treeStructureChanged(e);
			}
		}
	}
}
