package org.example.personalworkworkspace.api;


import org.example.workspacelib.service.RoleService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.security.Principal;

@RestController
@RequestMapping("/api/")
public class Register {
    private final OAuth2AuthorizedClientManager oAuth2AuthorizedClientManager;
    private final RoleService roleService;
    public Register(ClientRegistrationRepository clientRegistrationRepository,
                    OAuth2AuthorizedClientRepository authorizedClientRepository, RoleService roleService) {
        this.oAuth2AuthorizedClientManager = new DefaultOAuth2AuthorizedClientManager(clientRegistrationRepository, authorizedClientRepository);
        this.roleService = roleService;

    }

    @ModelAttribute("principal")
    public Principal principal(Principal principal) {
        return principal;
    }

    @GetMapping("/test")
    public String login() {
        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest.withClientRegistrationId("greetings-app-authorization-code")
                .principal(SecurityContextHolder.getContext().getAuthentication())
                .build();

        var authorizedClient = oAuth2AuthorizedClientManager.authorize(authorizeRequest);
        if (authorizedClient != null) {
            return authorizedClient.getAccessToken().getTokenValue();
        } else {
            return "Authorization failed";
        }
    }

    @GetMapping("/role")
    public String role() {
        return roleService.getRole(1L).toString();
    }

}
