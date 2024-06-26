/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.box.api;

import java.util.List;

import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxAPIException;
import com.box.sdk.BoxComment;
import com.box.sdk.BoxFile;
import org.apache.camel.RuntimeCamelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.camel.component.box.api.BoxHelper.buildBoxApiErrorMessage;

/**
 * Provides operations to manage Box comments.
 */
public class BoxCommentsManager {

    private static final Logger LOG = LoggerFactory.getLogger(BoxCommentsManager.class);

    /**
     * Box connection to authenticated user account.
     */
    private BoxAPIConnection boxConnection;

    /**
     * Create comments manager to manage the comments of Box connection's authenticated user.
     *
     * @param boxConnection - Box connection to authenticated user account.
     */
    public BoxCommentsManager(BoxAPIConnection boxConnection) {
        this.boxConnection = boxConnection;
    }

    /**
     * Add comment to file.
     *
     * @param  fileId  - the id of file.
     * @param  message - the comment's message.
     * @return         The commented file.
     */
    public BoxFile addFileComment(String fileId, String message) {
        try {
            LOG.debug("Adding comment to file(id={}) to '{}'", fileId, message);
            BoxHelper.notNull(fileId, BoxHelper.FILE_ID);
            BoxHelper.notNull(message, BoxHelper.MESSAGE);

            BoxFile fileToCommentOn = new BoxFile(boxConnection, fileId);
            fileToCommentOn.addComment(message);
            return fileToCommentOn;
        } catch (BoxAPIException e) {
            throw new RuntimeCamelException(
                    buildBoxApiErrorMessage(e), e);
        }
    }

    /**
     * Get a list of any comments on this file.
     *
     * @param  fileId - the id of file.
     * @return        The list of comments on this file.
     */
    public List<BoxComment.Info> getFileComments(String fileId) {
        try {
            LOG.debug("Getting comments of file(id={})", fileId);
            BoxHelper.notNull(fileId, BoxHelper.FILE_ID);

            BoxFile file = new BoxFile(boxConnection, fileId);

            return file.getComments();

        } catch (BoxAPIException e) {
            throw new RuntimeCamelException(
                    buildBoxApiErrorMessage(e), e);
        }
    }

    /**
     * Get comment information.
     *
     * @param  commentId - the id of comment.
     * @return           The comment information.
     */
    public BoxComment.Info getCommentInfo(String commentId) {
        try {
            LOG.debug("Getting info for comment(id={})", commentId);
            BoxHelper.notNull(commentId, BoxHelper.COMMENT_ID);

            BoxComment comment = new BoxComment(boxConnection, commentId);

            return comment.getInfo();
        } catch (BoxAPIException e) {
            throw new RuntimeCamelException(
                    buildBoxApiErrorMessage(e), e);
        }
    }

    /**
     * Reply to a comment.
     *
     * @param  commentId - the id of comment to reply to.
     * @param  message   - the message for the reply.
     * @return           The newly created reply comment.
     */
    public BoxComment replyToComment(String commentId, String message) {
        try {
            LOG.debug("Replying to comment(id={}) with message={}", commentId, message);
            BoxHelper.notNull(commentId, BoxHelper.COMMENT_ID);
            BoxHelper.notNull(message, BoxHelper.MESSAGE);

            BoxComment comment = new BoxComment(boxConnection, commentId);
            return comment.reply(message).getResource();
        } catch (BoxAPIException e) {
            throw new RuntimeCamelException(
                    buildBoxApiErrorMessage(e), e);
        }
    }

    /**
     * Change comment message.
     *
     * @param  commentId - the id of comment to change.
     * @param  message   - the new message for the comment.
     * @return           The comment with changed message.
     */
    public BoxComment changeCommentMessage(String commentId, String message) {
        try {
            LOG.debug("Changing comment(id={}) message={}", commentId, message);
            BoxHelper.notNull(commentId, BoxHelper.COMMENT_ID);
            BoxHelper.notNull(message, BoxHelper.MESSAGE);

            BoxComment comment = new BoxComment(boxConnection, commentId);
            return comment.changeMessage(message).getResource();
        } catch (BoxAPIException e) {
            throw new RuntimeCamelException(
                    buildBoxApiErrorMessage(e), e);
        }
    }

    /**
     * Delete comment.
     *
     * @param commentId - the id of comment to delete.
     */
    public void deleteComment(String commentId) {
        try {
            LOG.debug("Deleting comment(id={})", commentId);
            BoxHelper.notNull(commentId, BoxHelper.COMMENT_ID);
            BoxComment comment = new BoxComment(boxConnection, commentId);
            comment.delete();
        } catch (BoxAPIException e) {
            throw new RuntimeCamelException(
                    buildBoxApiErrorMessage(e), e);
        }
    }

}
