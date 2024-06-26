/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.index.tree.internal.nestedpages;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.index.tree.internal.AbstractEntityTreeNode;
import org.xwiki.tree.CompositeTreeNodeGroup;
import org.xwiki.tree.TreeNode;

/**
 * The wiki tree node.
 * 
 * @version $Id$
 * @since 8.3M2
 * @since 7.4.5
 */
@Component
@Named(WikiTreeNode.HINT)
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class WikiTreeNode extends AbstractEntityTreeNode implements Initializable
{
    /**
     * The component hint and also the tree node type.
     */
    public static final String HINT = "wiki";

    @Inject
    protected CompositeTreeNodeGroup childNodes;

    @Inject
    @Named("pinnedChildPages")
    private TreeNode pinnedChildPages;

    @Inject
    @Named("childDocuments")
    private TreeNode childDocuments;

    /**
     * Default constructor.
     */
    public WikiTreeNode()
    {
        super(HINT);
    }

    @Override
    public void initialize() throws InitializationException
    {
        this.childNodes.addTreeNode(this.pinnedChildPages, nodeId -> true);
        this.childNodes.addTreeNode(this.childDocuments, nodeId -> true);
    }

    @Override
    public int getChildCount(String nodeId)
    {
        return withSameProperties(this.childNodes).getChildCount(nodeId);
    }

    @Override
    public List<String> getChildren(String nodeId, int offset, int limit)
    {
        return withSameProperties(this.childNodes).getChildren(nodeId, offset, limit);
    }

    @Override
    public String getParent(String nodeId)
    {
        return FARM_NODE_ID;
    }
}
