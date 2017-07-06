freeStyleJob('update_fork_runc') {
    displayName('update-fork-runc')
    description('Rebase the primary branch (master) in jessfraz/runc fork.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/runc')
        sidebarLinks {
            link('https://github.com/opencontainers/runc', 'UPSTREAM: opencontainers/runc', 'notepad.png')
        }
    }

    logRotator {
        numToKeep(100)
        daysToKeep(15)
    }

    scm {
        git {
            remote {
                url('git@github.com:jessfraz/runc.git')
                name('origin')
                credentials('ssh-github-key')
                refspec('+refs/heads/master:refs/remotes/origin/master')
            }
            remote {
                url('https://github.com/opencontainers/runc.git')
                name('upstream')
                refspec('+refs/heads/master:refs/remotes/upstream/master')
            }
            branches('master', 'upstream/master')
            extensions {
                disableRemotePoll()
                wipeOutWorkspace()
                cleanAfterCheckout()
            }
        }
    }

    triggers {
        cron('H H * * *')
    }

    wrappers { colorizeOutput() }

    steps {
        shell('git rebase upstream/master')
    }

    publishers {
        git {
            branch('origin', 'master')
            pushOnlyIfSuccess()
        }

        extendedEmail {
            recipientList('$DEFAULT_RECIPIENTS')
            contentType('text/plain')
            triggers {
                stillFailing {
                    attachBuildLog(true)
                }
            }
        }

        wsCleanup()
    }
}
