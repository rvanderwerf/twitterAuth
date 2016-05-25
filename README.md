# twitterAuth
This is a Grails 3.0.x app that demonstrates all the functionality of Speechlets/Skills, OAuth2 Account Linking, and Spring Social integration with Twitter
To get this working you'll need to do the following steps:

## Authors
   Lee Fox @foxinatx
   Ryan Vanderwerf @RyanVanderwerf

## Using
### Quick start
To start using this app you'll need to set up a few things:

##TWITTER
- Create a twitter account
- Login to twitter apps management (https://apps.twitter.com/) and make yourself a developer
- Click on 'Create New App' -> Fill in app name, website, description leave callback url blank
- Go to consumer key -> manage keys and tokens
- Create consumer key and secret if you don't have one


##IDEA / EDITOR
- Fill in these details in file: application.groovy for fallback (if the user doesn't have an account).




##AMAZON Alexa skill / Grails tie together
- Sign up for the Amazon developer program [here](https://developer.amazon.com) if you haven't already
- Click on Apps and Services -> Alexa
- Click on Alexa Skill Kit / Get Started -> Add New Skill
- Pick any name and any invocation name you want to start the app on your Echo / Alexa Device


##IDEA / EDITOR -> FILES COPIED TO AMAZON ALEXA SKILL IN AWS
- Copy the contents of src/main/resources/IntentSchema.json into Intent Schema.
- Don't fill in anything for slots
- Under Sample Utterances, copy the contents of the file src/main/resources/SampleUtterances.txt
- Under configuration Copy the url for /twitterAuth/twitter/index for the endpoint for your server (Choose amazon https not ARN). Click next
- Check 'enable account linking'
- For authorization url enter your server name (https) with /twitterAuth/oauth/authorize?response_type=code
- Copy the redirect URL. You'll need that entered into application.groovy
- For client-id, enter the matching value you have in Bootstrap.groovy for the OAUTH2 client-id
- For domain list, enter a domain that matches your SSL cert the oauth tokens will be valid for.
- For scope, use 'read,write' or 'write'
- For authorization grant type select 'implicit'
- Enter the url for the privacy policy on yuor server. It can be any valid web page, a link will show during account linking in the alexa app
- Hit Save
- Click on SSL Certificate.
- if you have a self-signed cert (will only work for DEV mode) paste it here under 'I will upload a self-signed certificate in X.509 format.'
- Hit Save and go to Test page and hit Save
- Go to Privacy and Compliance, and fill out the info there (It's required)

## AMAZON ALEXA SKILL ID Copied to IDEA / EDITOR / NEW WAR
- Copy the application ID on the first tab 'SKILL INFORMATION', and paste that into application.groovy
- Build and deploy your war file to your server

## AMAZON Alexa Skill Test
- Go back to the Amazon Developer Console, Alexa, and open the skill you made.
- Click edit and hit next until you are the 'Test' tab. You can test it out there. Make sure there are no errors communicating with the services

## Link Account
- Open  your deployed war file. Go to the registration link. Confirm the registration in the email you receive.
- Open the alexa app on your phone or go to echo.amazon.com in your browser
- Go to Skills-> search for the app name your created (usually towards end of list)
- Click link account - it will take you to your servers login page
- Login to your account. If all is well, it will say account successfully linked

## Enter user twitter credentials
- Go to your deployed war, login with the user created in the step above
- Click on the link twitter account link, enter the keys you set up on twitter at the beginning of the readme.
- You can have one active set of credentials at a time. If you make a new one, it marks it as active and marks others as inactive. You can switch between them quickly for multiple twitter accounts.

## ECHO Test
- Now try it on your Echo/Alexa device. Say either 'start' or 'open' and the invocation name you gave the app and follow the prompts!

### Using the app:
Functions of the app:
Say open <invocation name>

1. you can say 'search <X> for <value>'  <X> is the number of tweets you want back. If you don't say a number it defaults to 1. Or you can say 'search for <value>'
2. 'get my timeline'
3. 'get my mentions' or 'get my last <X> mentions
4. 'get last <X> tweets' or 'get my latest tweets'
5.  check the SampleUtterances.txt for all of the latest options
6. Ask 'Do you run Groovy?' it wil tell you the groovy version the skill is running on.
