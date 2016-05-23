package com.vanderfox

import grails.transaction.Transactional


class TwitterCredentialsService {

    def serviceMethod() {

    }

    @Transactional
    void markOthersInactive(TwitterCredentials twitterCredentials, User currentUser) {
        if (twitterCredentials.active) {
            // turn off active on all others
            def otherCreds = TwitterCredentials.findAllByUserAndActive(currentUser, true)
            if (otherCreds?.size() > 0) {
                otherCreds.each { cred ->
                    cred.active = false
                    cred.save(flush: true) //TODO batch these up for perf
                }
            }
        }
    }

    TwitterCredentials getCredentialsForUser(User user) {
        return TwitterCredentials.findByUser(user)
    }
}
