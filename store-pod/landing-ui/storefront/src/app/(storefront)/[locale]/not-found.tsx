import {getTheme} from '@/shell/theme/get-theme';

export default async function NotFound() {
    const theme = await getTheme();
    return <theme.states.NotFound kind="route"/>;
}
