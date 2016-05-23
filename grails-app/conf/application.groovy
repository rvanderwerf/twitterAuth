

// Added by the Spring Security Core plugin:
grails.plugin.springsecurity.userLookup.userDomainClassName = 'com.vanderfox.User'
grails.plugin.springsecurity.userLookup.authorityJoinClassName = 'com.vanderfox.UserRole'
grails.plugin.springsecurity.authority.className = 'com.vanderfox.Role'
grails.plugin.springsecurity.authority.groupAuthorityNameField = 'authorities'
grails.plugin.springsecurity.useRoleGroups = false
grails.plugin.springsecurity.controllerAnnotations.staticRules = [
	[pattern: '/',               access: ['permitAll']],
	[pattern: '/error',          access: ['permitAll']],
	[pattern: '/index',          access: ['permitAll']],
	[pattern: '/index.gsp',      access: ['permitAll']],
	[pattern: '/shutdown',       access: ['permitAll']],
	[pattern: '/assets/**',      access: ['permitAll']],
	[pattern: '/**/js/**',       access: ['permitAll']],
	[pattern: '/**/css/**',      access: ['permitAll']],
	[pattern: '/**/images/**',   access: ['permitAll']],
	[pattern: '/**/favicon.ico', access: ['permitAll']],
	[pattern: '/logout**', access: ['permitAll']],
	[pattern: '/register/**', access: ['permitAll']],
	//[pattern: '/logout/**', access: ['permitAll']],
	[pattern: '/registrationCode/**', access: ['permitAll']],
	[pattern: '/twitter/**', access: ['permitAll']],
	[pattern: '/securityInfo/**', access: ['permitAll']],
	[pattern: '/user/**', access: ['permitAll']],
	[pattern: '/role/**', access: ['ROLE_ADMIN']],
	[pattern: '/securityInfo/**', access: ['ROLE_ADMIN']],
	[pattern: '/requestMap/**', access: ['ROLE_ADMIN']],
	[pattern: '/oauth/authorize',           access: "isFullyAuthenticated() and (request.getMethod().equals('GET') or request.getMethod().equals('POST'))"],
	[pattern: '/oauth/token',               access: "isFullyAuthenticated() and request.getMethod().equals('POST')"]
]


grails.plugin.springsecurity.filterChain.chainMap = [
	[pattern: '/assets/**',      filters: 'none'],
	[pattern: '/**/js/**',       filters: 'none'],
	[pattern: '/**/css/**',      filters: 'none'],
	[pattern: '/**/images/**',   filters: 'none'],
	[pattern: '/**/favicon.ico', filters: 'none'],
	[pattern: '/oauth/token',               filters: 'JOINED_FILTERS,-oauth2ProviderFilter,-securityContextPersistenceFilter,-logoutFilter,-authenticationProcessingFilter,-rememberMeAuthenticationFilter,-exceptionTranslationFilter'],
	[pattern: '/securedOAuth2Resources/**', filters: 'JOINED_FILTERS,-securityContextPersistenceFilter,-logoutFilter,-authenticationProcessingFilter,-rememberMeAuthenticationFilter,-oauth2BasicAuthenticationFilter,-exceptionTranslationFilter'],
	[pattern: '/**',                        filters: 'JOINED_FILTERS,-statelessSecurityContextPersistenceFilter,-oauth2ProviderFilter,-clientCredentialsTokenEndpointFilter,-oauth2BasicAuthenticationFilter,-oauth2ExceptionTranslationFilter']
]



// Added by the Spring Security OAuth2 Provider plugin:
grails.plugin.springsecurity.oauthProvider.clientLookup.className = 'com.vanderfox.Client'
grails.plugin.springsecurity.oauthProvider.authorizationCodeLookup.className = 'com.vanderfox.AuthorizationCode'
grails.plugin.springsecurity.oauthProvider.accessTokenLookup.className = 'com.vanderfox.AccessToken'
grails.plugin.springsecurity.oauthProvider.refreshTokenLookup.className = 'com.vanderfox.RefreshToken'
grails.plugin.springsecurity.ui.register.emailFrom = 'skills@vanderfox.com'
grails.plugin.springsecurity.logout.postOnly = false
//you can put fallback credentials here - if a user is not linked, or has none it will use these to demo
twitterSpeechlet {
	consumerKey = ""
	consumerSecret = ""
	accessToken = ""
	accessTokenSecret = ""
        // these app IDs are a comma delimited list you get from developer.amazon.com when you set up the app on your account
	awsApplicationId = ""
}
com.amazon.speech.speechlet.servlet.disableRequestSignatureCheck=true
// these are fallback appids comma delimited
skillsSdk.supportedApplicationIds=""

