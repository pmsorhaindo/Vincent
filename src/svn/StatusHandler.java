package svn;

/*
 * ====================================================================
 * Copyright (c) 2004-2008 TMate Software Ltd.  All rights reserved.
 *
 * This software is licensed as described in the file COPYING, which
 * you should have received as part of this distribution.  The terms
 * are also available at http://svnkit.com/license.html
 * If newer versions of this license are posted there, you may use a
 * newer version instead, at your option.
 * ====================================================================
 */

import org.tmatesoft.svn.core.SVNCancelException;
import org.tmatesoft.svn.core.SVNLock;
import org.tmatesoft.svn.core.wc.ISVNStatusHandler;
import org.tmatesoft.svn.core.wc.SVNStatus;
import org.tmatesoft.svn.core.wc.SVNStatusType;
import org.tmatesoft.svn.core.wc.SVNEvent;
import org.tmatesoft.svn.core.wc.ISVNEventHandler;
import org.tmatesoft.svn.core.wc.SVNEventAction;

/*
 * This is  an implementation of ISVNStatusHandler & ISVNEventHandler  that  is 
 * used in WorkingCopy.java to display status information. This  implementation  
 * is passed to 
 * 
 * SVNStatusClient.doStatus(File path, boolean recursive, boolean remote, 
 * boolean reportAll, boolean includeIgnored, boolean collectParentExternals, 
 * ISVNStatusHandler handler)
 * 
 * For each item to be processed doStatus(..) collects status  information  and 
 * creates an SVNStatus object which holds that information. Then  doStatus(..) 
 * calls an implementor's handler.handleStatus(SVNStatus) passing it the status 
 * info collected.
 * 
 * StatusHandler  will  be  also  provided  to  an  SVNStatusClient object as a 
 * handler of events generated by a doStatus(..) method. For  example,  if  the 
 * status is invoked with the flag remote=true (like 'svn status -u'  command), 
 * so then the status operation will be finished with dispatching  an  SVNEvent 
 * to ISVNEventHandler that will 'say' that the status is performed against the
 * youngest revision (the event holds that revision number). 
 */
