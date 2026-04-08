import Keycloak from 'keycloak-js';

// Hardcoded for the workshop demo: local Keycloak provisioned from
// src/test/resources/keycloak/test-realm.json and exposed by compose.yaml on :8090.
export const keycloak = new Keycloak({
  url: 'http://localhost:8090',
  realm: 'test-realm',
  clientId: 'bookshelf-spa',
});

export async function initKeycloak(): Promise<boolean> {
  return keycloak.init({
    onLoad: 'check-sso',
    pkceMethod: 'S256',
    checkLoginIframe: false,
  });
}

export function login(): void {
  keycloak.login({ redirectUri: window.location.href });
}

export function logout(): void {
  keycloak.logout({ redirectUri: window.location.origin + '/app/' });
}

export function currentUsername(): string | undefined {
  const tokenParsed = keycloak.tokenParsed as Record<string, unknown> | undefined;
  return tokenParsed?.preferred_username as string | undefined;
}

export function hasScope(scope: string): boolean {
  const tokenParsed = keycloak.tokenParsed as Record<string, unknown> | undefined;
  const scopes = (tokenParsed?.scope as string | undefined)?.split(' ') ?? [];
  if (scopes.includes(scope)) {
    return true;
  }
  const realmAccess = tokenParsed?.realm_access as { roles?: string[] } | undefined;
  return realmAccess?.roles?.includes(scope) ?? false;
}
