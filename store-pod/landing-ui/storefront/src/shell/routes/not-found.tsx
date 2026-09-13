import type {ThemeSource} from './theme-source';

export function notFoundPage(theme: ThemeSource) {
    return async function NotFound() {
        const {states} = await theme();
        return <states.NotFound kind="route"/>;
    };
}
