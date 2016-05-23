package com.vanderfox.twittersearch

import com.amazon.speech.slu.Intent
import com.amazon.speech.slu.Slot
import com.amazon.speech.speechlet.IntentRequest
import com.amazon.speech.speechlet.LaunchRequest
import com.amazon.speech.speechlet.Session
import com.amazon.speech.speechlet.SessionEndedRequest
import com.amazon.speech.speechlet.SessionStartedRequest
import com.amazon.speech.speechlet.Speechlet
import com.amazon.speech.speechlet.SpeechletException
import com.amazon.speech.speechlet.SpeechletResponse
import com.amazon.speech.ui.LinkAccountCard
import com.amazon.speech.ui.PlainTextOutputSpeech
import com.amazon.speech.ui.Reprompt
import com.amazon.speech.ui.SimpleCard
import com.twitter.Extractor
import com.vanderfox.TwitterCredentials
import com.vanderfox.User
import grails.config.Config
import grails.core.support.GrailsConfigurationAware
import groovy.util.logging.Slf4j
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.social.twitter.api.SearchResults
import org.springframework.social.twitter.api.Tweet
import org.springframework.social.twitter.api.impl.TwitterTemplate

/**
 * Created by rvanderwerf on 4/18/16.
 */
@Slf4j
class TwitterSearchSpeechlet implements Speechlet, GrailsConfigurationAware {

        //private  static final Logger log = LoggerFactory.getLogger(TwitterSearchSpeechlet.class);

        static String CONSUMER_KEY = ""
        static String CONSUMER_SECRET = ""
        static String ACCESS_TOKEN = ""
        static String ACCESS_TOKEN_SECRET = ""
        static TwitterTemplate twitter

        def speechletService
        def twitterCredentialsService
        Config grailsConfig


    void setConfiguration(Config co) {
        grailsConfig = co
    }

    @Override
        public void onSessionStarted(final SessionStartedRequest request, final Session session)
                throws SpeechletException {
            log.info("onSessionStarted requestId={}, sessionId={}", request.getRequestId(),
                    session.getSessionId());
            // any initialization logic goes here
        }

        @Override
        public SpeechletResponse onLaunch(final LaunchRequest request, final Session session)
                throws SpeechletException {
            log.info("onLaunch requestId={}, sessionId={}", request.getRequestId(),
                    session.getSessionId());


            loadCredentials(session)
            return getWelcomeResponse();
        }

        @Override
        public SpeechletResponse onIntent(final IntentRequest request, final Session session)
                throws SpeechletException {
            log.info("onIntent requestId={}, sessionId={}", request.getRequestId(),
                    session.getSessionId());

            Intent intent = request.getIntent();


            String intentName = (intent != null) ? intent.getName() : null;
            Slot query = intent.getSlot("SearchTerm")
            Slot count = intent.getSlot("Count")

            log.debug("invoking intent:${intentName}")
            switch (intentName) {
                case "TwitterSearchIntent":
                    getTwitterSearchResponse(query, count,session)
                    break

                case "TwitterTimelineIntent":
                    getTwitterTimelineResponse(query, count,session)
                    break

                case "TwitterMentionIntent":
                    getTwitterMentionResponse(query, count,session)
                    break

                case "TwitterMyLatestsPostsIntent":
                    getTwitterMyLatestPostsResponse(query, count,session)
                    break

                case "AMAZON.HelpIntent":
                    getHelpResponse()
                    break
                case "GroovyVersionIntent":
                    sayGroovyVersion()
                    break
                case "TwitterAccountLink":
                    createLinkCard(session)
                    break
                default:
                    getTwitterMyLatestPostsResponse(query, count,session)
                    break


            }

        }

        @Override
        public void onSessionEnded(final SessionEndedRequest request, final Session session)
                throws SpeechletException {
            log.info("onSessionEnded requestId={}, sessionId={}", request.getRequestId(),
                    session.getSessionId());
            // any cleanup logic goes here
        }

        /**
         * Creates and returns a {@code SpeechletResponse} with a welcome message.
         *
         * @return SpeechletResponse spoken and visual response for the given intent
         */
        private SpeechletResponse getWelcomeResponse() {
            String speechText = "Say search and a keyword and I'll search Twitter for relevant tweets.";

            // Create the Simple card content.
            SimpleCard card = new SimpleCard();
            card.setTitle("TwitterSearch");
            card.setContent(speechText);

            // Create the plain text output.
            PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
            speech.setText(speechText);

            // Create reprompt
            Reprompt reprompt = new Reprompt();
            reprompt.setOutputSpeech(speech);

            return SpeechletResponse.newAskResponse(speech, reprompt, card);
        }

