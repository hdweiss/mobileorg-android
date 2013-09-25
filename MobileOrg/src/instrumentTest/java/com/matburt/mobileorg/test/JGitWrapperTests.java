package com.matburt.mobileorg.test;

import android.test.AndroidTestCase;

import com.matburt.mobileorg.Git.JGitWrapper;
import com.matburt.mobileorg.test.util.TestUtils;

import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;

public class JGitWrapperTests extends AndroidTestCase {

    private final String localPath = "/sdcard/testrepo";

    private final String remotePathSSH = "git@github.com:hdweiss/test.git";
    private final String remotePath = "https://github.com/hdweiss/test.git";

    private final String testFile = "README.md";

    private JGitWrapper jGitWrapper;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        jGitWrapper = new JGitWrapper(localPath, remotePathSSH);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }


    public void testGitSetup() throws Exception {
        assertTrue(new File(localPath).exists());
        assertTrue(new File(localPath + "/.git").exists());
        assertTrue(jGitWrapper.getGit().branchList().call().size() > 0);
    }

    public void testCommitAndPush() throws GitAPIException {
        String orgContents = TestUtils.readFileAsString(localPath + "/" + testFile);
        TestUtils.writeStringAsFile(orgContents + "\nmoretest", localPath + "/" + testFile);
        jGitWrapper.commitAllChanges("Automatic commit");
        jGitWrapper.updateChanges();
    }
}
