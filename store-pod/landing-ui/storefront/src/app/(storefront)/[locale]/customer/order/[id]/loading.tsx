import {getTheme} from '@/shell/theme/get-theme';

export default async function Loading() {
    const theme = await getTheme();
    return <theme.states.PageSkeleton.order/>;
}
