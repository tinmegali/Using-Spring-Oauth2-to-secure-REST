package com.tinmegali.oauth2.security;

import com.tinmegali.oauth2.models.Account;
import com.tinmegali.oauth2.models.Role;
import com.tinmegali.oauth2.services.AccountService;
import com.tinmegali.oauth2.services.TokenBlackListService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

@Configuration
@EnableAuthorizationServer
public class AuthorizationConfig extends AuthorizationServerConfigurerAdapter {

    private Logger logger = Logger.getLogger(AuthorizationConfig.class);

    private int accessTokenValiditySeconds = 10000;
    private int refreshTokenValiditySeconds = 30000;

    @Value("${security.oauth2.resource.id}")
    private String resourceId;

    @Bean
    public UserDetailsService userDetailsService(){
        return new AccountService();
    }

    @Autowired
    private AuthenticationManager authenticationManager;

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints
                .authenticationManager(this.authenticationManager)
                .tokenServices(tokenServices())
                .tokenStore(tokenStore())
                .accessTokenConverter(accessTokenConverter());
    }


    @Override
    public void configure(AuthorizationServerSecurityConfigurer oauthServer) throws Exception {

        oauthServer
                // we're allowing access to the token only for clients with 'ROLE_TRUSTED_CLIENT' authority
                .tokenKeyAccess("hasAuthority('ROLE_TRUSTED_CLIENT')")
                .checkTokenAccess("hasAuthority('ROLE_TRUSTED_CLIENT')");

    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients
                .inMemory()

                .withClient("trusted-app")
                    .authorizedGrantTypes("client_credentials", "password", "refresh_token")
                    .authorities(Role.ROLE_TRUSTED_CLIENT.toString())
                    .scopes("read", "write")
                    .resourceIds(resourceId)
                    .accessTokenValiditySeconds(10)
                    .refreshTokenValiditySeconds(30000)
                    .secret("secret")
                .and()
                .withClient("register-app")
                    .authorizedGrantTypes("client_credentials")
                    .authorities(Role.ROLE_REGISTER.toString())
                    .scopes("registerUser")
                    .accessTokenValiditySeconds(10)
                    .refreshTokenValiditySeconds(10)
                    .resourceIds(resourceId)
                    .secret("secret");
    }

    @Bean
    public TokenStore tokenStore() {
        return new JwtTokenStore(accessTokenConverter());

    }

    @Bean
    public JwtAccessTokenConverter accessTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        KeyStoreKeyFactory keyStoreKeyFactory =
                new KeyStoreKeyFactory(
                        new ClassPathResource("mykeys.jks"),
                        "mypass".toCharArray());
        converter.setKeyPair(keyStoreKeyFactory.getKeyPair("mykeys"));
        return converter;
    }

    @Autowired
    private TokenBlackListService blackListService;

    @Bean
    @Primary
    public DefaultTokenServices tokenServices() {
        MyTokenService tokenService = new MyTokenService(blackListService);
        tokenService.setTokenStore(tokenStore());
        tokenService.setSupportRefreshToken(true);
        tokenService.setTokenEnhancer(accessTokenConverter());
        return tokenService;
    }


    static class MyTokenService extends DefaultTokenServices {
        Logger logger = Logger.getLogger(MyTokenService.class);

        private TokenBlackListService blackListService;

        public MyTokenService(TokenBlackListService blackListService) {
            this.blackListService = blackListService;
        }

        @Override
        public OAuth2AccessToken readAccessToken(String accessToken) {
            return super.readAccessToken(accessToken);
        }



        @Override
        public OAuth2AccessToken createAccessToken(OAuth2Authentication authentication) throws AuthenticationException {
            OAuth2AccessToken token = super.createAccessToken(authentication);
            Account account = (Account) authentication.getPrincipal();
            String jti = (String) token.getAdditionalInformation().get("jti");

            blackListService.addToEnabledList(
                    account.getId(),
                    jti,
                    token.getExpiration().getTime() );
            return token;
        }

        @Override
        public OAuth2AccessToken refreshAccessToken(String refreshTokenValue, TokenRequest tokenRequest) throws AuthenticationException {
            logger.info("refresh token:" + refreshTokenValue);
            String jti = tokenRequest.getRequestParameters().get("jti");
            try {
                if ( jti != null )
                        if ( blackListService.isBlackListed(jti) ) return null;


                OAuth2AccessToken token = super.refreshAccessToken(refreshTokenValue, tokenRequest);
                blackListService.addToBlackList(jti);
                return token;
            } catch (TokenBlackListService.TokenNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
