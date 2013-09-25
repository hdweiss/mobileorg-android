package com.matburt.mobileorg.Git;

import android.content.Context;

public class DVCSSynchronizer {

    private final String localPath;
    private final String remotePath;
    private JGitWrapper dvcs;

    public DVCSSynchronizer(String localPath, String remotePath) throws Exception {
        this.localPath = localPath;
        this.remotePath = remotePath;
        this.dvcs = new JGitWrapper(localPath, remotePath);
    }

    public void run(Context context) {
        DVCSSynchronizeTask task = new DVCSSynchronizeTask(context, dvcs);
        task.execute();
    }

    public void sync() throws Exception {
        dvcs.commitAllChanges("Automatic commit");
        dvcs.updateChanges(remotePath);
    }

    class DVCSSynchronizeTask extends SafeAsyncTask<Void, Void, Void> {
        private final JGitWrapper dvcs;

        public DVCSSynchronizeTask(Context context, JGitWrapper dvcs) {
            super(context, ReportMode.Log);
            this.dvcs = dvcs;
        }

        @Override
        protected Void safeDoInBackground(Void... params) throws Exception {
            sync();
            return null;
        }
    }
}
