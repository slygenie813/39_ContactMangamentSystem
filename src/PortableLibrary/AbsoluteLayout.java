/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package PortableLibrary;

/**
 *
 * @author nhlan
 */

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager2;
import java.util.Hashtable;

/**
 * Places each component at a fixed x, y position (with an optional fixed
 * size), as chosen in the NetBeans GUI designer.
 *
 * This small class lets NetBeans-designed windows that use AbsoluteLayout
 * compile and run without the separate AbsoluteLayout library.
 */
public class AbsoluteLayout implements LayoutManager2, java.io.Serializable {

    private static final long serialVersionUID = -1919857869177070440L;

    /** The constraints of every component added to the container. */
    protected Hashtable<Component, Object> constraints = new Hashtable<>();

    @Override
    public void addLayoutComponent(String name, Component comp) {
        throw new IllegalArgumentException("AbsoluteLayout needs AbsoluteConstraints");
    }

    @Override
    public void addLayoutComponent(Component comp, Object constr) {
        if (!(constr instanceof AbsoluteConstraints)) {
            throw new IllegalArgumentException("Wrong constraints: expected AbsoluteConstraints");
        }
        constraints.put(comp, constr);
    }

    @Override
    public void removeLayoutComponent(Component comp) {
        constraints.remove(comp);
    }

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        return sizeNeeded(parent, false);
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        return sizeNeeded(parent, true);
    }

    @Override
    public Dimension maximumLayoutSize(Container target) {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    @Override
    public float getLayoutAlignmentX(Container target) {
        return 0f;
    }

    @Override
    public float getLayoutAlignmentY(Container target) {
        return 0f;
    }

    @Override
    public void invalidateLayout(Container target) {
        // nothing is cached
    }

    @Override
    public void layoutContainer(Container parent) {
        for (Component comp : parent.getComponents()) {
            AbsoluteConstraints ac = (AbsoluteConstraints) constraints.get(comp);
            if (ac == null) {
                continue;
            }
            Dimension size = chosenSize(comp, ac, false);
            comp.setBounds(ac.x, ac.y, size.width, size.height);
        }
    }

    // The size a component gets: the fixed width/height if given (not -1),
    // otherwise its preferred (or minimum) size.
    private static Dimension chosenSize(Component comp, AbsoluteConstraints ac, boolean minimum) {
        int width = ac.width;
        int height = ac.height;
        if (width == -1 || height == -1) {
            Dimension natural = minimum ? comp.getMinimumSize() : comp.getPreferredSize();
            if (width == -1) {
                width = natural.width;
            }
            if (height == -1) {
                height = natural.height;
            }
        }
        return new Dimension(width, height);
    }

    // The smallest area that holds every component at its position.
    private Dimension sizeNeeded(Container parent, boolean minimum) {
        int maxWidth = 0;
        int maxHeight = 0;
        for (Component comp : parent.getComponents()) {
            AbsoluteConstraints ac = (AbsoluteConstraints) constraints.get(comp);
            if (ac == null) {
                continue;
            }
            Dimension size = chosenSize(comp, ac, minimum);
            maxWidth = Math.max(maxWidth, ac.x + size.width);
            maxHeight = Math.max(maxHeight, ac.y + size.height);
        }
        return new Dimension(maxWidth, maxHeight);
    }
}