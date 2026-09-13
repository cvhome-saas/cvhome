import type {ThemeDefinition} from '@store-front/theme';

export function notFoundPage(theme: ThemeDefinition) {
    return function NotFound() {
        const {states} = theme;
        return <states.NotFound kind="route"/>;
    };
}
