/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.netbeans.lib.awtextra;

/**
 *
 * @author nhlan
 */

import java.awt.Dimension;
import java.awt.Point;

/**
 * Position and size of one component in an AbsoluteLayout.
 * A width or height of -1 means "use the component's preferred size".
 *
 * This small class lets NetBeans-designed windows that use AbsoluteLayout
 * compile and run without the separate AbsoluteLayout library.
 */
public class AbsoluteConstraints implements java.io.Serializable {

    private static final long serialVersionUID = 5261460716622152494L;

    /** The X position of the component. */
    public int x;
    /** The Y position of the component. */
    public int y;
    /** The width of the component, or -1 for its preferred width. */
    public int width = -1;
    /** The height of the component, or -1 for its preferred height. */
    public int height = -1;

    public AbsoluteConstraints(Point pos) {
        this(pos.x, pos.y);
    }

    public AbsoluteConstraints(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public AbsoluteConstraints(Point pos, Dimension size) {
        this.x = pos.x;
        this.y = pos.y;
        if (size != null) {
            this.width = size.width;
            this.height = size.height;
        }
    }

    public AbsoluteConstraints(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Point getPosition() {
        return new Point(x, y);
    }

    public Dimension getSize() {
        return new Dimension(width, height);
    }

    @Override
    public String toString() {
        return super.toString() + " [x=" + x + ", y=" + y
                + ", width=" + width + ", height=" + height + "]";
    }
}