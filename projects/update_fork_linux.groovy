freeStyleJob('update_fork_linux') {
    displayName('update-fork-linux')
    description('Rebase the master branch in jessfraz/linux fork.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/linux')
    }

    logRotator {
        numToKeep(100)
        daysToKeep(15)
    }

    scm {
        git {
            remote {
                url('git@github.com:jessfraz/linux.git')
                name('origin')
                credentials('ssh-github-key')
                refspec('+refs/heads/master:refs/remotes/origin/master')
            }
            remote {
                url('https://git.kernel.org/pub/scm/linux/kernel/git/torvalds/linux.git')
                name('upstream')
                refspec('+refs/heads/master:refs/remotes/upstream/master')
            }
            branches('master', 'upstream/master')
            extensions {
                wipeOutWorkspace()
                cleanAfterCheckout()
            }
            configure { node ->
                node / 'extensions' / 'hudson.plugins.git.extensions.impl.CloneOption' {
                    noTags 'true'
                    timeout '40'
                }
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
