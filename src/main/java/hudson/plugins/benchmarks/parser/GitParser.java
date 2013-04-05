package hudson.plugins.benchmarks.parser;

import java.io.File;
import java.io.IOException;

import hudson.plugins.benchmarks.model.Change;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.util.FS;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Date;

public class GitParser {

    private final Repository repo;

    public GitParser(String path) throws IOException {

        File dir = new File(path);
        FS fs = FS.DETECTED;
        RepositoryCache.FileKey key = RepositoryCache.FileKey.lenient(dir, fs);

        repo = new RepositoryBuilder()
                .setFS(fs)
                .setGitDir(key.getFile())
                .setMustExist(true)
                .build();
    }

    public Change getChangeHead() throws IOException {

        RevWalk walk = new RevWalk(repo);
        AnyObjectId id = repo.readOrigHead();
        RevCommit commit = walk.parseCommit( id );

        return new Change(
                id.getName(),
                new Date(commit.getCommitTime()),
                commit.getCommitterIdent().getName(),
                commit.getCommitterIdent().getEmailAddress(),
                commit.getFullMessage()
        );
    }
}
