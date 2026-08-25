/** Default stylesheet for the cua login/registration pages (served at /css/login.css). Themes may override via ThemeDefinition.loginCss. */
export const DEFAULT_LOGIN_CSS = `:root {
    --background: #ffffff;
    --foreground: #1a1a1a;
    --primary: #000000;
    --primary-foreground: #ffffff;
    --muted: #737373;
    --border: #e5e5e5;
    --error: #ef4444;
    --radius: 0.625rem;
}

* {
    box-sizing: border-box;
    margin: 0;
    padding: 0;
}

body {
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
    background-color: var(--background);
    color: var(--foreground);
    display: flex;
    align-items: center;
    justify-content: center;
    min-height: 100vh;
    padding: 1rem;
}

.login-container {
    width: 100%;
    max-width: 400px;
    background: var(--background);
    padding: 2.5rem;
    border-radius: var(--radius);
    border: 1px solid var(--border);
    box-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1);
}

.store-header {
    text-align: center;
    margin-bottom: 2rem;
}

.store-logo {
    height: 3rem;
    width: auto;
    margin-bottom: 1rem;
}

.store-name {
    font-size: 1.5rem;
    font-weight: 700;
    letter-spacing: -0.025em;
}

h2 {
    text-align: center;
    font-size: 1.5rem;
    font-weight: 700;
    margin-bottom: 2rem;
}

.error {
    background-color: #fef2f2;
    color: var(--error);
    padding: 0.75rem;
    border-radius: calc(var(--radius) - 2px);
    font-size: 0.875rem;
    margin-bottom: 1.5rem;
    border: 1px solid #fee2e2;
    text-align: center;
}

.form-group {
    margin-bottom: 1.25rem;
}

.form-group label {
    display: block;
    font-size: 0.875rem;
    font-medium: 500;
    margin-bottom: 0.5rem;
    color: var(--foreground);
}

input {
    width: 100%;
    padding: 0.75rem;
    border-radius: calc(var(--radius) - 2px);
    border: 1px solid var(--border);
    background-color: var(--background);
    font-size: 1rem;
    transition: border-color 0.2s, ring 0.2s;
}

input:focus {
    outline: none;
    border-color: var(--primary);
    ring: 2px solid var(--primary);
}

button[type="submit"] {
    width: 100%;
    padding: 0.75rem;
    background-color: var(--primary);
    color: var(--primary-foreground);
    border: none;
    border-radius: calc(var(--radius) - 2px);
    font-size: 1rem;
    font-weight: 600;
    cursor: pointer;
    transition: opacity 0.2s;
    margin-top: 1rem;
}

button[type="submit"]:hover {
    opacity: 0.9;
}

.social-login {
    margin-top: 2rem;
    padding-top: 2rem;
    border-top: 1px solid var(--border);
}

.social-button {
    display: block;
    width: 100%;
    padding: 0.75rem;
    text-align: center;
    text-decoration: none;
    color: var(--foreground);
    background-color: #f5f5f5;
    border: 1px solid var(--border);
    border-radius: calc(var(--radius) - 2px);
    font-size: 0.875rem;
    font-weight: 500;
    margin-bottom: 0.75rem;
    transition: background-color 0.2s;
}

.social-button:hover {
    background-color: #eeeeee;
}

.footer {
    margin-top: 2rem;
    text-align: center;
    font-size: 0.875rem;
    color: var(--muted);
}

.footer a {
    color: var(--primary);
    text-decoration: none;
    font-weight: 600;
    margin-inline-start: 0.25rem;
}

.footer a:hover {
    text-decoration: underline;
}

/* RTL Support */
[dir="rtl"] .footer a {
    margin-inline-start: 0;
    margin-inline-end: 0.25rem;
}
`;
