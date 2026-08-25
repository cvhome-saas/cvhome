import {getTheme} from '@/shell/theme/get-theme';
import {DEFAULT_LOGIN_CSS} from '@/shell/auth/login-default-css';

/**
 * `cua` (the shopper auth server) renders its login/registration pages with a stylesheet link to
 * `/css/login.css`, which lands on the storefront through spg. The theme may ship its own CSS for those
 * pages (`ThemeDefinition.loginCss`); otherwise the neutral default is served.
 */
export async function GET() {
    const theme = await getTheme();
    return new Response(theme.loginCss ?? DEFAULT_LOGIN_CSS, {
        headers: {'Content-Type': 'text/css; charset=utf-8', 'Cache-Control': 'public, max-age=300'},
    });
}
