package ntou.soselab.swagger.web.oaspage;

import io.swagger.v3.oas.models.security.SecurityScheme;

public class SecuritySchemeInfo {
    SecurityScheme securityScheme;

    public SecurityScheme getSecurityScheme() {
        return securityScheme;
    }

    public void setSecurityScheme(SecurityScheme securityScheme) {
        this.securityScheme = securityScheme;
    }
}
