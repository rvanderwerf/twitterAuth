package com.vanderfox

import grails.plugin.springsecurity.annotation.Secured

import static org.springframework.http.HttpStatus.*
import grails.transaction.Transactional

@Transactional(readOnly = true)
class TwitterCredentialsController {

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]
    def springSecurityService
    def twitterCredentialsService

    @Secured(['ROLE_USER'])
    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        User currentUser = springSecurityService.currentUser
        params.put("user",currentUser)
        def twitterCriteria = TwitterCredentials.createCriteria()
        respond twitterCriteria.list(params) { eq ('user',currentUser)}, model:[twitterCredentialsCount: TwitterCredentials.count()]
    }

    @Secured(['ROLE_USER'])
    def show(TwitterCredentials twitterCredentials) {
        User currentUser = springSecurityService.currentUser
        if (twitterCredentials.user != currentUser) {
            render status: UNAUTHORIZED
        } else {
            respond twitterCredentials
        }
    }

    @Secured(['ROLE_USER'])
    def create() {
        respond new TwitterCredentials(params)
    }

    @Transactional
    @Secured(['ROLE_USER'])
    def save(TwitterCredentials twitterCredentials) {
        if (twitterCredentials == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }

        User currentUser = springSecurityService.currentUser
        twitterCredentials.user = currentUser
        twitterCredentialsService.markOthersInactive(twitterCredentials, currentUser)
        twitterCredentials.active = true
        twitterCredentials.save flush:true
        if (twitterCredentials.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond twitterCredentials.errors, view:'create'
            return
        }

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'twitterCredentials.label', default: 'TwitterCredentials'), twitterCredentials.id])
                redirect twitterCredentials
            }
            '*' { respond twitterCredentials, [status: CREATED] }
        }
    }



    @Secured(['ROLE_USER'])
    def edit(TwitterCredentials twitterCredentials) {
        respond twitterCredentials
    }

    @Transactional
    @Secured(['ROLE_USER'])
    def update(TwitterCredentials twitterCredentials) {
        if (twitterCredentials == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }

        if (twitterCredentials.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond twitterCredentials.errors, view:'edit'
            return
        }
        User currentUser = springSecurityService.currentUser
        twitterCredentialsService.markOthersInactive(twitterCredentials, currentUser)
        twitterCredentials.active = true
        if (twitterCredentials.user == currentUser) {
            twitterCredentials.save flush: true
        }

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'twitterCredentials.label', default: 'TwitterCredentials'), twitterCredentials.id])
                redirect twitterCredentials
            }
            '*'{ respond twitterCredentials, [status: OK] }
        }
    }

    @Transactional
    @Secured(['ROLE_ADMIN'])
    def delete(TwitterCredentials twitterCredentials) {

        if (twitterCredentials == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }

        twitterCredentials.delete flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.deleted.message', args: [message(code: 'twitterCredentials.label', default: 'TwitterCredentials'), twitterCredentials.id])
                redirect action:"index", method:"GET"
            }
            '*'{ render status: NO_CONTENT }
        }
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'twitterCredentials.label', default: 'TwitterCredentials'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
}
