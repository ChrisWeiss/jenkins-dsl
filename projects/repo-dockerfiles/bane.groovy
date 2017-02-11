freeStyleJob('bane') {
    displayName('bane')
    description('Build Dockerfiles in jessfraz/bane.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/bane')
        sidebarLinks {
            link('https://hub.docker.com/r/jess/bane', 'Docker Hub: jess/bane', 'notepad.png')
        }
    }

    logRotator {
        numToKeep(2)
        daysToKeep(2)
    }

    scm {
        git {
            remote {
                url('https://github.com/jessfraz/bane.git')
            }
branches('*/master')
            extensions {
                wipeOutWorkspace()
                cleanAfterCheckout()
            }
        }
    }

    triggers {
        cron('H H * * *')
        githubPush()
    }

    wrappers { colorizeOutput() }

    environmentVariables(DOCKER_CONTENT_TRUST: '0')
    steps {
        shell('docker build --rm --force-rm -t r.j3ss.co/bane:latest .')
        shell('docker tag r.j3ss.co/bane:latest jess/bane:latest')
        shell('docker push --disable-content-trust=false r.j3ss.co/bane:latest')
        shell('docker push --disable-content-trust=false jess/bane:latest')
    }

    publishers {
    postBuildScripts {
            steps {
                shell('docker rm $(docker ps --filter status=exited -q 2>/dev/null) 2> /dev/null || true')
                shell('docker rmi $(docker images --filter dangling=true -q 2>/dev/null) 2> /dev/null || true')
            }
            onlyIfBuildSucceeds(false)
        }

        retryBuild {
            retryLimit(2)
            fixedDelay(15)
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
