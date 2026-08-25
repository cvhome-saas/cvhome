import {getTheme} from '@/shell/theme/get-theme';
import {loadHome} from '@/shell/loaders/home';
import {loadPageContext} from '@/shell/loaders/page-context';

export default async function HomePage() {
    const [theme, ctx, data] = await Promise.all([getTheme(), loadPageContext(), loadHome()]);
    return <theme.pages.Home ctx={ctx} data={data}/>;
}
