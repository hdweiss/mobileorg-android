package com.matburt.mobileorg.Git;

import android.util.Log;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.matburt.mobileorg.util.OrgUtils;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.util.FS;

import java.io.File;
import java.util.Iterator;

public class JGitWrapper {

    private final String localPath;
    private final String remotePath;

    private final Git git;

    public JGitWrapper(String localPath, String remotePath) throws Exception {
        this.localPath = localPath;
        this.remotePath = remotePath;

        setupJGitAuthentication();
        this.git = initGitRepo();
    }

    private void setupJGitAuthentication() {
        String username = "hdweiss";
        String keyLocation = "/sdcard/ssh-keys/github_rsa";
        String knownHostsLocation = "/sdcard/ssh-keys/known_hosts";
        JGitConfigSessionFactory session = new JGitConfigSessionFactory(username, keyLocation, knownHostsLocation);
        SshSessionFactory.setInstance(session);
    }

    private Git initGitRepo() throws Exception {
        if (new File(localPath).exists() == false)
            createNewRepo();

        FileRepository fileRepository = new FileRepository(localPath + "/.git");
        return new Git(fileRepository);
    }

    private void createNewRepo() throws Exception {
        File localRepo = new File(localPath);
        if (localRepo.exists()) // Safety check so we don't accidentally delete directory
            throw new Exception("Directory already exists");

        try {
            Git.cloneRepository()
                    .setURI(remotePath)
                    .setDirectory(localRepo)
                    .setBare(false)
                    .call();
        } catch (GitAPIException e) {
            OrgUtils.deleteDirectory(localRepo);
            throw e;
        }
    }


    public Git getGit() {
        return this.git;
    }

    public void commitAllChanges(String commitMessage) throws GitAPIException {
        git.add().addFilepattern(".").call();
        git.commit().setMessage(commitMessage).setAuthor("Henning Weiss", "hdweiss@gmail.com").call();
    }

    public void updateChanges() throws Exception {
        git.fetch().call();

        // TODO Check for local changes before pushing

        Log.d("JGitWrapper", "Got sync state: " + getSyncState().name());

        switch(getSyncState()) {
            case Equal:
                // Do nothing
                break;

            case Ahead:
                git.push().setRemote(remotePath).call();
                break;

            case Behind:
                MergeResult result = git.merge().setFastForward(MergeCommand.FastForwardMode.FF_ONLY).call(); // TODO Set remote refs
                if (result.getMergeStatus().isSuccessful() == false) {
                    throw new Exception("Merge failed");
                } // TODO Handle failure to merge
                break;

            case Diverged:
                git.rebase().setUpstream("remotes/origin/master").call();
                if (getSyncState() == SyncState.Ahead)
                    git.push().setRemote(remotePath).call();
                else {
                    throw new Exception("Rebase failed");
                } // TODO Handle unable to rebase
                break;
        }
    }

    private enum SyncState {
        Equal, Ahead, Behind, Diverged
    }

    private SyncState getSyncState() throws Exception {
        Ref origin = git.getRepository().getRef("remotes/origin/master");
        Ref head = git.getRepository().getRef("HEAD");

        if (origin == null)
            throw new Exception("origin not found!");

        if (head == null)
            throw new Exception("head not found!");

        Iterable<RevCommit> call = git.log().addRange(origin.getObjectId(), head.getObjectId()).call();
        int originToHead = countIterator(call.iterator());

        Iterable<RevCommit> call2 = git.log().addRange(head.getObjectId(), origin.getObjectId()).call();
        int headToOrigin = countIterator(call2.iterator());

        Log.d("JGitWrapper", "origin->head: " + originToHead + " head->origin: " + headToOrigin);

        if (originToHead == 0 && headToOrigin == 0)
            return SyncState.Equal;
        else if (originToHead == 0)
            return SyncState.Behind;
        else if (headToOrigin == 0)
            return SyncState.Ahead;
        else
            return SyncState.Diverged;
    }

    public static int countIterator(Iterator<?> iterator) {
        int i = 0;
        while(iterator.hasNext()) {
            iterator.next();
            i++;
        }

        return i;
    }

    public static class JGitConfigSessionFactory extends JschConfigSessionFactory {
        private final String username;
        private final String keyLocation;
        private final String knowHostsLocation;

        public JGitConfigSessionFactory(String username, String keyLocation, String knowHostsLocation) {
            super();
            this.username = username;
            this.keyLocation = keyLocation;
            this.knowHostsLocation = knowHostsLocation;
        }

        @Override
        protected void configure(OpenSshConfig.Host host, Session session) {
//            session.setConfig("StrictHostKeyChecking", "no");
//            session.setConfig("HostName", "github.com");
            session.setConfig("User", username);

            try {
                JSch jSch = getJSch(host, FS.DETECTED);
                jSch.addIdentity(keyLocation);
                jSch.setKnownHosts(knowHostsLocation);
            } catch (JSchException e) {
                e.printStackTrace();
            }
        }
    }
}
