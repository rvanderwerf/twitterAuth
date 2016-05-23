import com.vanderfox.twittersearch.TwitterSearchSpeechlet

// Place your Spring DSL code here
beans = {
    twitterSearchSpeechlet(TwitterSearchSpeechlet) { bean ->
        bean.autowire = 'byName'
        //grailsApplication = ref('grailsApplication')
    }
}