        private SpeechletResponse createLinkCard(Session session) {
            String speechText = "Please use the alexa app to link account."

            // Create the Simple card content.
            LinkAccountCard card = new LinkAccountCard();
            //card.setTitle("LinkAccount");


            // Create the plain text output.
            PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
            speech.setText(speechText);
            log.debug("Session ID=${session.sessionId}")
            // Create reprompt
            Reprompt reprompt = new Reprompt();
            reprompt.setOutputSpeech(speech);
            //session.finalize()

            return SpeechletResponse.newTellResponse(speech, card);

        }
        /**
         * Creates a {@code SpeechletResponse} for the hello intent.
         *
         * @return SpeechletResponse spoken and visual response for the given intent
         */
        private SpeechletResponse getTwitterMyLatestPostsResponse(Slot query, Slot count, Session session) {

            loadCredentials(session)

            // Create the Simple card content.
            SimpleCard card = new SimpleCard();
            card.setTitle("Twitter Search Results");

            String searchTerm = query.getValue()
            String countString = count.getValue()
            int countInt = 1;
            if (countString && !"".equals(countString) && countString.isNumber()) {
                countInt = countString.toInteger();
            }
            def speechText = "Your latest tweets are:\n"
            String cardText = "Your latest tweets are:\n"
            try {
                List<Tweet> tweets = twitter.timelineOperations().getUserTimeline(countInt);
                tweets.eachWithIndex { tweet, index ->
                    if (index == 0) {
                        speechText += "First. Tweet:\n"
                        cardText += "First. Tweet:\n"
                    } else {
                        speechText += "Next. Tweet:\n"
                        cardText += "Next. Tweet:\n"
                    }
                    def tweetText = tweet.getText()
                    tweetText = cleanupVerbalText(tweetText)

                    speechText += "@${tweet.getFromUser()} said ${tweetText}\n"
                    cardText += "@${tweet.getFromUser()} said ${tweet.getText()}\n"

                }
            } catch (Exception e) {
                speechText = "Sorry.  I had some problems getting information from Twitter.  The exception message was ${e.getMessage()}"
            }

            // Create the plain text output.
            PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
            speech.setText(speechText);
            card.setContent(cardText);

            return SpeechletResponse.newTellResponse(speech, card);
        }

        private void loadCredentials(Session session) {

            if (session?.user?.accessToken) {
                log.debug("Looking up user for access token ${session.user.accessToken}")
                User twitterUser = speechletService.getUserForAccessToken(session.user.accessToken)
                if (twitterUser) {
                    def credentials = TwitterCredentials.findByUserAndActive(twitterUser,true)
                    log.debug("Looking up credentials for user u:${twitterUser.username} id:${twitterUser.id} and for access token ${session.user.accessToken}")
                    if (credentials) {
                        log.debug("Found credentials for user ${twitterUser.id} accessToken:${credentials.accessToken} accessTokenSecuret:${credentials.accessTokenSecret} consumerKey:${credentials.consumerKey} consumerKeySecret:${credentials.consumerSecret}")
                        ACCESS_TOKEN = credentials.accessToken
                        ACCESS_TOKEN_SECRET = credentials.accessTokenSecret
                        CONSUMER_KEY = credentials.consumerKey
                        CONSUMER_SECRET = credentials.consumerSecret
                        twitter = new TwitterTemplate(CONSUMER_KEY, CONSUMER_SECRET, ACCESS_TOKEN, ACCESS_TOKEN_SECRET);
                    } else {
                        log.error("Unable to find twitter credentials for access token ${session.user.accessToken}")
                    }
                } else {
                    log.error("Unable to find user for access token ${session.user.accessToken}")
                }

            }
            if (!ACCESS_TOKEN || !ACCESS_TOKEN_SECRET || !CONSUMER_KEY || !CONSUMER_SECRET) {
                // we can try defaults of some canned account to demo on file
                log.warn("Unable to load user credentials for user, or no user token given. Loading defaults from grails properties")
                final Properties properties = new Properties();
                try {
                    //InputStream stream = TwitterSearchSpeechlet.class.getClassLoader() getResourceAsStream("springSocial.properties")
                    //properties.load(stream);

                    ACCESS_TOKEN = grailsConfig.getProperty("twitterSpeechlet.accessToken")
                    ACCESS_TOKEN_SECRET = grailsConfig.getProperty("twitterSpeechlet.accessTokenSecret")
                    CONSUMER_KEY = grailsConfig.getProperty("twitterSpeechlet.consumerKey")
                    CONSUMER_SECRET = grailsConfig.getProperty("twitterSpeechlet.consumerSecret")
                    twitter = new TwitterTemplate(CONSUMER_KEY, CONSUMER_SECRET, ACCESS_TOKEN, ACCESS_TOKEN_SECRET);
                } catch (e) {
                    log.error("Unable to retrieve twitter credentials. Please set up a twitterSpeechlet{} in grails config")
                }
            }
        }


