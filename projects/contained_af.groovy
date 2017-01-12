freeStyleJob('contained_af') {
    displayName('contained.af')
    description('Build Dockerfiles for contained.af.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/contained.af')
    }

    logRotator {
        numToKeep(2)
        daysToKeep(2)
    }

    scm {
        git {
            remote { url('https://github.com/jessfraz/contained.af.git') }
            branches('*/master')
            extensions {
                wipeOutWorkspace()
                cleanAfterCheckout()
            }
        }
    }

    triggers {
        cron('H H/4 * * *')
        githubPush()
    }

    wrappers { colorizeOutput() }

    steps {
        shell('docker build --rm --force-rm -t r.j3ss.co/contained:latest .')
        shell('docker tag r.j3ss.co/contained:latest jess/contained:latest')
        shell('docker push --disable-content-trust=false r.j3ss.co/contained:latest')
        shell('docker push --disable-content-trust=false jess/contained:latest')

        shell('docker build --rm --force-rm -f Dockerfile.dind -t r.j3ss.co/docker:userns .')
        shell('docker tag r.j3ss.co/docker:userns jess/docker:userns')
        shell('docker push --disable-content-trust=false r.j3ss.co/docker:userns')
        shell('docker push --disable-content-trust=false jess/docker:userns')
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
            retryLimit(3)
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
    }
}