public class StatusHandler implements ISVNStatusHandler, ISVNEventHandler {
    private boolean myIsRemote;
    public StatusHandler(boolean isRemote) {
        myIsRemote = isRemote;
    }
    /*
     * This is  an  implementation  of ISVNStatusHandler.handleStatus(SVNStatus 
     * status)
     */
    public void handleStatus(SVNStatus status) {
        /*
         * Gets  the  status  of  file/directory/symbolic link  text  contents. 
         * It is  SVNStatusType  who  contains  information on the state of  an 
         * item. 
         */
        SVNStatusType contentsStatus = status.getContentsStatus();

        String pathChangeType = " ";
        
        boolean isAddedWithHistory = status.isCopied();
        if (contentsStatus == SVNStatusType.STATUS_MODIFIED) {
            /*
             * The contents of the file have been Modified.
             */
            pathChangeType = "M";
        } else if (contentsStatus == SVNStatusType.STATUS_CONFLICTED) {
            /*
             * The  file  item  is  in a state of  Conflict. That  is,  changes 
             * received from the server during an  update  overlap  with  local 
             * changes the user has in his working copy. 
             */
            pathChangeType = "C";
        } else if (contentsStatus == SVNStatusType.STATUS_DELETED) {
            /*
             * The file, directory or symbolic link item has been scheduled for 
             * Deletion from the repository.
             */
            pathChangeType = "D";
        } else if (contentsStatus == SVNStatusType.STATUS_ADDED) {
            /*
             * The file, directory or symbolic link item has been scheduled for 
             * Addition to the repository.
             */
            pathChangeType = "A";
        } else if (contentsStatus == SVNStatusType.STATUS_UNVERSIONED) {
            /*
             * The file, directory or symbolic link item is not  under  version 
             * control.
             */
            pathChangeType = "?";
        } else if (contentsStatus == SVNStatusType.STATUS_EXTERNAL) {
            /*
             * The item is unversioned, but is used by an eXternals definition.
             */
            pathChangeType = "X";
        } else if (contentsStatus == SVNStatusType.STATUS_IGNORED) {
            /*
             * The file, directory or symbolic link item is not  under  version 
             * control, and is configured to be Ignored during 'add',  'import' 
             * and 'status' operations. 
             */
            pathChangeType = "I";
        } else if (contentsStatus == SVNStatusType.STATUS_MISSING
                || contentsStatus == SVNStatusType.STATUS_INCOMPLETE) {
            /*
             * The file, directory or  symbolic  link  item  is  under  version 
             * control but is missing or somehow incomplete. The  item  can  be 
             * missing if it is removed using a command incompatible  with  the 
             * native Subversion command line client (for example, just removed 
             * from the filesystem). In the case the item is  a  directory,  it 
             * can  be  incomplete if the user happened to interrupt a checkout 
             * or update.
             */
            pathChangeType = "!";
        } else if (contentsStatus == SVNStatusType.STATUS_OBSTRUCTED) {
            /*
             * The file, directory or symbolic link item is in  the  repository 
             * as one kind of object, but what's actually in the user's working 
             * copy is some other kind. For example, Subversion  might  have  a 
             * file in the repository,  but  the  user  removed  the  file  and 
             * created a directory in its place, without using the 'svn delete' 
             * or 'svn add' command (or SVNKit analogues for them).
             */
            pathChangeType = "~";
        } else if (contentsStatus == SVNStatusType.STATUS_REPLACED) {
            /*
             * The file, directory or symbolic link item was  Replaced  in  the 
             * user's working copy; that is, the item was deleted,  and  a  new 
             * item with the same name was added (within  a  single  revision). 
             * While they may have the same name, the repository considers them 
             * to be distinct objects with distinct histories.
             */
            pathChangeType = "R";
        } else if (contentsStatus == SVNStatusType.STATUS_NONE
                || contentsStatus == SVNStatusType.STATUS_NORMAL) {
            /*
             * The item was not modified (normal).
             */
            pathChangeType = " ";
        }
        
        /*
         * If SVNStatusClient.doStatus(..) was invoked with  remote = true  the 
         * following code finds out whether the current item had  been  changed 
         * in the repository   
         */
        String remoteChangeType = " ";

        if(status.getRemotePropertiesStatus() != SVNStatusType.STATUS_NONE || 
           status.getRemoteContentsStatus() != SVNStatusType.STATUS_NONE) {
            /*
             * the local item is out of date
             */
            remoteChangeType = "*";
        }
        /*
         * Now getting the status of properties of an item. SVNStatusType  also 
         * contains information on the properties state.
         */
        SVNStatusType propertiesStatus = status.getPropertiesStatus();
        /*
         * Default - properties are normal (unmodified).
         */
        String propertiesChangeType = " ";
        if (propertiesStatus == SVNStatusType.STATUS_MODIFIED) {
            /*
             * Properties were modified.
             */
            propertiesChangeType = "M";
        } else if (propertiesStatus == SVNStatusType.STATUS_CONFLICTED) {
            /*
             * Properties are in conflict with the repository.
             */
            propertiesChangeType = "C";
        }

        /*
         * Whether the item was locked in the .svn working area  (for  example, 
         * during a commit or maybe the previous operation was interrupted, in 
         * this case the lock needs to be cleaned up). 
         */
        boolean isLocked = status.isLocked();
        /*
         * Whether the item is switched to a different URL (branch).
         */
        boolean isSwitched = status.isSwitched();
        /*
         * If the item is a file it may be locked.
         */
        SVNLock localLock = status.getLocalLock();
        /*
         * If  doStatus()  was  run  with  remote=true  and the item is a file, 
         * checks whether a remote lock presents.
         */
        SVNLock remoteLock = status.getRemoteLock();
        String lockLabel = " ";

        if (localLock != null) {
            /*
             * at first suppose the file is locKed
             */
            lockLabel = "K";
            if (remoteLock != null) {
                /*
                 * if the lock-token of the local lock differs from  the  lock-
                 * token of the remote lock - the lock was sTolen!
                 */
                if (!remoteLock.getID().equals(localLock.getID())) {
                    lockLabel = "T";
                }
            } else {
                if(myIsRemote){
	                /*
	                 * the  local  lock presents but there's  no  lock  in  the
	                 * repository - the lock was Broken. This  is  true only if 
                     * doStatus() was invoked with remote=true.
	                 */
	                lockLabel = "B";
                }
            }
        } else if (remoteLock != null) {
            /*
             * the file is not locally locked but locked  in  the  repository -
             * the lock token is in some Other working copy.
             */
            lockLabel = "O";
        }

        /*
         * Obtains the working revision number of the item.
         */
        long workingRevision = status.getRevision().getNumber();
        /*
         * Obtains the number of the revision when the item was last changed. 
         */
        long lastChangedRevision = status.getCommittedRevision().getNumber();
        String offset = "                                ";
        String[] offsets = new String[3];
        offsets[0] = offset.substring(0, 6 - String.valueOf(workingRevision)
                .length());
        offsets[1] = offset.substring(0, 6 - String
                .valueOf(lastChangedRevision).length());
        //status
        offsets[2] = offset.substring(0,
                offset.length()
                        - (status.getAuthor() != null ? status.getAuthor()
                                .length() : 1));
        /*
         * status is shown in the manner of the native Subversion command  line
         * client's command "svn status"
         */
        System.out.println(pathChangeType
                + propertiesChangeType
                + (isLocked ? "L" : " ")
                + (isAddedWithHistory ? "+" : " ")
                + (isSwitched ? "S" : " ")
                + lockLabel
                + "  "
                + remoteChangeType
                + "  "
                + workingRevision
                + offsets[0]
                + (lastChangedRevision >= 0 ? String
                        .valueOf(lastChangedRevision) : "?") + offsets[1]
                + (status.getAuthor() != null ? status.getAuthor() : "?")
                + offsets[2] + status.getFile().getPath());
    }
    
    /*
     * This is an implementation for 
     * ISVNEventHandler.handleEvent(SVNEvent event, double progress)
     */
    public void handleEvent(SVNEvent event, double progress) {
        /*
         * Gets the current action. An action is represented by SVNEventAction.
         * In case of a status operation a current action can be determined via 
         * SVNEvent.getAction() and SVNEventAction.STATUS_-like constants. 
         */
        SVNEventAction action = event.getAction();
        /*
         * Print out the revision against which the status was performed.  This 
         * event is dispatched when the SVNStatusClient.doStatus() was  invoked 
         * with the flag remote set to true - that is for  a  local  status  it 
         * won't be dispatched.
         */
        if(action == SVNEventAction.STATUS_COMPLETED){
            System.out.println("Status against revision:  "+ event.getRevision());
        }
    
    }

    /*
     * Should be implemented to check if the current operation is cancelled. If 
     * it is, this method should throw an SVNCancelException. 
     */
    public void checkCancelled() throws SVNCancelException {
    
    }
    
}