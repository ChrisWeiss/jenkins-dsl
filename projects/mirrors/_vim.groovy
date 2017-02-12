freeStyleJob('mirror__vim') {
    displayName('mirror-.vim')
    description('Mirror github.com/jessfraz/.vim to g.j3ss.co/.vim.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/.vim')
        sidebarLinks {
            link('https://git.j3ss.co/.vim', 'git.j3ss.co/.vim', 'notepad.png')
        }
    }

    logRotator {
        numToKeep(2)
        daysToKeep(2)
    }

    scm {
        git {
            remote {
                url('git@github.com:jessfraz/.vim.git')
                name('origin')
                credentials('ssh-github-key')
                refspec('+refs/heads/master:refs/remotes/origin/master')
            }
            remote {
                url('ssh://git@g.j3ss.co:2200/~/.vim.git')
                name('mirror')
                credentials('ssh-github-key')
                refspec('+refs/heads/master:refs/remotes/upstream/master')
            }
            branches('master')
            extensions {
                ignoreNotifyCommit()
                disableRemotePoll()

                submoduleOptions {
                    recursive()
                }

                wipeOutWorkspace()
                cleanAfterCheckout()
            }
        }
    }

    triggers {
        cron('H H * * *')
    }

    wrappers { colorizeOutput() }

    publishers {
        postBuildScripts {
            git {
                branch('mirror', 'master')
            }
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