        /**
         * Creates a {@code SpeechletResponse} for the hello intent.
         *
         * @return SpeechletResponse spoken and visual response for the given intent
         */
        private SpeechletResponse getTwitterTimelineResponse(Slot query, Slot count, Session session) {
            loadCredentials(session)

            PlainTextOutputSpeech speech = new PlainTextOutputSpeech();

            // Create the Simple card content.
            SimpleCard card = new SimpleCard();
            card.setTitle("Twitter Search Results");

            String searchTerm = query.getValue()
            String countString = count.getValue()
            int countInt = 1;
            if(countString && !"".equals(countString) && countString.isNumber()) {
                countInt = countString.toInteger();
            }
            def speechText = "Your timeline is:\n"
            String cardText = "Your timeline is:\n"
            try {
                List<Tweet> tweets = twitter.timelineOperations().getHomeTimeline();
                tweets[1..countInt].eachWithIndex { tweet, index ->
                    if(index == 0) {
                        speechText += "First. Tweet:\n"
                        cardText += "First. Tweet:\n"
                    } else {
                        speechText += "Next. Tweet:\n"
                        cardText += "Next. Tweet:\n"
                    }
                    def tweetText = tweet.getText()
                    tweetText = cleanupVerbalText(tweetText)
                    speechText += "@${tweet.getFromUser()} said ${tweetText}\n"
                    cardText += "@${tweet.getFromUser()} said ${tweet.getText()}\n"

                }
            } catch (Exception e) {
                speechText = "Sorry.  I had some problems getting information from Twitter."
            }

            // Create the plain text output.
            speech.setText(speechText);
            card.setContent(cardText);

            return SpeechletResponse.newTellResponse(speech, card);
        }


        /**
         * Creates a {@code SpeechletResponse} for the hello intent.
         *
         * @return SpeechletResponse spoken and visual response for the given intent
         */
        private SpeechletResponse getTwitterMentionResponse(Slot query, Slot count, Session session) {
            loadCredentials(session)
            PlainTextOutputSpeech speech = new PlainTextOutputSpeech()

            // Create the Simple card content.
            SimpleCard card = new SimpleCard();
            card.setTitle("Twitter Search Results")

            String searchTerm = query.getValue()
            String countString = count.getValue()
            int countInt = 1;
            if(countString && !"".equals(countString) && countString.isNumber()) {
                countInt = countString.toInteger();
            }
            def speechText = "Your latest mentions are:\n"
            String cardText = "Your latest mentions are:\n"
            try {
                List<Tweet> tweets = twitter.timelineOperations().getMentions(countInt);
                tweets.eachWithIndex { tweet, index ->
                    if(index == 0) {
                        speechText += "First. Tweet:\n"
                        cardText += "First. Tweet:\n"
                    } else {
                        speechText += "Next. Tweet:\n"
                        cardText += "Next. Tweet:\n"
                    }
                    String tweetText = tweet.getText()
                    tweetText = cleanupVerbalText(tweetText)

                    speechText += "@${tweet.getFromUser()} said ${tweetText}\n"
                    cardText += "@${tweet.getFromUser()} said ${tweet.getText()}\n"

                }
            } catch (Exception e) {
                speechText = "Sorry.  I had some problems getting information from Twitter."
            }

            // Create the plain text output.
            speech.setText(speechText);
            card.setContent(cardText);

            return SpeechletResponse.newTellResponse(speech, card);

        }


