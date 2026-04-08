import './styles.css';
import { initKeycloak } from './keycloak';
import { renderApp } from './ui';

const root = document.getElementById('app') as HTMLElement;

initKeycloak()
  .catch(error => {
    console.error('Keycloak init failed', error);
    return false;
  })
  .finally(() => renderApp(root));
