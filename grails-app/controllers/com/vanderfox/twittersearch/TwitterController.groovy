package com.vanderfox.twittersearch

class TwitterController {

    def speechletService
    def springSecurityService
    def twitterSearchSpeechlet

    // call parent class speechlet for twitter stuff

    def index() {
        speechletService.doSpeechlet(request,response, twitterSearchSpeechlet)
    }
}