        /**
         * Creates a {@code SpeechletResponse} for the hello intent.
         *
         * @return SpeechletResponse spoken and visual response for the given intent
         */
        private SpeechletResponse getTwitterSearchResponse(Slot query, Slot count, Session session) {
            loadCredentials(session)
            PlainTextOutputSpeech speech = new PlainTextOutputSpeech();

            // Create the Simple card content.
            SimpleCard card = new SimpleCard();
            card.setTitle("Twitter Search Results");

            String searchTerm = query.getValue()
            String countString = count.getValue()
            int countInt = 1;
            if(countString && !"".equals(countString) && countString.isNumber()) {
                countInt = countString.toInteger();
            }
            def speechText = "I found ${countInt} tweet${(countInt > 1) ? 's' : ''} for ${searchTerm}:\n"
            String cardText = "I found ${countInt} tweet${(countInt > 1) ? 's' : ''} for ${searchTerm}:\n".toString()
            try {
                if(!searchTerm) {
                    speechText = "Please tell me something to search for by saying search and a keyword"
                    cardText = speechText
                    Reprompt reprompt = new Reprompt();
                    reprompt.setOutputSpeech(speech);
                    card.setContent(cardText);
                    return SpeechletResponse.newAskResponse(speech, reprompt, card);
                } else {
                    log.info("searching twitter for:${searchTerm}")
                    log.debug("Found credentials for session ${session.sessionId} accessToken:${ACCESS_TOKEN} accessTokenSecuret:${ACCESS_TOKEN_SECRET} consumerKey:${CONSUMER_KEY} consumerKeySecret:${CONSUMER_SECRET}")

                    SearchResults results = twitter.searchOperations().search(searchTerm)
                    log.info("finished searching twitter")
                    List<Tweet> tweets = results.getTweets()
                    tweets[1..countInt].eachWithIndex { tweet, index ->

                        if(index == 0) {
                            speechText += "First. Tweet:\n"
                            cardText += "First. Tweet:\n"
                        } else {
                            speechText += "Next. Tweet:\n"
                            cardText += "Next. Tweet:\n"
                        }
                        String tweetText = tweet.getText()

                        tweetText = cleanupVerbalText(tweetText)

                        speechText += "@${tweet.getFromUser()} said ${tweetText}\n"
                        cardText += "@${tweet.getFromUser()} said ${tweet.getText()}\n"

                    }
                }
            } catch (Exception e) {
                speechText = "Sorry.  I had some problems getting information from Twitter."
            }

            // Create the plain text output.
            speech.setText(speechText)
            card.setContent(cardText)

            return SpeechletResponse.newTellResponse(speech, card)
        }

        private String cleanupVerbalText(String tweetText) {
            Extractor extractor = new Extractor()
            List<String> urls = extractor.extractURLs(tweetText)
            urls.each { url ->
                tweetText = tweetText.replaceAll(url, "")
            }
            List<String> hashTags = extractor.extractHashtags(tweetText)
            hashTags.each { tag ->
                tweetText = tweetText.replaceAll(tag, "")
            }
            List<String> mentions = extractor.extractMentionedScreennames(tweetText)
            mentions.each { mention ->
                tweetText = tweetText.replaceAll(mention, "")
            }
            List<String> cashTags = extractor.extractCashtags(tweetText)
            cashTags.each { tag ->
                tweetText = tweetText.replaceAll(tag, "")
            }
            return tweetText
        }

        /**
         * Creates a {@code SpeechletResponse} for the help intent.
         *
         * @return SpeechletResponse spoken and visual response for the given intent
         */
        private SpeechletResponse getHelpResponse() {
            String speechText = "You can say search and a keyword, and I will search Twitter for you."

            // Create the Simple card content.
            SimpleCard card = new SimpleCard()
            card.setTitle("TwitterSearch")
            card.setContent(speechText)

            // Create the plain text output.
            PlainTextOutputSpeech speech = new PlainTextOutputSpeech()
            speech.setText(speechText)

            // Create reprompt
            Reprompt reprompt = new Reprompt()
            reprompt.setOutputSpeech(speech)

            return SpeechletResponse.newAskResponse(speech, reprompt, card)
        }

        private SpeechletResponse sayGroovyVersion() {
            def speechText = "This skill is running Groovy Version ${GroovySystem.version}"

            // Create the Simple card content.
            SimpleCard card = new SimpleCard()
            card.setTitle("TwitterSearch")
            card.setContent(speechText)

            // Create the plain text output.
            PlainTextOutputSpeech speech = new PlainTextOutputSpeech()
            speech.setText(speechText.toString())


            return SpeechletResponse.newTellResponse(speech, card)
        }


        static void main(String[] args) {
            TwitterSearchSpeechlet speechlet = new TwitterSearchSpeechlet()
            speechlet.loadCredentials(null)
            List<Tweet> tweets = twitter.timelineOperations().getHomeTimeline()

            def tweet = tweets.get(0)
            def tweetText = tweet.getText()
            tweetText = speechlet.cleanupVerbalText(tweetText)
            String speechText = "From ${tweet.getFromUser()} ${tweetText}"
            println("Speech Text ${speechText}")
        }
}
