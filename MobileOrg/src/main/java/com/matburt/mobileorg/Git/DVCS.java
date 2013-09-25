package com.matburt.mobileorg.Git;

import org.eclipse.jgit.api.errors.GitAPIException;

public interface DVCS {
    public void createNewRepo(String remotePath) throws Exception;
    public void commitAllChanges(String commitMessage) throws GitAPIException;
    public void updateChanges(String remotePath) throws GitAPIException;
}
