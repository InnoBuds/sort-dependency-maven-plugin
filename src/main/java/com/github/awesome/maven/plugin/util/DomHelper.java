package com.github.awesome.maven.plugin.util;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.annotation.Nullable;

/**
 * Utility class for working with DOM elements.
 * This class provides static methods to work with DOM elements and nodes.
 * It is not meant to be instantiated.
 *
 * @author <a href="https://github.com/codeboyzhou">codeboyzhou</a>
 * @since 1.1.0
 */
public final class DomHelper {

    /**
     * Non-public constructor to prevent instantiation of the utility class.
     * This class should not be instantiated.
     * package-private access for testing.
     */
    DomHelper() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }

    /**
     * Finds the comment node that precedes the given element.
     * This method searches for the previous sibling of the element that is a comment node.
     * If no comment node is found, this method returns {@code null}.
     *
     * @param element The element for which to find the preceding comment node.
     * @return The comment node that precedes the element, or {@code null} if no comment node is found.
     */
    @Nullable
    public static Node findCommentNodeOf(Element element) {
        Node previousSibling = element.getPreviousSibling();
        while (previousSibling != null) {
            final short nodeType = previousSibling.getNodeType();
            if (nodeType == Node.COMMENT_NODE) {
                return previousSibling;
            }
            // It's not a direct comment node if meet another element in a half way
            if (nodeType == Node.ELEMENT_NODE) {
                return null;
            }
            previousSibling = previousSibling.getPreviousSibling();
        }
        return null;
    }

}
